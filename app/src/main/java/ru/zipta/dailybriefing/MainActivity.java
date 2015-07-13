package ru.zipta.dailybriefing;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.Switch;
import android.widget.TimePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;

public class MainActivity extends ActionBarActivity implements TimePicker.OnTimeChangedListener, CompoundButton.OnCheckedChangeListener {

    private static final String TAG = "MainActivity";

    public static final String HOUROFDAY = "HOUROFDAY";
    public static final String MINUTE = "MINUTE";
    public static final String ACTIVE = "ACTIVE";

    SharedPreferences sharedPref;
    private SharedPreferences.Editor prefsEditor;
    private TimePicker tp;
    private Switch sw;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sw = (Switch) findViewById(R.id.swActive);
        sw.setOnCheckedChangeListener(this);
        tp = (TimePicker) findViewById(R.id.tpTime);
        tp.setOnTimeChangedListener(this);
        tp.setIs24HourView(DateFormat.is24HourFormat(this));

        sharedPref = getPreferences(Context.MODE_PRIVATE);
        prefsEditor = sharedPref.edit();

    }

    @Override
    protected void onStart() {
        super.onStart();
        tp.setCurrentHour(sharedPref.getInt(HOUROFDAY, 9));
        tp.setCurrentMinute(sharedPref.getInt(MINUTE, 0));
        sw.setChecked(sharedPref.getBoolean(ACTIVE, false));
    }

    @Override
    protected void onPause() {
        Intent in = new Intent(this, MyIntentService.class);
        PendingIntent pin = PendingIntent.getService(this, 0, in, 0);
        AlarmManager alarm = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pin);
        if(sharedPref.getBoolean(ACTIVE, false)) {
            alarm.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 10000, pin);
        }
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
        Log.d(TAG, "time to " + hourOfDay + ":" + minute);
        prefsEditor.putInt(HOUROFDAY, hourOfDay);
        prefsEditor.putInt(MINUTE, minute);
        prefsEditor.commit();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Log.d(TAG, "active " + isChecked);
        prefsEditor.putBoolean(ACTIVE, isChecked);
        prefsEditor.commit();
    }
}
