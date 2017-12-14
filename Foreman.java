//////////////////////////////////////////////////////////////////////
//  File:     Foreman.java                                          //
//                                                                  //
//  Author:   Abe Pralle                                            //
//  Created:  9.3.99                                                //
//  Modified: 9.3.99                                                //
//                                                                  //
//  Purpose:  Foreman is the top of the pyramid of classes which    //
//            control the Foreman game as a whole.                  //
//////////////////////////////////////////////////////////////////////

class Foreman implements Runnable
{
  //Note the "implements Runnable" simply means that we guarantee
  //to supply a run() method so that this class can be started as
  //a separate thread.

  //We'll store a reference here to the class that was our parent
  //thread, GameKit, so we can talk back and forth with it.
  private GameKit gk;

  ///////////////////////////
  // Constructor:  Foreman //
  ///////////////////////////
  public Foreman(GameKit init_gk)
  {
    gk = init_gk;
  }

  ////////////////////////////////////////////////////////////
  // Method:       run                                      //
  // Description:  Is called when this class as a thread is //
  //               started up                               //
  ////////////////////////////////////////////////////////////
  public void run()
  {
    gk.debugMesg("Foreman thread running!");

    //Load machine graphics
    gk.showStatus("Getting machine images");
    gk.loadBMPGZ("machines.bmp.gz");
    int i, j, numTiles=0;
    for(j=0; j<10; j++)
    {
      for(i=0; i<8; i++)
      {
        gk.grabTile(j*8+i + numTiles, i*64, j*32, 64, 32);
      }
    }
    numTiles += 80;

    //Load terrain graphics
    gk.showStatus("Getting terrain images");
    gk.loadBMPGZ("isotiles.bmp.gz");
    for(j=0; j<9; j++)
    {
      for(i=0; i<4; i++)
      {
        gk.grabTile(j*4+i + numTiles, i*64, j*48, 64, 48);
      }
    }
    numTiles += 36;

    //Load wireframe terrain overlays
    gk.showStatus("Getting wireframe images");
    gk.loadBMPGZ("wireframe.bmp.gz");
    for(j=0; j<4; j++)
    {
      for(i=0; i<4; i++)
      {
        gk.grabTile(j*4+i + numTiles, i*64, j*48, 64, 48);
      }
    }
    numTiles += 16;

    Map map = new Map(gk);
    map.randomize();

    map.setFullRedraw();
    // begin infinite loop (this thread won't stop until
    // the applet stops it)
    for(;;)
    {
      gk.checkEvents();
      map.check();
      map.redraw();
      gk.updateScreen();
    }
  }
}

