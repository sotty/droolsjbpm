/*
 * Copyright 2010 salaboy.
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
 * under the License.
 */
package org.drools.grid.remote;

import com.sun.tools.xjc.Options;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Properties;
import java.util.UUID;
import org.drools.KnowledgeBase;
import org.drools.builder.DecisionTableConfiguration;
import org.drools.builder.JaxbConfiguration;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderConfiguration;
import org.drools.builder.KnowledgeBuilderFactoryService;
import org.drools.command.SetVariableCommand;
import org.drools.command.builder.NewKnowledgeBuilderCommand;
import org.drools.grid.Grid;
import org.drools.grid.GridNode;
import org.drools.grid.GridServiceDescription;
import org.drools.grid.io.ConversationManager;
import org.drools.grid.io.impl.CommandImpl;

/**
 *
 * @author salaboy
 */
public class KnowledgeBuilderProviderRemoteClient
    implements
    KnowledgeBuilderFactoryService {

    private Grid                             grid;
    private GridServiceDescription<GridNode> gsd;

    public KnowledgeBuilderProviderRemoteClient(Grid grid,
                                                GridServiceDescription gsd) {
        this.grid = grid;
        this.gsd = gsd;
    }

    public KnowledgeBuilderConfiguration newKnowledgeBuilderConfiguration() {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public KnowledgeBuilderConfiguration newKnowledgeBuilderConfiguration(Properties properties,
                                                                          ClassLoader... classLoader) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public DecisionTableConfiguration newDecisionTableConfiguration() {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public KnowledgeBuilder newKnowledgeBuilder() {
        return newKnowledgeBuilder( null,
                                    null );

    }

    public KnowledgeBuilder newKnowledgeBuilder(KnowledgeBuilderConfiguration conf) {
        return newKnowledgeBuilder( null,
                                    conf );
    }

    public KnowledgeBuilder newKnowledgeBuilder(KnowledgeBase kbase) {
        return newKnowledgeBuilder( kbase,
                                    null );
    }

    public KnowledgeBuilder newKnowledgeBuilder(KnowledgeBase kbase,
                                                KnowledgeBuilderConfiguration conf) {
        String localId = UUID.randomUUID().toString();

        CommandImpl cmd = new CommandImpl( "execute",
                                           Arrays.asList( new Object[]{new SetVariableCommand( "__TEMP__",
                                                                                                localId,
                                                                                                new NewKnowledgeBuilderCommand( conf ) )} ) );

        ConversationManager connm = this.grid.get( ConversationManager.class );
        ConversationUtil.sendMessage( connm,
                                      (InetSocketAddress) this.gsd.getAddresses().get( "socket" ).getObject(),
                                      this.gsd.getId(),
                                      cmd );

        return new KnowledgeBuilderRemoteClient( localId,
                                                 this.gsd,
                                                 connm );
    }

    public JaxbConfiguration newJaxbConfiguration(Options xjcOpts,
                                                  String systemId) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

}
