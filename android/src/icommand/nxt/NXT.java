package icommand.nxt;

import icommand.nxt.comm.DeviceInfo;
import icommand.nxt.comm.FirmwareInfo;
import icommand.nxt.comm.NXTCommand;

public class NXT {

  private static NXTCommand nxtCommand = NXTCommand.getSingleton();

  public static float getFirmwareVersion() {
    FirmwareInfo f = nxtCommand.getFirmwareVersion();
    return Float.parseFloat(f.firmwareVersion);
  }

  public static float getProtocolVersion() {
    FirmwareInfo f = nxtCommand.getFirmwareVersion();
    return Float.parseFloat(f.protocolVersion);
  }

  /**
   * 
   * @return Free memory remaining in FLASH
   */
  public static int getFlashMemory() {
    DeviceInfo i = nxtCommand.getDeviceInfo();
    return i.freeFlash;
  }

  /**
   * Deletes all user programs and data in FLASH memory
   * 
   * @return
   */
  public static byte deleteFlashMemory() {
    return nxtCommand.deleteUserFlash();
  }

  public static String getBrickName() {
    DeviceInfo i = nxtCommand.getDeviceInfo();
    return i.NXTname;
  }

  public static byte setBrickName(String newName) {
    return nxtCommand.setBrickName(newName);
  }

  /**
   * This doesn't seem to be implemented in Lego NXT firmware/protocol?
   * 
   * @return Seems to return 0 every time
   */
  public static int getSignalStrength() {
    DeviceInfo i = nxtCommand.getDeviceInfo();
    return i.signalStrength;
  }
}
