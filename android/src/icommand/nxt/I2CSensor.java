package icommand.nxt;

import icommand.nxt.comm.ErrorMessages;
import icommand.nxt.comm.NXTCommand;
import icommand.nxt.comm.NXTProtocol;

/**
 * A sensor wrapper to allow easy access to I2C sensors, like the ultrasonic sensor. Currently uses
 * the default I2C address of 0x02, but some sensors can be connected to same port lines and use
 * different addresses using the Auto Detecting Parallel Architecture (ADPA). Currently unsure if
 * there are commercial port expanders yet to use this function, or whether the Lego
 * UltrasonicSensor sensor is ADPA compatible.
 * 
 * @author BB
 */
public class I2CSensor {

  // Dick Swan mentions it needs a 30 ms delay between commands to work right,
  // Due to a bug in firmware.

  private static final NXTCommand nxtCommand = NXTCommand.getSingleton();

  protected static byte DEFAULT_ADDRESS = 0x02; // the default I2C address for a port. You can
  // change address of compass sensor (see docs) and
  // then communicate with multiple sensors on same
  // physical port.
  protected static byte STOP = 0x00; // Commands don't seem to use this?

  // Port information (constants)
  /**
   * Returns the version number of the sensor. e.g. "V1.0" Reply length = 8.
   */
  protected static byte VERSION = 0x00;
  /**
   * Returns the product ID of the sensor. e.g. "LEGO" Reply length = 8.
   */
  protected static byte PRODUCT_ID = 0x08;
  /**
   * Returns the sensor type. e.g. "Sonar" Reply length = 8.
   */
  protected static byte SENSOR_TYPE = 0x10;
  /**
   * Returns the "zero position" set at the factory for this sensor. e.g. 0 Reply length = 1.
   * 
   */

  byte port;

  /**
   * 
   * @param s
   *          A sensor. e.g. Port.S1
   */
  public I2CSensor(SensorPort s, byte sensorType) {
    port = (byte) s.getId();
    s.setTypeAndMode(sensorType, NXTProtocol.RAWMODE);
    nxtCommand.LSGetStatus(this.port); // Dick says to flush out data with Poll?
    nxtCommand.LSRead(this.port); // Dick says to flush out data with Poll?
  }

  public int getId() {
    return port;
  }

  /**
   * Method for retrieving data values from the sensor. BYTE0 (icommand.nxtcomm.I2CProtocol) is
   * usually the primary data value for the sensor. Data is read from registers in the sensor,
   * usually starting at 0x00 and ending around 0x49. Just supply the register to start reading at,
   * and the length of bytes to read (16 maximum). NOTE: The NXT supplies UBYTE (unsigned byte)
   * values but Java converts them into signed bytes (probably more practical to return short/int?)
   * 
   * @param register
   *          e.g. FACTORY_SCALE_DIVISOR, BYTE0, etc....
   * @param length
   *          Length of data to read (minimum 1, maximum 16)
   * @return
   */
  protected byte[] getData(byte register, int length) {
    byte[] txData = { DEFAULT_ADDRESS, register };
    nxtCommand.LSWrite(port, txData, (byte) length);

    byte[] status;
    do {
      status = nxtCommand.LSGetStatus(port);
    } while (status[0] == ErrorMessages.PENDING_COMMUNICATION_TRANSACTION_IN_PROGRESS
        | status[0] == ErrorMessages.SPECIFIED_CHANNEL_CONNECTION_NOT_CONFIGURED_OR_BUSY);
    // System.out.println("Error is " + status[0] + "  Data is now ready? " + status[1] +
    // " bytes available.");
    if (status[1] == 0) {
      System.out.println("No bytes to be read in I2CSensor.getData(). Returning 0.");
      return new byte[1];
    }

    byte[] result = nxtCommand.LSRead(port);
    return result;
  }

  /**
   * Helper method to return a single register byte.
   * 
   * @param register
   * @return
   */
  protected byte getData(byte register) {
    return getData(register, 1)[0];
  }

  /**
   * Sets a single byte in the I2C sensor.
   * 
   * @param register
   *          A data register in the I2C sensor. e.g. ACTUAL_ZERO
   * @param value
   *          The data value.
   */
  protected void sendData(byte register, byte value) {
    byte[] txData = { DEFAULT_ADDRESS, register, value };
    nxtCommand.LSWrite(this.port, txData, (byte) 0);
  }

  /**
   * EXPERIMENTAL for RCXLink. Sets two bytes in the I2C sensor.
   * 
   * @param register
   *          A data register in the I2C sensor. e.g. ACTUAL_ZERO
   * @param value
   *          The data value.
   */
  protected void sendData(byte register, byte value1, byte value2) {
    byte[] txData = { DEFAULT_ADDRESS, register, value1, value2 };
    nxtCommand.LSWrite(this.port, txData, (byte) 0);
  }

  /**
   * Returns the version number of the sensor hardware. NOTE: A little unreliable at the moment due
   * to a bug in firmware. Keep trying if it doesn't get it the first time.
   * 
   * @return The version number. e.g. "V1.0"
   */
  public String getVersion() {
    return fetchString(VERSION, 8);
  }

  /**
   * Returns the Product ID as a string. NOTE: A little unreliable at the moment due to a bug in
   * firmware. Keep trying if it doesn't get it the first time.
   * 
   * @return The product ID. e.g. "LEGO"
   */
  public String getProductID() {
    return fetchString(PRODUCT_ID, 8);
  }

  /**
   * Returns the type of sensor as a string. NOTE: A little unreliable at the moment due to a bug in
   * firmware. Keep trying if it doesn't get it the first time.
   * 
   * @return The sensor type. e.g. "Sonar"
   */
  public String getSensorType() {
    return fetchString(SENSOR_TYPE, 8);
  }

  /**
   * Helper method for retrieving string cosntants using I2C protocol.
   * 
   * @param constantEnumeration
   *          e.g. I2CProtocol.VERSION
   * @return
   */
  protected String fetchString(byte constantEnumeration, int rxLength) {
    byte[] stringBytes = getData(constantEnumeration, rxLength);

    // Get rid of everything after 0.
    int zeroPos = 0;
    for (zeroPos = 0; zeroPos < stringBytes.length; zeroPos++) {
      if (stringBytes[zeroPos] == 0)
        break;
    }
    String s = new String(stringBytes).substring(0, zeroPos);
    return s;
  }
}