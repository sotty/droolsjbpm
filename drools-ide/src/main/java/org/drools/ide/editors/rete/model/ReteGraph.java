package org.drools.ide.editors.rete.model;

import java.util.ArrayList;
import java.util.List;

import org.drools.reteoo.BaseVertex;
import org.eclipse.jface.util.PropertyChangeEvent;

/**
 * Rete graph containing a set of vertices that form the graph itself.
 */
public class ReteGraph extends ModelElement {

    /** Property ID to use when a child is added to this diagram. */
    public static final String PROP_CHILD_ADDED   = "ReteGraph.ChildAdded";

    /** Property ID to use when a child is removed from this diagram. */
    public static final String PROP_CHILD_REMOVED = "ReteGraph.ChildRemoved";

    private List               vertices           = new ArrayList();

    /** 
     * Add new BaseVertex to the graph
     * 
     * @param vertex
     * 
     * @return true, if vertex was added, false otherwise
     */
    public boolean addChild(BaseVertex vertex) {
        if ( vertex != null && vertices.add( vertex ) ) {
            firePropertyChange( PROP_CHILD_ADDED,
                                null,
                                vertex );
            return true;
        }
        return false;
    }

    /**
     * Return all Vertices in this graph
     */
    public List getChildren() {
        return vertices;
    }

    /**
     * Remove a vertex from this graph
     * 
     * @param vertex vertex to be removed
     * @return true, if the vertex removal succeeded, false otherwise
     */
    public boolean removeChild(BaseVertex vertex) {
        if ( vertex != null && vertices.remove( vertex ) ) {
            firePropertyChange( PROP_CHILD_REMOVED,
                                null,
                                vertex );
            return true;
        }
        return false;
    }

    /**
     * Removes all vertices from graph.
     */
    public void removeAll() {
        while ( vertices.size() > 0 ) {
            removeChild( ((BaseVertex) vertices.get( 0 )) );
        }
    }

    /**
     * Not implemented. Not required if graph editing functionality is not required.
     * 
     * @param event property change event
     */
    public void propertyChange(PropertyChangeEvent event) {
        // Not relevant
    }

}