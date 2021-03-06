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

package org.drools.runtime.pipeline.impl;

import java.io.StringWriter;

import javax.xml.bind.Marshaller;

import org.drools.runtime.pipeline.PipelineContext;
import org.drools.runtime.pipeline.Transformer;

public class JaxbToXmlTransformer extends BaseEmitter
    implements
    Transformer {
    private Marshaller            marshaller;

    public JaxbToXmlTransformer(Marshaller marshaller) {
        this.marshaller = marshaller;
    }

    public void receive(Object object,
                       PipelineContext context) {
        Object result = null;
        try {
            StringWriter stringWriter = new StringWriter();
            
            this.marshaller.marshal( object, stringWriter );

            result = stringWriter.getBuffer().toString();
        } catch ( Exception e ) {
            handleException( this,
                             object,
                             e );
        }
        
        emit( result,
              context );
    }

}
