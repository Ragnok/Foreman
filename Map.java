//////////////////////////////////////////////////////////////////////
//  File:     Map.java                                              //
//                                                                  //
//  Author:   Abe Pralle                                            //
//  Created:  9.13.99                                               //
//  Modified: 9.16.99                                               //
//                                                                  //
//  Purpose:  Map contains and manipulates the terrain map          //
//////////////////////////////////////////////////////////////////////

import java.util.Random;

class Map
{
  //tiled width & height
  final int WIDTH = 21, HEIGHT = 21;

  //width and height of elevation data points
  final int ELEV_WIDTH = WIDTH+1, ELEV_HEIGHT = HEIGHT+1;

  //size (in tiles) of viewport (visible subset of the larger map)
  final int VP_WIDTH = 10, VP_HEIGHT = 10;

  private GameKit gk;

  private Random randomGen = new Random();
  private int elevation[][] = new int[ELEV_WIDTH][ELEV_HEIGHT];
  private Terrain terrain[][] = new Terrain[WIDTH][HEIGHT];
  private JobQ jobQ = new JobQ();

  //redraw the whole map or just reuse the background from last time?
  private boolean needsFullRedraw;

  //offset should range from (0,0) (top left of map is displayed)
  //to (WIDTH - VP_WIDTH, HEIGHT - VP_HEIGHT)
  //(e.g. 11, 11, bottom right)
  private int offset_x, offset_y;

  //Information about the currently highlighted terrain tile
  private int selTileNum, selTile_x, selTile_y, selTileStage;
  private int selTile_i, selTile_j, selTile_height;

  ///////////////////////////
  // Constructor:  Map     //
  ///////////////////////////
  public Map(GameKit gkInit)
  {
    gk = gkInit;
    offset_x = 0;
    offset_y = 0;
    Terrain.setGK(gk);
    Terrain.setMap(this);
    Machine.setGK(gk);
    ObjList.setGK(gk);
    needsFullRedraw = true;
  }

  /////////////////////////////////////////////////////////////
  // Method:       setFullRedraw                             //
  // Description:  informs the map that it needs to be fully //
  //               redrawn as opposed to copying the stored  //
  //               BG and redrawing just the vehicles        //
  /////////////////////////////////////////////////////////////
  public void setFullRedraw()
  {
    needsFullRedraw = true;
  }

  ////////////////////////////////////////////////////////////
  // Method:       getTerrain                               //
  // Arguments:    i, j:  index of location to get terrain  //
  //                      at.                               //
  // Returns:      terrain object at (i,j) or null          //
  ////////////////////////////////////////////////////////////
  public Terrain getTerrain(int i, int j)
  {
    if(i<0 || i>=WIDTH || j<0 || j>=HEIGHT) return null;

    return terrain[i][j];
  }

  ////////////////////////////////////////////////////////////
  // Method:       setTerrain                               //
  // Arguments:    i, j:  index of location to set terrain  //
  //                      at.                               //
  //               newTerrain:  Terrain to put at location  //
  ////////////////////////////////////////////////////////////
  public void setTerrain(int i, int j, Terrain newTerrain)
  {
    if(i<0 || i>=WIDTH || j<0 || j>=HEIGHT) return;

    terrain[i][j] = newTerrain;
  }

  ////////////////////////////////////////////////////////////
  // Method:       getElevation                             //
  // Arguments:    x, y:  index of location to get elev     //
  //                      from.                             //
  // Returns:      elevation at (x,y) or zero if (x,y) is   //
  //               out of bounds                            //
  ////////////////////////////////////////////////////////////
  public int  getElevation(int x, int y)
  {
    if(x<0 || x>=ELEV_WIDTH || y<0 || y>=ELEV_HEIGHT) return 0;
    return elevation[x][y];
  }

  ////////////////////////////////////////////////////////////
  // Method:       setElevation                             //
  // Arguments:    x, y:  index of location to set elev.    //
  //                      Coords outside map have no effect //
  //               _elevation:  new elevation (zero based)  //
  ////////////////////////////////////////////////////////////
  public void setElevation(int x, int y, int _elevation)
  {
    if(x<0 || x>=ELEV_WIDTH || y<0 || y>=ELEV_HEIGHT) return;
    elevation[x][y] = _elevation;
  }

  ////////////////////////////////////////////////////////////
  // Method:       getJobQ                                  //
  ////////////////////////////////////////////////////////////
  public JobQ getJobQ()
  {
    return jobQ;
  }

  ////////////////////////////////////////////////////////////
  // Method:       randomize                                //
  // Description:  Fills in the elevation map with random   //
  //               heights and then smooths it so a given   //
  //               point is no more than 1 unit higher or   //
  //               lower than any of its neighbors.         //
  ////////////////////////////////////////////////////////////
  public void randomize()
  {
    //Create random elevations
    int i, j;
    for(j=0; j<ELEV_HEIGHT; j++)
    {
      for(i=0; i<ELEV_WIDTH; i++)
      {
        int r = randomGen.nextInt();
        if(r<0) r = -r;     //restrict to 0 or positive
        r %= 5;             //restrict to be 0-4 (20% chance each #)
        if(r>0)
        {
          if(r>=3) r = 2;   //40% chance of hill
          else     r = 1;   //20% chance of depression
        }

        elevation[i][j] = r;
      }
    }

    //make top-left corner and bottom-right corner have
    //elevation 1
    elevation[0][0] = 1;
    elevation[0][1] = 1;
    elevation[1][0] = 1;
    elevation[1][1] = 1;

    elevation[ELEV_WIDTH-1][ELEV_HEIGHT-1] = 1;
    elevation[ELEV_WIDTH-2][ELEV_HEIGHT-1] = 1;
    elevation[ELEV_WIDTH-1][ELEV_HEIGHT-2] = 1;
    elevation[ELEV_WIDTH-2][ELEV_HEIGHT-2] = 1;

    //Smooth map
    int pass, x, y;
    for(pass=0; pass<3; pass++)
    {
      boolean madeChanges = true;
      while(madeChanges)
      {
        madeChanges = false;
        for(j=0; j<ELEV_HEIGHT; j++)
        {    //for each spot on map
          for(i=0; i<ELEV_WIDTH; i++)
          {
            int myHeight = getElevation(i,j);
            if(myHeight != pass) continue;
            for(y=-1; y<=1; y++)
            {        //make sure neighbors rise
              for(x=-1; x<=1; x++)
              {      //no more than 1 higher
                if(x==0 && y==0) continue;
                if(getElevation(i+x, y+j) - myHeight > 1)
                {
                  setElevation(i+x, y+j, myHeight + 1);
                  madeChanges = true;
                }
              }
            }
          }
        }
      }
    }

    for(j=0; j<HEIGHT; j++)
    {
      for(i=0; i<WIDTH; i++)
      {
        //if(randomGen.nextInt() > 0){
        terrain[i][j] = Terrain.create("Grass",i,j);
        //}else{
        //terrain[i][j] = Terrain.create("Dirt",i,j);
        //}
      }

    }
    terrain[0][0] = Terrain.create("Depot",0,0);
    terrain[WIDTH-1][HEIGHT-1] = Terrain.create("Road",
                                 WIDTH-1, HEIGHT-1);

    terrain[1][0].addMachine(Machine.create("Hoe",terrain[1][0]));
    terrain[2][0].addMachine(Machine.create("Dozer",terrain[2][0]));
    terrain[3][0].addMachine(Machine.create("Roller",terrain[3][0]));
    terrain[4][0].addMachine(Machine.create("Grader",terrain[4][0]));
    terrain[5][0].addMachine(Machine.create("Truck",terrain[5][0]));
    terrain[6][0].addMachine(Machine.create("Truck",terrain[6][0]));
    terrain[7][0].addMachine(Machine.create("Truck",terrain[7][0]));
    terrain[8][0].addMachine(Machine.create("Truck",terrain[8][0]));
  }

  ////////////////////////////////////////////////////////////
  // Method:       check                                    //
  // Description:  Causes map to check inputs and process   //
  //               internal events.                         //
  ////////////////////////////////////////////////////////////
  public void check()
  {
    if(gk.getMouseClickB1() && selTileStage>0)
    {
      //check to see if all 4 corners are elevation 1
      int height = 0, i=selTile_i, j=selTile_j;
      int e1 = getElevation(i,   j);
      int e2 = getElevation(i+1, j);
      int e3 = getElevation(i+1, j+1);
      int e4 = getElevation(i,   j+1);
      if(e1==1 && e2==1 && e3==1 && e4==1)
      {
        height = 0;
      }
      else if(e1==0 || e2==0 || e3==0 || e4==0)
      {
        height = -1;
      }
      else
      {
        height = 1;
      }
      if(height < 0)
      {
        jobQ.addJob(JobQ.JOB_FILL, i, j, -1);
      }
      else if(height > 0)
      {
        jobQ.addJob(JobQ.JOB_CUT, i, j, -1);
      }
      else
      {
        //ground is flat at elevation 1
        if(terrain[i][j].getType() == "Grass")
        {
          jobQ.addJob(JobQ.JOB_CLEAR, i, j, -1);
        }
        else if(terrain[i][j].getType() == "Dirt")
        {
          jobQ.addJob(JobQ.JOB_PAVE, i, j, -1);
        }
      }
    }

    if(gk.getInkey()==38 && offset_y > 0)
    {
      offset_y--;
      needsFullRedraw = true;
    }
    else if(gk.getInkey()==39 && offset_x + VP_WIDTH < WIDTH)
    {
      offset_x++;
      needsFullRedraw = true;
    }
    else if(gk.getInkey()==40 && offset_y + VP_HEIGHT < HEIGHT)
    {
      offset_y++;
      needsFullRedraw = true;
    }
    else if(gk.getInkey()==37 && offset_x > 0)
    {
      offset_x--;
      needsFullRedraw = true;
    }

    //call each of the terrain obj's check methods
    int i, j;
    for(j=0; j<HEIGHT; j++)
    {
      for(i=0; i<WIDTH; i++)
      {
        terrain[i][j].check();
      }
    }
  }

  ////////////////////////////////////////////////////////////
  // Method:       drawTerrainTile                          //
  // Arguments:    num - number of tile to draw             //
  //               x, y - position to draw top left corner  //
  //               i, j - index of tile in map              //
  //               height - lowest elevation (0-2) of any   //
  //                        corner of tile                  //
  // Description:  draws tiles and remembers which one was  //
  //               under the mouse cursor                   //
  ////////////////////////////////////////////////////////////
  public void drawTerrainTile(int num, int x, int y,
                              int i, int j, int height)
  {
    //draw the actual tile if need be, otherwise skip that and
    //just check the drawing coordinates in relation to the mouse
    if(needsFullRedraw) gk.drawTile(num, x, y);

    if(selTileStage==2) return;   //already found best match

    int mx = gk.getMouseX();
    int my = gk.getMouseY();

    //return if not even a rough match
    if(mx < x || my < y+16 || mx >= x+64 || my >= y+48) return;

    //do we have a "precise" match?
    if((mx >= x + 16) && (my >= y + 24)
        && (mx < x + 48) && (my < y + 40))
    {
      selTileStage = 2;
    }
    else
    {
      //we must just have a rough match
      if(selTileStage==1) return;
      selTileStage = 1;
    }
    selTileNum = num - terrain[i][j].getTileNum();
    selTile_x = x;
    selTile_y = y;
    selTile_i = i;
    selTile_j = j;
    selTile_height = height;
  }


  ////////////////////////////////////////////////////////////
  // Method:       redraw                                   //
  // Description:  redraws a 10x10 tile portion of the map  //
  ////////////////////////////////////////////////////////////
  public void redraw()
  {
    if(needsFullRedraw)
    {
      gk.cls(0,0,128);
      drawTerrain();
      gk.copyBackground();
      needsFullRedraw = false;
    }
    else
    {
      gk.restoreBackground();
      drawTerrain();
    }
    highlightTerrain();
    drawMachines();
  }

  ////////////////////////////////////////////////////////////
  // Method:       drawTerrain                              //
  // Description:  draws a 10x10 tile portion of land only  //
  ////////////////////////////////////////////////////////////
  public void drawTerrain()
  {
    int dx, dy, dxStart, dyStart;
    int i, j;

    selTileStage = 0;

    dxStart = 288;
    dyStart = 144;
    for(j=0; j<VP_HEIGHT; j++)
    {
      dx = dxStart;
      dy = dyStart;
      for(i=0; i<VP_WIDTH; i++)
      {
        int tileNum = terrain[i+offset_x][j+offset_y].getTileNum();

        //find minimum height of the 4 corners
        int k = i + offset_x;
        int l = j + offset_y;
        int myHeight = getElevation(k, l);
        if(getElevation(k+1, l) < myHeight)   myHeight--;
        if(getElevation(k+1, l+1) < myHeight) myHeight--;
        if(getElevation(k, l+1) < myHeight)   myHeight--;

        //adjust tile number for different corner heights
        if(getElevation(k, l) > myHeight)     tileNum += 1;
        if(getElevation(k+1, l) > myHeight)   tileNum += 2;
        if(getElevation(k+1, l+1) > myHeight) tileNum += 4;
        if(getElevation(k, l+1) > myHeight)   tileNum += 8;

        //adjust y coordinate to account for minimum elevation
        int yAdj = dy - (myHeight * 16);
        drawTerrainTile(tileNum, dx, yAdj, k, l, myHeight);

        dx += 32;
        dy += 16;
      }
      dxStart -= 32;
      dyStart += 16;
    }

  }

  ////////////////////////////////////////////////////////////
  // Method:       drawMachines                             //
  // Description:  draws a 10x10 tile portion of land only  //
  ////////////////////////////////////////////////////////////
  public void drawMachines()
  {
    int dx, dy, dxStart, dyStart;
    int i, j;

    dxStart = 288;
    dyStart = 144;
    for(j=0; j<VP_HEIGHT; j++)
    {
      dx = dxStart;
      dy = dyStart;
      for(i=0; i<VP_WIDTH; i++)
      {
        //find minimum height of the 4 corners
        int k = i + offset_x;
        int l = j + offset_y;
        int myHeight = getElevation(k, l);
        if(getElevation(k+1, l) < myHeight)   myHeight--;
        if(getElevation(k+1, l+1) < myHeight) myHeight--;
        if(getElevation(k, l+1) < myHeight)   myHeight--;

        //adjust y coordinate to account for minimum elevation
        int yAdj = dy - (myHeight * 16);
        if(getElevation(k+1, l+1) > myHeight)      yAdj-=16;
        else if(getElevation(k, l) > myHeight)     yAdj-=8;
        //else if(getElevation(k+1, l) > myHeight)   yAdj-=4;
        //else if(getElevation(k, l+1) > myHeight)   yAdj-=4;

        terrain[i+offset_x][j+offset_y].draw(dx, yAdj);

        dx += 32;
        dy += 16;
      }
      dxStart -= 32;
      dyStart += 16;
    }
  }


  ////////////////////////////////////////////////////////////
  // Method:       highlightTerrain                         //
  // Description:  draws wireframe highlight on the terrain //
  //               under the mouse cursor                   //
  ////////////////////////////////////////////////////////////
  public void highlightTerrain()
  {
    if(selTileStage > 0)
    {
      Machine m = terrain[selTile_i][selTile_j].findMachine("Any",
                  JobQ.JOB_ANY);
      String mDesc = " ";
      if(m != null) mDesc += m.getJob().getDescription();
      gk.drawTile(116 + selTileNum, selTile_x, selTile_y);
      gk.showStatus("(" + selTile_i + "," + selTile_j + ") height: "
                    + selTile_height + mDesc);
    }
  }
}

