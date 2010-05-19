package icommand.nxt;

import icommand.nxt.comm.NXTProtocol;

/**
 * Currently supports Mindsensors.com Compass V1.1 and 2.0 (CMPS-Nx) and HiTechnic compass sensors.
 * Auto-detects appropriate model.
 * 
 * @author BB
 * 
 */
public class CompassSensor extends I2CSensor {
  // !!If there are differences between Mindsensors compass and Hitechnic
  // compass, detect product ID first and then act appropriately.

  // Mindsensors Compass Commands (from www.mindsensors.com):
  private final static byte AUTO_TRIG_ON = 0x41; // On by default
  private final static byte AUTO_TRIG_OFF = 0x53;
  private final static byte BYTE_MODE = 0x42;
  private final static byte INTEGER_MODE = 0x49;
  public final static byte USA_MODE = 0x55; // 60 Hz
  public final static byte EU_MODE = 0x45; // 50 Hz
  private final static byte ADPA_ON = 0x4E;
  private final static byte ADPA_OFF = 0x4F; // Off by default
  private final static byte BEGIN_CALIBRATION = 0x43;
  private final static byte END_CALIBRATION = 0x44;
  private final static byte LOAD_USER_CALIBRATION = 0x4C;

  // Mindsensors Compass Registers:
  // private final static byte NOTHING = 0x40; ?? Listed in Mindsensors docs
  private final static byte COMMAND = 0x41;
  private final static byte HEADING_LSB = 0x42;
  private final static byte HEADING_MSB = 0x43;
  // !! There are a bunch of calibration values I don't think I'll bother with.

  private double cartesianCalibrate = 0; // Used by both cartesian methods.

  private static final String MINDSENSORS_ID = "mndsnsrs";
  private boolean isMindsensors = false;

  /**
   * Initializes a Compass using USA mode by default.
   */
  public CompassSensor(SensorPort s) {
    super(s, NXTProtocol.LOWSPEED_9V);

    isMindsensors = (this.getProductID().equals(MINDSENSORS_ID));

    if (isMindsensors) {
      // Set to USA or EU household current frequency
      setRegion(USA_MODE);

      // Set mode as Integer compassing mode:
      super.sendData(COMMAND, INTEGER_MODE);
    }
  }

  /**
   * Mindsensors only. If you are using the compass in a building that uses household current, you
   * will get better readings by setting the frequency of your power (Europe = 50Hz, USA = 60Hz).
   * (USA 60Hz is default)
   * 
   * @param region
   *          Use Compass.USA_MODE or Compass.EU_MODE
   */
  public void setRegion(byte region) {
    super.sendData(COMMAND, region);
  }

  /**
   * Begins calibrating the compass sensor resulting in more accurate measurements. Rotate compass
   * at least two times, taking at least 20 seconds per rotation. Issue stopCalibration() when done.
   * NOTE: Once the compass is calibrated, you do not have to recalibrate even when the NXT is
   * turned off. The calibration settings are stored in internal non-volatile memory on the NXT.
   * 
   */
  public void startCalibration() {
    super.sendData(COMMAND, BEGIN_CALIBRATION);
  }

  /**
   * @see startCalibration()
   * 
   */
  public void stopCalibration() {
    super.sendData(COMMAND, END_CALIBRATION);
  }

  /**
   * Returns the directional heading in degrees. (0 to 2Pi) 0 is due North (on Mindsensors circuit
   * board a white arrow indicates the direction of compass). Reading increases clockwise.
   * 
   * @return Heading in radians.
   */
  public double getRadians() {
    return (getDegrees() / 360d) * 2 * Math.PI;
  }

  /**
   * Returns the directional heading in degrees. (0 to 359.9) 0 is due North (on Mindsensors circuit
   * board a white arrow indicates the direction of compass). Reading increases clockwise.
   * 
   * @return Heading in degrees. Resolution is within 0.1 degrees
   */
  public double getDegrees() {
    byte[] buf = getData(HEADING_LSB, 2);
    // System.out.print("LSB = " + vals[0] + "  MSB = " + vals[1] + "   ");

    if (isMindsensors) { // Check if this is mindsensors
      // NOTE: The following only works when Mindsensors compass in integer mode
      int iHeading = (0xFF & buf[0]) | ((0xFF & buf[1]) << 8);
      float dHeading = iHeading / 10.00F;
      return dHeading;
    } else { // HiTechnic
      return ((buf[0] & 0xff) << 1) + buf[1];
    }
  }

  /**
   * Compass readings increase clockwise from 0 to 360, but Cartesian coordinate systems increase
   * counter-clockwise. This method returns the Cartesian compass reading. Also, the
   * resetCartesianZero() method can be used to designate any direction as zero, rather than relying
   * on North as being zero.
   * 
   * @return Cartesian direction.
   */
  public double getDegreesCartesian() {
    double degrees = 360 - getDegrees() - cartesianCalibrate;
    if (degrees >= 360)
      degrees -= 360;
    if (degrees < 0)
      degrees += 360;
    return degrees;
  }

  /**
   * Changes the current direction the compass is facing into the zero angle.
   * 
   */
  public void resetCartesianZero() {
    cartesianCalibrate = getDegrees();
  }

  /**
   * Override method because of unreliability retrieving more than a single byte at a time with some
   * I2C Sensors (bug in Lego firmware). Note: This is slower because it takes more Bluetooth calls.
   * 
   * @param register
   *          e.g. FACTORY_SCALE_DIVISOR, BYTE0, etc....
   * @param length
   *          Length of data to read (minimum 1, maximum 16)
   * @return
   */
  protected byte[] getData(byte register, int length) {

    byte[] result = new byte[length];
    for (int i = 0; i < length; i++)
      result[i] = super.getData((byte) (register + i), 1)[0];

    return result;
  }
}
