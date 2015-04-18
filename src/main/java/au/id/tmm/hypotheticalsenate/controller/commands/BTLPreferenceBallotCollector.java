package au.id.tmm.hypotheticalsenate.controller.commands;

import au.id.tmm.hypotheticalsenate.controller.BallotCollector;
import au.id.tmm.hypotheticalsenate.model.AustralianState;
import au.id.tmm.hypotheticalsenate.model.Ballot;
import au.id.tmm.hypotheticalsenate.model.Election;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;

/**
 * An alternate {@link BallotCollector} that removes all above the line votes and weights below the line ballots
 * accordingly.
 *
 * @author timothy
 */
public class BTLPreferenceBallotCollector extends BallotCollector {

    private Map<Integer, Double> atlVoteTotals;

    public BTLPreferenceBallotCollector(Election election, AustralianState state) {
        super(election, state);
    }

    @Override
    protected List<Ballot> customiseATLBallots(List<Ballot> atlBallots) {

        // Compute the number of first-preferenced above the line candidates
        long numCandidates = atlBallots
                .stream()
                .map(Ballot::computeCurrentCandidate)
                .filter(OptionalInt::isPresent)
                .distinct()
                .count();

        this.atlVoteTotals = new HashMap<>((int) numCandidates);

        // For each above the line ballot, calculate how many votes were received by each candidate and store that
        // number in a map.
        atlBallots
                .stream()
                .forEach(ballot ->
                                ballot.computeCurrentCandidate().ifPresent(currentCandidate ->
                                                this.atlVoteTotals.merge(
                                                        currentCandidate,
                                                        ballot.getIntrinsicWeight(),
                                                        (previousWeight, newWeight) -> previousWeight + newWeight)
                                )
                );

        // Remove all above the line ballots.
        return Collections.emptyList();
    }

    @Override
    protected List<Ballot> customiseBTLBallots(List<Ballot> btlBallots) {

        Map<Integer, Integer> numFirstPreferencesPerCandidate = new HashMap<>(this.atlVoteTotals.size());

        btlBallots.forEach(ballot ->
                ballot.computeCurrentCandidate().ifPresent(firstPreferencedCandidate ->
                        numFirstPreferencesPerCandidate.merge(firstPreferencedCandidate, 1, Math::addExact)));

        btlBallots.forEach(ballot ->
                ballot.computeCurrentCandidate().ifPresent(firstPreferencedCandidate ->
                        ballot.setIntrinsicWeight(
                                1d + (
                                        this.atlVoteTotals.getOrDefault(firstPreferencedCandidate, 0d) /
                                                numFirstPreferencesPerCandidate.get(firstPreferencedCandidate)
                                ))));

        return btlBallots;
    }
}
