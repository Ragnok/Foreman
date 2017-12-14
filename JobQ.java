//////////////////////////////////////////////////////////////////////
//  File:     JobQ.java                                             //
//                                                                  //
//  Author:   Abe Pralle                                            //
//  Created:  10.18.99                                              //
//  Modified: 10.19.99                                              //
//                                                                  //
//  Purpose:  Implements a queue of jobs.  Jobs are added to the    //
//            tail.  Machines will search through the Q to find     //
//            the first job they can manage and then remove that    //
//            job from the queue.                                   //
//////////////////////////////////////////////////////////////////////

class JobQ
{
  static final int JOB_CLEAR=1, JOB_FILL=2, JOB_CUT=4, JOB_PAVE=8,
                                         JOB_ROLL=16, JOB_WAIT=32, JOB_LEVEL=64;
  static final int JOB_ANY =
    JOB_CLEAR | JOB_FILL | JOB_CUT | JOB_PAVE | JOB_ROLL | JOB_WAIT
    | JOB_LEVEL;
  private JobItem head, tail;
  private int numItems;

  ////////////////////////////////////////////////////////////////////
  //  Constructor:  JobQ                                            //
  ////////////////////////////////////////////////////////////////////
  public JobQ()
  {
    head = tail = null;
    numItems = 0;
  }

  ////////////////////////////////////////////////////////////////////
  //  Methods:      getFirstJob                                     //
  //                getNumJobs                                      //
  ////////////////////////////////////////////////////////////////////
  public JobItem getFirstJob()
  {
    return head;
  }
  public int     getNumJobs()
  {
    return numItems;
  }

  ////////////////////////////////////////////////////////////////////
  //  Method:       addJob                                          //
  //  Arguments:    type, i, j, param - parameters of job           //
  //                OR job - new job object                         //
  //  Returns:      JobItem object that was added to Queue          //
  //  Description:  Creates a new JobItem object from the parameters//
  //                and adds it to end of Q.                        //
  ////////////////////////////////////////////////////////////////////
  public JobItem addJob(int type, int i, int j, int param)
  {
    JobItem newItem = new JobItem(type, i, j, param);
    if(head==null)
    {
      head = tail = newItem;
    }
    else
    {
      tail.setNextItem(newItem);
      tail = newItem;
    }
    numItems++;
    return newItem;
  }

  public JobItem addJob(JobItem job)
  {
    job.setNextItem(null);
    if(head==null)
    {
      head = tail = job;
    }
    else
    {
      tail.setNextItem(job);
      tail = job;
    }
    numItems++;
    return job;
  }

  ////////////////////////////////////////////////////////////////////
  //  Method:       insertJob                                       //
  //  Arguments:    afterItem - item to insert new job after        //
  //                type, i, j, param - parameters of job           //
  //  Returns:      reference to inserted JobItem                   //
  //  Description:  Creates a new JobItem object from the           //
  //                parameters, inserts it after specified item in Q//
  ////////////////////////////////////////////////////////////////////
  public JobItem insertJob(JobItem afterItem, int type, int i, int j,
                           int param)
  {
    if(head==null || afterItem==null)
    {
      addJob(type, i, j, param);
      return tail;
    }

    JobItem newItem = new JobItem(type, i, j, param);
    newItem.setNextItem(afterItem.getNextItem());
    afterItem.setNextItem(newItem);
    if(tail==afterItem) tail = newItem;

    numItems++;
    return newItem;
  }

  ////////////////////////////////////////////////////////////////////
  //  Method:       findJob                                         //
  //  Arguments:    type - collection of one or more job types ORed //
  //                       together                                 //
  //                prev - JobItem to start searching after, or     //
  //                       "null" to start start searching from head//
  //  Returns:      next JobItem job which matches one of the       //
  //                specified types, or null if no matches          //
  ////////////////////////////////////////////////////////////////////
  public JobItem findJob(int type, JobItem prev)
  {
    JobItem cur;
    if(prev!=null) cur = prev.getNextItem();
    else           cur = head;

    while(cur!=null)
    {
      if((cur.getType() & type) != 0) return cur;
      cur = cur.getNextItem();
    }

    return null;
  }

  ////////////////////////////////////////////////////////////////////
  //  Method:       removeJob                                       //
  //  Arguments:    item - JobItem object to remove from Q          //
  //  Returns:      JobItem removed or null on failure              //
  //  Description:  Removes specified object from the Q             //
  ////////////////////////////////////////////////////////////////////
  public JobItem removeJob(JobItem item)
  {
    JobItem cur, prev=null;
    for(cur=head; cur!=null; cur=cur.getNextItem())
    {
      if(cur==item)
      {  //found the one to remove
        if(cur==head)
        {
          head = head.getNextItem();
          if(head==null) tail = null;
        }
        else if(cur==tail)
        {
          tail = prev;
          tail.setNextItem(null);
        }
        else
        {
          //removing from middle of list
          prev.setNextItem(cur.getNextItem());
        }
        numItems--;
        return cur;
      }
      prev = cur;
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////
  //  Method:       printContents                                   //
  //  Description:  prints a description of each item in the Q      //
  //                to System.out along with an index number for    //
  //                each (beginning at zero)                        //
  ////////////////////////////////////////////////////////////////////
  public void printContents()
  {
    if(head==null)
    {
      System.out.println("Job Queue is empty");
    }

    JobItem cur;
    for(cur=head; cur!=null; cur=cur.getNextItem())
    {
      System.out.println(" " + cur.getDescription());
    }
  }
}

