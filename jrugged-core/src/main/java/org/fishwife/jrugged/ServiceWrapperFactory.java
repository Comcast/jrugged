package org.fishwife.jrugged;

/**
 * A {@link ServiceWrapperFactory} is a way to create several related
 * but distinct {@link ServiceWrapper} instances.
 *
 */
public interface ServiceWrapperFactory {
    
    /**
     * Create a new <code>ServiceWrapper</code> using the given
     * (presumed unique) identifier.
     * @param name to apply to the new <code>ServiceWrapper</code>
     * @return <code>ServiceWrapper</code>
     */
    ServiceWrapper getWrapperWithName(String name);
}
