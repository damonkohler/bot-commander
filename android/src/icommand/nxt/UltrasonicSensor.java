package icommand.nxt;

import icommand.nxt.comm.NXTProtocol;

public class UltrasonicSensor extends I2CSensor {

  private static byte FACTORY_ZERO = 0x11;
  /**
   * Reply length = 1
   */
  private static byte FACTORY_SCALE_FACTOR = 0x12;
  /**
   * Reply length = 1
   */
  private static byte FACTORY_SCALE_DIVISOR = 0x13;
  /**
   * e.g. "10E-2M" Reply length = 7
   */
  private static byte MEASUREMENT_UNITS = 0x14;

  // Port information (variables)
  /**
   * Used as command or request with reply length = 1
   */
  private static byte CONTINUOUS_MEASUREMENT_INTERVAL = 0x40;
  /**
   * Used as command or request with reply length = 1
   */
  private static byte COMMAND_STATE = 0x41; // Command or reply length = 1
  // The following 8 registers are the echo's that the ultrasound returns.
  private static byte BYTE0 = 0x42; // reply length = 1
  private static byte BYTE1 = 0x43; // reply length = 1
  private static byte BYTE2 = 0x44; // reply length = 1
  private static byte BYTE3 = 0x45; // reply length = 1
  private static byte BYTE4 = 0x46; // reply length = 1
  private static byte BYTE5 = 0x47; // reply length = 1
  private static byte BYTE6 = 0x48; // reply length = 1
  private static byte BYTE7 = 0x49; // reply length = 1
  /**
   * Used as command or request with reply length = 1
   */
  private static byte ACTUAL_ZERO = 0x50;
  /**
   * Used as command or request with reply length = 1
   */
  private static byte ACTUAL_SCALE_FACTOR = 0x51;
  /**
   * Used as command or request with reply length = 1
   */
  private static byte ACTUAL_SCALE_DIVISOR = 0x52;

  // Port States (used with COMMAND_STATE to set state, or return values for reading COMMAND_STATE)
  private static byte OFF = 0x00;
  /**
   * Sonar sensor sends out one ping and records result.
   */
  private static byte SINGLE_SHOT = 0x01;
  /**
   * Sonar sensor making continuous readings at set intervals.
   */
  private static byte CONTINUOUS_MEASUREMENT = 0x02;
  /**
   * Puts the sonal sensor in passive mode to record pings that might come from other sonar sensors.
   */
  private static byte EVENT_CAPTURE = 0x03;
  /**
   * Apparently this resets the sensor.
   */
  private static byte REQUEST_WARM_RESET = 0x04;

  /**
   * Sets the sensor to LOWSPEED_9V and puts it into continuous measurement.
   * 
   * @param s
   */
  public UltrasonicSensor(SensorPort s) {
    super(s, NXTProtocol.LOWSPEED_9V);

    // Set in continuous mode:
    // this.setSensorMode(CONTINUOUS_MEASUREMENT); // Delete me
  }

  /**
   * Sets the state for the sensor. e.g. off, single pulse, continuous, passive, warm reset.
   * 
   * @param modeEnumeration
   *          e.g. OFF, SINGLE_SHOT, CONTINUOUS_MEASUREMENT, EVENT_CAPTURE, REQUEST_WARM_RESET
   */
  public void setSensorMode(byte modeEnumeration) {
    this.sendData(COMMAND_STATE, modeEnumeration);
  }

  /**
   * Returns the distance to the closest object.
   * 
   * @return Default value is in centimeters.
   */
  public int getDistance() {
    // issue single shot command:
    this.setSensorMode(SINGLE_SHOT);
    byte[] val = getData(BYTE0, 1);
    return 0xFF & val[0]; // Convert signed byte to unsigned (positive only)
  }

  /**
   * Determines if the sensor produces distances in cm or inches. NOT CURRENTLY IMPLEMENTED (cm
   * default)
   * 
   * @param isMetric
   *          true = cm's, false = inches
   */
  public void setMetric(boolean isMetric) {

  }

  /**
   * The ultrasonic sensor is capable of sending a ping and recording up to 8 echoes. A SINGLE_SHOT
   * command is used, rather than continuous pings. This method returns an array of up to 8
   * distances picked up by the UltrasonicSensor sensor.
   * 
   * @return eight distance values, mostly will be 0.
   */
  public byte[] getDistances() {
    // issue single shot command:
    this.setSensorMode(SINGLE_SHOT);
    // !!Note: Does this leave enough time for the UltrasonicSensor sensor
    // !!to collect all the returned pings and store them? Might need to
    // !!wait some time for the sound waves to collect.
    return getData(BYTE0, 8);
  }

  /**
   * The time interval between measurements in continuous mode.
   * 
   * @return Value 0-255 in MS
   */
  public int getMeasurementInterval() {
    byte val = getData(CONTINUOUS_MEASUREMENT_INTERVAL, 1)[0];
    return (0xFF & val);
  }

  /**
   * Sets the scale factor, producing either cm's, inches, or some other unit.
   * 
   * @param scale
   *          Can be a number 0 to 255 likely. Need to use short.
   */
  public void setScaleFactor(byte scale) {
    sendData(ACTUAL_SCALE_FACTOR, scale);
  }

  public void setZero(byte zeroPoint) {
    sendData(ACTUAL_ZERO, zeroPoint);
  }

  public void setScaleDivisor(byte divisor) {
    sendData(ACTUAL_SCALE_DIVISOR, divisor);
  }

  /**
   * If your sensor readings seem off, you can calibrate the sensor to make it different from
   * factory settings.
   * 
   * @param scaleFactor
   *          Raw reading is multiplied by this number. 1 = factory setting.
   * @param scaleDivisor
   *          Raw reading divides by this number. 14 = factory setting
   * @param zero
   *          e.g. If you find the real distance is 10 cm and the sensor says 12 cm you can adjust
   *          the zero. 0 = factory setting.
   */
  public void calibrate(byte scaleFactor, byte scaleDivisor, byte zero) {
    setScaleFactor(scaleFactor);
    setScaleDivisor(scaleDivisor);
    setZero(zero);
  }

  /**
   * Gets the scale factor, which produces either cm's, inches, or some other unit.
   * 
   * @return 0-255
   */
  public int getScaleFactor() {
    byte[] val = getData(ACTUAL_SCALE_FACTOR, 1);
    return (0xFF & val[0]);
  }

  /**
   * Gets the default scale factor, which produces either cm's, inches, or some other unit.
   * 
   * @return 0-255
   */
  public int getFactoryScaleFactor() {
    byte[] val = getData(FACTORY_SCALE_FACTOR, 1);
    return (0xFF & val[0]);
  }

  /**
   * Gets the default scale factor, which produces either cm's, inches, or some other unit.
   * 
   * @return 0-255
   */
  public int getFactoryScaleDivisor() {
    byte[] val = getData(FACTORY_SCALE_DIVISOR, 1);
    return (0xFF & val[0]);
  }

  /**
   * Gets the default "zero" point, which is 0.
   */
  public int getFactoryZero() {
    byte[] val = getData(FACTORY_ZERO, 1);
    return (0xFF & val[0]);
  }

  /**
   * 
   * @param interval
   *          !! Can be a number 0 to 255 likely. Need to use short.
   */
  public void setMeasurementInterval(byte interval) {
    sendData(CONTINUOUS_MEASUREMENT_INTERVAL, interval);
  }

  /**
   * Returns some sort of string that indicates measurement units. NOTE: A little unreliable at the
   * moment due to a bug in firmware. Keep trying if it doesn't get it the first time.
   * 
   * @return e.g. "10E-2m"
   */
  public String getMeasurementUnits() {
    return fetchString(MEASUREMENT_UNITS, 7);
  }
}