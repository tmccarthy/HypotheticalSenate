package au.id.tmm.hypotheticalsenate.controller;

import au.id.tmm.hypotheticalsenate.database.HypotheticalSenateDatabase;
import au.id.tmm.hypotheticalsenate.model.AustralianState;
import au.id.tmm.hypotheticalsenate.model.Ballot;
import au.id.tmm.hypotheticalsenate.model.Candidate;
import au.id.tmm.hypotheticalsenate.model.GroupVotingTicket;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;
import gnu.trove.iterator.TIntIntIterator;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import org.apache.commons.lang3.mutable.MutableInt;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.TreeSet;

/**
 * Engine for collecting {@link Ballot}s and {@link Candidate}s from a {@link HypotheticalSenateDatabase}.
 * <p>
 * Note that subclasses are given the opportunity to mutate the loaded ballots via the
 * {@link #customiseATLBallots(List)} and {@link #customiseBTLBallots(List)} methods, which do nothing by default but
 * can be overridden. Note also that the process for translating the preferences in the database to the candidate order
 * used in a {@link Ballot} is performed in {@link #candidateOrderFromPreferences(TIntIntMap)}.
 *
 * @author timothy
 */
public class BallotCollector {

    public static final int FETCH_SIZE = 100;
    private final AustralianState state;

    private List<Ballot> ballots;
    private Collection<Candidate> candidates;

    public BallotCollector(AustralianState state) {
        this.state = state;
    }

    public BallotCollector loadCandidates(HypotheticalSenateDatabase database) {
        candidates = new TreeSet<>();

        database.runWithConnection((connection, resources) -> {
            Statement statement = connection.createStatement();
            resources.add(statement);
            statement.setFetchSize(FETCH_SIZE);

            ResultSet resultSet = statement.executeQuery(
                    "SELECT candidateID, partyID, givenName, surname " +
                            "FROM Candidate " +
                            "WHERE candidateID IN (" +
                            "SELECT DISTINCT candidateID " +
                            "FROM Candidate " +
                            "JOIN GroupTicketPreference " +
                            "ON Candidate.candidateID = GroupTicketPreference.preferencedCandidate " +
                            "  AND GroupTicketPreference.stateCode = '" + this.state.getCode() + "');"
            );

            while(resultSet.next()) {
                candidates.add(new Candidate(
                        resultSet.getInt("candidateID"),
                        resultSet.getString("givenName"),
                        resultSet.getString("surname"),
                        resultSet.getString("partyID")
                ));
            }
        });

        return this;
    }

    public BallotCollector loadBallots(HypotheticalSenateDatabase database) {
        int numCandidates = computeNumCandidates(database);

        List<Ballot> atlBallots = this.loadATLBallots(database, numCandidates);
        atlBallots = this.customiseATLBallots(atlBallots);

        List<Ballot> btlBallots = this.loadBTLBallots(database, numCandidates);
        btlBallots = this.customiseBTLBallots(btlBallots);

        this.ballots = new ArrayList<>(atlBallots.size() + btlBallots.size());
        this.ballots.addAll(atlBallots);
        this.ballots.addAll(btlBallots);

        return this;
    }

    private List<Ballot> loadATLBallots(HypotheticalSenateDatabase database, int numCandidates) {
        int groupCount = computeNumGroups(database);

        List<Ballot> ballots = new ArrayList<>(groupCount * 2);

        TObjectIntMap<String> groupFirstPreferences = new TObjectIntHashMap<>(groupCount);
        ListMultimap<String, GroupVotingTicket> groupTicketMap = LinkedListMultimap.create(groupCount);

        database.runWithConnection((connection, resources) -> {
            Statement statement = connection.createStatement();
            resources.add(statement);
            statement.setFetchSize(FETCH_SIZE);

            ResultSet atlVotesResultSet = statement.executeQuery(
                    "SELECT groupID, votes " +
                            "FROM AboveTheLineVotes " +
                            "WHERE stateCode = '" + this.state.getCode() + "';"
            );

            while (atlVotesResultSet.next()) {
                groupFirstPreferences.put(atlVotesResultSet.getString("groupID"), atlVotesResultSet.getInt("votes"));
            }

            ResultSet groupPreferencesResultSet = statement.executeQuery(
                    "SELECT ownerGroup, ticket, preference, preferencedCandidate " +
                            "FROM GroupTicketPreference " +
                            "WHERE stateCode = '" + this.state.getCode() + "' " +
                            "ORDER BY ownerGroup ASC, ticket ASC, preference ASC;"
            );

            if (groupPreferencesResultSet.next()) {
                // Grab the first ticket and group, then restart the iterator.
                String currentGroup = groupPreferencesResultSet.getString("ownerGroup");
                int currentTicket = groupPreferencesResultSet.getInt("ticket");
                TIntIntMap currentTicketPreferences = new TIntIntHashMap(numCandidates);

                do {
                    if (!currentGroup.equals(groupPreferencesResultSet.getString("ownerGroup"))
                            || currentTicket != groupPreferencesResultSet.getInt("ticket")) {
                        groupTicketMap.put(currentGroup,
                                new GroupVotingTicket(currentGroup, currentTicket, currentTicketPreferences));

                        currentGroup = groupPreferencesResultSet.getString("ownerGroup");
                        currentTicket = groupPreferencesResultSet.getInt("ticket");
                        currentTicketPreferences = new TIntIntHashMap(numCandidates);
                    }

                    currentTicketPreferences.put(
                            groupPreferencesResultSet.getInt("preferencedCandidate"),
                            groupPreferencesResultSet.getInt("preference"));
                } while(groupPreferencesResultSet.next());
            }
        });

        groupTicketMap.asMap().forEach((group, tickets) -> {
            if (!tickets.isEmpty()) {
                double intrinsicBallotWeight = ((double) groupFirstPreferences.get(group))
                                / ((double) tickets.size());

                for (GroupVotingTicket ticket : tickets) {
                    this.candidateOrderFromPreferences(ticket.getPreferences()).ifPresent(candidateOrder ->
                            ballots.add(new Ballot(intrinsicBallotWeight, candidateOrder)));
                }
            }
        });

        return ballots;
    }

    protected List<Ballot> customiseATLBallots(List<Ballot> atlBallots) {
        return atlBallots;
    }

    private List<Ballot> loadBTLBallots(HypotheticalSenateDatabase database, int numCandidates) {

        int numBallots = computeNumBallots(database);

        List<Ballot> ballots = new ArrayList<>(numBallots);

        database.runWithConnection((connection, resources) -> {
            Statement statement = connection.createStatement();
            resources.add(statement);
            statement.setFetchSize(FETCH_SIZE);

            ResultSet resultSet = statement.executeQuery(
                    "SELECT ballotID, candidateID, preference " +
                            "FROM BelowTheLineBallot " +
                            "WHERE stateCode = '" + this.state.getCode() + "' " +
                            "ORDER BY ballotID ASC, preference ASC;");

            if (resultSet.next()) {
                // Grab the first ballot ID, then restart the iterator.
                String currentBallotID = resultSet.getString("ballotID");
                TIntIntMap currentBallotPreferences = new TIntIntHashMap(numCandidates);

                do {

                    if (!currentBallotID.equals(resultSet.getString("ballotID"))) {
                        candidateOrderFromPreferences(currentBallotPreferences).ifPresent(candidateOrder ->
                                ballots.add(new Ballot(1d, candidateOrder)));

                        currentBallotID = resultSet.getString("ballotID");
                        currentBallotPreferences = new TIntIntHashMap(numCandidates);
                    }

                    currentBallotPreferences.put(resultSet.getInt("candidateID"), resultSet.getInt("preference"));
                } while (resultSet.next());
            }
        });

        return ballots;
    }

    protected List<Ballot> customiseBTLBallots(List<Ballot> btlBallots) {
        return btlBallots;
    }

    private int computeNumBallots(HypotheticalSenateDatabase database) {
        MutableInt numBallots = new MutableInt();

        database.runWithConnection((connection, resources) -> {
            Statement statement = connection.createStatement();
            resources.add(statement);

            numBallots.setValue(
                    statement.executeQuery(
                            "SELECT COUNT(DISTINCT ballotID) AS numBallots " +
                                    "FROM BelowTheLineBallot " +
                                    "WHERE stateCode = '" + this.state.getCode() + "'")
                            .getLong("numBallots"));
        });

        return numBallots.intValue();
    }

    private int computeNumCandidates(HypotheticalSenateDatabase database) {
        MutableInt numCandidates = new MutableInt();

        database.runWithConnection((connection, resources) -> {
            Statement statement = connection.createStatement();
            resources.add(statement);

            numCandidates.setValue(
                    statement.executeQuery(
                            "SELECT COUNT(DISTINCT candidateID) AS numCandidates " +
                                    "FROM BelowTheLineBallot " +
                                    "WHERE stateCode = '" + this.state.getCode() + "'")
                            .getLong("numCandidates"));
        });

        return numCandidates.intValue();
    }

    private int computeNumGroups(HypotheticalSenateDatabase database) {
        MutableInt groupCount = new MutableInt();

        database.runWithConnection((connection, resources) -> {
            Statement statement = connection.createStatement();
            resources.add(statement);

            statement.setFetchSize(FETCH_SIZE);

            ResultSet groupCountResult = statement.executeQuery(
                    "SELECT COUNT(DISTINCT groupID) as groupCount " +
                            "FROM GroupTicketInfo " +
                            "WHERE stateCode = '" + this.state.getCode() + "'"
            );

            groupCountResult.next();
            groupCount.setValue(groupCountResult.getInt("groupCount"));
        });

        return groupCount.intValue();
    }

    /**
     * Converts the surjective relationship between candidates and preferences for a single ballot into a candidate
     * order. An empty {@link Optional} is returned if the vote is considered to be invalid and hence ignored.
     * <p>
     * Currently a ballot with any duplicated preferences is considered invalid. More work should be done to make this
     * more closely represent the AEC's processes for determining vote formality.
     */
    protected Optional<TIntList> candidateOrderFromPreferences(TIntIntMap candidateToPreferenceMap) {
        TIntIntMap preferenceToCandidateMap = new TIntIntHashMap(candidateToPreferenceMap.size());

        int maxPreference = 0;

        for (TIntIntIterator it = candidateToPreferenceMap.iterator(); it.hasNext() ;) {
            it.advance();
            if (preferenceToCandidateMap.containsKey(it.value())) {
                return Optional.empty();
            } else {
                preferenceToCandidateMap.put(it.value(), it.key());

                if (it.value() > maxPreference) {
                    maxPreference = it.value();
                }
            }
        }

        TIntList candidateOrder = new TIntArrayList(preferenceToCandidateMap.size());

        for (int preference = 0; preference <= maxPreference; preference++) {
            if (preferenceToCandidateMap.containsKey(preference)) {
                candidateOrder.add(preferenceToCandidateMap.get(preference));
            }
        }

        return Optional.of(candidateOrder);
    }

    public List<Ballot> getBallots() {
        return ballots;
    }

    public Collection<Candidate> getCandidates() {
        return candidates;
    }
}
