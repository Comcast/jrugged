package org.fishwife.jrugged.interval;

import static org.junit.Assert.*;

import java.util.Random;

import org.junit.Test;

public class TestDiscreteInterval {

    private Random random = new Random();
    
    @Test
    public void canCreate() {
        new DiscreteInterval(1, 3);
    }
    
    @Test
    public void allowsMinGreaterThanMax() {
        new DiscreteInterval(3, 1);
    }

    @Test
    public void canGetMinimum() {
        assertEquals(1, (new DiscreteInterval(1, 3)).getMin());
    }
    
    @Test
    public void canGetMaximum() {
        assertEquals(3, (new DiscreteInterval(1, 3)).getMax());
    }
    
    @Test
    public void canAdd() {
        DiscreteInterval i1 = new DiscreteInterval(1, 3);
        DiscreteInterval i2 = new DiscreteInterval(2, 4);
        DiscreteInterval out = i1.plus(i2);
        assertEquals(1 + 2, out.getMin());
        assertEquals(3 + 4, out.getMax());
    }
    
    @Test
    public void canNegate() {
        DiscreteInterval i = new DiscreteInterval(1, 3);
        DiscreteInterval out = i.negate();
        assertEquals(-3, out.getMin());
        assertEquals(-1, out.getMax());
    }
    
    @Test
    public void canSubtract() {
        DiscreteInterval i1 = new DiscreteInterval(1, 3);
        DiscreteInterval i2 = new DiscreteInterval(2, 4);
        DiscreteInterval out = i2.minus(i1);
        assertEquals(-1, out.getMin(), 0.0000001);
        assertEquals(3, out.getMax(), 0.0000001);
    }
    
    @Test
    public void subtractionIsAddingNegative() {
        DiscreteInterval i1 = new DiscreteInterval(random.nextInt(1000), random.nextInt(1000));
        DiscreteInterval i2 = new DiscreteInterval(random.nextInt(1000), random.nextInt(1000));
        DiscreteInterval sub = i1.minus(i2);
        DiscreteInterval sub2 = i1.plus(i2.negate());
        assertEquals(sub.getMin(), sub2.getMin(), 0.0000001);
        assertEquals(sub.getMax(), sub2.getMax(), 0.0000001);
    }
    
    @Test
    public void canMultiply() {
        DiscreteInterval i1 = new DiscreteInterval(1, 3);
        DiscreteInterval i2 = new DiscreteInterval(2, 4);
        DiscreteInterval out = i2.times(i1);
        assertEquals(1 * 2, out.getMin(), 0.0000001);
        assertEquals(3 * 4, out.getMax(), 0.0000001);
    }
    
    @Test
    public void canDivide() {
        DiscreteInterval i1 = new DiscreteInterval(2, 4);
        DiscreteInterval i2 = new DiscreteInterval(1, 3);
        DiscreteInterval out = i1.dividedBy(i2);
        assertEquals(2 / 3, out.getMin(), 0.0000001);
        assertEquals(4 / 1, out.getMax(), 0.0000001);
    }
    
    @Test
    public void multiplicationAndDivisionAreInversesButErrorGrows() {
        DiscreteInterval i1 = new DiscreteInterval(random.nextInt(1000), random.nextInt(1000));
        DiscreteInterval i2 = new DiscreteInterval(random.nextInt(1000), random.nextInt(1000));
        DiscreteInterval out = i1.times(i2).dividedBy(i2);
        assertTrue(i1.getMin() >= out.getMin() && i1.getMin() <= out.getMax());
        assertTrue(i1.getMax() >= out.getMin() && i1.getMax() <= out.getMax());
    }
    
    @Test
    public void doesNotContainNonOverlappingInterval() {
        DiscreteInterval i1 = new DiscreteInterval(1,3);
        assertFalse(i1.contains(new DiscreteInterval(5,6)));
    }
    
    @Test
    public void doesNotContainPartiallyHigherInterval() {
        DiscreteInterval i1 = new DiscreteInterval(1,3);
        assertFalse(i1.contains(new DiscreteInterval(2,4)));
    }

    @Test
    public void doesNotContainPartiallyLowerInterval() {
        DiscreteInterval i1 = new DiscreteInterval(1,3);
        assertFalse(i1.contains(new DiscreteInterval(0,2)));
    }

    @Test
    public void containsFullySurroundedInterval() {
        DiscreteInterval i1 = new DiscreteInterval(1,6);
        assertTrue(i1.contains(new DiscreteInterval(3,4)));
    }

    @Test
    public void containsIntervalWithMatchingMinimum() {
        DiscreteInterval i1 = new DiscreteInterval(1,6);
        assertTrue(i1.contains(new DiscreteInterval(1,4)));
    }

    @Test
    public void containsIntervalWithMatchingMaximum() {
        DiscreteInterval i1 = new DiscreteInterval(1,6);
        assertTrue(i1.contains(new DiscreteInterval(3,6)));
    }

    @Test
    public void containsItself() {
        DiscreteInterval i1 = new DiscreteInterval(1,6);
        assertTrue(i1.contains(i1));
    }

    @Test
    public void greaterThanLowerNonOverlappingInterval() {
        DiscreteInterval i1 = new DiscreteInterval(5,6);
        assertTrue(i1.greaterThan(new DiscreteInterval(1,2)));
    }
    
    @Test
    public void notGreaterThanLowerOverlappingInterval() {
        DiscreteInterval i1 = new DiscreteInterval(3,6);
        assertFalse(i1.greaterThan(new DiscreteInterval(1,4)));
    }

    @Test
    public void notGreaterThanMyself() {
        DiscreteInterval i1 = new DiscreteInterval(3,6);
        assertFalse(i1.greaterThan(i1));
    }

    @Test
    public void notGreaterThanContainedInterval() {
        DiscreteInterval i1 = new DiscreteInterval(3,6);
        assertFalse(i1.greaterThan(new DiscreteInterval(4,5)));
    }

    @Test
    public void notGreaterThanOverlappingPartiallyHigherInterval() {
        DiscreteInterval i1 = new DiscreteInterval(3,6);
        assertFalse(i1.greaterThan(new DiscreteInterval(4,8)));
    }

    @Test
    public void notGreaterThanNonOverlappingHigherInterval() {
        DiscreteInterval i1 = new DiscreteInterval(3,6);
        assertFalse(i1.greaterThan(new DiscreteInterval(7,8)));
    }

    @Test
    public void notLowerThanLowerNonOverlappingInterval() {
        DiscreteInterval i1 = new DiscreteInterval(5,6);
        assertFalse(i1.lessThan(new DiscreteInterval(1,2)));
    }
    
    @Test
    public void notLowerThanLowerPartiallyOverlappingInterval() {
        DiscreteInterval i1 = new DiscreteInterval(3,6);
        assertFalse(i1.lessThan(new DiscreteInterval(1,4)));
    }

    @Test
    public void notLowerThanFullyContainedInterval() {
        DiscreteInterval i1 = new DiscreteInterval(3,6);
        assertFalse(i1.lessThan(new DiscreteInterval(4,5)));
    }

    @Test
    public void notLowerThanMyself() {
        DiscreteInterval i1 = new DiscreteInterval(3,6);
        assertFalse(i1.lessThan(i1));
    }

    @Test
    public void notLowerThanPartiallyHigherInterval() {
        DiscreteInterval i1 = new DiscreteInterval(3,6);
        assertFalse(i1.lessThan(new DiscreteInterval(4,7)));
    }

    @Test
    public void lowerThanFullyHigherInterval() {
        DiscreteInterval i1 = new DiscreteInterval(3,6);
        assertTrue(i1.lessThan(new DiscreteInterval(7,8)));
    }

}
