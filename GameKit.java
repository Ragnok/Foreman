//////////////////////////////////////////////////////////////////////
//  File:     GameKit.java                                          //
//                                                                  //
//  Author:   Abe Pralle                                            //
//  Created:  1.22.99                                               //
//  Modified: 9.9.99                                                //
//                                                                  //
//  Purpose:  GameKit is an applet designed to start up another     //
//            class (e.g. Foreman) as a thread and supply all of    //
//            that second class's graphics and I/O requirements     //
//////////////////////////////////////////////////////////////////////
import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.net.*;
import java.io.*;
import java.util.zip.*;

public class GameKit extends Applet
{
  /////////////////
  // definitions //
  /////////////////
  final int MAX_TILES = 150;          //Maximum number of tile images
  final int HRES = 640, VRES = 480;  //screen resolution

  ///////////////////////
  // private variables //
  ///////////////////////
  private Image    logic_image;     //backbuffer image
  private Graphics logic_graphics;  //graphics interface to bkbuffer
  private Image    copy_image;
  private Graphics copy_graphics;


  //pixels from bmp file will be stored here in 24-bit format
  private int bmData[];
  private int bmPalette[] = new int[256];  //palette for 8-bit bmps
  private int bmWidth, bmHeight;

  //gfx to draw map with.  These will still need to be initialized
  //individually
  private Image tile[] = new Image[MAX_TILES];

  //keycodes of current/next key presses
  private int inkey = 0, nextkey = 0;

  //mouse stuff
  int mouseX, mouseY, nextMouseX, nextMouseY;
  boolean mouseB1, mouseB3, nextMouseB1, nextMouseB3;
  boolean mouseClickB1, mouseClickB3;

  // Foreman is a Runnable class that contains the actual game logic
  // We'll send a reference to ourself (GameKit) as we construct the
  // Foreman object, then start up the Foreman object as a separate
  // thread which will then run semi-independently of the GameKit
  // object.
  private Foreman foreman = new Foreman(this);
  private Thread thread;

  // The log window is a place to send debugging messages to
  private Frame logWindow = new Frame("Log Window");
  private List  logList   = new List();

  ///////////////////////////////////////////////////////////////
  // Method:       debugMesg                                   //
  // Arguments:    mesg - string to add to log window          //
  // Description:  Adds the specified message to the log       //
  //               window.  Useful for debugging.              //
  ///////////////////////////////////////////////////////////////
  void debugMesg(String s)
  {
    logList.add(s);
    logList.makeVisible(logList.getItemCount()-1);
    showStatus(s);
  }

  ///////////////////////////////////////////////////////////////
  // Method:       init                                        //
  // Description:  called by applet framework once to set up   //
  //               initial values                              //
  ///////////////////////////////////////////////////////////////
  public void init()
  {
    logWindow.add(logList);            //add list to window
    logWindow.setVisible(true);        //show window
    Point p = getLocation();           //get browser's location
    logWindow.setBounds(0, p.y+480, 512, 128);  //set log
    //window's loc.
    //create the back-buffer to draw on + its graphics interface
    logic_image = createImage(HRES, VRES);
    logic_graphics = logic_image.getGraphics();
    copy_image = createImage(HRES, VRES);
    copy_graphics = copy_image.getGraphics();
    prepareImage(logic_image, this);
    prepareImage(copy_image, this);

    enableEvents(java.awt.AWTEvent.KEY_EVENT_MASK
                 | java.awt.AWTEvent.MOUSE_EVENT_MASK
                 | java.awt.AWTEvent.MOUSE_MOTION_EVENT_MASK);
  }

  ////////////////////////////////////////////////////////////////////
  // Method:       start                                            //
  // Description:  called each time the applet restarts (eg the     //
  //               browser moved to a new web page and then came    //
  //               back to our applet                               //
  ////////////////////////////////////////////////////////////////////
  public void start()
  {
    logWindow.setVisible(true);
    thread = new Thread(foreman);
    thread.start();
  }

  ////////////////////////////////////////////////////////////////////
  // Method:       stop                                             //
  // Description:  This method is called by the applet framework to //
  //               inform us that we should stop whatever we'er     //
  //               doing (ie other threads) so that the applet can  //
  //               suspended or destroyed                           //
  ////////////////////////////////////////////////////////////////////
  public void stop()
  {
    logWindow.setVisible(false);
    thread.stop();
  }

  ////////////////////////////////////////////////////////////////////
  // Method:       update                                           //
  // Description:  this override's the default "update" to just     //
  //               paint the screen instead of clearing the screen  //
  //               AND painting it (to avoid flicker)               //
  ////////////////////////////////////////////////////////////////////
  public void update(Graphics g)
  {
    paint(g);
  }

  ////////////////////////////////////////////////////////////////////
  // Method:       paint                                            //
  // Arguments:    Graphics g - graphics info about drawing         //
  //                            destination                         //
  // Description:  Overrides the applet framework's paint method    //
  //               simply copies the logial buffer to the physical  //
  //               display.                                         //
  ////////////////////////////////////////////////////////////////////
  public void paint(Graphics g)
  {
    g.drawImage(logic_image,0,0,this);
  }

  ////////////////////////////////////////////////////////////////////
  // Method:       updateScreen                                     //
  // Description:  Requests that the applet call the paint method   //
  //               as soon as possible - in effect redraws the      //
  //               screen                                           //
  ////////////////////////////////////////////////////////////////////
  public void updateScreen()
  {
    repaint();
  }

  ////////////////////////////////////////////////////////////////////
  // Method:       checkEvents                                      //
  // Description:  Updates information about the current key press, //
  //               etc.  Information from methods such as "getInkey"//
  //               will not change except after a call to           //
  //               "checkEvents."                                   //
  ////////////////////////////////////////////////////////////////////
  public void checkEvents()
  {
    //put the thread to sleep (suspend it) for 1/10th second to
    //avoid dragging the system down
    try
    {
      thread.sleep(100);
    }
    catch(InterruptedException e)
    {
      //Don't care about this exception
    }


    inkey = nextkey;
    nextkey = 0;

    mouseX = nextMouseX;
    mouseY = nextMouseY;
    if(mouseB1 && !nextMouseB1) mouseClickB1 = true;
    else                        mouseClickB1 = false;
    if(mouseB3 && !nextMouseB3) mouseClickB3 = true;
    else                        mouseClickB3 = false;
    mouseB1 = nextMouseB1;
    mouseB3 = nextMouseB3;
  }

  ////////////////////////////////////////////////////////////////////
  // Methods:      processKeyEvent                                  //
  //               processMouseEvent                                //
  //               processMouseMotion                               //
  // Arguments:    Event e - description of event                   //
  // Description:  Java calls one of these methods whenever the     //
  //               input state of the mouse or keyboard changes     //
  ////////////////////////////////////////////////////////////////////
  protected void processKeyEvent(KeyEvent e)
  {
    if(e.getID() != e.KEY_PRESSED) return;  //skip release events

    if((nextkey = e.getKeyChar()) == 0)
    {
      nextkey = e.getKeyCode();
    }
  }

  protected void processMouseEvent(MouseEvent e)
  {
    int  m = e.getModifiers();
    switch(e.getID())
    {
    case e.MOUSE_PRESSED:
      if((m & e.BUTTON1_MASK)>0) nextMouseB1 = true;
      if((m & e.BUTTON3_MASK)>0) nextMouseB3 = true;
      break;
    case e.MOUSE_RELEASED:
      if((m & e.BUTTON1_MASK)>0) nextMouseB1 = false;
      if((m & e.BUTTON3_MASK)>0) nextMouseB3 = false;
      break;
    }
  }

  protected void processMouseMotionEvent(MouseEvent e)
  {
    nextMouseX = e.getX();
    nextMouseY = e.getY();
  }

  ////////////////////////////////////////////////////////////////////
  // Method:       getInkey                                         //
  // Returns:      keycode of last key press; usually ASCII         //
  ////////////////////////////////////////////////////////////////////
  public int getInkey()
  {
    return inkey;
  }

  ////////////////////////////////////////////////////////////////////
  // Methods:      getMouseX  - horiz pixel position of mouse       //
  //               getMouseY  - vertical pixel pos of mouse         //
  //               getMouseB1 - status of left mouse button         //
  //               getMouseB3 - status of right mouse button        //
  //               getMouseClickB1 - LMB clicked?                   //
  //               getMouseClickB3 - RMB clicked?                   //
  ////////////////////////////////////////////////////////////////////
  public int     getMouseX()
  {
    return mouseX;
  }
  public int     getMouseY()
  {
    return mouseY;
  }
  public boolean getMouseB1()
  {
    return mouseB1;
  }
  public boolean getMouseB3()
  {
    return mouseB3;
  }
  public boolean getMouseClickB1()
  {
    return mouseClickB1;
  }
  public boolean getMouseClickB3()
  {
    return mouseClickB3;
  }

  ////////////////////////////////////////////////////////////////////
  // Method:       cls                                              //
  // Arguments:    int r, int g, int b                              //
  // Description:  Clears the whole background to the color         //
  //               specified by the R,G,B color triplet             //
  ////////////////////////////////////////////////////////////////////
  public void cls(int r, int g, int b)
  {
    logic_graphics.setColor(new Color(r,g,b));
    logic_graphics.fillRect(0,0,HRES,VRES);
  }

  ////////////////////////////////////////////////////////////////////
  // Method:       setFont                                          //
  // Arguments:    Font font                                        //
  // Description:  Sets the current font to be used with drawString //
  ////////////////////////////////////////////////////////////////////
  public void setFont(Font font)
  {
    logic_graphics.setFont(font);
  }

  ////////////////////////////////////////////////////////////////////
  // Method:       drawString                                       //
  // Arguments:    String s, int x, int y, int r, int g, int b      //
  // Description:  Draws string "s" at location (x,y) in color      //
  //               (r,g,b)                                          //
  // Note:         If drawString is the last command before         //
  //               "updateScreen" it probably won't happen          //
  //               (something about separate threads I think)       //
  //               so it's best to have at least one "drawTile"     //
  //               after all the "drawStrings"                      //
  ////////////////////////////////////////////////////////////////////
  public void drawString(String s, int x, int y, int r, int g, int b)
  {
    logic_graphics.setColor(new Color(r,g,b));
    logic_graphics.drawString(s,x,y);
  }

  ////////////////////////////////////////////////////////////////////
  // Method:       clearRect                                        //
  // Arguments:    x, y, width, height - coordinates of rectangle   //
  // Description:  Draws a solid black rectangle at the specified   //
  //               coordinates.                                     //
  ////////////////////////////////////////////////////////////////////
  public void clearRect(int x, int y, int width, int height)
  {
    logic_graphics.setColor(new Color(0,0,0));
    logic_graphics.fillRect(x,y,width,height);
  }

  ////////////////////////////////////////////////////////////////////
  // Methods:      readWord                                         //
  //               readLong                                         //
  // Arguments:    in - input stream to read from                   //
  // Returns:      The next 16 or 32-bit value from the stream      //
  // Description:  Reads in 2 or 4 bytes from the input stream and  //
  //               returns their concatenated value.                //
  // Note:         Assumes input is in lo-byte/hi-byte format       //
  //               Primarily for use with loadBMPGZ()               //
  ////////////////////////////////////////////////////////////////////
  public int  readWord(InputStream in) throws IOException
  {
    int returnVal;
    returnVal = in.read();
    returnVal |= in.read() << 8;
    return returnVal;
  }

  public int  readLong(InputStream in) throws IOException
  {
    int returnVal;
    returnVal  = readWord(in);
    returnVal |= readWord(in) << 16;
    return returnVal;
  }

  ////////////////////////////////////////////////////////////////////
  // Method:       loadBMPGZ                                        //
  // Arguments:    String filename - name of .bmp.gz file to load   //
  // Description:  Loads the specified bitmap into the back-buffer  //
  ////////////////////////////////////////////////////////////////////
  public boolean loadBMPGZ(String filename)
  {

    //////////////////////////////////////////////////////
    //Attempt to open the filename as a DataInputStream //
    //////////////////////////////////////////////////////
    URL myURL;
    GZIPInputStream gzipInput;

    try
    {
      debugMesg("Opening " + filename + " for reading");
      myURL = new URL(getDocumentBase(), filename);
      gzipInput = new GZIPInputStream(myURL.openStream());
    }
    catch(Exception e)
    {
      debugMesg("Error opening file: " + e.toString());
      return false;
    }

    ////////////////////////////////
    // Read in 54-byte bmp header //
    ////////////////////////////////
    try
    {
      boolean badFile = false;
      if(gzipInput.read() != 'B') badFile = true;   //bytes 0,1 ID B,M
      else if(gzipInput.read() != 'M') badFile = true;
      if(badFile)
      {
        debugMesg("Invalid BMP file: " + filename);
      }

      //Read in extraneous stuff without saving values in order to get
      //to the good stuff.
      readLong(gzipInput);   //total file size
      readLong(gzipInput);
      int headerPlusPaletteSize = readLong(gzipInput);   //header + palette size
      int paletteEntries = (headerPlusPaletteSize - 54) / 4;
      readLong(gzipInput);
      bmWidth  = readLong(gzipInput);  //store w&h in instance variable
      bmHeight = readLong(gzipInput);
      readWord(gzipInput);
      int bpp = readWord(gzipInput);    //bits per pixel
      int i, j, pos;
      for(i=0; i<6; i++) readLong(gzipInput);  //skip remaining 24 bytes

      ////////////////////////
      // Read in the bitmap //
      ////////////////////////
      //place to store final image
      bmData = new int[bmWidth * bmHeight];

      //Note that I am using a single dimensional array to represent
      //two dimensional information.  This is really the way the
      //computer stores it anyways and 1D arrays end up being more
      //flexible than their 2D counterparts.
      //
      //2D:    buffer[x][y] = 5;
      //
      //1D:    buffer[(y * array_width) + x] = 5;


      if(bpp==8)
      {
        ///////////////
        // 8-bit bmp //
        ///////////////
        //8 bits per pixel, next read in color palette as series of
        //longs (4-byte b, g, r, 0 values)
        for(i=0; i<paletteEntries; i++)
        {
          bmPalette[i] = readLong(gzipInput);
        }

        //now read in (width*height) number of bytes, using each byte
        //as an index into the color table to get the actual color
        //BMPs are stored from left-to-right (normal) and bottom-to-top
        //(unusual) so we must skip around in our destination array
        //to read in it correctly.
        for(j=bmHeight-1; j>=0; j--)
        {
          pos = (j * bmWidth);  //start at left side, next row up
          for(i=0; i<bmWidth; i++)
          {
            int color = bmPalette[gzipInput.read()];
            if(color != 0xff)
            {        //blue stays transparent
              color |= 0xff000000;    //set alpha to opaque (ff)
            }

            bmData[pos++] = color;  //note that values are aarrggbb
            //a=alpha, r=red, g=green, b=blue
          }

        }
      }
      else if(bpp==24)
      {
        ////////////////
        // 24-bit bmp //
        ////////////////
        //For 24 bit pictures there is no indexed color palette.  We
        //just load in raw r, g, b byte-triplets for each pixel.
        for(j=bmHeight-1; j>=0; j--)
        {
          pos = (j * bmWidth);  //start at left side, next row up
          for(i=0; i<bmWidth; i++)
          {
            int color = gzipInput.read();     //blue
            color |= gzipInput.read() <<  8;  //green
            color |= gzipInput.read() << 16;  //red
            color |= 0xff000000;              //alpha; make opaque
            bmData[pos++] = color;
          }
        }
      }
      else
      {
        debugMesg("Error:  bitmap " + filename + " must be 8 or "
                  + "24 bpp!");
      }

      ///////////////////
      //close the file //
      ///////////////////
      gzipInput.close();


      ////////////////////////////////////////////////////////////////
      // We've read in the bitmap, now create a temporary image     //
      // from the bmData and then copy that to the logical          //
      // screen buffer                                              //
      ////////////////////////////////////////////////////////////////
      Image tempImage = createImage(
                          new MemoryImageSource(bmWidth, bmHeight, bmData, 0, bmWidth));
      prepareImage(tempImage, this);
      logic_graphics.drawImage(tempImage, 0, 0, this);

    }
    catch(Exception e)
    {
      debugMesg("Error reading bitmap: " + e.toString());
      return false;
    }

    debugMesg(filename + " successfully loaded");
    return true;
  }

  ////////////////////////////////////////////////////////////////////
  // Method:       grabTile                                         //
  // Arguments:    int n - the number of the tile to grab from the  //
  //                       back-buffer                              //
  //               int x - the left edge of the tile                //
  //               int y - the top edge of the tile                 //
  //               int w - the pixel width of the tile              //
  //               int h - the pixel height of the tile             //
  ////////////////////////////////////////////////////////////////////
  public void grabTile(int n, int x, int y, int w, int h)
  {
    int  buffer[] = new int[w*h];
    int sx, sy, i, j;
    sy = y;
    for(j=0; j<h; j++)
    {
      sx = x;
      for(i=0; i<w; i++)
      {
        buffer[j*w + i] = bmData[sy * bmWidth + sx];
        sx++;
      }
      sy++;
    }
    tile[n] = createImage(
                new MemoryImageSource(w, h, buffer, 0, w));
    prepareImage(tile[n], this);
  }

  ////////////////////////////////////////////////////////////////////
  // Method:       drawTile                                         //
  // Arguments:    int n - the tile number to draw on-screen        //
  //               int x - where to place the top edge of the tile  //
  //               int y - where to place the left edge of the tile //
  // Description:  Draw the specified tile on the back-buffer       //
  ////////////////////////////////////////////////////////////////////
  public void drawTile(int n, int x, int y)
  {
    logic_graphics.drawImage(tile[n], x, y, this);
  }

  ////////////////////////////////////////////////////////////////////
  // Methods:      copyBackground                                   //
  //               restoreBackground                                //
  // Description:  functions to copy and restore the logical buffer //
  //               to/from a backup buffer                          //
  ////////////////////////////////////////////////////////////////////
  public void copyBackground()
  {
    copy_graphics.drawImage(logic_image, 0, 0, this);
  }

  public void restoreBackground()
  {
    logic_graphics.drawImage(copy_image, 0, 0, this);
  }
}

