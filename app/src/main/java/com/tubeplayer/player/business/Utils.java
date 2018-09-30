package com.tubeplayer.player.business;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.display.DisplayManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.annotation.StringRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.Display;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.tube.playtube.BuildConfig;
import com.tube.playtube.R;
import com.tubeplayer.player.app.TubeApp;

import static android.content.Intent.FLAG_GRANT_PREFIX_URI_PERMISSION;
import static android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION;

/**
 * Created by liyanju on 2018/3/21.
 */

public class Utils {


    public static final String NATIVE_AD_ID = "";
    public static final String CHAPING_COMMON_AD = "";
    public static final String CHAPING_HIGH_AD = "";
    public static final String CHAPING_HIGH_AD2 = "";


//    public static final String NATIVE_AD_ID = "614083172292849_675113536189812";
//    public static final String CHAPING_COMMON_AD = "614083172292849_614084035626096";
//    public static final String CHAPING_HIGH_AD = "614083172292849_614084402292726";
//    public static final String CHAPING_HIGH_AD2 = "614083172292849_675114572856375";


    public static final long CHAPING_INMOBI = 1536250315208L;
    public static final long BANNER_INMOBI = 1537224062338L;
    public static final String ACCOUNT_ID = "5155d82a00cc4aaa9e83bf604f1dbd10";
    public static final long BANNER_INMOBI2 = 1538508419087L;

    public static final ExecutorService sExecutorService2 = Executors.newSingleThreadExecutor();
    public static final ExecutorService sExecutorService = Executors.newSingleThreadExecutor();

    private static Handler sHandler = new Handler(Looper.getMainLooper());

    public static void runUIThread(Runnable runnable) {
        sHandler.post(runnable);
    }

    public static void runSingleThread(Runnable runnable) {
        sExecutorService2.execute(runnable);
    }

    public static int dp2px(float dpValue) {
        float scale = TubeApp.getContext().getResources().getDisplayMetrics().density;
        return (int)(dpValue * scale + 0.5F);
    }

    private static boolean isEmulator(Context context) {
        try {
            return Build.FINGERPRINT.startsWith("generic")
                    || Build.FINGERPRINT.toLowerCase().contains("vbox")
                    || Build.FINGERPRINT.toLowerCase().contains("test-keys")
                    || Build.MODEL.contains("google_sdk")
                    || Build.MODEL.contains("Emulator")
                    || Build.MODEL.contains("Android SDK built for x86")
                    || Build.MANUFACTURER.contains("Genymotion")
                    || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                    || "google_sdk".equals(Build.PRODUCT);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * @return true 开启调试，false 未开启调试
     * @author James
     * @Description 是否是usb调试模式
     */
    @TargetApi(3)
    public static boolean isAdbDebugEnable(Context mContext) {
        boolean enableAdb = (Settings.Secure.getInt(
                mContext.getContentResolver(), android.provider.Settings.Global.ADB_ENABLED, 0) > 0);
        return enableAdb;
    }

    /**
     * 不是普通用户
     * 设备开启Debug模式，模拟器，以及ROOT的手机都认为不是普通玩家
     *
     * @return
     */
    public static boolean isNotCommongUser() {
        if (BuildConfig.DEBUG) {
            return false;
        }
        Context context = TubeApp.getContext();
        return isAdbDebugEnable(context) || isEmulator(context) || isRoot();
    }

    public static void transparence(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            View decorView = activity.getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            decorView.setSystemUiVisibility(option);
            activity.getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
    }

    public static boolean isRoot(){
        boolean bool = false;

        try{
            if ((!new File("/system/bin/su").exists())
                    && (!new File("/system/xbin/su").exists())){
                bool = false;
            } else {
                bool = true;
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return bool;
    }

    public static boolean isScreenOn() {
        try {
            if (android.os.Build.VERSION.SDK_INT >= 20) {
                // I'm counting
                // STATE_DOZE, STATE_OFF, STATE_DOZE_SUSPENDED
                // all as "OFF"
                DisplayManager dm = (DisplayManager) TubeApp.getContext().getSystemService(Context.DISPLAY_SERVICE);
                Display[] displays = dm.getDisplays();
                for (Display display : displays) {
                    if (display.getState() == Display.STATE_ON
                            || display.getState() == Display.STATE_UNKNOWN) {
                        return true;
                    }
                }
                return false;
            }

            // If you use less than API20:
            PowerManager powerManager = (PowerManager) TubeApp.getContext().getSystemService(Context.POWER_SERVICE);
            if (powerManager.isScreenOn()) {
                return true;
            }
            return false;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return true;
    }

    public static void showLongToastSafe(final @StringRes int resId) {
        sHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(TubeApp.getContext(), resId, Toast.LENGTH_LONG).show();
            }
        });
    }

    public static void playDownloadVideo(Context context, Uri uri) {
        try {
            Intent intent = new Intent();
            intent.setAction(android.content.Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "video/mp4");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(FLAG_GRANT_READ_URI_PERMISSION);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                intent.addFlags(FLAG_GRANT_PREFIX_URI_PERMISSION);
            }
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Utils.showLongToastSafe(R.string.player_not_install);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static boolean checkAndRequestPermissions(Activity activity) {
        try {
            ArrayList<String> permissionList = new ArrayList<>();
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            return checkAndRequestPermissions(activity, permissionList);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return true;
    }

    private static boolean checkAndRequestPermissions(Activity activity, ArrayList<String> permissionList) {
        ArrayList<String> list = new ArrayList<>(permissionList);
        Iterator<String> it = list.iterator();
        while (it.hasNext()) {
            String permission = it.next();
            //检查权限是否已经申请
            int hasPermission = ContextCompat.checkSelfPermission(activity, permission);
            if (hasPermission == PackageManager.PERMISSION_GRANTED) {
                it.remove();
            }
        }

        if (list.size() == 0) {
            return true;
        }
        String[] permissions = list.toArray(new String[0]);
        //正式请求权限
        ActivityCompat.requestPermissions(activity, permissions, 101);
        return false;
    }
}
