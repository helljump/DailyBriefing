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
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.Switch;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;

public class MainActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {

    private static final String TAG = "MainActivity";

    public static final String HOUROFDAY = "HOUROFDAY";
    public static final String MINUTE = "MINUTE";
    public static final String ACTIVE = "ACTIVE";

    SharedPreferences sharedPref;

    private TimePicker tp;
    private Switch sw;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sw = (Switch) findViewById(R.id.swActive);
        sw.setOnCheckedChangeListener(this);
        tp = (TimePicker) findViewById(R.id.tpTime);
        tp.setIs24HourView(DateFormat.is24HourFormat(this));

        sharedPref = getPreferences(Context.MODE_PRIVATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        tp.setCurrentHour(sharedPref.getInt(HOUROFDAY, 9));
        tp.setCurrentMinute(sharedPref.getInt(MINUTE, 0));
        sw.setChecked(sharedPref.getBoolean(ACTIVE, false));
    }

    @Override
    protected void onPause() {
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
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Log.d(TAG, "active " + isChecked);

        SharedPreferences.Editor prefsEditor = sharedPref.edit();

        prefsEditor.putBoolean(ACTIVE, isChecked);
        prefsEditor.putInt(HOUROFDAY, tp.getCurrentHour());
        prefsEditor.putInt(MINUTE, tp.getCurrentMinute());
        prefsEditor.apply();

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, tp.getCurrentHour());
        calendar.set(Calendar.MINUTE, tp.getCurrentMinute());

        Intent in = new Intent(this, MyIntentService.class);
        PendingIntent pin = PendingIntent.getService(this, 0, in, 0);
        AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pin);
        if(isChecked) {
            alarm.setInexactRepeating(
                    AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pin);
            Toast.makeText(getBaseContext(), "Service is enabled", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getBaseContext(), "Service is disabled", Toast.LENGTH_SHORT).show();
        }
    }
}
