package au.id.tmm.hypotheticalsenate.model;

import com.google.common.collect.ComparisonChain;

/**
 * @author timothy
 */
public class Candidate implements Comparable<Candidate> {
    private final int candiateID;
    private final String givenNames;
    private final String surname;
    private final String partyID;

    public Candidate(int candiateID, String givenNames, String surname, String partyID) {
        this.candiateID = candiateID;
        this.givenNames = givenNames;
        this.surname = surname;
        this.partyID = partyID;
    }

    @Override
    public int compareTo(Candidate other) {
        return ComparisonChain.start()
                .compare(this.surname, other.surname)
                .compare(this.givenNames, other.givenNames)
                .compare(this.partyID, other.partyID)
                .compare(this.candiateID, other.candiateID)
                .result();
    }

    public int getCandidateID() {
        return candiateID;
    }

    public String getGivenNames() {
        return givenNames;
    }

    public String getSurname() {
        return surname;
    }

    public String getPartyID() {
        return partyID;
    }

    @Override
    public String toString() {
        return this.givenNames + " " + this.surname;
    }
}
