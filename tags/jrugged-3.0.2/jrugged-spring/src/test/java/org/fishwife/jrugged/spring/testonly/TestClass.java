package org.fishwife.jrugged.spring.testonly;

import org.fishwife.jrugged.aspects.CircuitBreaker;
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

    @CircuitBreaker(name = "breakerA")
    public void circuitBreakerMethod1() {

    }

    @CircuitBreaker(name = "breakerB")
    public void circuitBreakerMethod2() {

    }

    public void unmonitoredMethod() {

    }

}
