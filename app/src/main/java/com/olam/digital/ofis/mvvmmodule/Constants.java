package com.olam.digital.ofis.mvvmmodule;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.widget.Toast;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.olam.digital.ofis.mvvmmodule.roomdb.entity.DmSurveyAnswerTable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Constants {


    public static String MODULE_ID = "0";
    public static String MODULE_NAME = "0";
    public static String FARMER_ID = "0";
    public static String FARMER_NAME = "0";
    public static String FARMER_GROUP_ID = "0";
    public static String ENUMERATOR_ID = "0";
    public static int ATTEMPT = 0;
    public static Boolean EDITABLE = false;
    public static ArrayList<String> questionIds = new ArrayList<>();
    public static ArrayList<String> questionIdsSection = new ArrayList<>();
    public static ArrayList<String> mandatedQuestionIds = new ArrayList<>();
    public static String RED = "RED";
    public static String GREEN = "GREEN";
    public static final String ORANGE = "YELLOW";
    public static String RESOLVED = "Resolved";
    public static String NOTRESOLVED = "NOT Resolved";
    public static String TotalActionPlanCount = "0";
    public static int capture_count;
    public static ArrayList<String> questionIdsFollowUp = new ArrayList<>();
    public static int PLOT_ID = 0;
    public static final String FDP = "FDP";
    public static final String CLMRS = "CLMRS";
    public static final String EUDR = "EUDR";
    public static final String PA = "PA";
    public static String CHECKBOX = "checkbox";
    public static String SIGNATURE = "signature";
    public static String COMPLIANT = "Compliant";
    public static String NON_COMPLIANT = "Non Compliant";
    public static String SATISFACTORY = "Satisfactory";


    public static Set<Integer> getUniqueQuestion(List<DmSurveyAnswerTable> dmSurveyAnswerTableList) {
        Set<Integer> uniqueQuestionIds = new HashSet<>();
        for (DmSurveyAnswerTable answer : dmSurveyAnswerTableList) {
            uniqueQuestionIds.add(Integer.valueOf(answer.questionID));
        }
        return uniqueQuestionIds;
    }

    public static String getFileExtension(String fileName) {
        String extension = null;
        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex != -1) {
            extension = fileName.substring(dotIndex + 1);
        }
        return extension;
    }

    public static String getFileName(Uri uri, Context context) {
        String fileName = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        fileName = cursor.getString(nameIndex);
                    }
                }
            }
        }
        if (fileName == null) {
            fileName = uri.getLastPathSegment();
        }
        return fileName;
    }

    public static File compressPdf(Uri uri, Context context, String fileName) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            File outputFile = new File(context.getCacheDir(), fileName);
            OutputStream outputStream = new FileOutputStream(outputFile);
            PdfReader reader = new PdfReader(inputStream);
            PdfStamper stamper = new PdfStamper(reader, outputStream);
            stamper.getWriter().setCompressionLevel(9);
            int total = reader.getNumberOfPages() + 1;

            for (int i = 1; i < total; i++) {
                reader.setPageContent(i + 1, reader.getPageContent(i + 1));
            }

            stamper.setFullCompression();
            stamper.close();
            reader.close();

            outputStream.flush();
            outputStream.close();
            inputStream.close();

            return outputFile;

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Error compressing PDF", Toast.LENGTH_SHORT).show();
            return null;
        } catch (DocumentException e) {
            throw new RuntimeException(e);
        }
    }

}
