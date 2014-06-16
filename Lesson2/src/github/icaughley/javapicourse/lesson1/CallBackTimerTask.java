package github.icaughley.javapicourse.lesson1;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

/**
 * A timer task that calls a list of callBacks when it is run. The call backs are called in the same order as they
 * are added to the task.
 */
public class CallBackTimerTask
  extends TimerTask
{
  private List<CallBackable> callBackList = new ArrayList<>();

  public void addCallBack( final CallBackable callBack )
  {
    callBackList.add( callBack );
  }

  public void run()
  {
    for ( CallBackable callBack : callBackList )
    {
      callBack.callBack();
    }
  }
}
