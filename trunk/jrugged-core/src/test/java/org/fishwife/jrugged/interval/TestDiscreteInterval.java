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
    public void addCanHandleWraparoundAddition() {
        long x = Long.MAX_VALUE - 5;
        DiscreteInterval i1 = new DiscreteInterval(x-1,x);
        DiscreteInterval out = i1.plus(new DiscreteInterval(10,10));
        assertEquals(Long.MIN_VALUE + 3, out.getMin());
        assertEquals(Long.MIN_VALUE + 4, out.getMax());
    }
    
    @Test
    public void addCanHandleAdditionOfStraddlingInterval() {
        DiscreteInterval i1 = new DiscreteInterval(Long.MAX_VALUE,Long.MIN_VALUE);
        DiscreteInterval out = i1.plus(new DiscreteInterval(10,10));
        assertEquals(Long.MIN_VALUE + 9, out.getMin());
        assertEquals(Long.MIN_VALUE + 10, out.getMax());
    }
    
    @Test
    public void canNegate() {
        DiscreteInterval i = new DiscreteInterval(1, 3);
        DiscreteInterval out = i.negate();
        assertEquals(-3, out.getMin());
        assertEquals(-1, out.getMax());
    }
    
    @Test
    public void sizeOfConstantIntervalIsOne() {
        DiscreteInterval i = new DiscreteInterval(1,1);
        assertEquals(1, i.size());
    }
    
    @Test
    public void sizeOfWiderIntervalReturnsDifferencePlusOne() {
        DiscreteInterval i = new DiscreteInterval(1,3);
        assertEquals(3, i.size());
    }
    
    @Test
    public void negationOfPositiveIntervalMaintainsSize() {
        DiscreteInterval i = new DiscreteInterval(1, 3);
        assertEquals(i.size(), i.negate().size());
    }
    
    @Test
    public void negationOfStraddlingIntervalMaintainsSize() {
        DiscreteInterval i = new DiscreteInterval(Long.MAX_VALUE, Long.MIN_VALUE);
        assertEquals(i.size(), i.negate().size());
    }
    
    @Test
    public void negationOfNegativeIntervalMaintainsSize() {
        DiscreteInterval i = new DiscreteInterval(-3, -1);
        assertEquals(i.size(), i.negate().size());
    }
    
    @Test
    public void canSubtract() {
        DiscreteInterval i1 = new DiscreteInterval(1, 3);
        DiscreteInterval i2 = new DiscreteInterval(2, 4);
        DiscreteInterval out = i2.minus(i1);
        assertEquals(-1, out.getMin());
        assertEquals(3, out.getMax());
    }
    
    @Test
    public void subtractionIsAddingNegative() {
        DiscreteInterval i1 = new DiscreteInterval(random.nextInt(1000), random.nextInt(1000));
        DiscreteInterval i2 = new DiscreteInterval(random.nextInt(1000), random.nextInt(1000));
        DiscreteInterval sub = i1.minus(i2);
        DiscreteInterval sub2 = i1.plus(i2.negate());
        assertEquals(sub.getMin(), sub2.getMin());
        assertEquals(sub.getMax(), sub2.getMax());
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

}
