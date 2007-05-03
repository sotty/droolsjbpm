package org.drools.brms.client;

import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.Widget;

/**
 * Introduction page.
 */
public class Info extends JBRMSFeature {

    public static ComponentInfo init() {
        return new ComponentInfo( "Info",
                                  "JBoss Rules Managment Console." ) {
            public JBRMSFeature createInstance() {
                return new Info();
            }

        };
    }

    public Info() {
        initWidget( getLayout() );
    }

    private Widget getLayout() {
        
        Frame f = new Frame("welcome.html");
        
//        FlexTable layout = new FlexTable();
//        Image logo = new Image( "images/logo.png" );
//        layout.setWidget( 0, 0, logo);
//        FlexCellFormatter formatter = layout.getFlexCellFormatter();
//        formatter.setColSpan( 0, 0, 1 );
//        
//        formatter.setHorizontalAlignment( 0, 0, HasHorizontalAlignment.ALIGN_CENTER );
//        
//        layout.setWidth( "100%" );
//        layout.setHeight( "100%" );
//        
//        HTML html = new HTML("<i>Product web site</i>");
//        
//        html.addClickListener( new ClickListener() {
//
//            public void onClick(Widget w) {
//                Window.open( "http://www.jboss.com/products/rules", "JBoss Rules", "" );                
//            }
//            
//        });
//        
//        HTML html2 = new HTML("<i>Community web site</i>");
//        html2.addClickListener( new ClickListener() {
//            public void onClick(Widget w) {
//                Window.open( "http://labs.jboss.com/portal/jbossrules", "JBoss Rules Community", "" );                
//
//            }
//        } );
//        layout.setWidget( 1, 0, html);
//        layout.setWidget( 1, 1, html2 );
//        
//        formatter.setHorizontalAlignment( 1, 0, HasHorizontalAlignment.ALIGN_LEFT );
//        formatter.setHorizontalAlignment( 1, 1, HasHorizontalAlignment.ALIGN_RIGHT );
        
        
//        layout.setStyleName( "welcome-Page" );
        f.setStyleName("welcome-Page");
        return f;
    }

    public void onShow() {
    }
}
