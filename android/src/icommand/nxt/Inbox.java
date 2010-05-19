package icommand.nxt;

import icommand.nxt.comm.NXTCommand;

/**
 * The message class is used to send messages between NXT bricks. UNTESTED
 * 
 * @author BB
 * 
 */
public class Inbox {

  private static final NXTCommand nxtCommand = NXTCommand.getSingleton();

  public static int sendMessage(byte[] message, int inbox) {
    return nxtCommand.messageWrite(message, (byte) inbox);
  }

  public static byte[] receiveMessage(int remoteInbox, int localInbox, boolean remove) {
    return nxtCommand.messageRead((byte) remoteInbox, (byte) localInbox, remove);
  }

}
