/**
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

/**
 * 
 */
package org.drools.runtime.pipeline.impl;

import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.drools.runtime.pipeline.JaxbTransformerProvider;
import org.drools.runtime.pipeline.Transformer;

public class JaxbTransformerProviderImpl implements JaxbTransformerProvider {
    public Transformer newJaxbFromXmlTransformer(Unmarshaller unmarshaller) {
        return new JaxbFromXmlTransformer( unmarshaller );
    }
    
    public Transformer newJaxbToXmlTransformer(Marshaller marshaller) {
        return new JaxbToXmlTransformer( marshaller );
    }    
}