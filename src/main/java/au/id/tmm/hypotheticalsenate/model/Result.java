package au.id.tmm.hypotheticalsenate.model;

import java.io.PrintStream;
import java.util.List;

/**
* @author timothy
*/
public class Result {
    private List<Candidate> electedCandidates;
    private List<CountStep> countSteps;

    public List<Candidate> getElectedCandidates() {
        return electedCandidates;
    }

    public void setElectedCandidates(List<Candidate> electedCandidates) {
        this.electedCandidates = electedCandidates;
    }

    public List<CountStep> getCountSteps() {
        return countSteps;
    }

    public void setCountSteps(List<CountStep> countSteps) {
        this.countSteps = countSteps;
    }

    public void printTo(PrintStream out) {
        out.println("Candidates Elected:");

        for (int i = 0; i < this.electedCandidates.size(); i++) {
            out.println("  " + (i + 1) + ": " + this.electedCandidates.get(i));
        }

        out.println();
    }
}
