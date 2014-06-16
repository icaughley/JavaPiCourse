package github.icaughley.javapicourse.lesson2;

import java.io.IOException;
import java.nio.ByteBuffer;
import jdk.dio.DeviceManager;
import jdk.dio.i2cbus.I2CDevice;
import jdk.dio.i2cbus.I2CDeviceConfig;

public abstract class AbstractI2CDevice
{
  private static final int I2C_BUS_NUMBER = 1;
  private static final int SERIAL_CLOCK_SPEED = 3400000;             // default clock 3.4MHz Max clock
  private static final int ADDRESS_SIZE = 7;
  private final ByteBuffer command = ByteBuffer.allocateDirect( ADDRESS_SIZE );
  private static final int REGISTRY_SIZE = 1;
  private final ByteBuffer byteToRead = ByteBuffer.allocateDirect( 1 );

  private I2CDevice device;
  private int address;

  protected AbstractI2CDevice( int address )
  {
    this.address = address;
    connectToDevice();
  }

  protected I2CDevice getDevice()
  {
    return device;
  }

  /**
   * This method tries to connect to the I2C device, initializing myDevice
   * variable
   */
  private void connectToDevice()
  {
    try
    {
      if ( device == null )
      {
        I2CDeviceConfig config = new I2CDeviceConfig( I2C_BUS_NUMBER, address, ADDRESS_SIZE, SERIAL_CLOCK_SPEED );
        device = DeviceManager.open( config );
      }
      System.out.println( "Connected to the device OK!!!" );
    }
    catch ( IOException e )
    {
      System.out.println( "Error connecting to device" );
    }
  }

  /**
   * This method tries to write one single byte to particular registry
   *
   * @param registry    Registry to write
   * @param byteToWrite Byte to be written
   */
  public void writeOneByte( int registry, byte byteToWrite )
  {
    command.clear();
    command.put( byteToWrite );
    command.rewind();

    try
    {
      device.write( registry, REGISTRY_SIZE, command );
    }
    catch ( IOException e )
    {
      System.out.println( "Error writing registry " + registry );
    }
  }

  /**
   * This method reads one byte from a specified registry address. The method
   * checks that the byte is actually read, otherwise it'll show some messages
   * in the output
   *
   * @param registry Registry to be read
   * @return Byte read from the registry
   */
  public byte readOneByte( int registry )
  {
    byteToRead.clear();
    int result = -1;
    try
    {
      result = device.read( registry, REGISTRY_SIZE, byteToRead );
    }
    catch ( IOException e )
    {
      System.out.println( "Error reading byte" );
    }
    if ( result < 1 )
    {
      System.out.println( "Byte could not be read" );
    }
    else
    {
      byteToRead.rewind();
      return byteToRead.get();
    }
    return 0;
  }

  /**
   * Gracefully close the open I2CDevice
   *
   * @throws IOException
   */
  public void close()
    throws IOException
  {
    device.close();
  }
}
