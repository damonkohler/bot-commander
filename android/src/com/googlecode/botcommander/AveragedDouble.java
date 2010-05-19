package com.googlecode.botcommander;

import java.util.ArrayList;
import java.util.List;

public class AveragedDouble {

  private final List<Double> mSamples;
  private final int mSampleSize;

  public AveragedDouble(int sampleSize) {
    mSampleSize = sampleSize;
    mSamples = new ArrayList<Double>();
  }

  public void add(double data) {
    mSamples.add(data);
    if (mSamples.size() > mSampleSize) {
      mSamples.remove(0);
    }
  }

  public double get() {
    double sum = 0;
    for (double sample : mSamples) {
      sum += sample;
    }
    return sum / mSamples.size();
  }

}
