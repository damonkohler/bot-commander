package icommand.nxt;

import icommand.nxt.comm.NXTProtocol;

// !! Dick Swan mentioned: RCX firmware has a bug that requires a 30 msec 
// delay between messages to reliably work. Seems to work fine though.
// Make private helper method that ensures 30msec has elapsed?

/**
 * TiltSensor by Mindsensors.com. Works with: ACCL-Nx-3g3x UNTESTED: ACCL-Nx-2g2x, ACCL-Nx-5g2x
 * 
 * @author BB
 */
public class TiltSensor extends I2CSensor {

  private static byte X_TILT = 0x42;
  private static byte Y_TILT = 0x43;
  private static byte Z_TILT = 0x44;

  private static byte X_ACCEL_LSB = 0x45;
  private static byte Y_ACCEL_LSB = 0x47;
  private static byte Z_ACCEL_LSB = 0x49;

  public TiltSensor(SensorPort s) {
    super(s, NXTProtocol.LOWSPEED_9V);
  }

  /**
   * Tilt of sensor along X-axis (see top of Mindsensors.com sensor for diagram of axis). 128 is
   * level.
   * 
   * @return
   */
  public int getXTilt() {
    byte[] val = getData(X_TILT, 1);
    return 0xFF & val[0];
  }

  public int getYTilt() {
    byte[] val = getData(Y_TILT, 1);
    return 0xFF & val[0];
  }

  public int getZTilt() {
    byte[] val = getData(Z_TILT, 1);
    return 0xFF & val[0];
  }

  /**
   * Acceleration along X axis. Positive or negative values in mg. (g = acceleration due to gravity
   * = 9.81 m/s^2)
   * 
   * @return Acceleration e.g. 9810 mg (falling on earth)
   */
  public int getXAccel() {
    byte[] vals = getData(X_ACCEL_LSB, 2);
    // System.out.print("LSB = " + vals[0] + "  MSB = " + vals[1] + "   ");
    int accel = (vals[0]) | ((vals[1]) << 8);
    return accel;
  }

  public int getYAccel() {
    byte[] vals = getData(Y_ACCEL_LSB, 2);
    // System.out.print("LSB = " + vals[0] + "  MSB = " + vals[1] + "   ");
    int accel = (vals[0]) | ((vals[1]) << 8);
    return accel;
  }

  public int getZAccel() {
    byte[] vals = getData(Z_ACCEL_LSB, 2);
    // System.out.print("LSB = " + vals[0] + "  MSB = " + vals[1] + "   ");
    int accel = (vals[0]) | ((vals[1]) << 8);
    return accel;
  }

  /**
   * Method to return all acceleration data in one call NOT AS RELIABLE AS GETTING INDIVIDUAL
   * VALUES!!!
   */
  public AccelData getAccelData() {
    byte[] vals = getData(X_TILT, 9);
    AccelData a = new AccelData();
    // Now plug all these values into AccellData object
    a.xTilt = 0xFF & vals[0];
    a.yTilt = 0xFF & vals[1];
    a.zTilt = 0xFF & vals[2];
    a.xAccel = vals[3] | (vals[4]) << 8;
    a.yAccel = vals[5] | (vals[6]) << 8;
    a.zAccel = vals[7] | (vals[8]) << 8;

    return a;
  }

  public class AccelData {
    public int xTilt;
    public int yTilt;
    public int zTilt;

    public int xAccel;
    public int yAccel;
    public int zAccel;
  }
}