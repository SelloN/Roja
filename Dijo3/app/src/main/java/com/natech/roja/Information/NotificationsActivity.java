package com.natech.roja.Information;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.support.v7.widget.SwitchCompat;
import android.widget.TextView;

import com.natech.roja.MainActivity;
import com.natech.roja.R;
import com.natech.roja.Utilities.CommonIdentifiers;

@SuppressWarnings("ConstantConditions")
public class NotificationsActivity extends AppCompatActivity {

    private SharedPreferences.Editor settingsEditor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);
        Toolbar toolbar = (Toolbar)findViewById(R.id.shadowToolBar);
        LinearLayout toolbarLayout = (LinearLayout)findViewById(R.id.toolbar_actionbar);
        TextView toolbarTitle = (TextView)findViewById(R.id.toolbar_title);
        toolbarTitle.setText("Notifications");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        final Animation toolbarAnim = AnimationUtils.loadAnimation(this, R.anim.abc_slide_in_top);
        final Animation contentAnim = AnimationUtils.loadAnimation(this, R.anim.abc_slide_in_bottom);
        toolbarLayout.startAnimation(toolbarAnim);
        final RelativeLayout content = (RelativeLayout)findViewById(R.id.contentPanel);
        SwitchCompat reviewSwitch = (SwitchCompat)findViewById(R.id.reviewSwitch);

        toolbarAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                content.setVisibility(View.VISIBLE);
                content.startAnimation(contentAnim);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        String SETTINGS = "settings";
        SharedPreferences settingsFile = getSharedPreferences(SETTINGS, MODE_PRIVATE);
        settingsEditor = settingsFile.edit();

        Boolean isReviewNotifications;

        if(settingsFile.contains(CommonIdentifiers.getReviewNotifications())) {
            isReviewNotifications = settingsFile.getBoolean(CommonIdentifiers.getReviewNotifications(), false);
            Log.i("", "Contains settings");
        }
        else
            isReviewNotifications = true;

        if(isReviewNotifications)
            reviewSwitch.setChecked(true);
        else
            reviewSwitch.setChecked(false);

        reviewSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(compoundButton.isChecked()){
                    settingsEditor.putBoolean(CommonIdentifiers.getReviewNotifications(),true);
                    MainActivity.mainActivity.prepareReviewsNotifications();
                    settingsEditor.apply();
                }
                else{
                    MainActivity.mainActivity.stopReviewsNotifications();
                    settingsEditor.putBoolean(CommonIdentifiers.getReviewNotifications(),false);
                    settingsEditor.apply();
                }
            }
        });


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
       // getMenuInflater().inflate(R.menu.menu_notifications, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id){
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
