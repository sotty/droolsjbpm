/*
 * Copyright 2010 JBoss Inc
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
 */

package org.drools.reteoo;

/**
 * Interface implemented by all sink adapters to allow
 * nodes to create tuples that properly reference the real
 * sink. 
 */
public interface LeftTupleSinkAdapter {

    /**
     * Returns the sink node that this adapter is wrapping
     */
    public LeftTupleSink getRealSink();
    
}
