package org.fishwife.jrugged.spring.testonly;

import org.fishwife.jrugged.aspects.PerformanceMonitor;
import org.junit.Ignore;

// Test class under test package to test package scan
@Ignore
public class TestClass {
    
    @PerformanceMonitor("monitorA")
    public void monitoredMethod1() {
        
    }
    
    @PerformanceMonitor("monitorB")
    public void monitoredMethod2() {
        
    }
    
    @PerformanceMonitor("monitorA")
    public void monitoredMethod3() {
        
    }
    
    public void unmonitoredMethod() {
        
    }

}
