package image;

import java.util.*;
import java.awt.*;
import java.net.URL;

public class Images{
  private final static HashMap images=new HashMap();
  private final static String[][] strings={{"S_BAUER"   ,"Schwarz_Bauer.gif"},
                                           {"S_PFERD"   ,"Schwarz_Pferd.gif"},
                                           {"S_L�UFER"  ,"Schwarz_Laeufer.gif"},
                                           {"S_TURM"    ,"Schwarz_Turm.gif"},
                                           {"S_DAME"    ,"Schwarz_Dame.gif"},
                                           {"S_K�NIG"   ,"Schwarz_Koenig.gif"},
                                           {"W_BAUER"   ,"Weiss_Bauer.gif"},
                                           {"W_PFERD"   ,"Weiss_Pferd.gif"},
                                           {"W_L�UFER"  ,"Weiss_Laeufer.gif"},
                                           {"W_TURM"    ,"Weiss_Turm.gif"},
                                           {"W_DAME"    ,"Weiss_Dame.gif"},
                                           {"W_K�NIG"   ,"Weiss_Koenig.gif"},
                                           {"W_S_K�NIG" ,"Weiss_Schwarz_Koenig.gif"},
                                           {"ICON"      ,"Icon.gif"}};


  public static void init( Component component ){
    Toolkit toolkit=component.getToolkit();
    MediaTracker mt=new MediaTracker( component );
    for( int i=0;i<strings.length;i++ ) {
      URL url=Images.class.getResource( strings[i][1] );
      Image img=toolkit.createImage( url );
      images.put( strings[i][0],img );
      mt.addImage( img,i );
    }
    try{
      mt.waitForAll();
    }catch( InterruptedException e ) {}
  }

  public static Image get( String imageName ) {
    return (Image)images.get( imageName );
  }
}
