package com.dekagames.glengineapp;

import android.os.Bundle;
import com.dekagames.gle.GLEActivity;
import com.dekagames.gle.GLE;

public class MainActivity extends GLEActivity { //AppCompatActivity {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GLE gle = new GLE(800, 600);
        MainScreen screen = new MainScreen(gle);
        gle.setScreen(screen);
        gle.start(this, "GLE Example windowed", true);
    }
}
