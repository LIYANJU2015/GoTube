package free.studio.tube.gui.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

import free.rm.gotube.R;

/**
 * Created by liyanju on 2018/3/22.
 */

public class SplashActivity extends AppCompatActivity{

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.activity_fade_out);
    }

    @Override
    protected void onResume() {
        super.onResume();
        startMain();
    }

    private void startMain() {
        getWindow().getDecorView().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.activity_fade_in, 0);
                finish();
            }
        }, 500);

    }
}
