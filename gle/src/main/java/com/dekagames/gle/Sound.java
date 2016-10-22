package com.dekagames.gle;

import android.media.SoundPool;

import java.util.LinkedList;

/**
 * Created by deka on 22.07.14.
 */
public class Sound {
    final SoundPool soundPool;
//    final AudioManager manager;
    final int soundId;
    final LinkedList<Integer> streamIds = new LinkedList<Integer>();

    Sound (SoundPool pool, int soundId) {
        this.soundPool = pool;
        this.soundId = soundId;
    }

//    @Override
//    public void dispose () {
//        soundPool.unload(soundId);
//    }

    public void play () {
        if (streamIds.size() == 8) streamIds.removeFirst();
        int streamId = soundPool.play(soundId, 1, 1, 1, 0, 1);
        streamIds.addLast(streamId);
    }

    public void stop () {
        for (int i = 0, n = streamIds.size(); i < n; i++)
            soundPool.stop(streamIds.get(i));
    }

}
