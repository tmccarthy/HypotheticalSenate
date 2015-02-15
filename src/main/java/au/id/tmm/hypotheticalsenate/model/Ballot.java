package au.id.tmm.hypotheticalsenate.model;

import gnu.trove.list.TIntList;

import java.util.OptionalInt;

/**
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

    public void multiplyCurrentWeightBy(double factor) {
        this.countWeight *= factor;
    }

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
