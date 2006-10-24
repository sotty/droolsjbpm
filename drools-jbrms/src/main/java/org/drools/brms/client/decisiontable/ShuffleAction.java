package org.drools.brms.client.decisiontable;

import org.drools.brms.client.decisiontable.EditableDTGrid.RowClickListener;

import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

/**
 * This shows the widget for moving a row up or down.
 * @author Steven Williams
 */
public class ShuffleAction extends Composite {

    private HorizontalPanel panel = new HorizontalPanel();
    private Image shuffle;
    private int row;
    
    public final static ShuffleAction EMPTY1 = new ShuffleAction() {
    	
    };
    
    public ShuffleAction() {
    	initWidget(panel);
    }
    /**
     * Pass in the click listener delegate for when the respective action is clicked and the
     * direction to move the row
     * @param currentRow
     * @param clickListener
     * @param direction
     */
    public ShuffleAction(final int currentRow, final RowClickListener clickListener, final String direction) {
        row = currentRow;
        shuffle = new Image("images/shuffle_" + direction + ".gif");
        shuffle.setTitle( "Move this row " + direction );
        shuffle.addClickListener( new ClickListener() {
			public void onClick(final Widget w) {
				clickListener.onClick(w, row);
			}
        });
        
        panel.add( shuffle);
        
        initWidget( panel );
    }
    
    public void setRow(final int row) {
    	this.row = row;
    }
    
    public int getRow() {
    	return row;
    }
    
}
