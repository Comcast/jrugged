package org.fishwife.jrugged.control;

interface Controller<M extends Model<? extends Event>, A extends Action> {
    void addObjective(Objective<M> objective);
    
    void withdrawObjective(Objective<M> objective);
    
    A selectAction(M model);

    void assessObjectives(M model);
}
