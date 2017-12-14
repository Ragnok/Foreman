//////////////////////////////////////////////////////////////////////
//  File:     Terrain.java                                          //
//                                                                  //
//  Author:   Abe Pralle                                            //
//  Created:  9.23.99                                               //
//  Modified: 9.23.99                                               //
//                                                                  //
//  Purpose:  Contains the Terrain base class as well as all        //
//            derived classes (Grass, Dirt, etc.)                   //
//////////////////////////////////////////////////////////////////////

import java.util.Random;
import java.lang.Math;

abstract class Terrain
{
  static GameKit gk;
  static Map map;
  static Random randomGen = new Random();

  protected int map_i, map_j;
  protected ObjList objList = new ObjList();

  ////////////////////////////////////////////////////////////////////
  // Constructor:  Terrain                                          //
  ////////////////////////////////////////////////////////////////////
  public Terrain(int i, int j)
  {
    map_i = i;
    map_j = j;

    //10% chance of machine on this tile  (0% as of step 6)
    int r = java.lang.Math.abs(randomGen.nextInt()) % 100;
    if(false)
    {  //if(r<10)
      r = java.lang.Math.abs(randomGen.nextInt()) % 5;
      switch(r)
      {
      case 0:
        objList.addItem(Machine.create("Hoe", this));
        break;
      case 1:
        objList.addItem(Machine.create("Dozer", this));
        break;
      case 2:
        objList.addItem(Machine.create("Roller", this));
        break;
      case 3:
        objList.addItem(Machine.create("Grader", this));
        break;
      case 4:
        objList.addItem(Machine.create("Truck", this));
        break;
      }
    }
  }

  ////////////////////////////////////////////////////////////////////
  // Methods:      setGK                                            //
  //               setMap                                           //
  //               getGK                                            //
  //               getMap                                           //
  ////////////////////////////////////////////////////////////////////
  static public void setGK(GameKit gkInit)
  {
    gk = gkInit;
  }
  static public void setMap(Map mapInit)
  {
    map = mapInit;
  }
  static public GameKit getGK()
  {
    return gk;
  }
  static public Map     getMap()
  {
    return map;
  }

  ////////////////////////////////////////////////////////////////////
  // Methods:      getI                                             //
  //               getJ                                             //
  ////////////////////////////////////////////////////////////////////
  public int  getI()
  {
    return map_i;
  }
  public int  getJ()
  {
    return map_j;
  }


  ////////////////////////////////////////////////////////////////////
  // Method:       create                                           //
  // Arguments:    type - string of "Grass", "Dirt", "Road",        //
  //               "AsphaltPile", "DirtPile", or "Depot"            //
  // Description:  Creates and returns an object of the specified   //
  //               type.  Java won't let us stick all our derived   //
  //               classes in the same source file as this base     //
  //               class unless we create the derived classes       //
  //               indirectly.                                      //
  ////////////////////////////////////////////////////////////////////
  static public Terrain create(String type, int i, int j)
  {
    if(type=="Grass")       return new Grass(i, j);
    if(type=="Dirt")        return new Dirt(i, j);
    if(type=="Road")        return new Road(i, j);
    if(type=="AsphaltPile") return new AsphaltPile(i, j);
    if(type=="DirtPile")    return new DirtPile(i, j);
    if(type=="Depot")       return new Depot(i, j);
    return null;
  }

  ////////////////////////////////////////////////////////////////////
  // Method:       createFromCurrent                                //
  // Arguments:    type - string of "Grass", "Dirt", "Road",        //
  //               "AsphaltPile", "DirtPile", or "Depot"            //
  // Description:  Creates and returns an object of the specified   //
  //               type, copying over all variables & references    //
  //               from the object of the method context            //
  ////////////////////////////////////////////////////////////////////
  public Terrain createFromCurrent(String type)
  {
    Terrain newTerrain = null;
    if(type=="Grass")       newTerrain = new Grass(map_i, map_j);
    if(type=="Dirt")        newTerrain = new Dirt(map_i, map_j);
    if(type=="Road")        newTerrain = new Road(map_i, map_j);
    if(type=="AsphaltPile") newTerrain = new AsphaltPile(map_i, map_j);
    if(type=="DirtPile")    newTerrain = new DirtPile(map_i, map_j);
    if(type=="Depot")       newTerrain = new Depot(map_i, map_j);
    if(newTerrain==null)
    {
      gk.debugMesg("createFromCurrent type not found!");
      return null;
    }

    newTerrain.map_i = map_i;
    newTerrain.map_j = map_j;
    newTerrain.objList = objList;
    getMap().setTerrain(map_i, map_j, newTerrain);
    getMap().setFullRedraw();

    return newTerrain;
  }

  ////////////////////////////////////////////////////////////////////
  // Method:       getTileNum                                       //
  // Returns:      number of tile to draw for this object           //
  ////////////////////////////////////////////////////////////////////
  public int  getTileNum()
  {
    //Override in derived classes
    return 0;
  }

  ////////////////////////////////////////////////////////////////////
  // Method:       getType                                          //
  // Returns:      returns String w/name of class                   //
  ////////////////////////////////////////////////////////////////////
  public String getType()
  {
    return "";
  }

  ////////////////////////////////////////////////////////////////////
  // Method:       isFlat                                           //
  // Returns:      True If 4 elevation corners are flat at elev=1   //
  ////////////////////////////////////////////////////////////////////
  public boolean isFlat()
  {
    if(map.getElevation(map_i, map_j) != 1) return false;
    if(map.getElevation(map_i+1, map_j) != 1) return false;
    if(map.getElevation(map_i+1, map_j+1) != 1) return false;
    if(map.getElevation(map_i, map_j+1) != 1) return false;
    return true;
  }

  ////////////////////////////////////////////////////////////////////
  // Method:       check                                            //
  // Description:  Calls the check() method of each of the Machines //
  //               in objList.                                      //
  ////////////////////////////////////////////////////////////////////
  public void check()
  {
    Machine m;
    for(m=objList.getFirstItem(); m!=null; m=objList.getNextItem())
    {
      m.check();
    }
  }

  ////////////////////////////////////////////////////////////////////
  // Method:       draw                                             //
  // Arguments:    x, y:  pixel coordinates of top-left edge of     //
  //               iso tile                                         //
  // Description:  Calls the draw method of each of the Machines    //
  //               in objList.                                      //
  ////////////////////////////////////////////////////////////////////
  public void draw(int x, int y)
  {
    Machine m;
    for(m=objList.getFirstItem(); m!=null; m=objList.getNextItem())
    {
      m.draw(x, y);
    }
  }

  ////////////////////////////////////////////////////////////////////
  // Methods:      addMachine                                       //
  //               removeMachine                                    //
  // Arguments:    machine - machine obj to add or remove           //
  // Description:  Adds the given machine object to the objList of  //
  //               the terrain object                               //
  ////////////////////////////////////////////////////////////////////
  public boolean addMachine(Machine m)
  {
    return objList.addItem(m);
  }

  public boolean removeMachine(Machine m)
  {
    return objList.removeItem(m);
  }

  ////////////////////////////////////////////////////////////////////
  // Method:       findMachine                                      //
  // Arguments:    type - machine type to find ("Hoe", "Dozer")     //
  //               jobType  - job machine must be doing             //
  // Returns:      first machine reference that performing job      //
  ////////////////////////////////////////////////////////////////////
  public Machine findMachine(String type, int jobType)
  {
    ObjItem oldCur = objList.getCurItem();
    Machine m;
    for(m=objList.getFirstItem(); m!=null; m=objList.getNextItem())
    {
      if(m.getType()==type && m.getJob()!=null &&
          (m.getJob().getType() & jobType) != 0)
      {
        objList.setCurItem(oldCur);
        return m;
      }
      else
      {
        if(m.getJob()!=null && (m.getJob().getType() & jobType) > 0
            && type=="Any")
        {
          objList.setCurItem(oldCur);
          return m;
        }
      }
    }
    objList.setCurItem(oldCur);
    return null;
  }
}

class Grass extends Terrain
{
  public Grass(int i, int j)
  {
    super(i, j);
  }

  public int  getTileNum()
  {
    return 80;
  }
  public String getType()
  {
    return "Grass";
  }
}

class Dirt extends Terrain
{
  public Dirt(int i, int j)
  {
    super(i, j);
  }

  public int  getTileNum()
  {
    return 96;
  }
  public String getType()
  {
    return "Dirt";
  }
}

class Road extends Terrain
{
  public Road(int i, int j)
  {
    super(i, j);
  }

  public int  getTileNum()
  {
    return 112;
  }
  public String getType()
  {
    return "Road";
  }
}

class AsphaltPile extends Terrain
{
  public AsphaltPile(int i, int j)
  {
    super(i, j);
  }

  public int  getTileNum()
  {
    return 113;
  }
  public String getType()
  {
    return "AsphaltPile";
  }
}

class DirtPile extends Terrain
{
  public DirtPile(int i, int j)
  {
    super(i, j);
  }

  public int  getTileNum()
  {
    return 114;
  }
  public String getType()
  {
    return "DirtPile";
  }
}

class Depot extends Terrain
{
  public Depot(int i, int j)
  {
    super(i, j);
  }

  public int  getTileNum()
  {
    return 115;
  }
  public String getType()
  {
    return "Depot";
  }
}

