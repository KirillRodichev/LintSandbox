package com.kiro.lintsandbox;

import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import java.io.File;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

public class DetectorTest {
    /*public void saveCredentials() {
        File uinfo = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/.uinfo.txt");
    }*/

    public void onCreate(SQLiteDatabase db) {
        db.execSQL("INSERT INTO notes(title,note) VALUES ('office', '10 Meetings. 5 Calls. Lunch with CEO');");
    }

    public Random getRandom() {
        return new Random();
    }

    public void cipher() throws NoSuchPaddingException, NoSuchAlgorithmException {
        Cipher des = Cipher.getInstance("DES/CBC/NoPadding");
    }
}