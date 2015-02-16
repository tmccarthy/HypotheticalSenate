package au.id.tmm.hypotheticalsenate.model;

import au.id.tmm.hypotheticalsenate.controller.BallotCollector;
import gnu.trove.list.TIntList;
import gnu.trove.map.TIntIntMap;

import java.util.OptionalInt;

/**
 * A ballot.
 * <p>
 * Each {@link Ballot} contains 4 properties used to represent both the preferences and its current state in a count:
 * <ul>
 *     <li>
 *         An {@link #intrinsicWeight}, which is a weight that is never altered during the count. For a normal ballot
 *         this would generally be {@code 1.0}, but group voting tickets are represented as a single {@code Ballot} with
 *         a weight according to the number of above-the-line votes it received.
 *     </li>
 *     <li>
 *         A {@link #candidateOrder}, which is the order of preferences associated with this ballot. Note that some
 *         processing has already occurred to convert the surjective (ie not necessarily one-to-one) relationship
 *         between a candidate and a preference to a preference-ordering of candidates. See
 *         {@link BallotCollector#candidateOrderFromPreferences(TIntIntMap)}
 *     </li>
 *     <li>
 *         A {@link #countWeight}, which represents the current weight given to this ballot at a particular point in the
 *         ballot. This will mutate as surplus votes are redistributed at a reduced rate throughout the count.
 *     </li>
 *     <li>
 *         A {@link #currentPreferenceIndex}, which represents the candidate being preferenced by this ballot at a
 *         particular point in the count. This will mutate as the vote is redistributed throughout the count.
 *     </li>
 * </ul>
 *
 * @author timothy
 */
public class Ballot {

    private double intrinsicWeight = 1.0d;
    private final TIntList candidateOrder;

    private int currentPreferenceIndex = 0;
    private double countWeight = 1.0d;

    public Ballot(double intrinsicWeight, TIntList candidateOrder) {
        this.intrinsicWeight = intrinsicWeight;
        this.candidateOrder = candidateOrder;
    }

    public Ballot(TIntList candidateOrder) {
        this(1.0d, candidateOrder);
    }

    public OptionalInt computeCurrentCandidate() {
        if (this.currentPreferenceIndex >= this.candidateOrder.size()) {
            return OptionalInt.empty();
        } else {
            int currentCandidate = this.candidateOrder.get(currentPreferenceIndex);
            if (currentCandidate == 0) {
                return OptionalInt.empty();
            } else {
                return OptionalInt.of(currentCandidate);
            }
        }
    }

    /**
     * Mutates the {@link #countWeight} by multiplying it by the given factor.
     */
    public void multiplyCurrentWeightBy(double factor) {
        this.countWeight *= factor;
    }

    /**
     * Returns the current weight of this {@code Ballot}, mutliplying the {@link #intrinsicWeight} by the
     * {@link #countWeight};
     */
    public double computeCurrentWeight() {
        return this.intrinsicWeight * this.countWeight;
    }

    public double getIntrinsicWeight() {
        return intrinsicWeight;
    }

    public TIntList getCandidateToPreferenceMap() {
        return candidateOrder;
    }

    public int getCurrentPreferenceIndex() {
        return currentPreferenceIndex;
    }

    public void setCurrentPreferenceIndex(int currentPreferenceIndex) {
        this.currentPreferenceIndex = currentPreferenceIndex;
    }

    public void incrementCurrentPreferenceIndex() {
        this.currentPreferenceIndex++;
    }

    public double getCountWeight() {
        return countWeight;
    }

    public void setCountWeight(float countWeight) {
        this.countWeight = countWeight;
    }
}
