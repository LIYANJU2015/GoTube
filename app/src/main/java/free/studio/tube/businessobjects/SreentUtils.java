package free.studio.tube.businessobjects;

import free.studio.tube.app.GoTubeApp;

/**
 * Created by liyanju on 2018/3/21.
 */

public class SreentUtils {

    public static int dp2px(float dpValue) {
        float scale = GoTubeApp.getContext().getResources().getDisplayMetrics().density;
        return (int)(dpValue * scale + 0.5F);
    }
}
