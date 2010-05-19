package icommand.nxt;

import icommand.nxt.comm.NXTProtocol;

public class LightSensor {

  SensorPort sensor;

  /**
   * LightSensor Port. LED light is on (active) by default.
   * 
   * @param sensor
   */
  public LightSensor(SensorPort sensor) {
    this.sensor = sensor;
    sensor.setTypeAndMode(NXTProtocol.LIGHT_ACTIVE, NXTProtocol.PCTFULLSCALEMODE);
  }

  /**
   * Returns light reading as a percentage.
   * 
   * @return 0 to 100 (0 = dark, 100 = bright)
   */
  public int getLightPercent() {
    return sensor.readScaledValue();
  }

  /**
   * Normalized value.
   * 
   * @return 0 to 1023
   */
  public int getLightValue() {
    return sensor.readNormalizedValue();
  }

  /**
   * Turns off the LED light so it can passively read light values.
   * 
   */
  public void passivate() {
    sensor.setTypeAndMode(NXTProtocol.LIGHT_INACTIVE, NXTProtocol.PCTFULLSCALEMODE);
  }

  /**
   * Turns on the LED light.
   * 
   */
  public void activate() {
    sensor.setTypeAndMode(NXTProtocol.LIGHT_ACTIVE, NXTProtocol.PCTFULLSCALEMODE);
  }
}
