package icommand.nxt;

import icommand.nxt.comm.NXTProtocol;

/**
 * HiTechnic color sensor.<br>
 * www.hitechnic.com
 * 
 * @author BB
 */
public class ColorSensor extends I2CSensor {

  /**
   * Creates an instance of the ColorSensor
   * 
   * @param s
   */
  public ColorSensor(SensorPort s) {
    super(s, NXTProtocol.LOWSPEED_9V);
  }

  /**
   * Returns the color index detected by the sensor.
   * 
   * @return Color index.<br>
   *         <li>0 = black <li>1 = violet <li>2 = purple <li>3 = blue <li>4 = green <li>5 = lime <li>
   *         6 = yellow <li>7 = orange <li>8 = red <li>9 = crimson <li>10 = magenta <li>11 to 16 =
   *         pastels <li>17 = white
   */
  public int getColorNumber() {
    byte[] val = getData((byte) 0x42, 1);
    return 0xFF & val[0]; // Convert signed byte to unsigned (positive only)
  }

  /**
   * Returns the red saturation of the color.
   * 
   * @return red value (0 to 255).
   */
  public int getRed() {
    byte[] val = getData((byte) 0x43, 1);
    return 0xFF & val[0]; // Convert signed byte to unsigned (positive only)
  }

  /**
   * Returns the green saturation of the color.
   * 
   * @return green value (0 to 255).
   */
  public int getGreen() {
    byte[] val = getData((byte) 0x44, 1);
    return 0xFF & val[0]; // Convert signed byte to unsigned (positive only)
  }

  /**
   * Returns the blue saturation of the color.
   * 
   * @return blue value (0 to 255).
   */
  public int getBlue() {
    byte[] val = getData((byte) 0x45, 1);
    return 0xFF & val[0]; // Convert signed byte to unsigned (positive only)
  }
}