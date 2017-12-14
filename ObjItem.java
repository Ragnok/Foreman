//////////////////////////////////////////////////////////////////////
//  File:     ObjList.java                                          //
//                                                                  //
//  Author:   Abe Pralle                                            //
//  Created:  9.27.99                                               //
//  Modified: 9.27.99                                               //
//                                                                  //
//  Purpose:  Node for the ObjList linked list.                     //
//////////////////////////////////////////////////////////////////////

class ObjItem
{
  protected Machine machine;
  protected ObjItem nextItem;

  ////////////////////////////////////////////////////////////////////
  // Constructor:  ObjItem                                          //
  ////////////////////////////////////////////////////////////////////
  public ObjItem(Machine m)
  {
    machine = m;
    nextItem = null;
  }

  ////////////////////////////////////////////////////////////////////
  // Methods:      setNextItem                                      //
  //               getNextItem                                      //
  //               getMachine                                       //
  // Description:  Access methods                                   //
  ////////////////////////////////////////////////////////////////////
  public void    setNextItem(ObjItem next)
  {
    nextItem = next;
  }

  public ObjItem getNextItem()
  {
    return nextItem;
  }

  public Machine getMachine()
  {
    return machine;
  }
}

