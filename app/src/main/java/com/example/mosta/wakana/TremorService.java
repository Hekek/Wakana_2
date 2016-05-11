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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by mosta on 10/05/16.
 */
public class TremorService extends Service {
    public String LOG = "BAZINGA";
    private int audioSource = MediaRecorder.AudioSource.MIC;
    private int samplingRate = 44100;
    private int channelConfig = AudioFormat.CHANNEL_IN_MONO;//NO
    private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    private int bufferSize = AudioRecord.getMinBufferSize(samplingRate,channelConfig,audioFormat);
    private int sampleNumBits = 16;
    private int numChannels = 1;
    public boolean running = false;
    public byte[] data;





    @Override
    public void onCreate() {
        super.onCreate();
        AudioRecord recorder = new AudioRecord(audioSource,samplingRate,channelConfig,audioFormat,bufferSize);
        recorder.startRecording();
        running = true;
        AudioTrack audioPlayer = new AudioTrack(AudioManager.STREAM_MUSIC,44100,
                AudioFormat.CHANNEL_OUT_MONO,AudioFormat.ENCODING_PCM_16BIT,bufferSize,AudioTrack.MODE_STREAM);
        if (audioPlayer.getPlayState() != AudioTrack.PLAYSTATE_PLAYING)
            audioPlayer.play();
        int readBytes=0 , writtenBytes=0;
        do {
            readBytes = recorder.read(data, 0 , bufferSize);

            if(AudioRecord.ERROR_INVALID_OPERATION != readBytes){
                //writtenBytes += audioPlayer(data,0,readBytes);
            }
        }while (running);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(LOG,"AAAAAAAAAAAAAAAAA");
        return super.onStartCommand(intent , flags , startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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
