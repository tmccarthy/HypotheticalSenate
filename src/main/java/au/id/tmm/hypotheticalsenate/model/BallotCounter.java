package au.id.tmm.hypotheticalsenate.model;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;

/**
 * @author timothy
 */
public class BallotCounter {

    private boolean hasRun = false;

    private final int vacancies;
    private int quota;

    private final TIntObjectMap<Candidate> idToCandidateMap;
    private final Map<Integer, List<Ballot>> candidateBallots = new HashMap<>();

    private final List<CountStep> steps = new LinkedList<>();
    private final List<Candidate> electedCandidates = new LinkedList<>();

    public BallotCounter(int vacancies, Collection<Candidate> candidates, List<Ballot> ballots) {
        this.vacancies = vacancies;

        // Compute the quota. The ignoring of the remainder is intended
        this.quota = ((int) Math.ceil(tallyBallots(ballots)) / (vacancies + 1)) + 1;


        this.idToCandidateMap = new TIntObjectHashMap<>(candidates.size());
        candidates.forEach(candidate -> this.idToCandidateMap.put(candidate.getCandidateID(), candidate));

        // Initialise candidate lists.
        candidates.forEach(candidate ->
                this.candidateBallots.put(candidate.getCandidateID(), new ArrayList<>(2 * quota)));

        ballots.forEach(ballot ->
                ballot.computeCurrentCandidate().ifPresent(firstPreferencedCandidate ->
                        this.candidateBallots.get(firstPreferencedCandidate).add(ballot)));
    }

    public Result run() {
        this.hasRunCheck();

        this.steps.add(this.initialAllocationStep());

        do {
            this.steps.add(this.normalCountStep());
        } while (this.electedCandidates.size() < this.vacancies);

        return this.bundleResult();
    }

    private void hasRunCheck() {
        if (this.hasRun) {
            throw new IllegalStateException("Attempted to run a Count that has already been run.");
        }

        this.hasRun = true;
    }

    private CountStep initialAllocationStep() {
        CountStep initialCountStep = new CountStep();

        initialCountStep.setVotes(this.currentCount().getVotes());

        return initialCountStep;
    }

    private CountStep normalCountStep() {
        CountStep countStep = new CountStep();
        VoteCount currentCount = this.currentCount();

        if (currentCount.get(currentCount.getHighestVoteCandidateID()) > quota) {
            // A candidate has exceeded quota
            countStep.setCandidateElected(this.idToCandidateMap.get(currentCount.getHighestVoteCandidateID()));

            this.distributeAfterElectionAndRemove(currentCount.getHighestVoteCandidateID());
        } else if (this.candidateBallots.size() == 1) {
            // There is only one candidate left
            int lastCandidate = this.candidateBallots.keySet().stream().findFirst().get();
            countStep.setCandidateElected(this.idToCandidateMap.get(
                    lastCandidate
            ));

            assert this.electedCandidates.size() == this.vacancies;

        } else {
            // We must exclude a candidate
            countStep.setCandidateExcluded(this.idToCandidateMap.get(currentCount.getLowestVoteCandidateID()));

            this.distributeAfterExclusionAndRemove(currentCount.getLowestVoteCandidateID());
        }

        countStep.getCandidateElected().ifPresent(this.electedCandidates::add);

        // The votes for this count step are computed once we've finish allocating votes from elected or excluded
        // candidates.
        countStep.setVotes(this.currentCount().getVotes());

        return countStep;
    }

    private void distributeAfterElectionAndRemove(int candidateID) {
        List<Ballot> candidateBallots = this.candidateBallots.get(candidateID);
        double totalVotes = tallyBallots(candidateBallots);
        double surplus = totalVotes - quota;

        if (surplus > 0) {
            double transferFactor = surplus / totalVotes;

            candidateBallots.forEach(ballot -> distributeBallot(ballot, transferFactor));
        }

        this.candidateBallots.remove(candidateID);
    }

    private void distributeAfterExclusionAndRemove(int candidateID) {
        this.candidateBallots.get(candidateID).forEach(ballot -> distributeBallot(ballot, 1.0f));

        this.candidateBallots.remove(candidateID);
    }

    private void distributeBallot(Ballot ballot, double transferFactor) {
        ballot.incrementCurrentPreferenceIndex();
        OptionalInt newCandidate = ballot.computeCurrentCandidate();

        if (!newCandidate.isPresent()) {
            // The ballot has expired
            return;
        } else if (!this.candidateBallots.containsKey(newCandidate.getAsInt())) {
            // The next preference has already been either excluded or elected, so we try to distribute again.
            this.distributeBallot(ballot, transferFactor);
        } else {
            ballot.multiplyCurrentWeightBy(transferFactor);
            this.candidateBallots.get(newCandidate.getAsInt()).add(ballot);
        }
    }

    private Result bundleResult() {
        Result result = new Result();

        result.setCountSteps(this.steps);
        result.setElectedCandidates(this.electedCandidates);

        return result;
    }

    private VoteCount currentCount() {
        VoteCount returnedCount = new VoteCount();

        this.candidateBallots.forEach((candidateID, ballots) -> returnedCount.put(
                candidateID,
                tallyBallots(ballots)
        ));

        return returnedCount;
    }

    private static double tallyBallots(Collection<Ballot> ballots) {
        return ballots
                .stream()
                .mapToDouble(Ballot::computeCurrentWeight)
                .reduce((total, currentWeight) -> total + currentWeight)
                .orElse(0d);
    }
}
