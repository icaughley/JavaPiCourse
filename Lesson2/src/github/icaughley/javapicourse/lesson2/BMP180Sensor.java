package github.icaughley.javapicourse.lesson2;

import java.io.IOException;
import java.nio.ByteBuffer;

public class BMP180Sensor
  extends AbstractI2CDevice
{
  //BMP180 control registry
  protected static final int CONTROL_REGISTER = 0xF4;
  /**
   * Device address BMP180 address is 0x77
   */
  private static final int BMP180_ADDR = 0x77;
  //Total of bytes use for calibration
  private static final int CALIBRATION_BYTES = 22;
  private static final int SUB_ADDRESS_SIZE = 1;
  // EEPROM address data
  private static final int EEPROM_START = 0xAA;
  // Temperature read address
  private static final int TEMP_ADDR = 0xF6;
  // Read temperature command
  private static final byte GET_TEMP_CMD = (byte) 0x2E;
  // Temperature read address
  private static final int PRESS_ADDR = 0xF6;
  //Barometer configuration
  private static final int BAROMETER_OVERSAMPLE_SETTING = 1;
  private static final byte BAROMETER_OVERSAMPLE_COMMAND =
    (byte) ( 0x34 + ( ( BAROMETER_OVERSAMPLE_SETTING << 6 ) & 0xC0 ) );
  private static final int BAROMETER_OVERSAMPLE_DELAY = 8;
  // EEPROM registers - these represent calibration data
  protected short AC1;
  protected short AC2;
  protected short AC3;
  protected int AC4;
  protected int AC5;
  protected int AC6;
  protected short B1;
  protected short B2;
  protected short MB;
  protected short MC;
  protected short MD;
  //Variable common between temperature & pressure calculations
  protected int B5;
  // Shared ByteBuffers
  private ByteBuffer temperatureReadBuffer = ByteBuffer.allocateDirect( 2 );
  private ByteBuffer pressureReadBuffer = ByteBuffer.allocateDirect( 3 );

  /**
   * BMP180 constructor invokes the parent constructor to initialize the
   * connection with the I2C device. Them we proceed with some initialization
   * steps(reading calibration data)
   */
  protected BMP180Sensor()
  {
    super( BMP180_ADDR );
    initDevice();
  }

  /**
   * This method read the calibration data common for the Temperature sensor and
   * Barometer sensor included in the BMP180
   */
  private void initDevice()
  {
    try
    {
      //Small delay before starting
      Thread.sleep( 500 );

      // Read all of the calibration data into a byte array
      ByteBuffer calibData = ByteBuffer.allocateDirect( CALIBRATION_BYTES );
      int result = getDevice().read( EEPROM_START, SUB_ADDRESS_SIZE, calibData );
      if ( result < CALIBRATION_BYTES )
      {
        System.out.println( "Not all the calibration bytes were read" );
        return;
      }
      // Read each of the pairs of data as a signed short
      calibData.rewind();
      AC1 = calibData.getShort();
      AC2 = calibData.getShort();
      AC3 = calibData.getShort();

      // Unsigned short values
      byte[] data = new byte[ 2 ];
      calibData.get( data );
      AC4 = ( ( ( data[ 0 ] << 8 ) & 0xFF00 ) + ( data[ 1 ] & 0xFF ) );
      calibData.get( data );
      AC5 = ( ( ( data[ 0 ] << 8 ) & 0xFF00 ) + ( data[ 1 ] & 0xFF ) );
      calibData.get( data );
      AC6 = ( ( ( data[ 0 ] << 8 ) & 0xFF00 ) + ( data[ 1 ] & 0xFF ) );

      // Signed sort values
      B1 = calibData.getShort();
      B2 = calibData.getShort();
      MB = calibData.getShort();
      MC = calibData.getShort();
      MD = calibData.getShort();
    }
    catch ( IOException e )
    {
      System.out.println( "Exception: " + e.getMessage() );
    }
    catch ( InterruptedException e )
    {
      System.out.println( "Interrupted Exception: " + e.getMessage() );
    }
  }

  /**
   * Method for reading the temperature and barometric pressure from the BMP
   * device as metric values
   *
   * @return A double array containing the current temperature in Celsius and
   * the barometric pressure in hPa
   * @throws IOException
   */
  public double[] getMetricTemperatureBarometricPressure()
    throws IOException
  {
    double[] result = new double[ 2 ];
    result[ 0 ] = getTemperatureInCelsius();
    result[ 1 ] = getPressureInHPa();
    return result;
  }

  /**
   * Method for reading the temperature. Remember the sensor will provide us
   * with raw data, and we need to transform in some analyzed value to make
   * sense. All the calculations are normally provided by the manufacturer. In
   * our case we use the calibration data collected at construction time.
   *
   * @return Temperature in Celsius as a double
   * @throws IOException If there is an IO error reading the sensor
   */
  public double getTemperatureInCelsius()
    throws IOException
  {
    // Write the read temperature command to the command register
    writeOneByte( CONTROL_REGISTER, GET_TEMP_CMD );

    // Delay before reading the temperature
    try
    {
      Thread.sleep( 100 );
    }
    catch ( InterruptedException ex )
    {
    }

    //Read uncompressed data
    temperatureReadBuffer.clear();
    int result = getDevice().read( TEMP_ADDR, SUB_ADDRESS_SIZE, temperatureReadBuffer );
    if ( result < 2 )
    {
      System.out.println( "Not enough data for temperature read" );
    }

    // Get the uncompensated temperature as a signed two byte word
    byte[] data = new byte[ 2 ];
    temperatureReadBuffer.rewind();
    temperatureReadBuffer.get( data );
    final int UT = ( ( data[ 0 ] << 8 ) & 0xFF00 ) + ( data[ 1 ] & 0xFF );

    // Calculate the actual temperature
    final int X1 = ( ( UT - AC6 ) * AC5 ) >> 15;
    final int X2 = ( MC << 11 ) / ( X1 + MD );
    B5 = X1 + X2;

    return (float) ( ( B5 + 8 ) >> 4 ) / 10;
  }

  /*
   * Read the temperature value as a Celsius value and the convert the value to Farenheit.
   *
   * @return Temperature in Celsius as a double
   * @throws IOException If there is an IO error reading the sensor
   */
  public double getTemperatureInFahrenheit()
    throws IOException
  {
    return ( getTemperatureInCelsius() * 1.8 ) + 32;
  }

  /**
   * Read the barometric pressure (in hPa) from the device.
   *
   * @return double Pressure measurement in hPa
   */
  public double getPressureInHPa()
    throws IOException
  {

    // Write the read pressure command to the command register
    writeOneByte( CONTROL_REGISTER, BAROMETER_OVERSAMPLE_COMMAND );

    // Delay before reading the pressure - use the value determined by the oversampling setting (mode)
    try
    {
      Thread.sleep( BAROMETER_OVERSAMPLE_DELAY );
    }
    catch ( InterruptedException ex )
    {
    }

    // Read the uncompensated pressure value
    pressureReadBuffer.clear();
    int result = getDevice().read( PRESS_ADDR, SUB_ADDRESS_SIZE, pressureReadBuffer );
    if ( result < 3 )
    {
      System.out.println( "Couldn't read all bytes, only read = " + result );
      return 0;
    }

    // Get the uncompensated pressure as a three byte word
    pressureReadBuffer.rewind();

    byte[] data = new byte[ 3 ];
    pressureReadBuffer.get( data );

    final int UP = ( ( ( ( data[ 0 ] << 16 ) & 0xFF0000 ) + ( ( data[ 1 ] << 8 ) & 0xFF00 ) + ( data[ 2 ] & 0xFF ) ) >>
                     ( 8 - BAROMETER_OVERSAMPLE_SETTING ) );

    // Calculate the true pressure
    final int B6 = B5 - 4000;
    int X1 = ( B2 * ( B6 * B6 ) >> 12 ) >> 11;
    int X2 = AC2 * B6 >> 11;
    int X3 = X1 + X2;
    final int B3 = ( ( ( ( AC1 * 4 ) + X3 ) << BAROMETER_OVERSAMPLE_SETTING ) + 2 ) / 4;
    X1 = AC3 * B6 >> 13;
    X2 = ( B1 * ( ( B6 * B6 ) >> 12 ) ) >> 16;
    X3 = ( ( X1 + X2 ) + 2 ) >> 2;
    int B4 = ( AC4 * ( X3 + 32768 ) ) >> 15;
    int B7 = ( UP - B3 ) * ( 50000 >> BAROMETER_OVERSAMPLE_SETTING );

    int Pa;
    if ( B7 < 0x80000000 )
    {
      Pa = ( B7 * 2 ) / B4;
    }
    else
    {
      Pa = ( B7 / B4 ) * 2;
    }

    X1 = ( Pa >> 8 ) * ( Pa >> 8 );
    X1 = ( X1 * 3038 ) >> 16;
    X2 = ( -7357 * Pa ) >> 16;

    Pa += ( ( X1 + X2 + 3791 ) >> 4 );

    return ( Pa ) / 100;
  }

  /**
   * Read the barometric pressure (in inches mercury) from the device.
   *
   * @return Current pressure in inches mercury
   * @throws IOException
   */
  public double getPressureInInchesMercury()
    throws IOException
  {
    return getPressureInHPa() * 0.0296;
  }

}

