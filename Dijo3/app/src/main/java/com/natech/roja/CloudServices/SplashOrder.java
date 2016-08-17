package com.natech.roja.CloudServices;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.natech.roja.R;


public class SplashOrder extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_order);
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar_actionbar2);
        //TextView toolbarTitle = (TextView)findViewById(R.id.toolbar_title);
        //LinearLayout toolbarLayout = (LinearLayout)findViewById(R.id.toolbar_actionbar);
        //toolbarTitle.setText("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        TextView tableNoTV = (TextView)findViewById(R.id.tableNo);
        TextView logTV = (TextView)findViewById(R.id.logID);
        tableNoTV.setText(getIntent().getExtras().getString("tableNo"));
        logTV.setText("Order No: "+getIntent().getExtras().getString("logID"));
       // Blurry.with(this).radius(25).sampling(2).async().animate(500).onto((ViewGroup)findViewById(R.id.background));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_splash_order, menu);
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
