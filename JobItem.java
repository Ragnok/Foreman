//////////////////////////////////////////////////////////////////////
//  File:     JobItem.java                                          //
//                                                                  //
//  Author:   Abe Pralle                                            //
//  Created:  10.18.99                                              //
//  Modified: 10.18.99                                              //
//                                                                  //
//  Purpose:  Node for JobQ                                         //
//////////////////////////////////////////////////////////////////////

class JobItem
{
  static final int JOB_CLEAR=1, JOB_FILL=2, JOB_CUT=4, JOB_PAVE=8,
                                         JOB_ROLL=16, JOB_WAIT=32, JOB_LEVEL=64;
  static final int JOB_ANY =
    JOB_CLEAR | JOB_FILL | JOB_CUT | JOB_PAVE | JOB_ROLL | JOB_WAIT
    | JOB_LEVEL;
  private int type, loc_i, loc_j, param;
  private JobItem nextItem;

  ////////////////////////////////////////////////////////////////////
  //  Constructor:  JobItem                                         //
  ////////////////////////////////////////////////////////////////////
  public JobItem(int _type, int i, int j, int _param)
  {
    type = _type;
    loc_i = i;
    loc_j = j;
    param = _param;
  }

  ////////////////////////////////////////////////////////////////////
  //  get/set methods                                               //
  ////////////////////////////////////////////////////////////////////
  public void setType(int n)
  {
    type = n;
  }
  public void setI(int n)
  {
    loc_i = n;
  }
  public void setJ(int n)
  {
    loc_j = n;
  }
  public void setParam(int n)
  {
    param = n;
  }
  public void setNextItem(JobItem next)
  {
    nextItem = next;
  }

  public int  getType()
  {
    return type;
  }
  public int  getI()
  {
    return loc_i;
  }
  public int  getJ()
  {
    return loc_j;
  }
  public int  getParam()
  {
    return param;
  }
  public JobItem getNextItem()
  {
    return nextItem;
  }

  ////////////////////////////////////////////////////////////////////
  //  Method:       getDescription                                  //
  //  Returns:      String containing a description of this job     //
  ////////////////////////////////////////////////////////////////////
  public String getDescription()
  {
    String st = new String();
    switch(type)
    {
    case JOB_CLEAR:
      st += "JOB_CLEAR ";
      break;
    case JOB_FILL:
      st += "JOB_FILL ";
      break;
    case JOB_CUT:
      st += "JOB_CUT ";
      break;
    case JOB_PAVE:
      st += "JOB_PAVE ";
      break;
    case JOB_WAIT:
      st += "JOB_WAIT ";
      break;
    case JOB_LEVEL:
      st += "JOB_LEVEL ";
      break;
    case JOB_ROLL:
      st += "JOB_ROLL ";
      break;
    }
    st += "(" + loc_i + ", " + loc_j + ") " + param;
    return st;
  }
}

