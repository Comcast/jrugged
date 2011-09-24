package org.fishwife.jrugged.control;

class ClosedLoop<E extends Event, M extends Model<E>, A extends Action> {
    
    private Controller<M,A> controller;
    private M model;
    
    ClosedLoop(Controller<M,A> controller, M model) {
        this.controller = controller;
        this.model = model;
    }
    
    void addObjective(Objective<M> objective) {
        controller.addObjective(objective);
    }
    
    void withdrawObjective(Objective<M> objective) {
        controller.withdrawObjective(objective);
    }
    
    void processEvent(E event) {
        model.update(event);
        controller.assessObjectives(model);
        A a = controller.selectAction(model);
        a.execute();
    }
    
}
