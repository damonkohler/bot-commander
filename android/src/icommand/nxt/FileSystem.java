package icommand.nxt;

import icommand.nxt.comm.FileInfo;
import icommand.nxt.comm.NXTCommand;

import java.io.*;
import java.util.ArrayList;

public class FileSystem {

  private static final NXTCommand nxtCommand = NXTCommand.getSingleton();

  // Make sure no one tries to instantiate this.
  private FileSystem() {
  }

  // Consider using String instead of File?
  public static byte upload(File localSource) {
    // FIRST get data from file
    byte[] data;
    try {
      FileInputStream in = new FileInputStream(localSource);
      data = new byte[in.available()];
      in.read(data);
      in.close();
    } catch (IOException e) {
      System.out.println("File read didn't work");
      e.printStackTrace();
      return -1;
    }

    byte handle = nxtCommand.openWrite(localSource.getName(), data.length);
    byte success = nxtCommand.writeFile(handle, data);
    nxtCommand.closeFile(handle);
    return success;
  }

  /**
   * 
   * @param fileName
   *          The name of the file on the NXT, including filename extension.
   * @return The file data, as an array of bytes. If there is a problem, the array will contain one
   *         byte with the error code.
   */
  public static byte[] download(String fileName) {
    byte[] data;
    FileInfo finfo = nxtCommand.openRead(fileName);
    if (finfo.status != 0) { // Return error message
      data = new byte[1];
      data[0] = finfo.status;
      return data;
    }
    data = nxtCommand.readFile(finfo.fileHandle, finfo.fileSize);
    nxtCommand.closeFile(finfo.fileHandle);
    return data;
  }

  /**
   * Download a file from the NXT and save it to a file.
   * 
   * @param fileName
   * @param destination
   *          Where the file will be saved. Can be directory or full path and filename.
   * @return Error code.
   */
  public static byte download(String fileName, File destination) {
    byte[] data = download(fileName);
    File fullFile;
    if (destination.isDirectory())
      fullFile = new File(destination.toString() + File.separator + fileName);
    else
      fullFile = destination;
    try {

      if (fullFile.createNewFile()) {
        FileOutputStream out = new FileOutputStream(fullFile);
        out.write(data);
        out.close();
      }
    } catch (IOException e) {
      System.out.println("File write didn't work");
      e.printStackTrace();
      return -1;
    }
    return 0;
  }

  /**
   * Download a file from the NXT and save it to a local directory.
   * 
   * @param fileName
   * @param destination
   *          Where the file will be saved. Can be directory or full path and filename. e.g.
   *          "c:/Documents/Lego Sounds/Sir.rso"
   * @return Error code.
   */
  public static byte download(String fileName, String destination) {
    File file = new File(destination);
    return download(fileName, file);
  }

  /**
   * Delete a file from the NXT.
   * 
   * @param fileName
   * @return 0 = success
   */
  public static byte delete(String fileName) {
    return nxtCommand.delete(fileName);
  }

  /**
   * Returns a list of all files on NXT brick.
   * 
   * @return An array on file names, or NULL if no files found.
   */
  public static String[] getFileNames() {
    return getFileNames("*.*");
  }

  /**
   * Returns a list of files on NXT brick.
   * 
   * @param searchCriteria
   *          "*.*" or [FileName].* or or *.[Extension] or [FileName].[Extension]
   * @return An array on file names, or NULL if nothing found.
   */
  // This method could provide file sizes by returning FileInfo objects
  // instead. It's simpler for users to return fileNames.
  public static String[] getFileNames(String searchCriteria) {
    ArrayList names = new ArrayList(1);
    FileInfo f = nxtCommand.findFirst(searchCriteria);
    if (f == null)
      return null;
    do {
      names.add(f.fileName);
      if (f != null)
        nxtCommand.closeFile(f.fileHandle); // According to protocol, must be closed when done with
      // it.
      f = nxtCommand.findNext(f.fileHandle);
    } while (f != null);

    String[] returnArray = new String[1];
    return (String[]) names.toArray(returnArray);
  }

  /**
   * Retrieves the file name of the Lego executable currently running on the NXT.
   * 
   * @return
   */
  public static String getCurrentProgramName() {
    return nxtCommand.getCurrentProgramName();
  }

  /**
   * Starts a Lego executable file on the NXT.
   * 
   * @param fileName
   * @return
   */
  public static byte startProgram(String fileName) {
    return nxtCommand.startProgram(fileName);
  }

  /**
   * Stops the currently running Lego executable on the NXT.
   * 
   * @param fileName
   * @return
   */
  public static byte stopProgram() {
    return nxtCommand.stopProgram();
  }
}
