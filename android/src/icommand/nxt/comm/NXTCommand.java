package icommand.nxt.comm;

import java.io.UnsupportedEncodingException;

import com.googlecode.botcommander.AndroidComm;

/**
 * NXTCommand contains easily accessible commands for the Lego NXT.
 * 
 * @author <a href="mailto:bbagnall@mts.net">Brian Bagnall</a>
 * @version 0.3 23-August-2006
 * 
 */
public class NXTCommand implements NXTProtocol {

  private static NXTCommand singleton = new NXTCommand();

  private NXTComm nxtComm;

  private static boolean verifyCommand = false;

  // Ensure no one tries to instantiate this.
  private NXTCommand() {
  }

  /**
   * Starts a program already on the NXT. UNTESTED
   * 
   * @param fileName
   * @return
   */
  public byte startProgram(String fileName) {
    byte[] request = { DIRECT_COMMAND_NOREPLY, START_PROGRAM };
    request = appendString(request, fileName);
    return sendRequest(request);
  }

  /**
   * Forces the currently executing program to stop. UNTESTED
   * 
   * @return Error value
   */
  public byte stopProgram() {
    byte[] request = { DIRECT_COMMAND_NOREPLY, STOP_PROGRAM };
    return sendRequest(request);
  }

  /**
   * Name of current running program. !! Leaves null character at end. UNTESTED
   * 
   * @return
   */
  public String getCurrentProgramName() {
    byte[] request = { DIRECT_COMMAND_REPLY, GET_CURRENT_PROGRAM_NAME };
    nxtComm.sendData(request);
    byte[] reply = nxtComm.readData();

    return new StringBuffer(new String(reply)).delete(0, 2).toString();
  }

  /**
   * Opens a file on the NXT for reading. Returns a handle number and file size, enclosed in a
   * FileInfo object.
   * 
   * @param fileName
   *          e.g. "Woops.rso"
   * @return
   */

  // !! Something might be wrong with this because I can't run a program
  // twice in a row after calling this. But other calls work after this.
  public FileInfo openRead(String fileName) {
    byte[] request = { SYSTEM_COMMAND_REPLY, OPEN_READ };
    request = appendString(request, fileName); // No padding required apparently
    nxtComm.sendData(request);
    byte[] reply = nxtComm.readData();
    FileInfo fileInfo = new FileInfo(fileName);
    fileInfo.status = reply[2];
    if (reply.length > 3) { // Check if all data included in reply
      fileInfo.fileHandle = reply[3];
      fileInfo.fileSize =
          (0xFF & reply[4]) | ((0xFF & reply[5]) << 8) | ((0xFF & reply[6]) << 16)
              | ((0xFF & reply[7]) << 24);
    }
    return fileInfo;
  }

  /**
   * Opens a file on the NXT for writing. UNFINISHED UNTESTED
   * 
   * @param fileName
   *          e.g. "Woops.rso"
   * @return File Handle number
   */
  public byte openWrite(String fileName, int size) {
    byte[] command = { SYSTEM_COMMAND_REPLY, OPEN_WRITE };
    // !! If filename longer than 15.3 need function to shrink name (NXT only).
    byte[] encFileName = null;
    try {
      encFileName = AsciizCodec.encode(fileName);
    } catch (UnsupportedEncodingException e) {
      System.err.println("Illegal characters in filename");
      return -1;
    }
    command = appendBytes(command, encFileName);
    byte[] request = new byte[22];
    System.arraycopy(command, 0, request, 0, command.length);
    byte[] fileLength =
        { (byte) size, (byte) (size >>> 8), (byte) (size >>> 16), (byte) (size >>> 24) };
    request = appendBytes(request, fileLength);
    nxtComm.sendData(request);
    byte[] reply = nxtComm.readData();
    return reply[3]; // The handle number
  }

  /**
   * Returns requested number of bytes from a file. File must first be opened using the openRead()
   * command.
   * 
   * @param handle
   *          File handle number (from openRead method)
   * @param length
   *          Number of bytes to read.
   * @return
   */
  public byte[] readFile(byte handle, int length) {
    byte[] request = { SYSTEM_COMMAND_REPLY, READ, handle, (byte) length, (byte) (length >>> 8) };
    nxtComm.sendData(request);
    byte[] reply = nxtComm.readData();
    byte[] data = new byte[reply.length - 6];
    // Copy data into array:
    // !! * Could crash due to receiving no data array. Test with bad handle.
    System.arraycopy(reply, 6, data, 0, data.length);
    return data; // The handle number
  }

  public byte writeFile(byte handle, byte[] data) {
    byte[] request = new byte[data.length + 3];
    byte[] command = { SYSTEM_COMMAND_NOREPLY, WRITE, handle };
    System.arraycopy(command, 0, request, 0, command.length);
    System.arraycopy(data, 0, request, 3, data.length);

    // The code below is repetative but can't use sendRequest() because SYSTEM_COMMAND being used
    byte verify = 0; // default of 0 means success
    if (verifyCommand)
      request[0] = SYSTEM_COMMAND_REPLY;
    nxtComm.sendData(request);
    if (verifyCommand) {
      byte[] reply = nxtComm.readData();
      verify = reply[2];
      // Next line can be used to confirm if data written:
      // int bytesWritten = (0xFF & reply[4]) | ((0xFF & reply[5]) << 8);
    }

    return verify;
  }

  /**
   * Closes an open file.
   * 
   * @param handle
   *          File handle number.
   * @return Error code 0 = success
   */
  public byte closeFile(byte handle) {
    byte[] request = { SYSTEM_COMMAND_NOREPLY, CLOSE, handle };

    // The code below is repetative but can't use sendRequest() because SYSTEM_COMMAND being used
    byte verify = 0; // default of 0 means success
    if (verifyCommand)
      request[0] = SYSTEM_COMMAND_REPLY;
    nxtComm.sendData(request);
    if (verifyCommand) {
      byte[] reply = nxtComm.readData();
      verify = reply[2];
    }

    return verify;
  }

  public byte delete(String fileName) {

    byte[] request = { SYSTEM_COMMAND_REPLY, DELETE };
    // !! Below, could use String concat function (str1 + str2)
    request = appendString(request, fileName);
    // request = (new String(request) + fileName).getBytes();

    // !! Below should be a method shared by System Commands and Direct Commands.
    nxtComm.sendData(request);
    byte[] reply = nxtComm.readData();
    return reply[2];
  }

  public FirmwareInfo getFirmwareVersion() {
    byte[] request = { SYSTEM_COMMAND_REPLY, GET_FIRMWARE_VERSION };
    nxtComm.sendData(request);
    byte[] reply = nxtComm.readData();
    FirmwareInfo info = new FirmwareInfo();
    info.status = reply[2];
    if (info.status == 0) {
      info.protocolVersion = new String(reply[4] + "." + reply[3]);
      info.firmwareVersion = new String(reply[6] + "." + reply[5]);
    } else
      System.out.println("Status = " + info.status);
    return info;
  }

  public byte setBrickName(String name) {
    byte[] request = { SYSTEM_COMMAND_REPLY, START_PROGRAM };
    request = appendString(request, name);
    nxtComm.sendData(request);
    byte[] reply = nxtComm.readData();
    return reply[2];
  }

  /*
   * BT Address still needs to be cleaned up to display unsigned bytes.
   */
  public DeviceInfo getDeviceInfo() {
    // !! Needs to check port to verify they are correct ranges.
    byte[] request = { SYSTEM_COMMAND_REPLY, GET_DEVICE_INFO };
    nxtComm.sendData(request);
    byte[] reply = nxtComm.readData();
    DeviceInfo d = new DeviceInfo();
    d.status = reply[2];
    d.NXTname = new StringBuffer(new String(reply)).delete(18, 33).delete(0, 3).toString();
    d.bluetoothAddress =
        Integer.toHexString(reply[18]) + ":" + Integer.toHexString(reply[19]) + ":"
            + Integer.toHexString(reply[20]) + ":" + Integer.toHexString(reply[21]) + ":"
            + Integer.toHexString(reply[22]) + ":" + Integer.toHexString(reply[23]) + ":"
            + Integer.toHexString(reply[24]);
    d.signalStrength =
        (0xFF & reply[25]) | ((0xFF & reply[26]) << 8) | ((0xFF & reply[27]) << 16)
            | ((0xFF & reply[28]) << 24);
    d.freeFlash =
        (0xFF & reply[29]) | ((0xFF & reply[30]) << 8) | ((0xFF & reply[31]) << 16)
            | ((0xFF & reply[32]) << 24);
    return d;
  }

  /**
   * Deletes user flash memory (not including system modules). UNTESTED
   * 
   * @return
   */
  public byte deleteUserFlash() {
    byte[] request = { SYSTEM_COMMAND_REPLY, DELETE_USER_FLASH };
    nxtComm.sendData(request);
    byte[] reply = nxtComm.readData();
    return reply[2];
  }

  /**
   * Returns the number of bytes for a command in the low-speed buffer or the high-speed buffer (0 =
   * no command is ready). UNTESTED
   * 
   * @param bufferNumber
   *          0 = poll buffer (low-speed) 1 = high-speed buffer
   * @return
   */
  public byte pollLength(byte bufferNumber) {
    byte[] request = { SYSTEM_COMMAND_REPLY, POLL_LENGTH, bufferNumber };
    nxtComm.sendData(request);
    byte[] reply = nxtComm.readData();
    return reply[4];
  }

  /**
   * Reads bytes from the low-speed or high-speed buffer. UNTESTED
   * 
   * @param bufferNumber
   *          0 = poll buffer (low-speed) 1 = high-speed buffer
   * @param commandLength
   *          Number of bytes obtained from pollLength()
   * @return
   */
  public byte[] poll(byte bufferNumber, byte commandLength) {
    byte[] request = { SYSTEM_COMMAND_REPLY, POLL, bufferNumber, commandLength };
    nxtComm.sendData(request);
    byte[] reply = nxtComm.readData();
    byte replyCommandLength = reply[4];
    byte[] replyPollCommand = new byte[replyCommandLength];
    if (reply[3] == 0) {
      System.out.println("Poll length is : " + replyCommandLength);
      System.out.println("Array length is : " + reply.length
          + " (should be 4 less than Poll length).");
      System.arraycopy(reply, 5, replyPollCommand, 0, replyCommandLength);
    } // else return single byte with error? Does it sometimes return 1 byte?
    return replyPollCommand;
  }

  /**
   * When no files exist within the system, an error message is returned in the package saying
   * "File not found". When this command returns a success, a close command is required for "closing
   * the handle" within the brick when handle is not needed anymore. If an error is returned, the
   * firmware will close the handle automatically.
   * 
   * @param wildCard
   *          [filename].[extension], *.[extension], [filename].*, *.*
   * @return
   */
  public FileInfo findFirst(String wildCard) {

    byte[] request = { SYSTEM_COMMAND_REPLY, FIND_FIRST };
    // !! Below, could use String concat function (str1 + str2)
    request = appendString(request, wildCard);
    // request = (new String(request) + fileName).getBytes();

    // !! Below should be a method shared by System Commands and Direct Commands.
    nxtComm.sendData(request);
    byte[] reply = nxtComm.readData();
    FileInfo fileInfo = null;
    if (reply[2] == 0) {
      fileInfo = new FileInfo("");
      fileInfo.status = reply[2];
      if (reply.length > 3) { // Check if all data included in reply
        fileInfo.fileHandle = reply[3];
        StringBuffer name = new StringBuffer(new String(reply)).delete(24, 27).delete(0, 4);
        int lastPos = name.indexOf(".") + 4; // find . in filename, index of last char.
        name.delete(lastPos, name.length());
        fileInfo.fileName = name.toString();
        fileInfo.fileSize =
            (0xFF & reply[24]) | ((0xFF & reply[25]) << 8) | ((0xFF & reply[26]) << 16)
                | ((0xFF & reply[27]) << 24);
      }
    }
    return fileInfo;
  }

  /**
   * When no files exist within the system, an error message is returned in the package saying
   * "File not found". When this command returns a success, a close command is required for "closing
   * the handle" within the brick when handle is not needed anymore. If an error is returned, the
   * firmware will close the handle automatically.
   * 
   * @param handle
   *          Handle number from the previous found file or fromthe Find First command.
   * @return
   */
  public FileInfo findNext(byte handle) {

    byte[] request = { SYSTEM_COMMAND_REPLY, FIND_NEXT, handle };

    // !! Below should be a method shared by System Commands and Direct Commands.
    nxtComm.sendData(request);
    byte[] reply = nxtComm.readData();
    FileInfo fileInfo = null;
    if (reply[2] == 0) {
      fileInfo = new FileInfo("");
      fileInfo.status = reply[2];
      if (reply.length > 3) { // Check if all data included in reply
        fileInfo.fileHandle = reply[3];
        StringBuffer name = new StringBuffer(new String(reply)).delete(24, 27).delete(0, 4);
        int lastPos = name.indexOf(".") + 4; // find . in filename, index of last char.
        name.delete(lastPos, name.length());
        fileInfo.fileName = name.toString();
        fileInfo.fileSize =
            (0xFF & reply[24]) | ((0xFF & reply[25]) << 8) | ((0xFF & reply[26]) << 16)
                | ((0xFF & reply[27]) << 24);
      }
    }
    return fileInfo;
  }

  public byte playSoundFile(String fileName, boolean repeat) {

    byte boolVal = 0;
    if (repeat)
      boolVal = (byte) 0xFF; // Convert boolean to number

    byte[] request = { DIRECT_COMMAND_NOREPLY, PLAY_SOUND_FILE, boolVal };
    byte[] encFileName = null;
    try {
      encFileName = AsciizCodec.encode(fileName);
    } catch (UnsupportedEncodingException e) {
      System.err.println("Illegal characters in filename");
      return -1;
    }
    request = appendBytes(request, encFileName);
    return sendRequest(request);
  }

  /**
   * Stops sound file playing.
   * 
   * @return
   */
  public byte stopSoundPlayback() {
    byte[] request = { DIRECT_COMMAND_NOREPLY, STOP_SOUND_PLAYBACK };
    return sendRequest(request);
  }

  /**
   * Helper code to append a string and null terminator at the end of a command request. Should use
   * String.concat if I could add a zero to end somehow.
   * 
   * @param command
   * @param str
   * @return
   */
  private byte[] appendString(byte[] command, String str) {
    String requestStr = new String(command);
    StringBuffer buff = new StringBuffer(requestStr);
    buff.append(str);
    // buff.append(0x00); // Need to add 0 to end of command array.
    buff.setLength(buff.length() + 1); // This is a hack to add null value to end
    return buff.toString().getBytes();
  }

  private byte[] appendBytes(byte[] array1, byte[] array2) {
    byte[] array = new byte[array1.length + array2.length];
    System.arraycopy(array1, 0, array, 0, array1.length);
    System.arraycopy(array2, 0, array, array1.length, array2.length);
    return array;
  }

  /**
   * 
   * @param port
   *          - Output port (0 - 2 or 0xFF for all three)
   * @param power
   *          - Setpoint for power. (-100 to 100)
   * @param mode
   *          - Setting the modes MOTORON, BRAKE, and/or REGULATED. This parameter is a bitfield, so
   *          to put it in brake mode and regulated, use BRAKEMODE + REGULATED
   * @param regulationMode
   *          - see NXTProtocol for enumerations
   * @param turnRatio
   *          - Need two motors? (-100 to 100)
   * @param runState
   *          - see NXTProtocol for enumerations
   * @param tachoLimit
   *          - Number of degrees(?) to rotate before stopping.
   */
  public byte setOutputState(int port, byte power, int mode, int regulationMode, int turnRatio,
      int runState, int tachoLimit) {
    // !! Needs to check port, power to verify they are correct ranges.
    byte[] request =
        { DIRECT_COMMAND_NOREPLY, SET_OUTPUT_STATE, (byte) port, power, (byte) mode,
          (byte) regulationMode, (byte) turnRatio, (byte) runState, (byte) tachoLimit,
          (byte) (tachoLimit >>> 8), (byte) (tachoLimit >>> 16), (byte) (tachoLimit >>> 24) };
    return sendRequest(request);
  }

  /**
   * Tells the NXT what type of sensor you are using and the mode to operate in.
   * 
   * @param port
   *          - 0 to 3
   * @param sensorType
   *          - Enumeration for sensor type (see NXTProtocol)
   * @param sensorMode
   *          - Enumeration for sensor mode (see NXTProtocol)
   */
  public byte setInputMode(int port, int sensorType, int sensorMode) {
    // !! Needs to check port to verify they are correct ranges.
    byte[] request =
        { DIRECT_COMMAND_NOREPLY, SET_INPUT_MODE, (byte) port, (byte) sensorType, (byte) sensorMode };
    return sendRequest(request);
  }

  /**
   * Retrieves the current output state for a port.
   * 
   * @param port
   *          - 0 to 3
   * @return OutputState - returns a container object for output state variables.
   */
  public OutputState getOutputState(int port) {
    // !! Needs to check port to verify they are correct ranges.
    byte[] request = { DIRECT_COMMAND_REPLY, GET_OUTPUT_STATE, (byte) port };
    nxtComm.sendData(request);
    byte[] reply = nxtComm.readData();

    if (reply[1] != GET_OUTPUT_STATE) {
      System.out.println("Oops! Error in NXTCommand.getOutputState.");
      System.out.println("Return data did not match request.");
      System.out.println("reply[0] = " + reply[0] + "  reply[1] = " + reply[1] + "  reply[2] = "
          + reply[2]);
    }
    OutputState outputState = new OutputState(port);
    outputState.status = reply[2];
    outputState.outputPort = reply[3];
    outputState.powerSetpoint = reply[4];
    outputState.mode = reply[5];
    outputState.regulationMode = reply[6];
    outputState.turnRatio = reply[7];
    outputState.runState = reply[8];
    outputState.tachoLimit =
        (0xFF & reply[9]) | ((0xFF & reply[10]) << 8) | ((0xFF & reply[11]) << 16)
            | ((0xFF & reply[12]) << 24);
    outputState.tachoCount =
        (0xFF & reply[13]) | ((0xFF & reply[14]) << 8) | ((0xFF & reply[15]) << 16)
            | ((0xFF & reply[16]) << 24);
    outputState.blockTachoCount =
        (0xFF & reply[17]) | ((0xFF & reply[18]) << 8) | ((0xFF & reply[19]) << 16)
            | ((0xFF & reply[20]) << 24);
    outputState.rotationCount =
        (0xFF & reply[21]) | ((0xFF & reply[22]) << 8) | ((0xFF & reply[23]) << 16)
            | ((0xFF & reply[24]) << 24);
    return outputState;
  }

  public InputValues getInputValues(int port) {
    // !! Needs to check port to verify they are correct ranges.
    byte[] request = { DIRECT_COMMAND_REPLY, GET_INPUT_VALUES, (byte) port };
    nxtComm.sendData(request);
    byte[] reply = nxtComm.readData();
    InputValues inputValues = new InputValues();
    inputValues.inputPort = reply[3];
    // 0 is false, 1 is true.
    inputValues.valid = (reply[4] != 0);
    // 0 is false, 1 is true.
    inputValues.isCalibrated = (reply[5] == 0);
    inputValues.sensorType = reply[6];
    inputValues.sensorMode = reply[7];
    inputValues.rawADValue = (0xFF & reply[8]) | ((0xFF & reply[9]) << 8);
    inputValues.normalizedADValue = (0xFF & reply[10]) | ((0xFF & reply[11]) << 8);
    inputValues.scaledValue = (short) ((0xFF & reply[12]) | (reply[13] << 8));
    inputValues.calibratedValue = (short) ((0xFF & reply[14]) | (reply[15] << 8));
    // * Untested if scaledValue and calibrateValue above work as shown. Alt code below.
    // inputValues.scaledValue = (short)((0xFF & reply[12]) | ((0xFF & reply[13]) << 8));
    // inputValues.calibratedValue = (short)((0xFF & reply[14]) | ((0xFF & reply[15]) << 8));

    return inputValues;
  }

  /**
   * UNTESTED
   * 
   * @param port
   * @return
   */
  public byte resetScaledInputValue(int port) {
    byte[] request = { DIRECT_COMMAND_NOREPLY, RESET_SCALED_INPUT_VALUE, (byte) port };
    return sendRequest(request);
  }

  /**
   * Sends a message to an inbox on the NXT for storage(?) For future reference, message size must
   * be capped at 59 for USB. UNTESTED
   * 
   * @param message
   *          String to send. A null termination is automatically appended.
   * @param inbox
   *          Inbox Number 0 - 9
   * @return
   */
  public byte messageWrite(byte[] message, byte inbox) {
    byte[] request = { DIRECT_COMMAND_NOREPLY, MESSAGE_WRITE, inbox, (byte) (message.length) };
    request = appendBytes(request, message);
    return sendRequest(request);
  }

  /**
   * UNTESTED
   * 
   * @param remoteInbox
   *          0-9
   * @param localInbox
   *          0-9
   * @param remove
   *          True clears the message from the remote inbox.
   * @return
   */
  public byte[] messageRead(byte remoteInbox, byte localInbox, boolean remove) {
    byte[] request = { DIRECT_COMMAND_REPLY, MESSAGE_READ };
    nxtComm.sendData(request);
    byte[] reply = nxtComm.readData();
    byte[] message = new byte[reply[3]];
    System.arraycopy(reply, 4, message, 0, reply[3]);
    return message;
  }

  /**
   * Resets either RotationCount or BlockTacho
   * 
   * @param port
   *          Output port (0-2)
   * @param relative
   *          TRUE: BlockTacho, FALSE: RotationCount
   */
  public byte resetMotorPosition(int port, boolean relative) {
    // !! Needs to check port to verify they are correct ranges.
    // !!! I'm not sure I'm sending boolean properly
    byte boolVal = 0;
    if (relative)
      boolVal = (byte) 0xFF;
    byte[] request = { DIRECT_COMMAND_NOREPLY, RESET_MOTOR_POSITION, (byte) port, boolVal };
    return sendRequest(request);
  }

  /**
   * Plays a tone on NXT speaker. If a new tone is sent while the previous tone is playing, the new
   * tone command will stop the old tone command.
   * 
   * @param frequency
   *          - 100 to 2000?
   * @param duration
   *          - In milliseconds.
   * @return - Returns true if command worked, false if it failed.
   */
  public byte playTone(int frequency, int duration) {
    byte[] request =
        { DIRECT_COMMAND_NOREPLY, PLAY_TONE, (byte) frequency, (byte) (frequency >>> 8),
          (byte) duration, (byte) (duration >>> 8) };
    return sendRequest(request);
  }

  public int getBatteryLevel() {
    byte[] request = { DIRECT_COMMAND_REPLY, GET_BATTERY_LEVEL };
    nxtComm.sendData(request);
    byte[] reply = nxtComm.readData();
    if (reply[1] != GET_BATTERY_LEVEL)
      System.out.println("Weird data reply received.");
    if (reply[2] != 0)
      System.out.println("NXT reports the check battery command did not work.");
    int batteryLevel = (0xFF & reply[3]) | ((0xFF & reply[4]) << 8);
    return batteryLevel;
  }

  /**
   * Keeps the NXT from shutting off. NOTE: Normal Bluetooth commands do not keep the NXT alive. It
   * will power off even if you have been regularly sending commands. Must use keepAlive() UNTESTED
   * 
   * @return The current sleep time limit, in milliseconds.
   */
  public long keepAlive() {
    byte[] request = { DIRECT_COMMAND_REPLY, KEEP_ALIVE };
    nxtComm.sendData(request);
    byte[] reply = nxtComm.readData();
    int sleepLimit =
        (0xFF & reply[3]) | ((0xFF & reply[4]) << 8) | ((0xFF & reply[5]) << 16)
            | ((0xFF & reply[6]) << 24);
    return sleepLimit;
  }

  /**
   * Returns the status for an Inter-Integrated Circuit (I2C) sensor (the ultrasound sensor) via the
   * Low Speed (LS) data port. The port must first be configured to type LOWSPEED or LOWSPEED_9V.
   * 
   * @param port
   *          0-3
   * @return byte[0] = status, byte[1] = Bytes Ready (count of available bytes to read)
   */
  public byte[] LSGetStatus(byte port) {
    byte[] request = { DIRECT_COMMAND_REPLY, LS_GET_STATUS, port };
    nxtComm.sendData(request);
    byte[] reply = nxtComm.readData();
    if (reply[2] == ErrorMessages.COMMUNICATION_BUS_ERROR)
      System.out.println("NXTCommand.LSGetStatus() error: Communications Bus Error");
    else if (reply[2] == ErrorMessages.PENDING_COMMUNICATION_TRANSACTION_IN_PROGRESS)
      System.out
          .println("NXTCommand.LSGetStatus() error: Pending communication transaction in progress");
    else if (reply[2] == ErrorMessages.SPECIFIED_CHANNEL_CONNECTION_NOT_CONFIGURED_OR_BUSY)
      System.out
          .println("NXTCommand.LSGetStatus() error: Specified channel connection not configured or busy");
    else if (reply[2] != 0)
      System.out.println("NXTCommand.LSGetStatus() Error Number " + reply[2]);
    byte[] returnData = { reply[2], reply[3] };
    return returnData;
  }

  /**
   * Used to request data from an Inter-Integrated Circuit (I2C) sensor (the ultrasound sensor) via
   * the Low Speed (LS) data port. The port must first be configured to type LOWSPEED or
   * LOWSPEED_9V. Data lengths are limited to 16 bytes per command. Rx (receive) Data Length MUST be
   * specified in the write command since reading from the device is done on a master-slave basis.
   * 
   * @param txData
   *          Transmitted data.
   * @param rxDataLength
   *          Receive data length.
   * @param port
   *          0-3
   * @return
   */
  public byte LSWrite(byte port, byte[] txData, byte rxDataLength) {
    byte[] request = { DIRECT_COMMAND_NOREPLY, LS_WRITE, port, (byte) txData.length, rxDataLength };
    request = appendBytes(request, txData);
    return sendRequest(request);
  }

  /**
   * Reads data from an Inter-Integrated Circuit (I2C) sensor (the ultrasound sensor) via the Low
   * Speed (LS) data port. The port must first be configured to type LOWSPEED or LOWSPEED_9V. Data
   * lengths are limited to 16 bytes per command. The response will also contain 16 bytes, with
   * invalid data padded with zeros.
   * 
   * @param port
   * @return
   */
  public byte[] LSRead(byte port) {
    byte[] request = { DIRECT_COMMAND_REPLY, LS_READ, port };
    nxtComm.sendData(request);
    byte[] reply = nxtComm.readData();

    byte rxLength = reply[3];
    byte[] rxData = new byte[rxLength];
    if (reply[2] == 0) {
      System.arraycopy(reply, 4, rxData, 0, rxLength);
    } else if (reply[2] == ErrorMessages.SPECIFIED_CHANNEL_CONNECTION_NOT_CONFIGURED_OR_BUSY) {
      System.out
          .println("NXTCommand.LSRead error: Specified channel connection not configured or busy.");
      return null;
    } else {
      System.out.println("NXTCommand.LSRead error: " + reply[2]);
      return null;
    }
    return rxData;
  }

  /**
   * Small helper method to send request to NXT and return verification result.
   * 
   * @param request
   * @return
   */
  private byte sendRequest(byte[] request) {
    byte verify = 0; // default of 0 means success
    if (verifyCommand)
      request[0] = DIRECT_COMMAND_REPLY;

    nxtComm.sendData(request);
    if (verifyCommand) {
      byte[] reply = nxtComm.readData();
      verify = reply[2];
    }
    return verify;
  }

  public boolean isVerify() {
    return verifyCommand;
  }

  public static void setVerify(boolean verify) {
    verifyCommand = verify;
  }

  /**
   * Opens a connection using iCommand. As of Sept-16-2007 this method throws an exception rather
   * than System.exit() so that user interfaces can handle errors.
   * 
   * @throws Exception
   *           When open fails.
   */
  public static void open() throws RuntimeException {
    singleton.nxtComm = AndroidComm.getInstance();
    try {
      singleton.nxtComm.open();
    } catch (Exception e) {
      // System.err.println("NXTCommand.open(): Error while connecting ...");
      // e.printStackTrace();
      // System.exit(-1);
      throw new RuntimeException(e);
    }
  }

  /**
   * Call the close() command when your program ends, otherwise you will have to turn the NXT brick
   * off/on before you run another program using iCommand.
   * 
   */
  public static void close() {

    // Set all motors to float mode:
    byte ALL_MOTORS = (byte) 0xFF;
    singleton.setOutputState(ALL_MOTORS, (byte) 0, 0x00, REGULATION_MODE_IDLE, 0,
        MOTOR_RUN_STATE_IDLE, 0);

    // Signal to iCommand thru LCP to close connection first:
    // 1. Check if brick has NXJ or standard firmware.
    // FirmwareInfo fw = NXTCommand.getSingleton().getFirmwareVersion();
    // System.out.println("FW_VERSION: " + fw.firmwareVersion);
    // System.out.println("PROTOCOL_VERS: " + fw.protocolVersion);

    // 2. If NXJ, then send LCP command NXJ_DISCONNECT = 0x20

    singleton.nxtComm.close();
  }

  /**
   * Disconnects Bluetooth connection cleanly. Custom leJOS NXJ command (not part of official LCP)
   * This command only works with leJOS NXJ firmware. It will not work with the LEGO firmware.
   * UNTESTED
   * 
   * @return
   */
  public byte nxjDisconnect() {
    byte[] request = { DIRECT_COMMAND_NOREPLY, NXJ_DISCONNECT };
    return sendRequest(request);
  }

  /**
   * Defrag the flash memory file system. Custom leJOS NXJ command (not part of official LCP) This
   * command only works with leJOS NXJ firmware. It will not work with the LEGO firmware. UNTESTED
   * 
   * @return
   */
  public byte nxjDefrag() {
    byte[] request = { DIRECT_COMMAND_NOREPLY, NXJ_DEFRAG };
    return sendRequest(request);
  }

  public static NXTCommand getSingleton() {
    return singleton;
  }

}
