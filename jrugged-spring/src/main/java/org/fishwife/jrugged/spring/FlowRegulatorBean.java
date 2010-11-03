package org.fishwife.jrugged.spring;

import org.fishwife.jrugged.ConstantFlowRegulator;
import org.springframework.jmx.export.annotation.ManagedAttribute;

public class FlowRegulatorBean extends ConstantFlowRegulator {
    public FlowRegulatorBean() {
        super();
    }

    @ManagedAttribute
    @Override
    public void setRequestPerSecondThreshold(int i) {
        super.setRequestPerSecondThreshold(i);
    }

    @ManagedAttribute
    @Override
    public int getRequestPerSecondThreshold() {
        return super.getRequestPerSecondThreshold();
    }
}
