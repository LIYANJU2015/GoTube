package free.studio.tube.businessobjects;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.StringRes;
import android.support.v4.content.FileProvider;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import free.rm.gotube.R;
import free.studio.tube.app.GoTubeApp;

import static android.content.Intent.FLAG_GRANT_PREFIX_URI_PERMISSION;
import static android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION;

/**
 * Created by liyanju on 2018/3/21.
 */

public class Utils {

    public static final ExecutorService sExecutorService2 = Executors.newSingleThreadExecutor();
    public static final ExecutorService sExecutorService = Executors.newSingleThreadExecutor();

    private static Handler sHandler = new Handler(Looper.getMainLooper());

    public static void runSingleThread(Runnable runnable) {
        sExecutorService2.execute(runnable);
    }

    public static int dp2px(float dpValue) {
        float scale = GoTubeApp.getContext().getResources().getDisplayMetrics().density;
        return (int)(dpValue * scale + 0.5F);
    }

    public static void transparence(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            View decorView = activity.getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            decorView.setSystemUiVisibility(option);
            activity.getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
    }

    public static void showLongToastSafe(final @StringRes int resId) {
        sHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(GoTubeApp.getContext(), resId, Toast.LENGTH_LONG).show();
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
}
