package com.olam.digital.ofis.smartnotification.listener;

import android.view.View;

import com.olam.digital.ofis.downloadsurvey.dto.AnswerRequestDTO;
import com.olam.digital.ofis.smartnotification.dto.ModuleListDTO;
import com.olam.digital.ofis.smartnotification.dto.NotificationDTO;

public interface OnItemNotificationClickListener {

    void onNotificationClick(View view, int position, NotificationDTO notificationDTO);

    void onModuleClick(View view, int position, ModuleListDTO moduleListDTO);
}


