package org.drools.brms.client.packages;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.drools.brms.client.common.ErrorPopup;
import org.drools.brms.client.common.FormStyleLayout;
import org.drools.brms.client.common.FormStylePopup;
import org.drools.brms.client.common.GenericCallback;
import org.drools.brms.client.common.LoadingPopup;
import org.drools.brms.client.rpc.BuilderResult;
import org.drools.brms.client.rpc.PackageConfigData;
import org.drools.brms.client.rpc.RepositoryServiceFactory;
import org.drools.brms.client.rpc.SnapshotInfo;
import org.drools.brms.client.rulelist.EditItemEvent;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * This is the widget for building packages, validating etc. 
 * 
 * @author Michael Neale
 */
public class PackageBuilderWidget extends Composite {


    public FormStyleLayout layout;
    private PackageConfigData conf;
    private EditItemEvent editEvent;

    
    public PackageBuilderWidget(final PackageConfigData conf, EditItemEvent editEvent) {
        layout = new FormStyleLayout("images/package_builder.png", "Verify and assemble package");
        this.conf = conf;
        this.editEvent = editEvent;
        
        final SimplePanel buildResults = new SimplePanel();

        Button b = new Button("Build package");
        b.addClickListener( new ClickListener() {
            public void onClick(Widget w) {
                doBuild(buildResults);
            }
        } );
        
        Button buildSource = new Button("Show package source");
        buildSource.addClickListener( new ClickListener() {
            public void onClick(Widget w) {
                doBuildSource(conf.uuid, conf.name);
            }
        } );
        layout.addAttribute( "View source for package", buildSource );
        layout.addAttribute( "Build binary package:", b);
        layout.addRow( new HTML("<i><small>Building a package will collect all the assets, validate and compile into a deployable package.</small></i>") );
        layout.addRow( buildResults );
        
        layout.setStyleName( "package-Editor" );
        
        layout.setWidth( "100%" );
        
        initWidget( layout );
    }
    
    
    
    /**
     * Actually build the source, and display it.
     */
    public static void doBuildSource(final String uuid, final String name) {
        LoadingPopup.showMessage( "Assembling package source..." );
        DeferredCommand.add( new Command() {
            public void execute() {
                RepositoryServiceFactory.getService().buildPackageSource( uuid, new GenericCallback() {
                    public void onSuccess(Object data) {
                        String content = (String) data;
                        showSource(content, name);
                    }
                });
            }
        } );
    }



    /**
     * Popup the view source.
     */
    private static void showSource(String content, String name) {
        FormStylePopup pop = new FormStylePopup("images/view_source.gif", "Viewing source for: " + name);
        TextArea area = new TextArea();
        area.setVisibleLines( 48 );
        area.setWidth( "100%" );
        area.setCharacterWidth( 80 );
        pop.addRow( area );
        area.setText( content );
        pop.setPopupPosition( 30, 30 );
        LoadingPopup.close();
        pop.show();
    }



    /**
     * Actually do the building.
     * @param buildResults The panel to stuff the results in.
     */
    private void doBuild(final Panel buildResults) {

        buildResults.clear();
        
        final HorizontalPanel busy = new HorizontalPanel();
        busy.add( new Label("Validating and building package, please wait...") );
        busy.add( new Image("images/spinner.gif") );
        
        buildResults.add( busy );
        
        DeferredCommand.add( new Command() {
            public void execute() {
                RepositoryServiceFactory.getService().buildPackage( conf.uuid, new AsyncCallback() {
                    public void onSuccess(Object data) {
                        if (data == null) {
                            showSuccessfulBuild(buildResults);
                            
                        } else {
                            BuilderResult[] results = (BuilderResult[]) data;
                            showBuilderErrors(results, buildResults);
                        }
                    }
                    public void onFailure(Throwable t) {
                        ErrorPopup.showMessage( t.getMessage() );
                        buildResults.clear();
                    }
                });
            }
        });
        

        
    }    
    
    /**
     * This is called to display the success (and a download option).
     * @param buildResults
     */
    private void showSuccessfulBuild(Panel buildResults) {
        buildResults.clear();
        VerticalPanel vert = new VerticalPanel();
        
        vert.add( new HTML("<img src='images/tick_green.gif'/><i>Package built successfully.</i>") );
        final String hyp = getDownloadLink(this.conf);
        Button download = new Button("Download binary package");
        download.addClickListener( new ClickListener()  {
            public void onClick(Widget arg0) {
                        Window.open( hyp, "downloading...", "" );
            }
        });
        
        vert.add( download );
        
        Button snap = new Button("Create snapshot for deployment");
        snap.addClickListener( new ClickListener() {
            public void onClick(Widget w) {
                showSnapshotDialog(w);
            }
        } );
        vert.add( snap );
        
        buildResults.add( vert );
    }


    /**
     * Get a download link for the binary package.
     */
    public static String getDownloadLink(PackageConfigData conf) {
        String hurl = GWT.getModuleBaseURL() + "package/" + conf.name;
        if (!conf.isSnapshot) {
            hurl = hurl + "/" + PackageSnapshotView.LATEST_SNAPSHOT;
        } else {
            hurl = hurl + "/" + conf.snapshotName;
        }
        final String uri = hurl;
        return uri;
    }
    
    
    /**
     * This is called in the unhappy event of there being errors.  
     */
    private void showBuilderErrors(BuilderResult[] results, Panel buildResults) {
        buildResults.clear();
        
        FlexTable errTable = new FlexTable();
        errTable.setStyleName( "build-Results" );
        errTable.setText( 0, 1, "Format" );
        errTable.setText( 0, 2, "Name" );
        errTable.setText( 0, 3, "Message" );
        
        for ( int i = 0; i < results.length; i++ ) {
            int row = i+1;
            final BuilderResult res = results[i];
            errTable.setWidget( row, 0, new Image("images/error.gif"));
            errTable.setText( row, 1, res.assetFormat );
            errTable.setText( row, 2, res.assetName );
            errTable.setText( row, 3, res.message );
            
            if (!"package".equals( res.assetFormat)) {
                Button show = new Button("Show");
                show.addClickListener( new ClickListener() {
                    public void onClick(Widget w) {
                        editEvent.open( res.uuid );
                    }
                } );
                errTable.setWidget( row, 4, show );                
            }
        }
        
        errTable.setWidth( "100%" );
        buildResults.add( errTable );
        
    }        
    
    /**
     * This will display a dialog for creating a snapshot.
     */
    private void showSnapshotDialog(Widget w) {
        LoadingPopup.showMessage( "Loading existing snapshots..." );
        final FormStylePopup form = new FormStylePopup("images/snapshot.png", "Create a snapshot for deployment.");
        form.addRow( new HTML("<i>A package snapshot is essentially a " +
                "read only 'locked in' and labelled view of a package at a point in time, which can be used for deployment.</i>") );
        
        final VerticalPanel vert = new VerticalPanel();
        form.addAttribute( "Choose or create snapshot name:",  vert);
        final List radioList = new ArrayList();
        final TextBox newName = new TextBox();
        final String newSnapshotText = "NEW: ";
        
        RepositoryServiceFactory.getService().listSnapshots( conf.name, new GenericCallback() {
            public void onSuccess(Object data) {
                SnapshotInfo[] result = (SnapshotInfo[]) data;
                for ( int i = 0; i < result.length; i++ ) {
                    RadioButton existing = new RadioButton("snapshotNameGroup", result[i].name);
                    radioList.add( existing );
                    vert.add( existing );    
                }
                HorizontalPanel newSnap = new HorizontalPanel();
                
                final RadioButton newSnapRadio = new RadioButton("snapshotNameGroup", newSnapshotText);                
                newSnap.add( newSnapRadio );
                newName.setEnabled( false );
                newSnapRadio.addClickListener( new ClickListener() {

                    public void onClick(Widget w) {
                        newName.setEnabled( true );
                    }
                    
                });
                
                newSnap.add( newName );
                radioList.add( newSnapRadio );
                vert.add( newSnap );
                
                LoadingPopup.close();
            }
        });
        
        final TextBox comment = new TextBox();
        form.addAttribute( "Comment:", comment );
        
        Button create = new Button("Create new snapshot");
        form.addAttribute( "", create );
        
        create.addClickListener( new ClickListener() {
            String name = "";
            public void onClick(Widget w) {
                boolean replace = false;
                for ( Iterator iter = radioList.iterator(); iter.hasNext(); ) {
                    RadioButton but = (RadioButton) iter.next();
                    if (but.isChecked()) {
                        name = but.getText();
                        if (!but.getText().equals( newSnapshotText )) {
                            replace = true;
                        }
                        break;
                    }
                }
                if (name.equals( newSnapshotText )) {
                    name = newName.getText();
                }
                
                if (name.equals( "" )) {
                    Window.alert( "You have to enter or chose a label (name) for the snapshot." );
                    return;
                }
                
                
                RepositoryServiceFactory.getService().createPackageSnapshot( conf.name, name, replace, comment.getText(), new GenericCallback() {
                    public void onSuccess(Object data) {
                        Window.alert( "The snapshot called: " + name + " was successfully created." );
                        form.hide();
                    }
                });
            }
        } );
        
        form.setWidth( "50%" );
        form.setPopupPosition( Window.getClientWidth() / 3, Window.getClientHeight() / 3 );
        form.show();
        
        
    }    
    
    
}
