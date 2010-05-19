package icommand.nxt;

import icommand.nxt.comm.NXTProtocol;

public class SoundSensor {

  SensorPort sensor;
  int type = NXTProtocol.SOUND_DB; // default type

  /**
   * Used to detect the loudness of sounds in the environment.
   * 
   * @param sensor
   *          e.g. Port.S1
   */
  public SoundSensor(SensorPort sensor) {
    this.sensor = sensor;
    sensor.setTypeAndMode(type, NXTProtocol.PCTFULLSCALEMODE);
  }

  /**
   * Returns the decibels measured by the sound sensor.<br>
   * e.g. Whispering = 20 dB<br>
   * Vacuum cleaner = 80 dB<br>
   * Jet engine = 150 dB<br>
   * NOTE: Uncertain if PCTFILLSCALEMODE is producing accurate dB values.
   * 
   * @return dB - decibels
   */
  public int getdB() {
    if (type != NXTProtocol.SOUND_DB) {
      type = NXTProtocol.SOUND_DB;
      sensor.setTypeAndMode(type, NXTProtocol.PCTFULLSCALEMODE);
    }

    return sensor.readScaledValue();
  }

  /**
   * Returns sound within the human hearing frequency range, normalized by A-weighting. Extremely
   * high frequency or low frequency sounds are not detected with this filtering (regardless of
   * loudness).
   * 
   * @return dB(A) - decibels with A-weighting
   */
  public int getdBA() {
    if (type != NXTProtocol.SOUND_DBA) {
      type = NXTProtocol.SOUND_DBA;
      sensor.setTypeAndMode(type, NXTProtocol.PCTFULLSCALEMODE);
    }
    return sensor.readScaledValue();
  }
}
