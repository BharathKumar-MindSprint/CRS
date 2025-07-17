package com.olam.digital.ofis.downloadsurvey.polygondatadownload.dto;

import android.database.Cursor;


import com.olam.digital.ofis.OFISDBContract;

import java.util.ArrayList;
import java.util.List;

public class PlotCursorToDto {
    public static List<PlotDisplayDto> cursorToDtoFarmerList(Cursor cursor, String name, Long farmerId) {
        List<PlotDisplayDto> plotList = new ArrayList<>();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                PlotDisplayDto.Builder plotDisplayDto = new PlotDisplayDto.Builder();
                plotDisplayDto.setFarmerId(farmerId)
                        .setFarmerName(name)
                        .setPlotId(cursor.getLong(cursor.getColumnIndex(OFISDBContract.PlotEntry.COLUMN_NAME_ID)))
                        .setPlotName(cursor.getString(cursor.getColumnIndexOrThrow(OFISDBContract.PlotEntry.COLUMN_NAME_PLOT_NAME)))
                        .setPlotSize(cursor.getString(cursor.getColumnIndexOrThrow(OFISDBContract.PlotEntry.COLUMN_NAME_POLYGON_SIZE)))
                        .setIsChecked(false);

                plotList.add(plotDisplayDto.build());
            } while (cursor.moveToNext());
        }
        if (cursor != null) {
            cursor.close();
        }
        return plotList;
    }
}