package icommand.nxt;

import icommand.nxt.comm.NXTProtocol;

/**
 * MTRMX-Nx by Mindsensors.com is a motor multiplexer for RCX motors.
 * 
 * RCXMotorMultiplexer expansion = new RCXMotorMultiplexer(Port.S1); expansion.A.forward();
 * 
 * @author BB
 */
public class RCXMotorMultiplexer extends I2CSensor {

  public RCXMotorMultiplexer.Motor A = new Motor(0);
  public RCXMotorMultiplexer.Motor B = new Motor(1);
  public RCXMotorMultiplexer.Motor C = new Motor(2);
  public RCXMotorMultiplexer.Motor D = new Motor(2);

  private final static byte COMMAND = 0x41;
  private final static byte MOTOR_A_DIR = 0x42;
  private final static byte MOTOR_A_SPEED = 0x43;
  /*
   * Unused constants because each Motor object calculates based on Motor.port private final static
   * byte MOTOR_B_DIR = 0x44; private final static byte MOTOR_B_SPEED = 0x45; private final static
   * byte MOTOR_C_DIR = 0x46; private final static byte MOTOR_C_SPEED = 0x47; private final static
   * byte MOTOR_D_DIR = 0x48; private final static byte MOTOR_D_SPEED = 0x49;
   */
  private final static byte FLT = 0x00;
  private final static byte FORWARD = 0x01;
  private final static byte BACKWARD = 0x02;
  private final static byte BRAKE = 0x03;

  /**
   * Initializes the Multiplexer.
   */
  public RCXMotorMultiplexer(SensorPort s) {
    super(s, NXTProtocol.LOWSPEED_9V);
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

  public class Motor {

    private byte port;
    private int speed;

    public Motor(int port) {
      this.port = (byte) port;
    }

    /**
     * Changes the speed to the RCX motor.
     * 
     * @param speed
     *          Value between 0 and 255 inclusive
     */
    public void setSpeed(int speed) {
      sendData(COMMAND, (byte) (MOTOR_A_SPEED + (port * 0x02)), (byte) speed);
      this.speed = speed;
    }

    public int getSpeed() {
      return this.speed;
    }

    public void backward() {
      // Macro commands for different motors are seperated by
      // 2 so multiply port by 0x02 to get correct motor command
      sendData(COMMAND, (byte) (MOTOR_A_DIR + (port * 0x02)), BACKWARD);
    }

    public void flt() {
      sendData(COMMAND, (byte) (MOTOR_A_DIR + (port * 0x02)), FLT);
    }

    public void forward() {
      sendData(COMMAND, (byte) (MOTOR_A_DIR + (port * 0x02)), FORWARD);
    }

    public void stop() {
      sendData(COMMAND, (byte) (MOTOR_A_DIR + (port * 0x02)), BRAKE);
    }
  }
}