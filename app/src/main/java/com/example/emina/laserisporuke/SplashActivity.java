package com.example.emina.laserisporuke;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

public class SplashActivity extends Activity {
    private int Connect;
    private String text = "";
    private String rezultat = "";

    int id = -1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        //overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_right);
        TextView parkEasily = (TextView) findViewById(R.id.ParkEasily);
        Animation fade1 = AnimationUtils.loadAnimation(SplashActivity.this, R.anim.fade_in);
        parkEasily.startAnimation(fade1);
        parkEasily.startAnimation(fade1);
        int SPLASH_TIME_OUT = 1500;
        new Handler().postDelayed(new Runnable() {

			/*
             * Showing splash screen with a timer. This will be useful when you
			 * want to show case your app logo / company
			 */

            @Override
            public void run() {
                // This method will be executed once the timer is over
                // Start your app main activity
                Intent i = new Intent(SplashActivity.this, Intro.class);
                startActivity(i);
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_right);
                // close this activity
                finish();
            }
        }, SPLASH_TIME_OUT);

        fade1.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                SplashActivity.this.finish();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationStart(Animation animation) {
            }
        });
    }


}
