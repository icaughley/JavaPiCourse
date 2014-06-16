package github.icaughley.javapicourse.lesson2;

import github.icaughley.javapicourse.lesson1.CallBackTimerTask;
import github.icaughley.javapicourse.lesson1.CallBackable;
import java.io.IOException;
import java.util.Timer;
import javax.microedition.midlet.MIDlet;

public class Lesson2
  extends MIDlet
  implements CallBackable
{
  private static final long INTERVAL = 1000;
  private BMP180Sensor sensor;

  @Override
  public void startApp()
  {
    sensor = new BMP180Sensor();

    CallBackTimerTask callBackTask = new CallBackTimerTask();
    callBackTask.addCallBack( this );

    new Timer().scheduleAtFixedRate( callBackTask, 0, INTERVAL );
  }

  @Override
  public void destroyApp( boolean unconditional )
  {
    try
    {
      if ( sensor != null )
      {
        sensor.close();
      }
    }
    catch ( IOException ex )
    {
      System.out.println( "Exception closing sensor: " + ex );
    }
  }

  @Override
  public void callBack()
  {
    try
    {
      final double[] result = sensor.getMetricTemperatureBarometricPressure();
      double tempC = result[ 0 ];
      double pressureHPa = result[ 1 ];
      System.out.format( "Temp: %.2f C\n", tempC );
      System.out.format( "Pressure: %.2f hPa\n", pressureHPa );
    }
    catch ( IOException e )
    {
      System.out.println( "Exception in startApp: " + e );
    }
  }
}
