/*
 * Copyright 2007 JBoss Inc
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Created on Nov 29, 2007
 */
package org.drools;

import org.drools.temporal.SessionClock;
import org.drools.time.impl.SessionPseudoClock;

/**
 * This enum represents all engine supported clocks
 * 
 * @author etirelli
 */
public enum ClockType {
//  SYSTEM_CLOCK {
//      public SessionClock newInstance() {
//          return new SessionPseudoClock();
//      }
//  },
  
  /**
   * A Pseudo clock is a clock that is completely controled by the
   * client application. It is usually used during simulations or tests
   */
  PSEUDO_CLOCK {
      public SessionClock createInstance() {
          return new SessionPseudoClock();
      }
  };
  
  public abstract SessionClock createInstance();
  
}

