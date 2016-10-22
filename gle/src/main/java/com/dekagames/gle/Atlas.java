package com.dekagames.gle;

import org.json.JSONObject;

public class Atlas {
    Texture texture;
    JSONObject atlasJSON;

    Atlas(Texture tex, JSONObject atlas) {
        texture = tex;
        atlasJSON = atlas;
    }

    public Sprite getSprite(String name, int fps){
        return new Sprite(texture, atlasJSON, name, fps);
    }

    public Font getFont(String name){
        return new Font(texture, atlasJSON, name);
    }
}
