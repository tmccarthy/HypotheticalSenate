package au.id.tmm.hypotheticalsenate.controller;

import au.id.tmm.hypotheticalsenate.model.Ballot;
import au.id.tmm.hypotheticalsenate.model.Candidate;
import au.id.tmm.hypotheticalsenate.model.Result;
import au.id.tmm.hypotheticalsenate.model.VoteCount;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * An engine for performing a count, given a number of vacancies, a set of candidates and a list of ballots.
 * <p>
 * Once run, a {@code BallotCounter} cannot be run again.
 *
 * @author timothy
 */
public class BallotCounter {

    private boolean hasRun = false;

    private final int vacancies;
    private final int quota;

    private final TIntObjectMap<Candidate> idToCandidateMap;
    private final Map<Candidate, List<Ballot>> candidateBallots = new HashMap<>();

    private final List<CountStep> steps = new LinkedList<>();
    private final List<Candidate> electedCandidates = new LinkedList<>();

    private int currentCountStepNumber = 0;

    public BallotCounter(int vacancies, Collection<Candidate> candidates, List<Ballot> ballots) {
        this.vacancies = vacancies;

        // Compute the quota. The ignoring of the remainder is intended
        this.quota = ((int) Math.ceil(tallyBallots(ballots)) / (vacancies + 1)) + 1;


        this.idToCandidateMap = new TIntObjectHashMap<>(candidates.size());
        candidates.forEach(candidate -> this.idToCandidateMap.put(candidate.getCandidateID(), candidate));

        // Initialise candidate lists.
        candidates.forEach(candidate ->
                this.candidateBallots.put(candidate, new ArrayList<>(2 * quota)));

        ballots.forEach(ballot ->
                ballot.computeCurrentCandidate().ifPresent(firstPreferencedCandidate ->
                        this.candidateBallots.get(this.idToCandidateMap.get(firstPreferencedCandidate)).add(ballot)));
    }

    public Result run() {
        this.hasRunCheck();

        this.steps.add(this.initialAllocationStep());

        do {
            this.currentCountStepNumber ++;
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
        CountStep initialCountStep = new CountStep(this.currentCountStepNumber);

        initialCountStep.setVotes(this.currentCount().getVotes());

        return initialCountStep;
    }

    private CountStep normalCountStep() {
        CountStep countStep = new CountStep(this.currentCountStepNumber);
        VoteCount currentCount = this.currentCount();

        if (currentCount.get(currentCount.getHighestVoteCandidate()) > quota) {
            // A candidate has exceeded quota
            countStep.setCandidateElected(currentCount.getHighestVoteCandidate());

            this.distributeAfterElectionAndRemove(currentCount.getHighestVoteCandidate());
        } else if (this.candidateBallots.size() == 1) {
            // There is only one candidate left
            Candidate lastCandidate = this.candidateBallots.keySet().stream().findFirst().get();
            countStep.setCandidateElected(lastCandidate);

            assert this.electedCandidates.size() == this.vacancies;

        } else {
            // We must exclude a candidate
            countStep.setCandidateExcluded(currentCount.getLowestVoteCandidate());

            this.distributeAfterExclusionAndRemove(currentCount.getLowestVoteCandidate());
        }

        countStep.getCandidateElected().ifPresent(this.electedCandidates::add);

        // The votes for this count step are computed once we've finish allocating votes from elected or excluded
        // candidates.
        countStep.setVotes(this.currentCount().getVotes());

        return countStep;
    }

    /**
     * Distributes votes from a candidate once they have been elected, and then removes them from the list of current
     * candidates. This is a different process from distributing votes from an excluded candidate as we must compute the
     * transfer value of the surplus votes.
     */
    private void distributeAfterElectionAndRemove(Candidate candidate) {
        List<Ballot> candidateBallots = this.candidateBallots.get(candidate);
        double totalVotes = tallyBallots(candidateBallots);
        double surplus = totalVotes - quota;

        if (surplus > 0) {
            double transferFactor = surplus / totalVotes;

            candidateBallots.forEach(ballot -> distributeBallot(ballot, transferFactor));
        }

        this.candidateBallots.remove(candidate);
    }

    private void distributeAfterExclusionAndRemove(Candidate candidate) {
        this.candidateBallots.get(candidate).forEach(ballot -> distributeBallot(ballot, 1.0f));

        this.candidateBallots.remove(candidate);
    }

    private void distributeBallot(Ballot ballot, double transferFactor) {
        ballot.incrementCurrentPreferenceIndex();
        OptionalInt newCandidateID = ballot.computeCurrentCandidate();
        Optional<Candidate> newCandidate = newCandidateID.isPresent()
                ? Optional.of(this.idToCandidateMap.get(newCandidateID.getAsInt()))
                : Optional.empty();

        if (!newCandidate.isPresent()) {
            // The ballot has expired
            return;
        } else if (!this.candidateBallots.containsKey(newCandidate.get())) {
            // The next preference has already been either excluded or elected, so we try to distribute again.
            this.distributeBallot(ballot, transferFactor);
        } else {
            ballot.multiplyCurrentWeightBy(transferFactor);
            this.candidateBallots.get(newCandidate.get()).add(ballot);
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
