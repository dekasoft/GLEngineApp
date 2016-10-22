package com.dekagames.gle;

import android.media.MediaPlayer;

import java.io.IOException;

/**
 * Created by deka on 22.07.14.
 */
public class Music {
    private final Audio audio;
    private MediaPlayer player;
    private boolean isPrepared = true;
    protected boolean wasPlaying = false;

    Music (Audio audio, MediaPlayer player) {
        this.audio = audio;
        this.player = player;
    }

    public void dispose () {
        if (player == null) return;
        try {
            if (player.isPlaying()) player.stop();
            player.release();
        } catch (Throwable t) {
            Log.error("Error while disposing AndroidMusic instance, non-fatal");
        } finally {
            player = null;
            synchronized (audio.musics) {
                audio.musics.remove(this);
            }
        }
    }


    public boolean isPlaying () {
        return player.isPlaying();
    }

    public void pause () {
        if (player.isPlaying()) player.pause();
    }

    public void play () {
        if (player.isPlaying()) return;

        try {
            if (!isPrepared) {
                player.prepare();
                isPrepared = true;
            }
            player.start();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setLooping (boolean isLooping) {
        player.setLooping(isLooping);
    }

    public void setVolume (float volume) {
        player.setVolume(volume, volume);
    }

    public void stop () {
        if (isPrepared) {
            player.seekTo(0);
        }
        player.stop();
        isPrepared = false;
    }

}
