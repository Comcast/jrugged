package org.fishwife.jrugged.clocks;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import org.fishwife.jrugged.interval.DiscreteInterval;
import org.junit.Before;
import org.junit.Test;


public class TestTimer {

    private HardwareClock mockClock;
    private Timer impl;
    
    @Before
    public void setUp() {
        mockClock = createMock(HardwareClock.class);
        impl = new Timer(mockClock);
    }
    
    @Test
    public void createTimer() {
        new Timer();
    }
    
    @Test
    public void provideAlternativeHardwareClock() {
        new Timer(mockClock);
    }
    
    @Test
    public void canSetTimer() {
        impl.set(40000L,3000L);
    }
    
    @Test(expected=IllegalStateException.class)
    public void cannotStartTimerThanHasNotBeenSet() {
        expect(mockClock.getGranularity()).andReturn(1000L).anyTimes();
        replay(mockClock);
        impl.start();
        verify(mockClock);
    }
    
    @Test
    public void canStartATimerThatHasBeenSet() {
        expect(mockClock.getGranularity()).andReturn(1000L).anyTimes();
        expect(mockClock.getNanoTime()).andReturn(new DiscreteInterval(3L,4L)).anyTimes();
        replay(mockClock);
        impl.set(40000L,3000L);
        impl.start();
        verify(mockClock);
    }
    
    @Test
    public void cannotSetATimerWithASmallerErrorThanClockGranularity() {
        expect(mockClock.getGranularity()).andReturn(1000L).anyTimes();
        replay(mockClock);
        assertFalse(impl.set(1000L, 500L));
        verify(mockClock);
    }

    @Test
    public void hasNotElapsedIfNotStarted() {
        expect(mockClock.getGranularity()).andReturn(1000L).anyTimes();
        expect(mockClock.getNanoTime()).andReturn(new DiscreteInterval(3L,4L)).anyTimes();
        replay(mockClock);
        impl.set(1000L,1000L);
        assertFalse(impl.hasElapsed());
    }
    
    @Test
    public void hasNotElapsedIfClockHasNotTicked() {
        expect(mockClock.getGranularity()).andReturn(1L).anyTimes();
        expect(mockClock.getNanoTime()).andReturn(new DiscreteInterval(4L,6L)).anyTimes();
        replay(mockClock);
        impl.set(1000L,100L);
        impl.start();
        assertFalse(impl.hasElapsed());
        verify(mockClock);
    }
    
    @Test
    public void hasElapsedIfCurrentClockReadingIsWithinTargetRange() {
        expect(mockClock.getGranularity()).andReturn(1L).anyTimes();
        expect(mockClock.getNanoTime()).andReturn(new DiscreteInterval(4L,5L));
        expect(mockClock.getNanoTime()).andReturn(new DiscreteInterval(1010L,1015L)).anyTimes();
        replay(mockClock);
        impl.set(1000L,100L);
        impl.start();
        assertTrue(impl.hasElapsed());
        verify(mockClock);
    }
    
    @Test
    public void hasNotElapsedIfCurrentClockReadingIsNotEntirelyWithinTargetRange() {
        expect(mockClock.getGranularity()).andReturn(1L).anyTimes();
        expect(mockClock.getNanoTime()).andReturn(new DiscreteInterval(4L,5L));
        expect(mockClock.getNanoTime()).andReturn(new DiscreteInterval(900L,1015L)).anyTimes();
        replay(mockClock);
        impl.set(1000L,100L);
        impl.start();
        assertFalse(impl.hasElapsed());
        verify(mockClock);
    }

    @Test
    public void hasElapsedIfCurrentClockReadingPartiallyExceedsTargetRange() {
        expect(mockClock.getGranularity()).andReturn(1L).anyTimes();
        expect(mockClock.getNanoTime()).andReturn(new DiscreteInterval(4L,5L));
        expect(mockClock.getNanoTime()).andReturn(new DiscreteInterval(1015L,2000L)).anyTimes();
        replay(mockClock);
        impl.set(1000L,100L);
        impl.start();
        assertTrue(impl.hasElapsed());
        verify(mockClock);
    }

    @Test
    public void hasElapsedIfCurrentClockReadingCompletelyExceedsTargetRange() {
        expect(mockClock.getGranularity()).andReturn(1L).anyTimes();
        expect(mockClock.getNanoTime()).andReturn(new DiscreteInterval(4L,5L));
        expect(mockClock.getNanoTime()).andReturn(new DiscreteInterval(2000L,2015L)).anyTimes();
        replay(mockClock);
        impl.set(1000L,100L);
        impl.start();
        assertTrue(impl.hasElapsed());
        verify(mockClock);
    }
    
    @Test
    public void isNotLateIfCurrentClockReadingIsBeforeTargetRange() {
        expect(mockClock.getGranularity()).andReturn(1L).anyTimes();
        expect(mockClock.getNanoTime()).andReturn(new DiscreteInterval(4L,5L));
        expect(mockClock.getNanoTime()).andReturn(new DiscreteInterval(200L,205L)).anyTimes();
        replay(mockClock);
        impl.set(1000L,100L);
        impl.start();
        assertFalse(impl.isLate());
        verify(mockClock);
    }

    @Test
    public void isNotLateIfCurrentClockReadingIsPartiallyBeforeTargetRange() {
        expect(mockClock.getGranularity()).andReturn(1L).anyTimes();
        expect(mockClock.getNanoTime()).andReturn(new DiscreteInterval(4L,5L));
        expect(mockClock.getNanoTime()).andReturn(new DiscreteInterval(200L,1006L)).anyTimes();
        replay(mockClock);
        impl.set(1000L,100L);
        impl.start();
        assertFalse(impl.isLate());
        verify(mockClock);
    }

    @Test
    public void isNotLateIfCurrentClockReadingIsWithinTargetRange() {
        expect(mockClock.getGranularity()).andReturn(1L).anyTimes();
        expect(mockClock.getNanoTime()).andReturn(new DiscreteInterval(4L,5L));
        expect(mockClock.getNanoTime()).andReturn(new DiscreteInterval(1005L,1006L)).anyTimes();
        replay(mockClock);
        impl.set(1000L,100L);
        impl.start();
        assertFalse(impl.isLate());
        verify(mockClock);
    }

    @Test
    public void isLateIfCurrentClockReadingIsPartiallyBeyondTargetRange() {
        expect(mockClock.getGranularity()).andReturn(1L).anyTimes();
        expect(mockClock.getNanoTime()).andReturn(new DiscreteInterval(4L,5L));
        expect(mockClock.getNanoTime()).andReturn(new DiscreteInterval(1005L,2006L)).anyTimes();
        replay(mockClock);
        impl.set(1000L,100L);
        impl.start();
        assertTrue(impl.isLate());
        verify(mockClock);
    }

    @Test
    public void isLateIfCurrentClockReadingIsCompletelyBeyondTargetRange() {
        expect(mockClock.getGranularity()).andReturn(1L).anyTimes();
        expect(mockClock.getNanoTime()).andReturn(new DiscreteInterval(4L,5L));
        expect(mockClock.getNanoTime()).andReturn(new DiscreteInterval(2005L,2006L)).anyTimes();
        replay(mockClock);
        impl.set(1000L,100L);
        impl.start();
        assertTrue(impl.isLate());
        verify(mockClock);
    }

    @Test
    public void isNotLateIfNotStarted() {
        expect(mockClock.getGranularity()).andReturn(1L).anyTimes();
        expect(mockClock.getNanoTime()).andReturn(new DiscreteInterval(4L,5L)).anyTimes();
        replay(mockClock);
        impl.set(1000L,100L);
        assertFalse(impl.isLate());
        verify(mockClock);
    }

    @Test
    public void timeRemainingTillElapsedIsDurationIfNotStarted() {
        expect(mockClock.getGranularity()).andReturn(1L).anyTimes();
        expect(mockClock.getNanoTime()).andReturn(new DiscreteInterval(4L,5L)).anyTimes();
        replay(mockClock);
        impl.set(1000L,100L);
        assertEquals(new DiscreteInterval(900L,1100L), impl.getTimeRemaining());
        verify(mockClock);
    }
    
    @Test(expected=IllegalStateException.class)
    public void cannotAskForRemainingTimeIfNotSet() {
        expect(mockClock.getGranularity()).andReturn(1L).anyTimes();
        expect(mockClock.getNanoTime()).andReturn(new DiscreteInterval(4L,5L)).anyTimes();
        replay(mockClock);
        impl.getTimeRemaining();
    }
    
    @Test
    public void hasFullTimeRemainingIfClockHasNotTicked() {
        expect(mockClock.getGranularity()).andReturn(1L).anyTimes();
        expect(mockClock.getNanoTime()).andReturn(new DiscreteInterval(4L,5L)).anyTimes();
        replay(mockClock);
        impl.set(1000L,100L);
        impl.start();
        assertEquals(new DiscreteInterval(899L,1101L), impl.getTimeRemaining());
        verify(mockClock);
    }

    
    @Test
    public void canComputePartialTimeRemaining() {
        expect(mockClock.getGranularity()).andReturn(1L).anyTimes();
        expect(mockClock.getNanoTime()).andReturn(new DiscreteInterval(4L,5L));
        expect(mockClock.getNanoTime()).andReturn(new DiscreteInterval(504L,505L)).anyTimes();
        replay(mockClock);
        impl.set(1000L,100L);
        impl.start();
        assertEquals(new DiscreteInterval(399L,601L), impl.getTimeRemaining());
        verify(mockClock);
    }

    @Test
    public void hasZeroTimeRemainingIfElapsed() {
        expect(mockClock.getGranularity()).andReturn(1L).anyTimes();
        expect(mockClock.getNanoTime()).andReturn(new DiscreteInterval(4L,5L));
        expect(mockClock.getNanoTime()).andReturn(new DiscreteInterval(1005L,1006L)).anyTimes();
        replay(mockClock);
        impl.set(1000L,100L);
        impl.start();
        assertEquals(new DiscreteInterval(0L,0L), impl.getTimeRemaining());
        verify(mockClock);
    }
    
    @Test
    public void canSleepUntilClockElapses() {
        expect(mockClock.getGranularity()).andReturn(1L).anyTimes();
        expect(mockClock.getNanoTime()).andReturn(new DiscreteInterval(4L,5L));
        expect(mockClock.getNanoTime()).andReturn(new DiscreteInterval(504L,505L));
        expect(mockClock.getNanoTime()).andReturn(new DiscreteInterval(704L,705L));
        expect(mockClock.getNanoTime()).andReturn(new DiscreteInterval(1005L,1006L));
        replay(mockClock);
        impl.set(1000L,100L);
        impl.start();
        impl.waitUntilElapsed();
        verify(mockClock);
    }
}
