//////////////////////////////////////////////////////////////////////
//  File:     ObjList.java                                          //
//                                                                  //
//  Author:   Abe Pralle                                            //
//  Created:  9.27.99                                               //
//  Modified: 9.27.99                                               //
//                                                                  //
//  Purpose:  Stores a list of machine objects using a linked list  //
//////////////////////////////////////////////////////////////////////

class ObjList
{
  static private GameKit gk;
  private ObjItem head, tail, lastReturned;

  ////////////////////////////////////////////////////////////////////
  // Constructor:  ObjList                                          //
  ////////////////////////////////////////////////////////////////////
  public ObjList()
  {
    head = tail = null;
  }

  ////////////////////////////////////////////////////////////////////
  // Method:       setGK                                            //
  ////////////////////////////////////////////////////////////////////
  static public void setGK(GameKit gkInit)
  {
    gk = gkInit;
  }

  ////////////////////////////////////////////////////////////////////
  // Method:       addItem                                          //
  // Arguments:    item - machine object to add to list.            //
  // Returns:      true                                             //
  // Description:  Adds object to end of list unless already        //
  //               present.                                         //
  ////////////////////////////////////////////////////////////////////
  public boolean addItem(Machine item)
  {
    //list empty?
    if(head==null)
    {
      head = tail = new ObjItem(item);
      return true;
    }

    //is object already in list?
    ObjItem cur;
    for(cur=head; cur!=null; cur=cur.getNextItem())
    {
      if(cur.getMachine() == item) return true;
    }

    //add object to end of list
    tail.setNextItem(new ObjItem(item));
    tail = tail.getNextItem();
    return true;
  }

  ////////////////////////////////////////////////////////////////////
  // Method:       removeItem                                       //
  // Arguments:    item - item to remove from list                  //
  // Returns:      false if item not in list, otherwise true        //
  // Description:  Removes object from list if present and          //
  //               collapses list                                   //
  ////////////////////////////////////////////////////////////////////
  public boolean removeItem(Machine item)
  {
    //perform two independent checks to simplify things:
    // 1) is item at head of list?
    // 2) is item in middle of list?

    // 1) is item at head of list?
    if(head.getMachine() == item)
    {
      head = head.getNextItem();
      if(head == null) tail = null;
      return true;
    }

    // 2) is item in middle of list?
    ObjItem prev = null, cur;
    for(cur=head; cur!=null; cur=cur.getNextItem())
    {
      if(cur.getMachine() == item)
      {
        if(cur == tail)
        {
          tail = prev;
          tail.setNextItem(null);
        }
        else
        {
          prev.setNextItem(cur.getNextItem());
        }
        return true;
      }
      prev = cur;  //remember previous element
    }


    return false;  //didn't find it;
  }

  ////////////////////////////////////////////////////////////////////
  // Methods:      setCurItem                                       //
  //               getCurItem                                       //
  ////////////////////////////////////////////////////////////////////

  public void    setCurItem(ObjItem m)
  {
    lastReturned = m;
  }
  public ObjItem getCurItem()
  {
    return lastReturned;
  }

  ////////////////////////////////////////////////////////////////////
  // Method:       getFirstItem                                     //
  // Returns:      first Machine object in list or null if none     //
  // Note:         Saves index of returned object for use with      //
  //               getNextItem                                      //
  ////////////////////////////////////////////////////////////////////
  public Machine getFirstItem()
  {
    if(head==null) return null;
    lastReturned = head;
    return lastReturned.getMachine();
  }

  ////////////////////////////////////////////////////////////////////
  // Method:       getNextItem                                      //
  // Returns:      next Machine object in list after previous call  //
  //               to getFirstItem or getNextItem or null if end    //
  //               of list.                                         //
  // Note:         Saves index of returned object for use with      //
  //               getNextItem.                                     //
  ////////////////////////////////////////////////////////////////////
  public Machine getNextItem()
  {
    lastReturned = lastReturned.getNextItem();
    if(lastReturned==null) return null;
    return lastReturned.getMachine();
  }
}

