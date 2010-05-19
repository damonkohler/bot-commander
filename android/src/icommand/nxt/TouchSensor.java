package icommand.nxt;

import icommand.nxt.comm.NXTProtocol;

/**
 * TouchSensor Port class.
 * 
 * @author BB
 * 
 */
public class TouchSensor {

  SensorPort sensor;

  public TouchSensor(SensorPort sensor) {
    this.sensor = sensor;
    sensor.setTypeAndMode(NXTProtocol.SWITCH, NXTProtocol.BOOLEANMODE);
  }

  /**
   * 
   * @return true if sensor is pressed.
   */
  public boolean isPressed() {
    return (sensor.readScaledValue() == 1);
  }
}
