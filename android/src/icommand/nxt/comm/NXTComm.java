package icommand.nxt.comm;

public interface NXTComm {

  public void open() throws Exception;

  public void sendData(byte[] request);

  public byte[] readData();

  public void close();

}
