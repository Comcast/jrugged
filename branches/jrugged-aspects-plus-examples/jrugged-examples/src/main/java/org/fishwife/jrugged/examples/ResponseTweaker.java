package org.fishwife.jrugged.examples;

import java.util.Random;

import org.fishwife.jrugged.aspects.Monitorable;

public class ResponseTweaker {

    @Monitorable("ResponseTweaker")
    public int delay() {
        Random r = new Random();
        int count = r.nextInt(2001);
        try {
            Thread.sleep(count);
        } catch (InterruptedException e) { }
        return count;
    }
    
}
