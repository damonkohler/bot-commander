package com.googlecode.botcommander;

import icommand.nxt.LightSensor;
import icommand.nxt.Motor;
import icommand.nxt.SensorPort;
import icommand.nxt.comm.NXTCommand;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class Main extends Activity {

  private static final String TAG = "NxtRemote";
  private static final String NXT_DEVICE_NAME = "NXT";

  private BluetoothAdapter mAdapter;

  private Map<MotorController, CheckBox> mLinks;
  private TiltController mTiltController;

  private Button mConnectButton;
  private SeekBar mMotorASeekBar;
  private CheckBox mMotorAReverse;
  private SeekBar mMotorBSeekBar;
  private CheckBox mMotorBReverse;
  private SeekBar mMotorCSeekBar;
  private CheckBox mMotorCReverse;
  private CheckBox mMotorALink;
  private CheckBox mMotorBLink;
  private CheckBox mMotorCLink;
  private CheckBox mTiltControl;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
    mAdapter = BluetoothAdapter.getDefaultAdapter();
    mConnectButton = (Button) findViewById(R.id.ConnectButton);
    mConnectButton.setText("Connect");
    mConnectButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        Main.this.findNxtDevice();
      }
    });
    findViews();
    setMotorControlViewsEnabled(false);

    MotorController motorControllerA = new MotorController(mMotorASeekBar, mMotorAReverse, Motor.A);
    MotorController motorControllerB = new MotorController(mMotorBSeekBar, mMotorBReverse, Motor.B);
    MotorController motorControllerC = new MotorController(mMotorCSeekBar, mMotorCReverse, Motor.C);

    mLinks = new HashMap<MotorController, CheckBox>();
    mLinks.put(motorControllerA, mMotorALink);
    mLinks.put(motorControllerB, mMotorBLink);
    mLinks.put(motorControllerC, mMotorCLink);

    for (final Entry<MotorController, CheckBox> entry : mLinks.entrySet()) {
      entry.getValue().setOnCheckedChangeListener(new OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
          updateLinks(entry.getKey(), entry.getValue());
        }
      });
    }

    mTiltController =
        new TiltController((SensorManager) getSystemService(SENSOR_SERVICE), motorControllerA,
            motorControllerB);
    mTiltControl.setEnabled(false);
    mTiltControl.setOnCheckedChangeListener(new OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        setTiltControllerEnabled(isChecked);
      }
    });
  }

  private void setTiltControllerEnabled(boolean enabled) {
    setMotorControlViewsEnabled(!enabled);
    mTiltController.setEnabled(enabled);
  }

  private void findViews() {
    mMotorASeekBar = (SeekBar) findViewById(R.id.MotorASpeed);
    mMotorAReverse = (CheckBox) findViewById(R.id.MotorAReverse);
    mMotorALink = (CheckBox) findViewById(R.id.MotorALink);
    mMotorBSeekBar = (SeekBar) findViewById(R.id.MotorBSpeed);
    mMotorBReverse = (CheckBox) findViewById(R.id.MotorBReverse);
    mMotorBLink = (CheckBox) findViewById(R.id.MotorBLink);
    mMotorCSeekBar = (SeekBar) findViewById(R.id.MotorCSpeed);
    mMotorCReverse = (CheckBox) findViewById(R.id.MotorCReverse);
    mMotorCLink = (CheckBox) findViewById(R.id.MotorCLink);
    mTiltControl = (CheckBox) findViewById(R.id.TiltControl);
  }

  private void setMotorControlViewsEnabled(boolean enabled) {
    mMotorASeekBar.setEnabled(enabled);
    mMotorAReverse.setEnabled(enabled);
    mMotorALink.setEnabled(enabled);
    mMotorBSeekBar.setEnabled(enabled);
    mMotorBReverse.setEnabled(enabled);
    mMotorBLink.setEnabled(enabled);
    mMotorCSeekBar.setEnabled(enabled);
    mMotorCReverse.setEnabled(enabled);
    mMotorCLink.setEnabled(enabled);
  }

  private void updateLinks(MotorController controller, CheckBox checkBox) {
    for (Entry<MotorController, CheckBox> entry : mLinks.entrySet()) {
      if (checkBox.isChecked()) {
        if (entry.getValue().isChecked()) {
          controller.link(entry.getKey());
          entry.getKey().link(controller);
        }
      } else {
        controller.unlink(entry.getKey());
        entry.getKey().unlink(controller);
      }
    }
  }

  private void disconnect() {
    mConnectButton.setText("Connect");
    mConnectButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        Main.this.findNxtDevice();
      }
    });
    setMotorControlViewsEnabled(false);
    mTiltControl.setEnabled(false);
    setTiltControllerEnabled(false);
    NXTCommand.close();
    Toast.makeText(this, "NXT Disconnected", Toast.LENGTH_SHORT).show();
  }

  private void connected() {
    mConnectButton.setText("Disconnect");
    mConnectButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        disconnect();
      }
    });
    setMotorControlViewsEnabled(true);
    mTiltControl.setEnabled(true);
    Toast.makeText(this, "NXT Connected", Toast.LENGTH_SHORT).show();
    watchLightSensor();
  }

  private void watchLightSensor() {
    final LightSensor sensor = new LightSensor(SensorPort.S2);
    sensor.activate();
    new Thread() {
      @Override
      public void run() {
        while (true) {
          Log.v(TAG, "LS: " + sensor.getLightPercent());
          try {
            Thread.sleep(1000);
          } catch (InterruptedException e) {
            Log.e(TAG, "Interrupted.", e);
          }
        }
      };
    }.start();

  }

  private void findNxtDevice() {
    if (!mAdapter.isEnabled()) {
      Toast.makeText(this, "Please enable Bluetooth first.", Toast.LENGTH_SHORT).show();
      return;
    }

    // First see if we've already paired with the NXT brick before.
    Set<BluetoothDevice> pairedDevices = mAdapter.getBondedDevices();
    for (BluetoothDevice device : pairedDevices) {
      if (device.getName().equals(NXT_DEVICE_NAME)) {
        foundNxtDevice(device);
        return;
      }
    }

    // We haven't paired before, so we need to discover the NXT brick now.
    BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
          BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
          if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
            if (device.getName().equals(NXT_DEVICE_NAME)) {
              foundNxtDevice(device);
              try {
                context.unregisterReceiver(this);
              } catch (IllegalArgumentException e) {
                Log.e(TAG, "Unregister failed.", e);
              }
            }
          }
        } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
          try {
            context.unregisterReceiver(this);
          } catch (IllegalArgumentException e) {
            Log.e(TAG, "Unregister failed.", e);
          }
        }
      }
    };

    registerReceiver(bluetoothReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
    registerReceiver(bluetoothReceiver,
        new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));

    mAdapter.enable();
    mAdapter.startDiscovery();
  }

  private void foundNxtDevice(BluetoothDevice device) {
    mAdapter.cancelDiscovery();
    AndroidComm.getInstance().setDevice(device);
    NXTCommand.open();
    connected();
  }

}
