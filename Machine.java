//////////////////////////////////////////////////////////////////////
//  File:     Machine.java                                          //
//                                                                  //
//  Author:   Abe Pralle                                            //
//  Created:  9.27.99                                               //
//  Modified: 9.27.99                                               //
//                                                                  //
//  Purpose:  Machine class & derived classes                       //
//////////////////////////////////////////////////////////////////////

import java.util.Random;
import java.lang.Math;

abstract class Machine
{
  final int DIR_N=0, DIR_NE=1, DIR_E=2, DIR_SE=3, DIR_S=4, DIR_SW=5,
                                     DIR_W=6, DIR_NW=7;
  final int ACTION_NONE=0, ACTION_MOVE=1;
  final int MOVE_DECIDE=0, MOVE_TO_EDGE=1, MOVE_TO_CENTER=2,
                                        TURN_45_CW=3, TURN_45_CCW=4;
  static protected GameKit gk;
  static protected Random randomGen = new Random();

  protected Terrain terrain;
  protected int facing, frame;

  protected int actionType;
  protected int move_i, move_j;
  protected int moveType, movePercent;
  protected JobItem curJob;
  protected int     jobStep;

  ////////////////////////////////////////////////////////////////////
  // Constructor:  Machine                                          //
  // Description:  Sets facing to a random # 0-7                    //
  ////////////////////////////////////////////////////////////////////
  public Machine(Terrain _terrain)
  {
    terrain = _terrain;
    facing = java.lang.Math.abs(randomGen.nextInt()) % 8;
    actionType = ACTION_NONE;
    curJob = null;
  }

  ////////////////////////////////////////////////////////////////////
  // Method:       setGK                                            //
  ////////////////////////////////////////////////////////////////////
  static public void setGK(GameKit gkInit)
  {
    gk = gkInit;
  }

  ////////////////////////////////////////////////////////////////////
  // Methods:      setFacing                                        //
  //               setFrame                                         //
  //               getFacing                                        //
  //               getFrame                                         //
  //               getJob                                           //
  //               getType                                          //
  ////////////////////////////////////////////////////////////////////
  public void setFacing(int n)
  {
    facing = n;
  }
  public void setFrame(int n)
  {
    frame = n;
  }
  public int  getFacing()
  {
    return facing;
  }
  public int  getFrame()
  {
    return frame;
  }
  public JobItem getJob()
  {
    return curJob;
  }
  public String  getType()
  {
    return "";
  }

  ////////////////////////////////////////////////////////////////////
  // Method:       create                                           //
  // Arguments:    type, one of:                                    //
  //                 "Hoe"                                          //
  //                 "Dozer"                                        //
  //                 "Roller"                                       //
  //                 "Grader"                                       //
  //                 "Truck"                                        //
  // Description:  Creates and returns obj of specified type        //
  ////////////////////////////////////////////////////////////////////
  static public Machine create(String type, Terrain t)
  {
    if(type=="Hoe")
    {
      return new Hoe(t);
    }
    else if(type=="Dozer")
    {
      return new Dozer(t);
    }
    else if(type=="Roller")
    {
      return new Roller(t);
    }
    else if(type=="Grader")
    {
      return new Grader(t);
    }
    else if(type=="Truck")
    {
      return new Truck(t);
    }
    else
    {
      return null;
    }
  }

  ////////////////////////////////////////////////////////////////////
  // Method:       findJob                                          //
  // Arguments:    jobTypes - ORed set of job values this machine   //
  //               can handle                                       //
  // Description:  Finds the next appropriate job for a machine.    //
  //               Sets instance variables curJob and/or            //
  //               bestJobDistance.                                 //
  ////////////////////////////////////////////////////////////////////
  public void findJob(int jobTypes)
  {
    JobQ jobQ = terrain.getMap().getJobQ();
    curJob = jobQ.findJob(jobTypes, null);
    jobQ.removeJob(curJob);
  }

  ////////////////////////////////////////////////////////////////////
  // Method:       handleMovement                                   //
  // Description:  Called to handle movement by any machine whose   //
  //               current action type is ACTION_MOVE.              //
  ////////////////////////////////////////////////////////////////////
  public void handleMovement()
  {
    if(moveType==MOVE_DECIDE)
    {
      //where are we?
      int map_i = terrain.getI();
      int map_j = terrain.getJ();

      //if we're on the destination then we're done
      if(map_i==move_i && map_j==move_j)
      {
        actionType = ACTION_NONE;

      }
      else
      {
        movePercent = 0;

        //pick direction to go
        int desiredDir;
        if(map_i == move_i)
        {
          //src & dest equal horizontally, move vertically
          if(move_j < map_j) desiredDir = DIR_N;
          else               desiredDir = DIR_S;

        }
        else if(map_j == move_j)
        {
          //src & dest equal vertically, move horizontally
          if(move_i < map_i) desiredDir = DIR_W;
          else               desiredDir = DIR_E;

        }
        else
        {
          //we can move diagonally
          if(move_j < map_j && move_i > map_i)
          {
            desiredDir = DIR_NE;
          }
          else if(move_j > map_j && move_i > map_i)
          {
            desiredDir = DIR_SE;
          }
          else if(move_j > map_j && move_i < map_i)
          {
            desiredDir = DIR_SW;
          }
          else
          {
            desiredDir = DIR_NW;
          }
        }

        if(facing != desiredDir)
        {
          //we must turn to face appropriate direction
          int turnsCW = (desiredDir - facing) & 7;
          int turnsCCW = (facing - desiredDir) & 7;
          if(turnsCCW < turnsCW)
          {
            moveType = TURN_45_CCW;
          }
          else
          {
            moveType = TURN_45_CW;
          }

        }
        else
        {
          //begin to leave square, enter adjacent
          moveType = MOVE_TO_EDGE;
        }
      }
    }

    switch(moveType)
    {
    case MOVE_TO_EDGE:
      movePercent += 10;
      if(movePercent >= 100)
      {
        int map_i = terrain.getI();
        int map_j = terrain.getJ();
        switch(facing)
        {
        case DIR_N:
          map_j--;
          break;
        case DIR_NE:
          map_i++;
          map_j--;
          break;
        case DIR_E:
          map_i++;
          break;
        case DIR_SE:
          map_i++;
          map_j++;
          break;
        case DIR_S:
          map_j++;
          break;
        case DIR_SW:
          map_i--;
          map_j++;
          break;
        case DIR_W:
          map_i--;
          break;
        case DIR_NW:
          map_i--;
          map_j--;
          break;
        }
        terrain.removeMachine(this);
        terrain = terrain.getMap().getTerrain(map_i, map_j);
        terrain.addMachine(this);
        moveType = MOVE_TO_CENTER;
        movePercent = 0;
      }
      break;
    case MOVE_TO_CENTER:
      movePercent += 10;
      if(movePercent >= 100) moveType = MOVE_DECIDE;
      break;
    case TURN_45_CW:
      movePercent += 10;
      if(movePercent >= 100)
      {
        facing = (facing + 1) & 7;
        moveType = MOVE_DECIDE;
      }
      break;
    case TURN_45_CCW:
      movePercent += 10;
      if(movePercent >= 100)
      {
        facing = (facing - 1) & 7;
        moveType = MOVE_DECIDE;
      }
      break;
    }
  }

  ////////////////////////////////////////////////////////////////////
  // Method:       check                                            //
  // Description:  empty virtual method to be implemented by a      //
  //               specific machine.                                //
  ////////////////////////////////////////////////////////////////////
  public void check()
{}

  ////////////////////////////////////////////////////////////////////
  // Method:       getOffsetX                                       //
  //               getOffsetY                                       //
  // Returns:      x or y offset that machine should be drawn from  //
  //               default position.  Call from overridden draw     //
  //               methods.                                         //
  ////////////////////////////////////////////////////////////////////

  public int  getOffsetX()
  {
    int x = 0;
    if(actionType==ACTION_MOVE)
    {
      int offset = 0;
      int dir = facing;
      if(moveType==MOVE_TO_EDGE) offset = movePercent;
      else if(moveType==MOVE_TO_CENTER)
      {
        offset = 100 - movePercent;
        dir = (dir + 4) & 7;
      }

      if(dir == DIR_NE)
      {
        x += (32 * offset) / 100;
      }
      else if(dir == DIR_SW)
      {
        x -= (32 * offset) / 100;
      }
      else if(dir == DIR_N || dir == DIR_E)
      {
        x += (16 * offset) / 100;
      }
      else if(dir == DIR_S || dir == DIR_W)
      {
        x -= (16 * offset) / 100;
      }
    }
    return x;
  }

  public int  getOffsetY()
  {
    int y = 16;
    if(actionType==ACTION_MOVE)
    {
      int offset = 0;
      int dir = facing;
      if(moveType==MOVE_TO_EDGE) offset = movePercent;
      else if(moveType==MOVE_TO_CENTER)
      {
        offset = 100 - movePercent;
        dir = (dir + 4) & 7;
      }

      if(dir == DIR_NW)
      {
        y -= (16 * offset) / 100;
      }
      else if(dir == DIR_SE)
      {
        y += (16 * offset) / 100;
      }
      else if(dir == DIR_N || dir == DIR_W)
      {
        y -= (8 * offset) / 100;
      }
      else if(dir == DIR_E || dir == DIR_S)
      {
        y += (8 * offset) / 100;
      }
    }
    return y;
  }

  ////////////////////////////////////////////////////////////////////
  // Method:       draw                                             //
  // Arguments:    x, y:  top-left corner of isotile machine is on. //
  ////////////////////////////////////////////////////////////////////
  public void draw(int x, int y)
  {
    //empty function, overridden
  }

  ////////////////////////////////////////////////////////////////////
  // Method:       moveTo                                           //
  // Arguments:    i, j:  destination tile indices                  //
  // Description:  Sets this machines action to move to the dest    //
  //               tile.  It should first move along the axis with  //
  //               the shortest distance, then move along the other //
  //               axis.                                            //
  ////////////////////////////////////////////////////////////////////

  public void moveTo(int i, int j)
  {
    //make sure machine's not already on destination index
    if(terrain.getI()==i && terrain.getJ()==j)
    {
      actionType = ACTION_NONE;
      return;
    }

    move_i = i;
    move_j = j;
    actionType = ACTION_MOVE;
    moveType = MOVE_DECIDE;
  }

  ////////////////////////////////////////////////////////////////////
  // Method:       turnTo                                           //
  // Arguments:    int desiredDir                                   //
  // Returns:      true if facing desired direction or false if not //
  // Description:  If necessary initiates a TURN_CW or TURN_CCW,    //
  //               whichever is nearer.                             //
  ////////////////////////////////////////////////////////////////////
  public boolean turnTo(int desiredDir)
  {
    if(facing == desiredDir) return true;

    //initiate turn 1 unit closer to desired direction
    int turnsCW = (desiredDir - facing) & 7;
    int turnsCCW = (facing - desiredDir) & 7;
    if(turnsCCW < turnsCW)
    {
      moveType = TURN_45_CCW;
    }
    else
    {
      moveType = TURN_45_CW;
    }
    movePercent = 0;
    actionType = ACTION_MOVE;

    return false;
  }

  ////////////////////////////////////////////////////////////////////
  // Method:       getDesiredDir                                    //
  // Arguments:    int sx, sy - source (origin)                     //
  //               int dx, dy - destination                         //
  // Returns:      direction to take to get from source to dest     //
  ////////////////////////////////////////////////////////////////////
  public int getDesiredDir(int sx, int sy, int dx, int dy)
  {
    int xdiff = dx - sx;
    int ydiff = dy - sy;
    if(xdiff==0 && ydiff<0) return DIR_N;
    if(xdiff>0 && ydiff<0) return DIR_NE;
    if(xdiff>0 && ydiff==0) return DIR_E;
    if(xdiff>0 && ydiff>0) return DIR_SE;
    if(xdiff==0 && ydiff>0) return DIR_S;
    if(xdiff<0 && ydiff>0) return DIR_SW;
    if(xdiff<0 && ydiff==0) return DIR_W;
    if(xdiff<0 && ydiff<0) return DIR_NW;
    return 0;
  }
}

class Hoe extends Machine
{
  protected int digDir;

  public Hoe(Terrain _terrain)
  {
    super(_terrain);
  }

  public String  getType()
  {
    return "Hoe";
  }

  public void draw(int x, int y)
  {
    gk.drawTile(frame*8 + facing, x+getOffsetX(), y+getOffsetY());
  }

  //request that an empty truck come over to receive dirt
  //request is for NW of Hoe unless on boundary
  public void callTruck()
  {
    int i = terrain.getI();
    int j = terrain.getJ();
    JobQ jobQ = terrain.getMap().getJobQ();
    if(i==0) i = 1;
    else     i--;
    if(j==0) j = 1;
    else     j--;
    jobQ.addJob(JobQ.JOB_WAIT, i, j, -1);
  }

  public void check()
  {
    switch(actionType)
    {
    case ACTION_NONE:
      if(curJob==null)
      {
        findJob(JobQ.JOB_CUT);
        jobStep = 0;
      }
      if(curJob != null)
      {
        int i = terrain.getI();
        int j = terrain.getJ();
        switch(jobStep)
        {
        case 0:  //go to the site
          moveTo(curJob.getI(), curJob.getJ());
          jobStep++;
          break;
        case 1:  //at the job site, check nw corner
          if(terrain.getMap().getElevation(i, j)==2)
          {
            //need to dig it up.  Facing right way?
            digDir = DIR_NW;
            if(!turnTo(DIR_NW)) return;

            //Facing correct direction by now.  Dig!
            callTruck();
            movePercent = 0;
            jobStep = 5;
            return;
          }
          jobStep++;
          break;
        case 2:  //check ne corner
          if(terrain.getMap().getElevation(i+1, j)==2)
          {
            digDir = DIR_NE;
            if(!turnTo(DIR_NE)) return;
            callTruck();
            movePercent = 0;
            jobStep = 5;
            return;
          }
          jobStep++;
          break;
        case 3:  //check se corner
          if(terrain.getMap().getElevation(i+1, j+1)==2)
          {
            digDir = DIR_SE;
            if(!turnTo(DIR_SE)) return;
            callTruck();
            movePercent = 0;
            jobStep = 5;
            return;
          }
          jobStep++;
          break;
        case 4:  //check sw corner
          if(terrain.getMap().getElevation(i, j+1)==2)
          {
            digDir = DIR_SW;
            if(!turnTo(DIR_SW)) return;
            callTruck();
            movePercent = 0;
            jobStep = 5;
            return;
          }
          curJob = null;   //all done!
          break;
        case 5:  //dug up a load.  Wait for empty dump truck adj
          //to this machine to dump load into
          int di, dj, desiredDir=4;
          Machine m = null;
          for(dj=-1; dj<=1; dj++)
          {
            for(di=-1; di<=1; di++)
            {
              Terrain t = terrain.getMap().getTerrain(i+di,j+dj);
              if(t == null) continue;
              m = t.findMachine("Truck", JobQ.JOB_WAIT);
              desiredDir = (dj+1) * 3 + (di+1);
              if(m != null) break;
            }
            if(m != null) break;
          }
          if(m == null)
          {
            movePercent += 10;
            if(movePercent >= 100)
            {
              movePercent = 0;
              frame ^= 1;
            }
            return;   //look again next time
          }

          //turn to face truck

          switch(desiredDir)
          {
          case 0:
            desiredDir = DIR_NW;
            break;
          case 1:
            desiredDir = DIR_N;
            break;
          case 2:
            desiredDir = DIR_NE;
            break;
          case 3:
            desiredDir = DIR_W;
            break;
          case 4:
            desiredDir = facing;
            break;
          case 5:
            desiredDir = DIR_E;
            break;
          case 6:
            desiredDir = DIR_SW;
            break;
          case 7:
            desiredDir = DIR_S;
            break;
          case 8:
            desiredDir = DIR_SE;
            break;
          }
          if(!turnTo(desiredDir)) return;  //try until okay

          frame = 0;
          m.setFrame(1);   //"put dirt" in truck

          //lower corner we were just digging at
          di=0;
          dj=0;
          switch(digDir)
          {
          case DIR_NW:
            di=0;
            dj=0;
            break;
          case DIR_NE:
            di=1;
            dj=0;
            break;
          case DIR_SE:
            di=1;
            dj=1;
            break;
          case DIR_SW:
            di=0;
            dj=1;
            break;
          }
          terrain = terrain.createFromCurrent("Dirt");
          terrain.getMap().setElevation(i+di,j+dj,1);

          jobStep = 1;     //go back to checking corners

          break;
        }
      }
      break;
    case ACTION_MOVE:
      handleMovement();
      break;
    }
  }
}

class Dozer extends Machine
{
  public Dozer(Terrain _terrain)
  {
    super(_terrain);
  }

  public String  getType()
  {
    return "Dozer";
  }

  public void draw(int x, int y)
  {
    gk.drawTile(16 + frame*8 + facing, x+getOffsetX(),
                y+getOffsetY());
  }

  public void check()
  {
    switch(actionType)
    {
    case ACTION_NONE:
      if(curJob==null)
      {
        findJob(JobQ.JOB_LEVEL);
        jobStep = 0;
      }
      if(curJob != null)
      {
        switch(jobStep)
        {
        case 0:
          moveTo(curJob.getI(),curJob.getJ());
          jobStep++;
          break;
        case 1:
          turnTo(curJob.getParam());
          jobStep++;
          break;
        case 2:
          int i = terrain.getI();
          int j = terrain.getJ();
          switch(curJob.getParam())
          {
          case DIR_N:
            j--;
            break;
          case DIR_NE:
            i++;
            j--;
            break;
          case DIR_E:
            i++;
            break;
          case DIR_SE:
            i++;
            j++;
            break;
          case DIR_S:
            j++;
            break;
          case DIR_SW:
            i--;
            j++;
            break;
          case DIR_W:
            i--;
            break;
          case DIR_NW:
            i--;
            j--;
            break;
          }
          terrain = terrain.createFromCurrent("Dirt");
          moveTo(i,j);
          jobStep++;
          break;
        case 3:
          terrain = terrain.createFromCurrent("Dirt");
          i = terrain.getI();
          j = terrain.getJ();
          Map map = terrain.getMap();

          //fill in a corner
          if(map.getElevation(i,j)==0)
          {
            map.setElevation(i,j,1);
          }
          else if(map.getElevation(i+1,j)==0)
          {
            map.setElevation(i+1,j,1);
          }
          else if(map.getElevation(i+1,j+1)==0)
          {
            map.setElevation(i+1,j+1,1);
          }
          else
          {
            map.setElevation(i,j+1,1);
          }
          terrain = terrain.createFromCurrent("Dirt");
          curJob = null;
          break;
        }
      }
      break;
    case ACTION_MOVE:
      handleMovement();
      break;
    }
  }
}

class Roller extends Machine
{
  public Roller(Terrain _terrain)
  {
    super(_terrain);
  }

  public String  getType()
  {
    return "Roller";
  }

  public void draw(int x, int y)
  {
    gk.drawTile(24 + frame*8 + facing, x+getOffsetX(),
                y+getOffsetY());
  }

  public void check()
  {
    switch(actionType)
    {
    case ACTION_NONE:
      if(curJob==null)
      {
        findJob(JobQ.JOB_ROLL);
        jobStep = 0;
      }
      if(curJob != null)
      {
        switch(jobStep)
        {
        case 0:
          moveTo(curJob.getI(),curJob.getJ());
          jobStep++;
          break;
        case 1:
          int i = terrain.getI();
          int j = terrain.getJ();
          terrain = terrain.createFromCurrent("Road");
          curJob = null;
          break;
        }
      }
      break;
    case ACTION_MOVE:
      handleMovement();
      break;
    }
  }
}

class Grader extends Machine
{
  public Grader(Terrain _terrain)
  {
    super(_terrain);
  }

  public String  getType()
  {
    return "Grader";
  }

  public void draw(int x, int y)
  {
    gk.drawTile(32 + frame*8 + facing, x+getOffsetX(),
                y+getOffsetY());
  }

  public void check()
  {
    switch(actionType)
    {
    case ACTION_NONE:
      if(curJob==null)
      {
        findJob(JobQ.JOB_CLEAR);
        jobStep = 0;
      }
      if(curJob != null)
      {
        switch(jobStep)
        {
        case 0:
          moveTo(curJob.getI(),curJob.getJ());
          jobStep++;
          break;
        case 1:
          int i = terrain.getI();
          int j = terrain.getJ();
          terrain = terrain.createFromCurrent("Dirt");
          curJob = null;
          break;
        }
      }
      break;
    case ACTION_MOVE:
      handleMovement();
      break;
    }
  }
}

class Truck extends Machine
{
  public Truck(Terrain _terrain)
  {
    super(_terrain);
  }

  public String  getType()
  {
    return "Truck";
  }

  public void draw(int x, int y)
  {
    gk.drawTile(40 + frame*8 + facing, x+getOffsetX(),
                y+getOffsetY());
  }

  public void check()
  {
    switch(actionType)
    {
    case ACTION_NONE:
      if(curJob==null)
      {
        if(frame==0) findJob(JobQ.JOB_PAVE | JobQ.JOB_WAIT);
        if(frame==1) findJob(JobQ.JOB_FILL);
        jobStep = 0;
      }
      if(curJob != null)
      {
        if(curJob.getType() == JobQ.JOB_PAVE)
        {
          switch(jobStep)
          {
          case 0:  //go to the depot
            moveTo(0, 0);
            jobStep++;
            break;
          case 1:  //get a load of asphalt, head to the job site
            frame = 3;
            moveTo(curJob.getI(), curJob.getJ());
            jobStep++;
            break;
          case 2:  //at the job site, begin dump animation
            frame = 4;
            jobStep++;
            movePercent = 0;
            break;
          case 3:  //display dump operation until done
            movePercent += 10;
            if(movePercent >= 100)
            {
              int i = terrain.getI();
              int j = terrain.getJ();
              terrain = terrain.createFromCurrent("AsphaltPile");
              terrain.getMap().getJobQ().addJob(
                JobQ.JOB_ROLL,i,j,-1);
              frame = 0;
              curJob = null;
            }
            break;
          }
        }
        else if(curJob.getType() == JobQ.JOB_WAIT)
        {
          switch(jobStep)
          {
          case 0:  //move to site
            moveTo(curJob.getI(), curJob.getJ());
            jobStep++;
            break;
          case 1:  //wait for load until frame gets set
            //to 1 by the Hoe
            if(frame==1)
            {
              curJob = null;
            }
            break;
          }
        }
        else if(curJob.getType() == JobQ.JOB_FILL)
        {
          int i = terrain.getI();
          int j = terrain.getJ();
          Map map = terrain.getMap();
          switch(jobStep)
          {
          case 0:  //find flat dirt adjacent to site
            //to dump load of dirt
            i = curJob.getI();
            j = curJob.getJ();
            int di, dj;
            for(dj=-1; dj<=1; dj++)
            {
              for(di=-1; di<=1; di++)
              {
                if(di==0 && dj==0) continue;
                Terrain t = map.getTerrain(i+di, j+dj);
                if(t==null) continue;
                if(t.isFlat() && t.getType()=="Dirt")
                {
                  //found somewhere to put dirt
                  moveTo(i+di, j+dj);
                  jobStep++;
                  return;
                }
              }
            }

            //nowhere to put dirt; replace job on end of queue
            map.getJobQ().addJob(curJob);
            curJob = null;
            break;
          case 1:
            //dump load of dirt
            frame = 2;
            jobStep++;
            movePercent = 0;
            break;
          case 2:  //display dump operation until done
            movePercent += 10;
            if(movePercent >= 100)
            {
              terrain = terrain.createFromCurrent("DirtPile");
              terrain.getMap().getJobQ().addJob(
                JobQ.JOB_LEVEL, i, j,
                getDesiredDir(i, j,
                              curJob.getI(), curJob.getJ()));
              frame = 0;
              curJob = null;
            }
            break;
          }
        }
      }
      break;
    case ACTION_MOVE:
      handleMovement();
      break;
    }
  }
}

