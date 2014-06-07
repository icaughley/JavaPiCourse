package github.icaughley.javapicourse.lesson1;

import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

public class Lesson1
  extends MIDlet
  implements CallBackable
{

  private CallBackTimerTask callBackTask;
  private PrintToOutTimerTask messageTask;

  protected void startApp()
    throws MIDletStateChangeException
  {
    System.out.println( "Start!" );
    callBackTask = new CallBackTimerTask();
    callBackTask.addCallBack( this );
    messageTask = new PrintToOutTimerTask( "Listening..." );

    final Date startTime = calcNext5MinBoundary();

    System.out.println( "StartTime: " + now() );

    new Timer().scheduleAtFixedRate( callBackTask, startTime, 1000*60*5 );
  }

  public void callBack()
  {
    final String now = now();

    System.out.println( "Time: " + now );

    if ( now.endsWith( "5" ) )
    {
      new Timer().schedule( messageTask, 0, 30000 );
    }
    else
    {
      messageTask.cancel();
    }
  }

  protected void destroyApp( boolean b )
    throws MIDletStateChangeException
  {
    System.out.println( "Destroy!" );
    if ( callBackTask != null )
    {
      callBackTask.cancel();
    }
    if ( messageTask != null )
    {
      messageTask.cancel();
    }
  }

  private Date calcNext5MinBoundary()
  {
    final Calendar cal = Calendar.getInstance();
    cal.set( Calendar.SECOND, 0 );
    cal.set( Calendar.MINUTE, cal.get( Calendar.MINUTE ) / 5 * 5 + 5 );
    return cal.getTime();
  }

  private String now()
  {
    final Calendar cal = Calendar.getInstance();
    return String.format( "%2d:%2d", cal.get( Calendar.HOUR ), cal.get( Calendar.MINUTE ) );
  }
}
