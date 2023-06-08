package com.olam.digital.ofis.mvvmmodule;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;

public class PDFUtils
{
    public static byte[] convertPDFToByteArray(String filePath) throws IOException {
        FileInputStream fis = null;
        ByteArrayOutputStream bos = null;

        try {
            fis = new FileInputStream(filePath);
            bos = new ByteArrayOutputStream();

            byte[] buffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = fis.read(buffer)) != -1) {
                bos.write(buffer, 0, bytesRead);
            }

            return bos.toByteArray();
        } finally {
            if (fis != null) {
                fis.close();
            }

            if (bos != null) {
                bos.close();
            }
        }
    }
}
