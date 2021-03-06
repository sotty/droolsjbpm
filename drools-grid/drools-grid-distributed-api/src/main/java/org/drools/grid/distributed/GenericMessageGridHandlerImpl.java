package org.drools.grid.distributed;

import org.drools.SystemEventListener;
import org.drools.command.impl.ContextImpl;
import org.drools.command.impl.GenericCommand;
import org.drools.grid.DirectoryNodeService;
import org.drools.grid.internal.GenericIoWriter;
import org.drools.grid.internal.GenericMessageHandler;
import org.drools.grid.internal.Message;
import org.drools.grid.internal.NodeData;
import org.drools.runtime.impl.ExecutionResultImpl;

public class GenericMessageGridHandlerImpl
    implements
    GenericMessageHandler {
    private SystemEventListener  systemEventListener;

    private NodeData             data;
    private DirectoryNodeService directory;

    public GenericMessageGridHandlerImpl(NodeData data,
                                         SystemEventListener systemEventListener) {
        this.systemEventListener = systemEventListener;
        this.data = data;

        //        Class[] classes = new Class[]{org.drools.grid.DirectoryNodeService.class};
        //        ServiceTemplate tmpl = new ServiceTemplate(null, classes,null);
        //
        //        LookupDiscoveryManager lookupDiscovery = null;
        //        try {
        //            lookupDiscovery = new LookupDiscoveryManager(LookupDiscoveryManager.ALL_GROUPS, null, null);
        //        } catch (IOException ex) {
        //            Logger.getLogger(GenericMessageGridHandlerImpl.class.getName()).log(Level.SEVERE, null, ex);
        //        }
        //
        //        System.out.println("Discovering Manager service ...");
        //
        //        ServiceDiscoveryManager serviceDiscovery =  null;
        //        try {
        //            serviceDiscovery = new ServiceDiscoveryManager(lookupDiscovery, new LeaseRenewalManager());
        //        } catch (IOException ex) {
        //            Logger.getLogger(GenericMessageGridHandlerImpl.class.getName()).log(Level.SEVERE, null, ex);
        //        }
        //
        //        /* Wait no more then 10 seconds to discover the service */
        //        ServiceItem item = null;
        //        try {
        //            try {
        //                item = serviceDiscovery.lookup(tmpl, null, 1000);
        //            } catch (RemoteException ex) {
        //                Logger.getLogger(GenericMessageGridHandlerImpl.class.getName()).log(Level.SEVERE, null, ex);
        //            }
        //        } catch (InterruptedException ex) {
        //            Logger.getLogger(GenericMessageGridHandlerImpl.class.getName()).log(Level.SEVERE, null, ex);
        //        }
        //        serviceDiscovery.terminate();
        //
        //        if(item != null) {
        //            System.out.println("Discovered Registry service");
        //            if(item.service instanceof DirectoryNodeService){
        //                registry = (DirectoryNodeService)item.service;
        //
        //            }
        //
        //
        //        } else {
        //            System.out.println("Registry service not discovered, make sure the"+
        //                               "service is deployed");
        //        }

    }

    /* (non-Javadoc)
     * @see org.drools.vsm.GenericMessageHandler#messageReceived(org.drools.vsm.GenericIoWriter, org.drools.vsm.Message)
     */
    public void messageReceived(GenericIoWriter session,
                                Message msg) throws Exception {
        this.systemEventListener.debug( "Message receieved : " + msg );

        // we always need to process a List, for genericity, but don't force a List on the payload
        //        List<GenericCommand> commands;
        //        if ( msg.getPayload() instanceof List ) {
        //            commands = (List<GenericCommand>) msg.getPayload();
        //        } else {
        //            commands = new ArrayList<GenericCommand>();
        //            commands.add( (GenericCommand) msg.getPayload() );
        //        }
        GenericCommand command = (GenericCommand) msg.getPayload();

        // Setup the evaluation context 
        ContextImpl localSessionContext = new ContextImpl( "session_" + msg.getSessionId(),
                                                           this.data.getContextManager(),
                                                           this.data.getTemp() );
        ExecutionResultImpl localKresults = new ExecutionResultImpl();
        localSessionContext.set( "kresults_" + msg.getSessionId(),
                                 localKresults );
        //@TODO: replace with Environment ?? this needs to change..
        localSessionContext.set( "registry",
                                 this.directory );

        //        for ( GenericCommand cmd : commands ) {
        //            // evaluate the commands
        //            cmd.execute( localSessionContext );
        //        }

        Object result = command.execute( localSessionContext );

        if ( localSessionContext.get( "kbase" ) != null ) {
            session.write( new Message( msg.getSessionId(),
                                        msg.getResponseId(),
                                        msg.isAsync(),
                                        localSessionContext.get( "kbase" ) ),
                           null );
            return;
        }
        session.write( new Message( msg.getSessionId(),
                                    msg.getResponseId(),
                                    msg.isAsync(),
                                    result ),
                       null );

        //        else if ( !msg.isAsync() && localKresults.getIdentifiers().isEmpty() ) {
        //            // if it's not an async invocation and their are no results, just send a simple notification message
        //            session.write( new Message( msg.getSessionId(),
        //                                        msg.getResponseId(),
        //                                        msg.isAsync(),
        //                                        new FinishedCommand() ), null );
        //
        //
        //        }  else{
        //            // return the payload
        //            session.write( new Message( msg.getSessionId(),
        //                                        msg.getResponseId(),
        //                                        msg.isAsync(),
        //                                        localKresults ), null );
        //        }
    }
}
