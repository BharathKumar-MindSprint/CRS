package com.webpoint0.ofiscompanion;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.content.res.Resources;
import android.os.Bundle;

/**
 * Created by ben on 08/11/2016.
 */

class SyncAdapter extends AbstractThreadedSyncAdapter {

    private final NetworkLayer nl;

    SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        nl = new NetworkLayer(context, Resources.getSystem());
    }

    SyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        nl = new NetworkLayer(context, Resources.getSystem());
    }

    @Override
    public void onPerformSync(Account account, Bundle bundle, String s, ContentProviderClient contentProviderClient, SyncResult syncResult) {
        if (nl.isPhoneOnline()) {
            nl.syncTasks(OFISDBContract.Task.COLUMN_NAME_SYNCED);
            nl.syncTasks(OFISDBContract.Task.COLUMN_NAME_DELIVERY_CONFIRMED);
        }
    }

}
