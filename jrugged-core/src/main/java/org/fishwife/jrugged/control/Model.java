package org.fishwife.jrugged.control;

interface Model<E extends Event> {
    void update(E event);
}
