package com.example.mosta.wakana;

import android.app.Service;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * Created by mosta on 10/05/16.
 */
public class TremorService extends Service {
    public String TAG = "TREMOR SERVICE";
    private int GFreqSize = 448;
    private int GFreq = 8;
    boolean isRecording = true;
    public final int[] RANGE = new int[] { 40, 80, 120, 180, 301 };
    final int bufferSize = 4096;//AudioRecord.getMinBufferSize(44100,AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
    final AudioRecord recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, 44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);
    final byte[] buffer = new byte[bufferSize];
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    int CHUNK_SIZE = 4096;
    private static final int FUZ_FACTOR = 2;
    double highscores[][];
    long points[][];
    int contatore = 0 ;

    //TEST
    ExecutorService executor = Executors.newFixedThreadPool(2);

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG,"Tremor Started");
        Log.i(TAG,"BUFF:"+bufferSize);
        //DELETE OLD SAVED HASHES
        try
        {
            String root = Environment.getExternalStorageDirectory().getPath();
            File file = new File(root,"HASHES.txt");
            if(file.exists())
                file.delete();
        }catch (Exception e){
            e.printStackTrace();
        }
        recorder.startRecording();

        new Thread(new Runnable(){
            @Override
            public void run() {
                try{
                    while (isRecording) {
                        int count = recorder.read(buffer, 0, bufferSize);
                        if (count > 0) {
                            out.write(buffer, 0, count);
                        }
                        //If Size of the Buffer is more than 176kb
                        if(out.size() > 176000)
                        {
                            contatore ++ ;
                            byte audio[] = out.toByteArray();
                            Log.i(TAG,"Analyze("+contatore+")");
                            Runnable taskOne = new TremorElaborator("TASK"+contatore,audio);
                            executor.execute(taskOne);
                            //elaborate();
                            out.reset();
                            out.flush();
                            out.close();
                        }
                    }

                    out.close();
                }catch (IOException e) {
                    System.err.println("I/O problems: " + e);
                    System.exit(-1);
                }
            }

        }).start();
        return super.onStartCommand(intent , flags , startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRecording = false;
        recorder.stop();
        recorder.release();
        Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }

    public void elaborate(){
        Log.i(TAG,"ANALYZE");
        byte audio[] = out.toByteArray();
        final int totalSize = audio.length;
        Log.i(TAG,"Total Size Of data: "+totalSize);
        int amountPossible = totalSize/CHUNK_SIZE;
        Log.i(TAG,"Amount of CHUNKS: "+amountPossible);
        //When turning into frequency domain we'll need complex numbers:
        Complex[][] results = new Complex[amountPossible][];

        //For all the chunks:
        for(int times = 0;times < amountPossible; times++) {
            Complex[] complex = new Complex[CHUNK_SIZE];
            for(int i = 0;i < CHUNK_SIZE;i++) {
                //Put the time domain data into a complex number with imaginary part as 0:
                complex[i] = new Complex(audio[(times*CHUNK_SIZE)+i], 0);
            }
            //Perform FFT analysis on the chunk:
            results[times] = FFT.fft(complex);
        }

        highscores = new double[results.length][5];
        for(int i = 0;i < results.length;i++)
        {
            for(int j = 0;j < 5;j++)
            {
                highscores[i][j] = 0;
            }
        }
        points = new long[results.length][5];
        for(int i = 0;i < results.length;i++)
        {
            for(int j = 0;j < 5;j++)
            {
                points[i][j] = 0;
            }
        }
        for (int t = 0 ; t < results.length ; t++){
            for (int freq = 30; freq < 300-1; freq++) {
                //Get the magnitude:
                double mag = Math.log(results[t][freq].abs() + 1);

                //Find out which range we are in:
                int index = getIndex(freq);

                //Save the highest magnitude and corresponding frequency:
                if (mag > highscores[t][index]) {
                    highscores[t][index] = mag;
                    points[t][index] = freq;
                }
            }
            Log.i(TAG,points[t][0]+"-"+points[t][1]+"-"+points[t][2]+"-"+points[t][3]+"-"+points[t][4]);
            long h = hash(points[t][0], points[t][1], points[t][2], points[t][3]);

        }

    }

    // find out in which range is frequency
    public int getIndex(int freq) {
        int i = 0;
        while (RANGE[i] < freq)
            i++;
        return i;
    }

    private long hash(long p1, long p2, long p3, long p4) {
        return (p4 - (p4 % FUZ_FACTOR)) * 100000000 + (p3 - (p3 % FUZ_FACTOR))
                * 100000 + (p2 - (p2 % FUZ_FACTOR)) * 100
                + (p1 - (p1 % FUZ_FACTOR));
    }

    public void computeFFT(){
        Log.i(TAG,"COMPUTE");
        //Conversion from short to double
        double[] micBufferData = new double[bufferSize];//size may need to change
        final int bytesPerSample = 2; // As it is 16bit PCM
        final double amplification = 100.0; // choose a number as you like
        for (int index = 0, floatIndex = 0; index < bufferSize - bytesPerSample + 1; index += bytesPerSample, floatIndex++) {
            double sample = 0;
            for (int b = 0; b < bytesPerSample; b++) {
                int v = buffer[index + b];
                if (b < bytesPerSample - 1 || bytesPerSample == 1) {
                    v &= 0xFF;
                }
                sample += v << (b * 8);
            }
            double sample32 = amplification * (sample / 32768.0);
            micBufferData[floatIndex] = sample32;
        }

        //Create Complex array for use in FFT
        Complex[] fftTempArray = new Complex[bufferSize];
        for (int i=0; i<bufferSize; i++)
        {
            fftTempArray[i] = new Complex(micBufferData[i], 0);
        }

        //Obtain array of FFT data
        final Complex[] fftArray = FFT.fft(fftTempArray);
        final Complex[] fftInverse = FFT.ifft(fftTempArray);

        //Create an array of magnitude of fftArray
        double[] magnitude = new double[fftArray.length];
        for (int i=0; i<fftArray.length; i++){
            magnitude[i]= fftArray[i].abs();
        }

        Log.i(TAG,"fftArray is "+ fftArray[500] +" and fftTempArray is "+fftTempArray[500] + " and fftInverse is "+fftInverse[500]+" and audioData is "+buffer[500]+ " and magnitude is "+ magnitude[1] + ", "+magnitude[500]+", "+magnitude[1000]+" You rock dude!");
        /*for(int i = 2; i < samples; i++){
            Log.i(TAG," " + magnitude[i] + " Hz");
        }*/
    }

    private void generateData(byte[] fft){
        Float mean = 0.0f;
        Float absoluteMean = 0.0f;
        Float [] Mean = new Float[8];
        Float [] AbsMean = new Float[8];
        String var = Arrays.toString(fft);
        String[] vars = var.replaceAll("\\[", "").replaceAll("\\]", "").replaceAll(" ","").split(",");
        for (int j = 0 ; j < GFreq ; j++){
            mean = 0.0f;
            absoluteMean = 0.0f;
            for (int i = 0 ; i < GFreqSize ; i++){
                mean += Integer.parseInt(vars[i+j]);
                absoluteMean += Math.abs(Integer.parseInt(vars[i+j]));
            }
            Mean[j] = mean/16.f;
            AbsMean[j] = absoluteMean/16.f;
        }
        Log.i(TAG, Arrays.toString(AbsMean));
    }
}
