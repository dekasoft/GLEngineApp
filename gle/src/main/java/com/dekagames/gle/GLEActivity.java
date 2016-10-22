package com.dekagames.gle;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;

@TargetApi(Build.VERSION_CODES.FROYO)
public class GLEActivity extends Activity {
    GLE gle;
//    protected   PowerManager.WakeLock   wakeLock;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
    }


    @Override
    public void onDestroy(){
//        if (gle.audio != null){
//            gle.audio.dispose();
//        }
        super.onDestroy();
    }




    //----------------------------- обработка клавиш -----------------------------------
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
//        Log.info("AndroidApp onKeyDown. keyCode ="+keyCode);
//        gle.input.lastKeyDown = keyCode;
//        gle.input.keys.add(keyCode);
////        game.input.catchBackKey = true;
//
//        if (gle.input.catchBackKey && (keyCode == gle.input.BACK))
//            return true;                // предотвращаем обработку клавиши системой
//
//
//        if (gle.input.catchMenuKey && keyCode == gle.input.MENU)
//            return true;                // предотвращаем обработку клавиши системой
//
        return super.onKeyDown(keyCode, event);
    }


    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event){
//        Log.info("AndroidApp onKeyUp. keyCode ="+keyCode);
//        gle.input.lastKeyUp = keyCode;
//        gle.input.keys.remove(keyCode);
//        if (gle.input.catchBackKey && keyCode == gle.input.BACK)
//            return true;                // предотвращаем обработку клавиши системой
//        if (gle.input.catchMenuKey && keyCode == gle.input.MENU)
//            return true;                // предотвращаем обработку клавиши системой
        return super.onKeyUp(keyCode, event);
    }


    @Override
    public void onResume(){
        super.onResume();
        if (gle != null)
            gle.resume();
    }


    @Override
    public void onPause(){
        if (gle != null)
            gle.pause();
        super.onPause();
    }



}
