package com.googlecode.botcommander;

import icommand.nxt.comm.NXTComm;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

public class AndroidComm implements NXTComm {

  private final static String TAG = "AndroidComm";
  private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

  private BluetoothDevice mDevice;
  private BluetoothSocket mSocket;
  private OutputStream mOutputStream;
  private InputStream mInputStream;

  static class SingletonHolder {
    static AndroidComm mmInstance = new AndroidComm();
  }

  public static AndroidComm getInstance() {
    return SingletonHolder.mmInstance;
  }

  private AndroidComm() {
    // Singleton class.
  }

  public void setDevice(BluetoothDevice device) {
    close();
    mDevice = device;
  }

  @Override
  public void open() throws Exception {
    try {
      mSocket = mDevice.createRfcommSocketToServiceRecord(MY_UUID);
      mSocket.connect();
      mOutputStream = mSocket.getOutputStream();
      mInputStream = mSocket.getInputStream();
      Log.d(TAG, "Connected.");
    } catch (IOException e) {
      Log.e(TAG, "Connection failed.", e);
    }
  }

  @Override
  public void close() {
    if (mSocket != null) {
      try {
        mSocket.close();
      } catch (IOException e) {
        Log.e(TAG, "Failed to close connection.", e);
      }
    }
    mSocket = null;
  }

  @Override
  public byte[] readData() {
    byte[] buffer = new byte[66];
    int numBytes;
    try {
      numBytes = mInputStream.read(buffer);
    } catch (IOException e) {
      Log.e(TAG, "Read failed.", e);
      throw new RuntimeException(e);
    }
    byte[] result = new byte[numBytes];
    for (int i = 0; i < numBytes; i++) {
      result[i] = buffer[i];
    }
    Log.v(TAG, "Read: " + result);
    return result;
  }

  @Override
  public void sendData(byte[] request) {
    int lsb = request.length;
    int msb = request.length >>> 8;
    try {
      mOutputStream.write((byte) lsb);
      mOutputStream.write((byte) msb);
      mOutputStream.write(request);
    } catch (IOException e) {
      Log.e(TAG, "Write failed.", e);
      throw new RuntimeException(e);
    }
    Log.v(TAG, "Sent: " + request);
  }

}