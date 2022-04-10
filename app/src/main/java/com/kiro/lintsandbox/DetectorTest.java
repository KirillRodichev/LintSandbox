package com.kiro.lintsandbox;

import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import java.io.File;
import java.util.Random;

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
}
