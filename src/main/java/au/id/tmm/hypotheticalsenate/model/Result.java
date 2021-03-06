package au.id.tmm.hypotheticalsenate.model;

import au.id.tmm.hypotheticalsenate.controller.BallotCounter;
import au.id.tmm.hypotheticalsenate.controller.CountStep;

import java.io.PrintStream;
import java.util.List;

/**
 * Bundle of {@link CountStep}s and a list of {@link Candidate}s that have been elected generated by a completed
 * {@link BallotCounter}.
 *
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
        this.countSteps.forEach(out::println);

        out.println();

        out.println("Candidates Elected:");

        for (int i = 0; i < this.electedCandidates.size(); i++) {
            out.println("  " + (i + 1) + ": " + this.electedCandidates.get(i));
        }

        out.println();
    }
}
