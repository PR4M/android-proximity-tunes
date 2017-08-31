package com.codetensor.pramana.proximitytunes;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;

public class PlayerActivity extends AppCompatActivity {

    private static MediaPlayer mediaPlayer;

    private SensorManager sensorManager;
    private Sensor proximitySensor;

    private int position;
    private SeekBar seekBar;
    private ArrayList<File> mySongs;
    private Thread updateSeekBar;
    private Button pause,forward,reverse,next,previous;

    // Sensor Listener of Proximity Sensor
    SensorEventListener proximitySensorListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            if(sensorEvent.values[0] < proximitySensor.getMaximumRange()) {
                playRandomSong();
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_player);
        pause = (Button)findViewById(R.id.pause);
        forward = (Button)findViewById(R.id.forward);
        previous = (Button)findViewById(R.id.previous);
        next = (Button)findViewById(R.id.next);
        reverse = (Button)findViewById(R.id.reverse);
        seekBar =(SeekBar)findViewById(R.id.seekBar);

        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        updateSeekBar=new Thread(){
            @Override
            public void run(){
                int totalDuration = mediaPlayer.getDuration();
                int currentPosition = 0;
                //seekBar.setMax(totalDuration);
                while(currentPosition < totalDuration){
                    try{
                        sleep(500);
                        currentPosition= mediaPlayer.getCurrentPosition();
                        seekBar.setProgress(currentPosition);
                    }
                    catch (InterruptedException e){
                        e.printStackTrace();
                    }
                }
            }

        };

        if(mediaPlayer != null){
            mediaPlayer.stop();
            mediaPlayer.release();
        }

        Intent i = getIntent();
        Bundle b = i.getExtras();
        mySongs = (ArrayList) b.getParcelableArrayList("songs");
        position = b.getInt("pos",0);
        Uri u = Uri.parse(mySongs.get(position).toString());
        mediaPlayer = MediaPlayer.create(getApplicationContext(),u);

        mediaPlayer.start();
        seekBar.setMax(mediaPlayer.getDuration());
        updateSeekBar.start();
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaPlayer.seekTo(seekBar.getProgress());
            }

        });

        pauseSong();
        forwardTimeSong();
        reverseTimeSong();
        playNextSong();
        playPrevSong();

    }

    @Override
    protected void onResume() {
        super.onResume();

        sensorManager.registerListener(proximitySensorListener, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();

        sensorManager.unregisterListener(proximitySensorListener);
    }

    private void playRandomSong() {
        Random randNumber = new Random();
        int randNext = randNumber.nextInt(mySongs.size());

        mediaPlayer.stop();
        mediaPlayer.release();
        position = ((position+randNext) % mySongs.size());
        Uri u = Uri.parse(mySongs.get(position).toString());
        mediaPlayer = MediaPlayer.create(getApplicationContext(), u);
        mediaPlayer.start();
    }

    private void pauseSong() {

        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                seekBar.setMax(mediaPlayer.getDuration());

                if(mediaPlayer.isPlaying()){
                    pause.setText(">");
                    mediaPlayer.pause();
                }
                else {
                    pause.setText("||");
                    mediaPlayer.start();
                }

            }
        });
    }

    private void playNextSong() {
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.stop();
                mediaPlayer.release();
                position=((position+1)%mySongs.size());
                Uri u = Uri.parse(mySongs.get( position).toString());
                mediaPlayer = MediaPlayer.create(getApplicationContext(),u);
                mediaPlayer.start();
            }
        });
    }

    private void playPrevSong() {
        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.stop();
                mediaPlayer.release();
                position=((position-1)<0)?(mySongs.size()-1):(position-1);
                Uri u = Uri.parse(mySongs.get( position).toString());//%mysongs so that it do not go to invalid position
                mediaPlayer = MediaPlayer.create(getApplicationContext(),u);
                mediaPlayer.start();
            }
        });
    }

    private void forwardTimeSong() {
        forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                seekBar.setMax(mediaPlayer.getDuration());

                mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()+5000);
            }
        });
    }

    private void reverseTimeSong() {
        reverse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                seekBar.setMax(mediaPlayer.getDuration());

                mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()-5000);
            }
        });
    }

}