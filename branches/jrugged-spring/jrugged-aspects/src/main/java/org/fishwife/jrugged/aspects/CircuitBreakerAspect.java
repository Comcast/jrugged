/* Copyright 2009-2010 Comcast Interactive Media, LLC.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package org.fishwife.jrugged.aspects;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Callable;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.fishwife.jrugged.CircuitBreaker;
import org.fishwife.jrugged.DefaultFailureInterpreter;
import org.fishwife.jrugged.FailureInterpreter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Surrounds methods annotated with <code>ExceptionCircuit</code> with a 
 * named CircuitBreaker.
 * 
 * @see ExceptionCircuit
 */
@Aspect
public class CircuitBreakerAspect {

    /**
     * Used to grab properties.
     */
    private static final String CONFIG_KEY_PREFIX = "circuit.";

    // configuration keys
    private static final String LIMIT_KEY = "limit";
    private static final String WINDOWMILLIS_KEY = "windowMillis";
    private static final String RESETMILLIS_KEY = "resetMillis";

    /**
     * Maps names to CircuitBreakers.
     */
    private final Map<String, CircuitBreaker> circuits = new HashMap<String, CircuitBreaker>();

    /**
     * Built from provided properties.
     */
    private final Map<String, CircuitConfig> circuitConfigs = new HashMap<String, CircuitConfig>();

    private final Map<String, Boolean> initializedCircuits = new HashMap<String, Boolean>();
    
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private Properties props;

    public CircuitBreakerAspect() {}

    @Around("@annotation(circuitTag)")
    public Object monitor(final ProceedingJoinPoint pjp,
						  org.fishwife.jrugged.aspects.CircuitBreaker circuitTag) 
		throws Throwable {
        final String name = circuitTag.name();
        CircuitBreaker circuit;
        
        // locks on circuit map to ensure that I don't create 2 copies of a
        // CircuitBreaker if 2 simultaneous threads with the same circuit
        // name hit this block for the first time
        synchronized (this.circuits) {
            if (circuits.containsKey(name)) {
                circuit = circuits.get(name);
            } else {
                circuit = new CircuitBreaker();

                // property config was not provided, so I use
                // a default CircuitBreaker (which trips on a single exception)
                this.logger.info(
                        "circuit '{}' -> using default CircuitBreaker.", name);

                circuits.put(name, circuit);
            }
            
            // sets trip for circuit if it hasn't been initialized
            if (!this.initializedCircuits.containsKey(name)) {
                final FailureInterpreter interpreter = 
					circuit.getFailureInterpreter();

                if(interpreter instanceof DefaultFailureInterpreter) {
                    final Class<? extends Throwable>[] ignore = 
						circuitTag.ignore();
                    ((DefaultFailureInterpreter)interpreter).setIgnore(ignore);
                }
                this.initializedCircuits.put(name, Boolean.TRUE);
            }            
        }

        
        this.logger.debug("circuit:{}, status:{}", name, circuit.getStatus());

        return circuit.invoke(new Callable<Object>() {
             public Object call() throws Exception {
                 try {
                     return pjp.proceed();
                 } catch (Throwable e) {
                     if (e instanceof Exception) {
                         throw (Exception) e;
					 } else if (e instanceof Error) {
                         throw (Error) e;
					 } else {
                         throw (RuntimeException) e;
					 }
                 }
             }
         });
    }

	/** Returns the names of all configured {@link CircuitBreaker}
	 * aspects.
	 * @return {@link Set} of {@link String}s
	 */
    public Set<String> getCircuitBreakerNames() {
        return this.circuits.keySet();
    }

    /**
     * Returns the named {@link CircuitBreaker} if it exists, or
	 *   <code>null</code> if it doesn't.
     * @param circuitName name of the desired CircuitBreaker
	 * @return <code>CircuitBreaker</code> or <code>null</code>
     */
    public CircuitBreaker getCircuitBreaker(String circuitName) {
        return this.circuits.get(circuitName);
    }

    /**
     * Specifies the tolerance configurations for the annotated CircuitBreakers.
     * @param props configuration specified as {@link Properties}
     */
    public void setProperties(Properties props) {
        this.props = props;
    }

    private void loadCircuitConfig() {
        for (final Map.Entry<Object, Object> entry : this.props.entrySet()) {
            try {
                final String key = (String) entry.getKey();
                final String value = (String) entry.getValue();

                if (!key.startsWith(CONFIG_KEY_PREFIX)) {
                    continue;
                }

                final String[] parts = key.substring(CONFIG_KEY_PREFIX.length()).split("\\.", 2);

                if (parts.length != 2)
                    continue;

                final String circuitName = parts[0];
                CircuitConfig config;

                if (this.circuitConfigs.containsKey(circuitName)) {
                    config = this.circuitConfigs.get(circuitName);
                } else {
                    config = new CircuitConfig();
                    this.circuitConfigs.put(circuitName, config);
                }

                final String propName = parts[1];

                if (propName.equals(LIMIT_KEY))
                    config.limit = Integer.parseInt(value);
                else if (propName.equals(WINDOWMILLIS_KEY))
                    config.windowMillis = Integer.parseInt(value);
                else if (propName.equals(RESETMILLIS_KEY))
                    config.resetMillis = Integer.parseInt(value);
                else
                    this.logger.warn("unrecognized property: "
                            + key);
            } catch (Exception e) {
                this.logger.warn("error parsing config", e);
            }
        }
        
        for (final Map.Entry<String, CircuitConfig> e : 
				 this.circuitConfigs.entrySet()) {
            final String circuitName = e.getKey();
            final CircuitConfig config = e.getValue();

            // if these 3 values were set, we can inject a 
            // FailureInterpreter
            if (config.limit > 0 && config.windowMillis > 0
				&& config.resetMillis > 0) {

                final CircuitBreaker circuit = new CircuitBreaker();
                
                // defaults to Exception for trip;
                // I make it more specific when we process the
                // annotation
				FailureInterpreter fi = 
					new DefaultFailureInterpreter(config.limit,
												  config.windowMillis);
                circuit.setFailureInterpreter(fi);
                
                // only sets values if > 0, 
                // since default values in annotation are -1
                if (config.resetMillis > 0) {
                    circuit.setResetMillis(config.resetMillis);
				}

                this.circuits.put(circuitName, circuit);
                this.logger.info(
                        "circuit '{}' -> frequency:{}, period:{}, reset:{}",
                        new Object[] { circuitName, 
									   config.limit, 
									   config.windowMillis, 
									   config.resetMillis });
            }
        }
    }
    
    public void afterPropertiesSet() throws Exception {
        if (this.props == null)
            throw new IllegalArgumentException("[properties] must be provided.");
        this.loadCircuitConfig();
    }

    private static final class CircuitConfig {
        long resetMillis = -1, windowMillis = -1;
        int limit = -1;
    }

}

