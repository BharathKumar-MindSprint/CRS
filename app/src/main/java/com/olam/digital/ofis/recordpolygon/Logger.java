package com.olam.digital.ofis.recordpolygon;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;

import android.os.Environment;
import android.util.Log;


public class Logger {

    public static  FileHandler logger = null;
    private static String filename = "polygonTest";

    static boolean isExternalStorageAvailable = false;
    static boolean isExternalStorageWriteable = false;
    static String state = Environment.getExternalStorageState();

    public static void addRecordToLog(String message) {

/*
        File root = Environment.getExternalStorageDirectory();
        File dir = new File(root.getAbsolutePath() + "/OFISPolyTest");
        if(!dir.exists()) {
            Log.d("Dir created ", "Dir created ");
            dir.mkdirs();
        }
        File logFile = new File(root.getAbsolutePath() + "/OFISPolyTest/"+filename+".txt");

        if (!logFile.exists())  {
            try  {
                Log.d("File created ", "File created ");
                logFile.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        try {
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            Date today = new Date();
            SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            String date = format.format(today);
            buf.write(date + " : "+message + "\r\n");
            //buf.append(message);
            buf.newLine();
            buf.flush();
            buf.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
*/
    }
}

