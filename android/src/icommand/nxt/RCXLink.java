package icommand.nxt;

import icommand.nxt.comm.NXTProtocol;

// !! Commands seem to return values? Ones like check battery are
// necessary to return values. Maybe need to do some RX/TX stuff?

// !! Dick Swan mentioned: RCX firmware has a bug that requires a 30 msec
// delay between messages to reliably work. Seems to work fine though.
// Make private helper method that ensures 30msec has elapsed?

/**
 * NRLink by Mindsensors.com is a bridge between NXT and RCX bricks. The adapter allows the NXT to
 * control the motors on an RCX brick (not sensors) via the IR port on the RCX. e.g. RCXLink rcx =
 * new RCXLink(Port.S1); rcx.A.forward();
 * 
 * @author BB
 */
public class RCXLink extends I2CSensor {

  public RCXLink.Motor A = new Motor(0);
  public RCXLink.Motor B = new Motor(1);
  public RCXLink.Motor C = new Motor(2);

  private final static byte SLOW_SPEED = 0x44; // Default 2400 baud
  private final static byte FLUSH = 0x46; // Flush the FIFO buffer
  private final static byte HIGH_SPEED = 0x48; // 4800 baudprivate final static byte LONG_RANGE =
  // 0x4C;
  private final static byte SHORT_RANGE = 0x53;
  private final static byte LONG_RANGE = 0x4c;
  private final static byte TRANSMIT_MACRO = 0x55; // Transmit Unassembled raw macro data;
  private final static byte COMMAND = 0x41;
  private final static byte RUN = 0x52;

  // ROM Macro Definitions:
  public final static byte SHORT_RANGE_IR = 0x01;
  public final static byte LONG_RANGE_IR = 0x04;
  public final static byte POWER_OFF_RCX = 0x07;
  public final static byte RUN_PROGRAM_1 = 0x09;
  public final static byte RUN_PROGRAM_2 = 0x0D;
  public final static byte RUN_PROGRAM_3 = 0x11;
  public final static byte RUN_PROGRAM_4 = 0x15;
  public final static byte RUN_PROGRAM_5 = 0x19;
  public final static byte STOP_ALL_PROGRAMS = 0x1D;
  public final static byte MOTOR_A_FORWARD = 0x21;
  public final static byte MOTOR_A_REVERSED = 0x25;
  public final static byte MOTOR_B_FORWARD = 0x29;
  public final static byte MOTOR_B_REVERSED = 0x2D;
  public final static byte MOTOR_C_FORWARD = 0x31;
  public final static byte MOTOR_C_REVERSED = 0x35;
  /**
   * NOTE: The BEEP macro is very unreliable. It seems to work one time and then stop working.
   */
  public final static byte BEEP = 0x39;

  // EPROM Macro Definitions:
  public final static byte EPROM_MOTOR_A_ON = 0x50;
  public final static byte EPROM_MOTOR_A_FORWARD = 0x53;
  public final static byte EPROM_MOTOR_A_REVERSED = 0x56;
  public final static byte EPROM_MOTOR_A_FLIP_DIRECTION = 0x59;
  public final static byte EPROM_MOTOR_A_OFF = 0x5C;
  public final static byte EPROM_MOTOR_B_ON = 0x5F;
  public final static byte EPROM_MOTOR_B_FORWARD = 0x62;
  public final static byte EPROM_MOTOR_B_REVERSED = 0x65;
  public final static byte EPROM_MOTOR_B_FLIP_DIRECTION = 0x68;
  public final static byte EPROM_MOTOR_B_OFF = 0x6B;
  public final static byte EPROM_MOTOR_C_ON = 0x6E;
  public final static byte EPROM_MOTOR_C_FORWARD = 0x71;
  public final static byte EPROM_MOTOR_C_REVERSED = 0x74;
  public final static byte EPROM_MOTOR_C_FLIP_DIRECTION = 0x77;
  public final static byte EPROM_MOTOR_C_OFF = 0x7A;
  public final static byte EPROM_GET_BATTERY_POWER = 0x7D;

  /**
   * Initializes the RCXLink.
   */
  public RCXLink(SensorPort s) {
    super(s, NXTProtocol.LOWSPEED_9V);
  }

  /**
   * Runs a program on the RCX.
   * 
   * @param programNumber
   *          1-5
   */
  public void runProgram(int programNumber) {
    --programNumber;
    runMacro((byte) (RUN_PROGRAM_1 + (programNumber * 4)));
  }

  /**
   * Stops any currently executing programs on the RCX.
   * 
   */
  public void stopAllPrograms() {
    runMacro(STOP_ALL_PROGRAMS);
  }

  /**
   * Sends command to turn off the RCX brick.
   * 
   */
  public void powerOff() {
    runMacro(POWER_OFF_RCX);
  }

  /**
   * Makes the RCX chirp. NOTE: Unreliable. Works maybe once then stops working.
   * 
   */
  public void beep() {
    runMacro(BEEP);
  }

  /**
   * Sets the range of the IR light on the RCXLink. Long range uses 25mA, short range 15mA.
   * 
   * @param longrange
   *          true = long range, false = short range
   */
  public void setLongRange(boolean longrange) {
    if (longrange)
      super.sendData(COMMAND, LONG_RANGE);
    else
      super.sendData(COMMAND, SHORT_RANGE);
  }

  /**
   * Sets the communications speed of the RCXLink.
   * 
   * @param highspeed
   *          true = 4800 baud, false = 2400 baud (default)
   */
  public void setHighSpeed(boolean highspeed) {
    if (highspeed)
      super.sendData(COMMAND, HIGH_SPEED);
    else
      super.sendData(COMMAND, SLOW_SPEED);
  }

  /**
   * Transmit Unassembled raw macro data. Up to 175 macro commands may be sent to the RCX. NOT
   * IMPLEMENTED
   */
  public void sendMacro(byte[] macroCommands) {

  }

  /**
   * Run the ROM/EEPROM macro at address 0xXX
   * 
   */
  public void runMacro(byte macro) {
    super.sendData(COMMAND, RUN, macro);
  }

  /**
   * Flush the FIFO (First In First Out) buffer
   * 
   */
  public void flush() {
    super.sendData(COMMAND, FLUSH);
  }

  /**
   * Test method to retrieve macro data.
   * 
   * @return All 175 bytes right now
   */
  public byte[] getMacroData() {
    System.out.print("(Getting " + (0xFF - 0x50) + " values.)  ");
    return getData((byte) 0x50, (0xFF - 0x50));
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

  public class Motor {

    private byte port;

    public Motor(int port) {
      this.port = (byte) port;
    }

    public void backward() {
      // Macro commands for different motors are seperated by
      // 15 (0x0F) so multiply port by 0x0F to get correct macro
      runMacro((byte) (RCXLink.EPROM_MOTOR_A_REVERSED + (port * 0x0F)));
      runMacro((byte) (RCXLink.EPROM_MOTOR_A_ON + (port * 0x0F)));

    }

    public void forward() {
      runMacro((byte) (RCXLink.EPROM_MOTOR_A_FORWARD + (port * 0x0F)));
      runMacro((byte) (RCXLink.EPROM_MOTOR_A_ON + (port * 0x0F)));

    }

    public void stop() {
      runMacro((byte) (RCXLink.EPROM_MOTOR_A_OFF + (port * 0x0F)));
    }
  }
}