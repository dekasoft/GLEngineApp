package com.dekagames.glengineapp;

import com.dekagames.gle.GLE;
import com.dekagames.gle.gui.Control;
import com.dekagames.gle.gui.Window;
import com.dekagames.gle.gui.controls.Button;
import com.dekagames.gle.gui.controls.ToggleButton;

/**
 * Created by deka on 05.01.15.
 */
public class MainWindow extends Window {
    public Button button;
    public ToggleButton toggleButton;
    private MainScreen screen;

    public MainWindow(GLE gle, MainScreen scr, int width, int height) {
        super(gle, width, height);
        screen = scr;
    }

    @Override
    public void initControls(){
        button = new Button(screen.atlas, "sprButton", 0, 0);
        toggleButton = new ToggleButton(screen.atlas, "sprButton", 500, 200);
        addCtrl(button);
        addCtrl(toggleButton);
    }

    @Override
    public Control update(float deltaTime){
        Control ctrl = super.update(deltaTime);

        if (ctrl == button){
            buttonPressed();
        }

        if (ctrl == toggleButton){
            toggleButtonPressed();
        }
        return ctrl;
    }



    public void buttonPressed(){
        screen.sound.play();
    }


    public void toggleButtonPressed(){
        screen.sound.play();
    }

}
