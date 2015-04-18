package au.id.tmm.hypotheticalsenate.model;

import gnu.trove.map.TObjectDoubleMap;
import gnu.trove.map.hash.TObjectDoubleHashMap;

/**
 * Essentially a wrapper around a map relating a {@link Candidate} to the number of votes they have, except that this
 * class also keeps track of the {@code Candidate}s with the least and most votes.
 *
 * @author timothy
 */
public class VoteTally {

    private TObjectDoubleMap<Candidate> votes = new TObjectDoubleHashMap<>();
    private Candidate lowestVoteCandidate;
    private Candidate highestVoteCandidate;

    public void put(Candidate candidate, double votes) {
        this.votes.put(candidate, votes);

        if (this.lowestVoteCandidate == null || votes < this.votes.get(lowestVoteCandidate)) {
            this.lowestVoteCandidate = candidate;
        }

        if (this.highestVoteCandidate == null || votes > this.votes.get(highestVoteCandidate)) {
            this.highestVoteCandidate = candidate;
        }
    }

    public double get(Candidate candidate) {
        return votes.get(candidate);
    }

    public TObjectDoubleMap<Candidate> getVotes() {
        return votes;
    }

    public Candidate getLowestVoteCandidate() {
        return lowestVoteCandidate;
    }

    public Candidate getHighestVoteCandidate() {
        return highestVoteCandidate;
    }
}
