package cz.vsb.gis.ruz76.android.patracmonitor.utils;

import android.content.Context;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.media.ToneGenerator;
import android.net.Uri;

public class Sound {

    public static void playRing(Context context, int time) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setStreamVolume(AudioManager.STREAM_DTMF, audioManager.getStreamMaxVolume(AudioManager.STREAM_DTMF), AudioManager.FLAG_PLAY_SOUND);

        ToneGenerator tone = new ToneGenerator(AudioManager.STREAM_DTMF, 100); // 100 is max volume
        tone.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, time); // 500ms

        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            Ringtone r = RingtoneManager.getRingtone(context, notification);
            r.play();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            r.stop();
        } catch (Exception e) {
            // No ring
        }
    }
}
