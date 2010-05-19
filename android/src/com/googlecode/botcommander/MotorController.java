package com.googlecode.botcommander;

import icommand.nxt.Motor;

import java.util.HashSet;
import java.util.Set;

import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class MotorController {
  final SeekBar mMotorSeekBar;
  final CheckBox mMotorReverseCheckBox;
  final Motor mMotor;
  final Set<MotorController> mLinks;

  public MotorController(SeekBar seekBar, CheckBox checkBox, Motor motor) {
    mLinks = new HashSet<MotorController>();
    mMotorSeekBar = seekBar;
    mMotorReverseCheckBox = checkBox;
    mMotor = motor;

    mMotorSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {
      }

      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
          update(progress);
          for (MotorController controller : mLinks) {
            controller.update(progress);
          }
        }
      }
    });

    mMotorReverseCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked && mMotor.isMoving()) {
          mMotor.backward();
        } else if (Motor.A.isMoving()) {
          mMotor.forward();
        }
      }
    });
  }

  public void link(MotorController controller) {
    if (controller != this) {
      mLinks.add(controller);
    }
  }

  public void unlink(MotorController controller) {
    mLinks.remove(controller);
  }

  private void update(int progress) {
    mMotorSeekBar.setProgress(progress);
    if (progress > 0) {
      mMotor.setSpeed(progress);
      if (mMotorReverseCheckBox.isChecked()) {
        mMotor.backward();
      } else {
        mMotor.forward();
      }
    } else {
      mMotor.stop();
    }
  }

  @Override
  public int hashCode() {
    return mMotor.getId();
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof MotorController)) {
      return false;
    }
    MotorController other = (MotorController) o;
    return other.hashCode() == hashCode();
  }

  public void forward(int speed) {
    mMotorReverseCheckBox.setChecked(false);
    update(speed);
  }

  public void backward(int speed) {
    mMotorReverseCheckBox.setChecked(true);
    update(speed);
  }
}
