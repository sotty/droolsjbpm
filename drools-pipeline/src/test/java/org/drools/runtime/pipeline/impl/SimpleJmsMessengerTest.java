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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.naming.Context;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

import org.apache.activemq.broker.BrokerService;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.pipeline.ResultHandler;
import org.drools.runtime.pipeline.ResultHandlerFactory;
import org.drools.runtime.rule.FactHandle;

public class SimpleJmsMessengerTest {

    private SimpleProducer simpleProducer;
    private BrokerService  broker;
    private String         destinationName = "dynamicQueues/FOO.BAR";
    private String         url             = "vm://localhost:61616";

    private Properties     props;

//    public void setUp() {
//        try {
//            this.broker = new BrokerService();
//            // configure the broker
//            this.broker.setBrokerName( "consumer" );
//            this.broker.addConnector( url );
//            this.broker.start();
//
//            props = new Properties();
//            props.setProperty( Context.INITIAL_CONTEXT_FACTORY,
//                               "org.apache.activemq.jndi.ActiveMQInitialContextFactory" );
//            props.setProperty( Context.PROVIDER_URL,
//                               this.url );
//
//            this.simpleProducer = new SimpleProducer( props,
//                                                      this.destinationName );
//            this.simpleProducer.start();
//        } catch ( Exception e ) {
//            throw new RuntimeException( e );
//        }
//    }
//    
//    public void tearDown() throws Exception {
//        this.simpleProducer.stop();
//        this.broker.stop();
//    }    

    @Test @Ignore
    public void testJms() throws Exception {
        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();

        StatefulKnowledgeSessionPipelineImpl pipeline = new StatefulKnowledgeSessionPipelineImpl( ksession );

        JmsUnwrapMessageObject unwrapObjectStage = new JmsUnwrapMessageObject();
        StatefulKnowledgeSessionInsertStage insertStage = new StatefulKnowledgeSessionInsertStage();
        unwrapObjectStage.setReceiver( insertStage );
        ExecuteResultHandler resultHandlerStage = new ExecuteResultHandler();
        insertStage.setReceiver( resultHandlerStage );
        pipeline.setReceiver( unwrapObjectStage );

        ResultHandleFactoryImpl factory = new ResultHandleFactoryImpl();
        JmsMessenger feeder = new JmsMessenger( pipeline,
                                                props,
                                                this.destinationName,
                                                factory );
        feeder.start();
        this.simpleProducer.sendObject( "hello" );
        this.simpleProducer.sendObject( "hello1" );
        this.simpleProducer.sendObject( "hello2" );
        this.simpleProducer.sendObject( "hello3" );

        for ( int i = 0; i < 5; i++ ) {
            // iterate and sleep 5 times, to give these messages time to complete.
            if ( factory.list.size() == 4 ) {
                break;
            }
            Thread.sleep( 500 );
        }

        assertEquals( 4,
                      factory.list.size() );

        FactHandle factHandle = (FactHandle) ((Map) ((ResultHandlerImpl) factory.list.get( 0 )).getObject()).keySet().iterator().next();
        assertEquals( "hello",
                      ksession.getObject( factHandle ) );

        factHandle = (FactHandle) ((Map) ((ResultHandlerImpl) factory.list.get( 1 )).getObject()).keySet().iterator().next();
        assertEquals( "hello1",
                      ksession.getObject( factHandle ) );

        factHandle = (FactHandle) ((Map) ((ResultHandlerImpl) factory.list.get( 2 )).getObject()).keySet().iterator().next();
        assertEquals( "hello2",
                      ksession.getObject( factHandle ) );

        factHandle = (FactHandle) ((Map) ((ResultHandlerImpl) factory.list.get( 3 )).getObject()).keySet().iterator().next();
        assertEquals( "hello3",
                      ksession.getObject( factHandle ) );
        feeder.stop();

    }

    public static class ResultHandleFactoryImpl
        implements
        ResultHandlerFactory {
        List list = new ArrayList();

        public ResultHandler newResultHandler() {
            ResultHandler handler = new ResultHandlerImpl();
            list.add( handler );
            return handler;
        }

    }

    public static class ResultHandlerImpl
        implements
        ResultHandler {
        Object object;

        public void handleResult(Object object) {
            this.object = object;
        }

        public Object getObject() {
            return this.object;
        }
    }
}
