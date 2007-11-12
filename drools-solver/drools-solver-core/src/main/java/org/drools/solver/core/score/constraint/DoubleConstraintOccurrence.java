package org.drools.solver.core.score.constraint;

/**
 * @author Geoffrey De Smet
 */
public class DoubleConstraintOccurrence extends ConstraintOccurrence {

    protected double weight;

    public DoubleConstraintOccurrence(String ruleId, Object... causes) {
        this(ruleId, ConstraintType.NEGATIVE_HARD, causes);
    }

    public DoubleConstraintOccurrence(String ruleId, ConstraintType constraintType, Object... causes) {
        this(ruleId, constraintType, 1.0, causes);
    }

    public DoubleConstraintOccurrence(String ruleId, double weight, Object... causes) {
        this(ruleId, ConstraintType.NEGATIVE_HARD, weight, causes);
    }

    public DoubleConstraintOccurrence(String ruleId, ConstraintType constraintType, double weight, Object... causes) {
        super(ruleId, constraintType, causes);
        this.weight = weight;
    }

    public double getWeight() {
        return weight;
    }

}