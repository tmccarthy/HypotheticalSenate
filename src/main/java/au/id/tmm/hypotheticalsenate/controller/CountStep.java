package au.id.tmm.hypotheticalsenate.controller;

import au.id.tmm.hypotheticalsenate.model.Candidate;
import gnu.trove.decorator.TObjectDoubleMapDecorator;
import gnu.trove.map.TObjectDoubleMap;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
* @author timothy
*/
public class CountStep {
    private static final DecimalFormat VOTES_FORMAT = new DecimalFormat("###,###.###");

    private final int countStepNumber;

    private Optional<Candidate> candidateElected = Optional.empty();
    private Optional<Candidate> candidateExcluded = Optional.empty();

    private TObjectDoubleMap<Candidate> votes;

    public CountStep(int countStepNumber) {
        this.countStepNumber = countStepNumber;
    }

    public TObjectDoubleMap<Candidate> getVotes() {
        return votes;
    }

    public void setVotes(TObjectDoubleMap<Candidate> votes) {
        this.votes = votes;
    }

    public Optional<Candidate> getCandidateElected() {
        return candidateElected;
    }

    public void setCandidateElected(@Nullable Candidate candidateElected) {
        this.candidateElected = Optional.ofNullable(candidateElected);
    }

    public Optional<Candidate> getCandidateExcluded() {
        return candidateExcluded;
    }

    public void setCandidateExcluded(@Nullable Candidate candidateExcluded) {
        this.candidateExcluded = Optional.ofNullable(candidateExcluded);
    }

    @Override
    public String toString() {
        return "Count " + this.countStepNumber + "\n"
                + this.candidateExcluded.map(candidate -> "\tCandidate Excluded = " + candidate + "").orElse("")
                + this.candidateElected.map(candidate -> "\tCandidate Elected = " + candidate + "").orElse("")
                + "\n"
                + this.votesToString();
    }

    public String votesToString() {
        List<String> entriesAsStrings = new TObjectDoubleMapDecorator<>(this.votes).entrySet()
                .stream()
                // Sort by the number of votes
                .sorted((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()))
                .map(entry -> "\t\t" + entry.getKey() + " has " + VOTES_FORMAT.format(entry.getValue()) + " votes\n")
                .collect(Collectors.toList());

        return StringUtils.join(entriesAsStrings, "");
    }
}
