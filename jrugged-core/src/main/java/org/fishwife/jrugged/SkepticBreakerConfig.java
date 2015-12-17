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

/**
 * The SkepticBreakerConfig class holds a
 * {@link org.fishwife.jrugged.SkepticBreaker} configuration.
 */
public class SkepticBreakerConfig implements BreakerConfig {

    private FailureInterpreter failureInterpreter;
    
    private long waitMult; 
    private long waitBase; 
    
    private long goodMult; 
    private long goodBase; 
    
    private long maxLevel; 

    public SkepticBreakerConfig(long goodBase, long goodMult, long waitBase, long waitMult,
            long maxLevel, FailureInterpreter failureInterpreter) {
       
    	//ASSUMPTION: start level = 0
    	this.waitMult = waitMult; 
    	this.waitBase = waitBase; 
    	this.goodMult = goodMult; 
    	this.goodBase = goodBase; 
    	this.maxLevel = maxLevel;
        
    	this.failureInterpreter = failureInterpreter;
    }    

    public FailureInterpreter getFailureInterpreter() {
        return failureInterpreter;
    }

	public long getWaitMult() {
		return waitMult;
	}

	public long getWaitBase() {
		return waitBase;
	}

	public long getGoodMult() {
		return goodMult;
	}

	public long getGoodBase() {
		return goodBase;
	}

	public long getMaxLevel() {
		return maxLevel;
	}
}
