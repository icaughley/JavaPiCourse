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
  public static final int FIVE_MINUTES = 1000 * 60 * 5;
  public static final int THIRTY_SECONDS = 30000;
  public static final String MESSAGE = "Listening...";

  private volatile Timer messageTimer;

  protected void startApp()
    throws MIDletStateChangeException
  {
    System.out.println( "StartTime: " + now() );

    CallBackTimerTask callBackTask = new CallBackTimerTask();
    callBackTask.addCallBack( this );

    final Date startTime = calcNext5MinBoundary();
    new Timer().scheduleAtFixedRate( callBackTask, startTime, FIVE_MINUTES );
  }

  public void callBack()
  {
    final String now = now();

    System.out.println( "Time: " + now );

    if ( messageTimer != null )
    {
      messageTimer.cancel();
    }

    if ( now.endsWith( "5" ) )
    {
      messageTimer = new Timer();
      messageTimer.schedule( new PrintToOutTimerTask( MESSAGE ), 0, THIRTY_SECONDS );
    }
  }

  protected void destroyApp( boolean b )
    throws MIDletStateChangeException
  {
    System.out.println( "Destroy!" );
    if ( messageTimer != null )
    {
      messageTimer.cancel();
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
    return String.format( "%02d:%02d", cal.get( Calendar.HOUR ), cal.get( Calendar.MINUTE ) );
  }
}
