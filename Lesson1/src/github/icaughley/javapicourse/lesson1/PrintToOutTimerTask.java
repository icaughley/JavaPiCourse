package github.icaughley.javapicourse.lesson1;

import java.util.TimerTask;

/**
 * A timer task that prints a message when it is run.
 */
public class PrintToOutTimerTask
  extends TimerTask
{

  private final String message;

  public PrintToOutTimerTask( final String msg )
  {
    this.message = msg;
  }

  public void run()
  {
    System.out.println( message );
  }
}
