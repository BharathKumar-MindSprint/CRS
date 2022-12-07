package com.olam.digital.ofis.smartnotification.listener;

import android.view.View;

import com.olam.digital.ofis.downloadsurvey.dto.AnswerRequestDTO;
import com.olam.digital.ofis.smartnotification.dto.NotificationDTO;

public interface OnItemNotificationClickListener {

    void onNotificationClick(View view, int position, NotificationDTO notificationDTO);
}
