package free.studio.tube.gui.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import free.studio.tube.businessobjects.SuperVersions;
import free.studio.tube.businessobjects.facebook.InstallReferrerReceiverHandler;

/**
 * Created by liyanju on 2018/3/22.
 */

public class FacebookInstallReferrerReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            InstallReferrerReceiverHandler.onHandleIntent(context, intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
