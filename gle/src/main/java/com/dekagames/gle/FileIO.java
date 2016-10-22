package com.dekagames.gle;

import android.content.res.AssetManager;
import android.os.Environment;

import java.io.*;

public class FileIO {
    public static AssetManager assetManager;
    public static String  externalStoragePath;


    public FileIO(AssetManager assetManager){
        this.assetManager = assetManager;
        this.externalStoragePath = Environment.getExternalStorageDirectory().getAbsolutePath()+ File.separator;
    }

    public InputStream readAsset(String filename) {
        InputStream is=null;
        try {
            is = assetManager.open(filename);
        } catch (IOException e) {
            Log.exception("Could not read asset " + filename, e);
        }
        return is;
    }

    public InputStream readFile(String filename) {
        InputStream is = null;
        try {
            is = new FileInputStream(externalStoragePath+filename);
        } catch (FileNotFoundException e) {
            Log.exception("Could not read file " + filename, e);
        }
        return is;
    }

    public OutputStream writeFile(String filename) {
        OutputStream os = null;
        try {
            os = new FileOutputStream(externalStoragePath+filename);
        } catch (FileNotFoundException e) {
            Log.exception("Could not write file " + filename, e);
        }
        return os;
    }

    public boolean isFileExists(String filename) {
        File f = new File(externalStoragePath+filename);
        return (!f.isDirectory() && f.exists());
    }


    public boolean isDirectoryExists(String dirname){
        File f = new File(externalStoragePath+dirname);
        return (f.isDirectory() && f.exists());
    }

    public boolean createDirectory(String dirname){
        File f = new File(externalStoragePath+dirname);
        return f.mkdirs();
    }
}
