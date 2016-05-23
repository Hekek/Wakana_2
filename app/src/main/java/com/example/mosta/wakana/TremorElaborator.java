package com.example.mosta.wakana;


import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by mosta on 15/05/16.
 */
public class TremorElaborator implements Runnable {
    final String TAG = "TREMOR-ELABORATOR";
    private String myName;
    public final int[] RANGE = new int[] { 40, 80, 120, 180, 301 };
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    int CHUNK_SIZE = 4096;
    private static final int FUZ_FACTOR = 2;
    double highscores[][];
    long points[][];
    public byte Audio[];
    public boolean isMatching = false;
    int songId = 0;

    Map<Long, List<DataPoint>> hashMap;
    Map<Integer, Map<Integer, Integer>> matchMap;   // Map<SongId, Map<Offset,
                                                    // Count>>

    TremorElaborator(String name , byte[] audio){
        this.myName = name;
        this.Audio = audio;
    }

    @Override
    public void run(){
        elaborate();
        System.out.println(myName + ")Finished");
    }
    public void elaborate(){
        Log.i(TAG,"ANALYZE");
        final int totalSize = Audio.length;
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
                complex[i] = new Complex(Audio[(times*CHUNK_SIZE)+i], 0);
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
            //Log.i(TAG,points[t][0]+"-"+points[t][1]+"-"+points[t][2]+"-"+points[t][3]+"-"+points[t][4]);
            long h = hash(points[t][0], points[t][1], points[t][2], points[t][3]);

            //IF i am matching sound
            if (isMatching)
            {
                //Create a list of DP(songid,time)
                List<DataPoint> listPoints;
                //If i found the hash in my hashmap
                if((listPoints = hashMap.get(h)) != null)
                {

                }
            }
            //else add new sound into database
            else
            {
                List<DataPoint> listPoints = null;
                if((listPoints = hashMap.get(h)) == null)
                {
                    listPoints = new ArrayList<DataPoint>();
                    DataPoint point = new DataPoint((int) songId, t);
                    listPoints.add(point);
                    hashMap.put(h, listPoints);
                }
                else
                {
                    DataPoint Point = new DataPoint((int) songId,t);
                    listPoints.add(Point);
                }
            }
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
}
