package com.olam.digital.ofis;

import android.content.Context;
import android.content.res.Resources;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by ben on 04/11/2016.
 */
public class companionFirebaseInstanceIdService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        sendRegistrationToServer(refreshedToken);
    }

    private void sendRegistrationToServer(final String token) {
        Context context = getApplicationContext();
        Resources res = getResources();
        NetworkLayer nl = new NetworkLayer(context, res);

        if (nl.isPhoneOnline()) {
            nl.postFirebaseToken(token);
        }
    }
}
