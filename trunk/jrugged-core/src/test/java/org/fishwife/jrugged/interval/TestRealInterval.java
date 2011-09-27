package org.fishwife.jrugged.interval;

import static org.junit.Assert.*;

import java.util.Random;

import org.junit.Test;

public class TestRealInterval {

    private Random random = new Random();
    
    @Test
    public void canCreate() {
        new RealInterval(0.1, 0.3);
    }
    
    @Test
    public void allowsMinGreaterThanMax() {
        new RealInterval(0.3, 0.1);
    }

    @Test
    public void canGetMinimum() {
        assertEquals(0.1, (new RealInterval(0.1, 0.3)).getMin(), 0.0000001);
    }
    
    @Test
    public void canGetMaximum() {
        assertEquals(0.3, (new RealInterval(0.1, 0.3)).getMax(), 0.0000001);
    }
    
    @Test
    public void canAdd() {
        RealInterval i1 = new RealInterval(0.1, 0.3);
        RealInterval i2 = new RealInterval(0.2, 0.4);
        RealInterval out = i1.plus(i2);
        assertEquals(0.3, out.getMin(), 0.0000001);
        assertEquals(0.7, out.getMax(), 0.0000001);
    }
    
    @Test
    public void canNegate() {
        RealInterval i = new RealInterval(0.1, 0.3);
        RealInterval out = i.negate();
        assertEquals(-0.3, out.getMin(), 0.0000001);
        assertEquals(-0.1, out.getMax(), 0.0000001);
    }
    
    @Test
    public void canSubtract() {
        RealInterval i1 = new RealInterval(0.1, 0.3);
        RealInterval i2 = new RealInterval(0.2, 0.4);
        RealInterval out = i2.minus(i1);
        assertEquals(-0.1, out.getMin(), 0.0000001);
        assertEquals(0.3, out.getMax(), 0.0000001);
    }
    
    @Test
    public void subtractionIsAddingNegative() {
        RealInterval i1 = new RealInterval(random.nextDouble(), random.nextDouble());
        RealInterval i2 = new RealInterval(random.nextDouble(), random.nextDouble());
        RealInterval sub = i1.minus(i2);
        RealInterval sub2 = i1.plus(i2.negate());
        assertEquals(sub.getMin(), sub2.getMin(), 0.0000001);
        assertEquals(sub.getMax(), sub2.getMax(), 0.0000001);
    }
    
    @Test
    public void canMultiply() {
        RealInterval i1 = new RealInterval(0.1, 0.3);
        RealInterval i2 = new RealInterval(0.2, 0.4);
        RealInterval out = i2.times(i1);
        assertEquals(0.02, out.getMin(), 0.0000001);
        assertEquals(0.12, out.getMax(), 0.0000001);
    }
    
    @Test
    public void canDivide() {
        RealInterval i1 = new RealInterval(0.2, 0.4);
        RealInterval i2 = new RealInterval(0.1, 0.3);
        RealInterval out = i1.dividedBy(i2);
        assertEquals(0.2 / 0.3, out.getMin(), 0.0000001);
        assertEquals(0.4 / 0.1, out.getMax(), 0.0000001);
    }
    
    @Test
    public void multiplicationAndDivisionAreInversesButErrorGrows() {
        RealInterval i1 = new RealInterval(random.nextDouble(), random.nextDouble());
        RealInterval i2 = new RealInterval(random.nextDouble(), random.nextDouble());
        RealInterval out = i1.times(i2).dividedBy(i2);
        assertTrue(i1.getMin() >= out.getMin() && i1.getMin() <= out.getMax());
        assertTrue(i1.getMax() >= out.getMin() && i1.getMax() <= out.getMax());
    }
}
