package icommand.nxt;

import icommand.nxt.comm.InputValues;
import icommand.nxt.comm.NXTCommand;
import icommand.nxt.comm.NXTProtocol;

/**
 * Port class. Contains 4 Port instances.<br>
 * Usage: Port.S4.readValue();
 * 
 * @author <a href="mailto:bbagnall@mts.net">Brian Bagnall</a>
 * @version 0.3 29-October-2006
 * 
 */
public class SensorPort implements NXTProtocol {

  private static final NXTCommand nxtCommand = NXTCommand.getSingleton();

  private int id;

  public static SensorPort S1 = new SensorPort(0);
  public static SensorPort S2 = new SensorPort(1);
  public static SensorPort S3 = new SensorPort(2);
  public static SensorPort S4 = new SensorPort(3);

  private SensorPort(int port) {
    id = port;
  }

  public int getId() {
    return id;
  }

  public void setTypeAndMode(int type, int mode) {
    nxtCommand.setInputMode(id, type, mode);
  }

  /**
   * Reads the boolean value of the sensor.
   * 
   * @return Boolean value of sensor.
   */
  public boolean readBooleanValue() {
    InputValues vals = nxtCommand.getInputValues(id);
    // I thought open sensor would produce 0 value. My UWORD conversion wrong?
    return (vals.rawADValue < 500);
  }

  /**
   * Reads the raw value of the sensor.
   * 
   * @return Raw sensor value. Range is device dependent.
   */
  public int readRawValue() {
    InputValues vals = nxtCommand.getInputValues(id);
    return vals.rawADValue;
  }

  /**
   * Reads the normalized value of the sensor.
   * 
   * @return Normalized value. 0 to 1023
   */
  public int readNormalizedValue() {
    InputValues vals = nxtCommand.getInputValues(id);
    return vals.normalizedADValue;
  }

  /**
   * Returns scaled value, depending on mode of sensor. e.g. BOOLEANMODE returns 0 or 1. e.g.
   * PCTFULLSCALE returns 0 to 100.
   * 
   * @return
   * @see SensorPort#setTypeAndMode(int, int)
   */
  public int readScaledValue() {
    InputValues vals = nxtCommand.getInputValues(id);
    return vals.scaledValue;
  }
}
