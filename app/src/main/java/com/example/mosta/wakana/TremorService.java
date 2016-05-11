package com.example.mosta.wakana;

import android.app.Service;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.util.Arrays;


/**
 * Created by mosta on 10/05/16.
 */
public class TremorService extends Service {
    public String TAG = "BAZINGA";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG,"Tremor Started");
        final int bufferSize = AudioRecord.getMinBufferSize(44100,AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        Log.i(TAG,"BUFF:"+bufferSize);
        short[] audiodata = new short[bufferSize];
        AudioRecord recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, 44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);
        recorder.startRecording();
        boolean isRecording = true;
        //AudioTrack audioPlayer = new AudioTrack(AudioManager.STREAM_MUSIC, 44100, AudioFormat.CHANNEL_OUT_MONO,
        //        AudioFormat.ENCODING_PCM_16BIT, bufferSize, AudioTrack.MODE_STREAM);
        //if(audioPlayer.getPlayState() != AudioTrack.PLAYSTATE_PLAYING)
           // audioPlayer.play();
        int readBytes=0, writtenBytes=0;
        do{
            readBytes = recorder.read(audiodata, 0, bufferSize);
            Log.i(TAG, "FREQ"+ Arrays.toString(audiodata));

            //if(AudioRecord.ERROR_INVALID_OPERATION != readBytes){
                //writtenBytes += audioPlayer.write(audiodata, 0, readBytes);
            //}
        }
        while(isRecording);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent , flags , startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }

    public void toast(String text){
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
    }
}
