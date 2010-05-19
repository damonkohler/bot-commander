package com.googlecode.botcommander;

import java.util.List;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class TiltController {

  private final static String TAG = "TiltController";
  private final static int MATRIX_SIZE = 16;

  private boolean mEnabled;

  private float[] mMagneticValues;
  private float[] mAccelerometerValues;

  private final AveragedDouble mAzimuth;
  private final AveragedDouble mPitch;
  private final AveragedDouble mRoll;

  private boolean mReverse;
  private boolean mLeft;

  private double mMultiplier;
  private double mTurnAdjustment;

  private final MotorController mLeftMotor;
  private final MotorController mRightMotor;

  private class SensorValuesCollector implements SensorEventListener {

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
      synchronized (TiltController.this) {
        switch (event.sensor.getType()) {
        case Sensor.TYPE_MAGNETIC_FIELD:
          mMagneticValues = event.values.clone();
          break;
        case Sensor.TYPE_ACCELEROMETER:
          mAccelerometerValues = event.values.clone();
          break;
        }

        if (mMagneticValues != null && mAccelerometerValues != null) {
          float[] R = new float[MATRIX_SIZE];
          SensorManager.getRotationMatrix(R, null, mAccelerometerValues, mMagneticValues);
          float[] orientation = new float[3];
          SensorManager.getOrientation(R, orientation);
          mAzimuth.add(orientation[0]);
          mPitch.add(orientation[1]);
          mRoll.add(orientation[2]);

          if (mRoll.get() > 0) {
            // Ignore the inverted state.
            return;
          }

          double absRoll = Math.abs(mRoll.get());
          if (absRoll < Math.PI / 2) {
            mReverse = false;
            absRoll = Math.PI / 2 - absRoll;
          } else {
            mReverse = true;
            absRoll -= Math.PI / 2;
          }
          mMultiplier = Math.min(absRoll, Math.PI / 4) / (Math.PI / 4);

          if (mPitch.get() < 0) {
            mLeft = true;
          } else {
            mLeft = false;
          }
          double absPitch = Math.abs(mPitch.get());
          mTurnAdjustment = Math.min(absPitch, Math.PI / 4) / (Math.PI / 4);

          updateMotors();
        }
      }
    }

    private void updateMotors() {
      int rightSpeed =
          (int) (900 * (mLeft ? Math.max(mMultiplier - mTurnAdjustment, 0) : mMultiplier));
      int leftSpeed =
          (int) (900 * (mLeft ? mMultiplier : Math.max(mMultiplier - mTurnAdjustment, 0)));
      if (mEnabled) {
        if (mReverse) {
          mLeftMotor.backward(leftSpeed);
          mRightMotor.backward(rightSpeed);
        } else {
          mLeftMotor.forward(leftSpeed);
          mRightMotor.forward(rightSpeed);
        }
      }
    }
  }

  public TiltController(SensorManager manager, MotorController left, MotorController right) {
    mEnabled = false;
    mAzimuth = new AveragedDouble(10);
    mPitch = new AveragedDouble(10);
    mRoll = new AveragedDouble(10);
    mLeftMotor = left;
    mRightMotor = right;
    List<Sensor> sensors = manager.getSensorList(Sensor.TYPE_MAGNETIC_FIELD);
    Sensor magneticSensor = sensors.get(0);
    sensors = manager.getSensorList(Sensor.TYPE_ACCELEROMETER);
    Sensor accelerometerSensor = sensors.get(0);
    manager.registerListener(new SensorValuesCollector(), magneticSensor,
        SensorManager.SENSOR_DELAY_NORMAL);
    manager.registerListener(new SensorValuesCollector(), accelerometerSensor,
        SensorManager.SENSOR_DELAY_NORMAL);
  }

  public void setEnabled(boolean enabled) {
    mEnabled = enabled;
  }
}
