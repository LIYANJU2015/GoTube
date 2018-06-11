package free.studio.tube.businessobjects;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.view.View;

import free.studio.tube.app.GoTubeApp;

/**
 * Created by liyanju on 2018/3/21.
 */

public class SreentUtils {

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
}
