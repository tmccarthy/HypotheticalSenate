package au.id.tmm.hypotheticalsenate.model;

import gnu.trove.map.TIntDoubleMap;
import gnu.trove.map.hash.TIntDoubleHashMap;

/**
 * @author timothy
 */
public class VoteCount {

    private TIntDoubleMap votes = new TIntDoubleHashMap();
    private int lowestVoteCandidateID;
    private int highestVoteCandidateID;

    public void put(int candidateID, double votes) {
        this.votes.put(candidateID, votes);

        if (this.lowestVoteCandidateID == 0 || votes < this.votes.get(lowestVoteCandidateID)) {
            this.lowestVoteCandidateID = candidateID;
        }

        if (this.highestVoteCandidateID == 0 || votes > this.votes.get(highestVoteCandidateID)) {
            this.highestVoteCandidateID = candidateID;
        }
    }

    public double get(int key) {
        return votes.get(key);
    }

    public TIntDoubleMap getVotes() {
        return votes;
    }

    public int getLowestVoteCandidateID() {
        return lowestVoteCandidateID;
    }

    public int getHighestVoteCandidateID() {
        return highestVoteCandidateID;
    }
}
