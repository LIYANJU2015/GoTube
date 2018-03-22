package free.studio.tube.gui.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import free.studio.tube.businessobjects.SuperVersions;

/**
 * Created by liyanju on 2018/3/22.
 */

public class InstallReferrerReceiver extends BroadcastReceiver {

    private SuperVersions.InstallReferrerReceiverHandler receiverHandler =
            SuperVersions.createInstallReferrerReceiverHandler();

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            receiverHandler.onHandleIntent(context, intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
