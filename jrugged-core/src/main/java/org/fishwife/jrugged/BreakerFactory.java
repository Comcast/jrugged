/* Copyright 2009-2015 Comcast Interactive Media, LLC.

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
package org.fishwife.jrugged;

import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory to create new {@link CircuitBreaker} and {@link SkepticBreaker} 
 * instances and keep track of them.
 */
public class BreakerFactory {

	public static final String CIRCUIT_BREAKER_CONFIG_KEY_PREFIX = "circuit";
	public static final String SKEPTIC_BREAKER_CONFIG_KEY_PREFIX = "skeptic";
	public static final String LIMIT_KEY = "limit";
	public static final String WINDOWMILLIS_KEY = "windowMillis";
	public static final String RESETMILLIS_KEY = "resetMillis";
	
	public static final String WAITMULT_KEY = "waitMult";
    public static final String GOODMULT_KEY = "goodMult";
    public static final String WAITBASE_KEY = "waitBase";
    public static final String GOODBASE_KEY = "goodBase";
    public static final String MAXLEVEL_KEY = "maxLevel";

	private final ConcurrentHashMap<String, CircuitBreaker> circuitBreakerMap =
			new ConcurrentHashMap<String, CircuitBreaker>();
	 private final ConcurrentHashMap<String, SkepticBreaker> skepticBreakerMap =
	            new ConcurrentHashMap<String, SkepticBreaker>();

	private Properties circuitBreakerProperties;
	private Properties skepticBreakerProperties;

	private final Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * Create a new {@link CircuitBreaker} and map it to the provided name.
	 * If the CircuitBreaker already exists, then the existing instance is
	 * returned.
	 * @param name the name of the {@link CircuitBreaker}
	 * @param config the {@link CircuitBreakerConfig} with the configuration
	 * values.
	 * @return the created {@link CircuitBreaker}
	 */
	public synchronized CircuitBreaker createCircuitBreaker(String name,
			CircuitBreakerConfig config) {
		CircuitBreaker circuitBreaker = findCircuitBreaker(name);

		if (circuitBreaker == null) {
			circuitBreaker = new CircuitBreaker(name);

			configureCircuitBreaker(name, circuitBreaker, config);
			addCircuitBreakerToMap(name, circuitBreaker);
		}

		return circuitBreaker;
	}
	
	/**
     * Create a new {@link SkepticBreaker} and map it to the provided name.
     * If the SkepticBreaker already exists, then the existing instance is
     * returned.
     * @param name the name of the {@link SkepticBreaker}
     * @param config the {@link SkepticBreakerConfig} with the configuration
     * values.
     * @return the created {@link SkepticBreaker}
     */
    public synchronized SkepticBreaker createSkepticBreaker(String name,
            SkepticBreakerConfig config) {
        SkepticBreaker skepticBreaker = findSkepticBreaker(name);

        if (skepticBreaker == null) {
            skepticBreaker = new SkepticBreaker(name);

            configureSkepticBreaker(name, skepticBreaker, config);
            addSkepticBreakerToMap(name, skepticBreaker);
        }

        return skepticBreaker;
    }

    /**
	 * Set the {@link Properties} object to search for {@link CircuitBreaker}
	 * property override values.  The override values can be specified as:<br>
	 *   circuit.{circuit_name}.limit<br>
	 *   circuit.{circuit_name}.resetmillis<br>
	 *   circuit.{circuit_name}.windowmillis
	 * @param properties the {@link Properties} object to search.
	 */
	public void setCircuitBreakerProperties(Properties properties) {
		this.circuitBreakerProperties = properties;
	}
    
	/**
	 * Set the {@link Properties} object to search for {@link SkepticBreaker}
	 * property override values.  The override values can be specified as:<br>
	 *   skeptic.{skeptic_name}.limit<br>
	 *   skeptic.{skeptic_name}.waitmult<br>
	 *   skeptic.{skeptic_name}.goodmult<br>
	 *   skeptic.{skeptic_name}.waitbase<br>
	 *   skeptic.{skeptic_name}.goodbase<br>
	 *   skeptic.{skeptic_name}.maxlevel<br>
	 *   skeptic.{skeptic_name}.windowmillis
	 * @param properties the {@link Properties} object to search.
	 */
	public void setSkepticBreakerProperties(Properties properties) {
		this.skepticBreakerProperties = properties;
	}

	/**
	 * Find an existing {@link CircuitBreaker}
	 * @param name the value for the {@link CircuitBreaker}
	 * @return the found {@link CircuitBreaker}, or null if it is not found.
	 */
	public CircuitBreaker findCircuitBreaker(String name) {
		return circuitBreakerMap.get(name);
	}

    /**
     * Find an existing {@link SkepticBreaker}
     * @param name the value for the {@link SkepticBreaker}
     * @return the found {@link SkepticBreaker}, or null if it is not found.
     */
    public SkepticBreaker findSkepticBreaker(String name) {
        return skepticBreakerMap.get(name);
    }

    /**
	 * Get the {@link Set} of created {@link CircuitBreaker} names.
	 * @return the {@link Set} of names.
	 */
	public Set<String> getCircuitBreakerNames() {
		return circuitBreakerMap.keySet();
	}

    /**
     * Get the {@link Set} of created {@link SkepticBreaker} names.
     * @return the {@link Set} of names.
     */
    public Set<String> getSkepticBreakerNames() {
        return skepticBreakerMap.keySet();
    }

    protected void configureCircuitBreaker(String name,
			CircuitBreaker circuit,
			CircuitBreakerConfig config) {

		long resetMillis = config.getResetMillis();
		Long resetMillisOverride = getCircuitBreakerLongPropertyOverrideValue(name, RESETMILLIS_KEY);
		if (resetMillisOverride != null) {
			resetMillis = resetMillisOverride;
		}

		FailureInterpreter fi = config.getFailureInterpreter();
		circuit.setFailureInterpreter(fi);

		if (resetMillis > 0) {
			circuit.setResetMillis(resetMillis);
		}

		if (fi instanceof DefaultFailureInterpreter) {
			configureCircuitBreakerDefaultFailureInterpreter(name, resetMillis, circuit);
		}
		else {
			logger.info(
					"Created CircuitBreaker '{}', resetMillis={}",
					new Object[] {
							name,
							resetMillis
					});
		}
	}

    protected void configureSkepticBreaker(String name,
            SkepticBreaker skeptic,
            SkepticBreakerConfig config) {

    	long waitMult = config.getWaitMult();
        Long waitMultOverride = getSkepticBreakerLongPropertyOverrideValue(name, WAITMULT_KEY);
        if (waitMultOverride != null) {
            waitMult = waitMultOverride;
        }
        
        long goodMult = config.getGoodMult();
        Long goodMultOverride = getSkepticBreakerLongPropertyOverrideValue(name, GOODMULT_KEY);
        if (goodMultOverride != null) {
            goodMult = goodMultOverride;
        }
    	
        long waitBase = config.getWaitBase();
        Long waitBaseOverride = getSkepticBreakerLongPropertyOverrideValue(name, WAITBASE_KEY);
        if (waitBaseOverride != null) {
            waitBase = waitBaseOverride;
        }
        
        long goodBase = config.getGoodBase();
        Long goodBaseOverride = getSkepticBreakerLongPropertyOverrideValue(name, GOODBASE_KEY);
        if (goodBaseOverride != null) {
            goodBase = goodBaseOverride;
        }
        
        long maxLevel = config.getMaxLevel();
        Long maxLevelOverride = getSkepticBreakerLongPropertyOverrideValue(name, MAXLEVEL_KEY);
        if (maxLevelOverride != null) {
        	maxLevel = maxLevelOverride;
        }

        FailureInterpreter fi = config.getFailureInterpreter();
        skeptic.setFailureInterpreter(fi);

        if (waitMult > 0) {
            skeptic.setWaitMult(waitMult);
        }
        
        if (goodMult > 0) {
            skeptic.setGoodMult(goodMult);
        }
        
        if (waitBase > 0) {
            skeptic.setWaitBase(waitBase);
        }
        
        if (goodBase > 0) {
            skeptic.setGoodBase(goodBase);
        }
        
        if (maxLevel > 0) {
            skeptic.setMaxLevel(maxLevel);
        }
        
        skeptic.updateTimers();

        if (fi instanceof DefaultFailureInterpreter) {
            configureSkepticBreakerDefaultFailureInterpreter(name, waitMult, goodMult, waitBase, 
            		goodBase, maxLevel, skeptic);
        }
        else {
            logger.info(
                "Created SkepticBreaker '{}', waitMult={}, goodMult={}, waitBase={}, " +
                "goodBase={}, maxLevel={}",
                new Object[] {
                  name,
                  waitMult,
                  goodMult,
                  waitBase,
                  goodBase,
                  maxLevel
                });
        }
    }

    private void configureCircuitBreakerDefaultFailureInterpreter(String name, long resetMillis, CircuitBreaker circuit) {
		DefaultFailureInterpreter fi = (DefaultFailureInterpreter) circuit.getFailureInterpreter();

		Integer limitOverride = getCircuitBreakerIntegerPropertyOverrideValue(name, LIMIT_KEY);

		if (limitOverride != null) {
			fi.setLimit(limitOverride);
		}

		Long windowMillisOverride = getCircuitBreakerLongPropertyOverrideValue(name, WINDOWMILLIS_KEY);

		if (windowMillisOverride != null) {
			fi.setWindowMillis(windowMillisOverride);
		}

		logger.info(
				"Created CircuitBreaker '{}', limit={}, windowMillis={}, resetMillis={}",
				new Object[] {
						name,
						fi.getLimit(),
						fi.getWindowMillis(),
						resetMillis
				});
	}
    
    private void configureSkepticBreakerDefaultFailureInterpreter(String name, long waitMult, 
    		long goodMult, long waitBase, long goodBase, long maxLevel, SkepticBreaker skeptic) {
        DefaultFailureInterpreter fi = (DefaultFailureInterpreter) skeptic.getFailureInterpreter();

        Integer limitOverride = getSkepticBreakerIntegerPropertyOverrideValue(name, LIMIT_KEY);

        if (limitOverride != null) {
            fi.setLimit(limitOverride);
        }

        Long windowMillisOverride = getSkepticBreakerLongPropertyOverrideValue(name, WINDOWMILLIS_KEY);

        if (windowMillisOverride != null) {
            fi.setWindowMillis(windowMillisOverride);
        }

        logger.info(
            "Created SkepticBreaker '{}', limit={}, windowMillis={}, waitMult={}, " + 
            "goodMult={}, waitBase={}, goodBase={}, maxLevel={}",
            new Object[] {
              name,
              fi.getLimit(),
              fi.getWindowMillis(),
              waitMult,
              goodMult,
              waitBase,
              goodBase,
              maxLevel
            });
    }

    /**
	 * Add a {@link CircuitBreaker} to the map.
	 * @param name the name for the {@link CircuitBreaker}
	 * @param circuitBreaker the {@link CircuitBreaker} to add.
	 */
	protected void addCircuitBreakerToMap(String name, CircuitBreaker circuitBreaker) {
		circuitBreakerMap.put(name, circuitBreaker);
	}

    /**
     * Add a {@link SkepticBreaker} to the map.
     * @param name the name for the {@link SkepticBreaker}
     * @param skepticBreaker the {@link SkepticBreaker} to add.
     */
    protected void addSkepticBreakerToMap(String name, SkepticBreaker skepticBreaker) {
        skepticBreakerMap.put(name, skepticBreaker);
    }

    /**
	 * Get the property name for a circuit name and key.
	 * @param name the circuit name.
	 * @param key the property key.
	 * @return the property name.
	 */
	private String getCircuitBreakerPropertyName(String name, String key) {
		return CIRCUIT_BREAKER_CONFIG_KEY_PREFIX + '.' + name + '.' + key;
	}

	/**
	 * Get the property name for a skeptic name and key.
	 * @param name the skeptic name.
	 * @param key the property key.
	 * @return the property name.
	 */
	private String getSkepticBreakerPropertyName(String name, String key) {
		return SKEPTIC_BREAKER_CONFIG_KEY_PREFIX + '.' + name + '.' + key;
	}
	
	/**
     * Get an integer property override value.
     * @param name the {@link CircuitBreaker} name.
     * @param key the property override key.
     * @return the property override value, or null if it is not found.
     */
    private Integer getCircuitBreakerIntegerPropertyOverrideValue(String name, String key) {
        if (circuitBreakerProperties != null) {
            String propertyName = getCircuitBreakerPropertyName(name, key);

            String propertyOverrideValue = circuitBreakerProperties.getProperty(propertyName);

            if (propertyOverrideValue != null) {
                try {
                    return Integer.parseInt(propertyOverrideValue);
                }
                catch (NumberFormatException e) {
                    logger.error("Could not parse property override key={}, value={}",
                            key, propertyOverrideValue);
                }
            }
        }
        return null;
    }

    /**
	 * Get an integer property override value.
	 * @param name the {@link SkepticBreaker} name.
	 * @param key the property override key.
	 * @return the property override value, or null if it is not found.
	 */
	private Integer getSkepticBreakerIntegerPropertyOverrideValue(String name, String key) {
		if (skepticBreakerProperties != null) {
			String propertyName = getSkepticBreakerPropertyName(name, key);

			String propertyOverrideValue = skepticBreakerProperties.getProperty(propertyName);

			if (propertyOverrideValue != null) {
				try {
					return Integer.parseInt(propertyOverrideValue);
				}
				catch (NumberFormatException e) {
					logger.error("Could not parse property override key={}, value={}",
							key, propertyOverrideValue);
				}
			}
		}
		return null;
	}

    /**
     * Get an {@link Long} property override value.
     * @param name the {@link CircuitBreaker} name.
     * @param key the property override key.
     * @return the property override value, or null if it is not found.
     */
    private Long getCircuitBreakerLongPropertyOverrideValue(String name, String key) {
        if (circuitBreakerProperties != null) {
            String propertyName = getCircuitBreakerPropertyName(name, key);

            String propertyOverrideValue = circuitBreakerProperties.getProperty(propertyName);

            if (propertyOverrideValue != null) {
                try {
                    return Long.parseLong(propertyOverrideValue);
                }
                catch (NumberFormatException e) {
                    logger.error("Could not parse property override key={}, value={}",
                            key, propertyOverrideValue);
                }
            }
        }
        return null;
    }

	/**
	 * Get an {@link Long} property override value.
	 * @param name the {@link SkepticBreaker} name.
	 * @param key the property override key.
	 * @return the property override value, or null if it is not found.
	 */
	private Long getSkepticBreakerLongPropertyOverrideValue(String name, String key) {
		if (skepticBreakerProperties != null) {
			String propertyName = getSkepticBreakerPropertyName(name, key);

			String propertyOverrideValue = skepticBreakerProperties.getProperty(propertyName);

			if (propertyOverrideValue != null) {
				try {
					return Long.parseLong(propertyOverrideValue);
				}
				catch (NumberFormatException e) {
					logger.error("Could not parse property override key={}, value={}",
							key, propertyOverrideValue);
				}
			}
		}
		return null;
	}
}
