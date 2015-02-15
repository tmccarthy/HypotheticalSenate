package au.id.tmm.hypotheticalsenate.model;

import gnu.trove.decorator.TIntDoubleMapDecorator;
import gnu.trove.map.TIntDoubleMap;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
* @author timothy
*/
public class CountStep {
    private Optional<Candidate> candidateElected = Optional.empty();
    private Optional<Candidate> candidateExcluded = Optional.empty();

    private TIntDoubleMap votes;

    public TIntDoubleMap getVotes() {
        return votes;
    }

    public void setVotes(TIntDoubleMap votes) {
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
        return "CountStep{" +
                "candidateExcluded=" + candidateExcluded.orElse(null) +
                ", candidateElected=" + candidateElected.orElse(null) +
                ", votes=\n" + this.votesToString() +
                '}';
    }

    public String votesToString() {
        List<String> entriesAsStrings = new TIntDoubleMapDecorator(this.votes).entrySet()
                .stream()
                .sorted((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()))
                .map(entry -> "\t" + entry.getKey() + " => " + entry.getValue() + "\n")
                .collect(Collectors.toList());

        return StringUtils.join(entriesAsStrings, "");
    }
}
