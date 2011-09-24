package org.fishwife.jrugged.control;

interface Objective<M extends Model<? extends Event>> {
    boolean isMetBy(M state);
    boolean hasFailed(M state);
    void succeed();
    void fail();
}
