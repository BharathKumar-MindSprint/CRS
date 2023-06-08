package com.olam.digital.ofis.mvvmmodule;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileUtils
{
    public static byte[] convertFileToByteArray(Context context, Uri fileUri) throws IOException {
        ContentResolver contentResolver = context.getContentResolver();
        InputStream inputStream = null;
        ByteArrayOutputStream bos = null;

        try {
            inputStream = contentResolver.openInputStream(fileUri);
            bos = new ByteArrayOutputStream();

            byte[] buffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                bos.write(buffer, 0, bytesRead);
            }

            return bos.toByteArray();
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }

            if (bos != null) {
                bos.close();
            }
        }
    }
}
