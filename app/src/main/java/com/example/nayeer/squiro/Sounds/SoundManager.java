package com.example.nayeer.squiro.Sounds;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;

import com.example.nayeer.squiro.R;

public class SoundManager extends Activity
{
    protected static SoundPool soundPool;
    protected static int[] sm;
    protected MediaPlayer mediaPlayer;
    protected Context context;
    protected boolean gameOver; //ce booleen sert à empêcher d'autres sons d'être joués pendant le son du gameover

    public SoundManager(Context context) {
        this.context = context;
        int maxStreams = 1;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            soundPool = new SoundPool.Builder()
                    .setMaxStreams(maxStreams)
                    .build();
        } else {
            //Pour Versions inférieures
            soundPool = new SoundPool(maxStreams, AudioManager.STREAM_MUSIC, 0);
        }

        sm = new int[2];
        sm[0] = soundPool.load(context, R.raw.fall, 0);
        sm[1] = soundPool.load(context, R.raw.gameover, 100);

        gameOver = false;

    }

    public void playSound(int sound, boolean loop) {
        soundPool.play(sm[sound], 1, 1, 1, loop ? -1 : 0, 1f);
    }

    public void playFall() {
        if(!gameOver)
        playSound(0,false);
    }

    public void playGameOver() {
        gameOver = true;
        playSound(1,false);
    }

    public void playAmbiance() {
        mediaPlayer = MediaPlayer.create(context, R.raw.ambiance);
        mediaPlayer.setLooping(true);
        mediaPlayer.setVolume(0.5f,0.5f);
        mediaPlayer.start();
    }

    public void stopAmbiance() {
        try {
            if (mediaPlayer != null) {
                if (mediaPlayer.isPlaying())
                    mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setGameOver(boolean b) {
        gameOver = b;
    }
}
