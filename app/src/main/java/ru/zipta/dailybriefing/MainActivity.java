package ru.zipta.dailybriefing;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.Context;
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

    private static final String TAG = "MainActivityFragment";

    public static final Uri CALENDARS_URI = Uri.parse("content://com.android.calendar/calendars");
    private final static Uri EVENTS_URI = Uri.parse("content://com.android.calendar/events");
    private final static Uri TASKS_URI = Uri.parse("content://org.dayup.gtask.data/tasks");
    public static final int GT_TITLE = 1;
    public static final int GT_DATE = 3;
    public static final int CAL_TITLE = 0;
    public static final int CAL_DATE = 1;
    public static final int CAL_ALLDAY = 3;
    public static final String HOUROFDAY = "HOUROFDAY";
    public static final String MINUTE = "MINUTE";
    public static final String ACTIVE = "ACTIVE";

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
        tp.setIs24HourView(DateFormat.is24HourFormat(this));
        tp.setOnTimeChangedListener(this);

        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        prefsEditor = sharedPref.edit();
        tp.setCurrentHour(sharedPref.getInt(HOUROFDAY, 9));
        tp.setCurrentMinute(sharedPref.getInt(MINUTE, 0));

        int nid = 0;
        Context context = this.getBaseContext();
        ContentResolver cr = context.getContentResolver();
        NotificationManager nfm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        java.text.DateFormat sdf = SimpleDateFormat.getDateTimeInstance();

        Date now = new Date();

        Calendar c = Calendar.getInstance();
        c.setTime(now);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);

        Log.d(TAG, "now: " + c.getTime());

        long today = c.getTime().getTime();
        c.add(Calendar.HOUR_OF_DAY, 23);
        c.add(Calendar.MINUTE, 59);
        c.add(Calendar.SECOND, 59);
        long tomorrow = c.getTime().getTime();

        //gtasks

        Cursor cursor;

        cursor = cr.query(TASKS_URI, null, null, new String[]{"-1", "false"}, null);

        String title = null;
        Long date;
        Notification nf;
        Notification.Builder nfb = new Notification.Builder(context);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                title = cursor.getString(GT_TITLE);
                date = cursor.getLong(GT_DATE);
                if (date < today) {
                    Log.d(TAG, "gtasks title: " + title + " date " + new Date(date));
                    nfb.setContentTitle(title)
                            .setSmallIcon(android.R.drawable.ic_menu_my_calendar)
                            .setAutoCancel(false);
                    if(date != 0){
                        nfb.setContentText("at " + sdf.format(new Date(date)));
                    } else {
                        nfb.setContentText("at all day");
                    }
                    nf = nfb.build();
                    nfm.notify(nid, nf);
                    nid ++;
                }
            }
            cursor.close();
        } else {
            Log.d(TAG, "no tasks");
        }

        //calendar

        cursor = cr.query(CALENDARS_URI, (new String[]{"_id"}), null, null, null);
        HashSet<String> calendarIds = new HashSet<String>();
        if (cursor.getCount() > 0) {
            String _id;
            String displayName;
            Boolean selected;
            while (cursor.moveToNext()) {
                _id = cursor.getString(0);
                calendarIds.add(_id);
            }
        }
        cursor.close();

        long allday;

        for (String id : calendarIds) {
            cursor = cr.query(EVENTS_URI, new String[]{"title", "dtstart", "dtend", "allDay"},
                    "dtstart>=" + today + " and dtstart<" + tomorrow + " and calendar_id=" + id,
                    null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    title = cursor.getString(CAL_TITLE);
                    date = cursor.getLong(CAL_DATE);
                    allday = cursor.getLong(CAL_ALLDAY);
                    Log.d(TAG, "calendar title: " + title + " date " + new Date(date) + " allday " + allday);
                    nfb.setContentTitle(title)
                            .setSmallIcon(android.R.drawable.ic_menu_my_calendar)
                            .setAutoCancel(false);
                    if(allday == 0){
                        nfb.setContentText("at " + sdf.format(new Date(date)));
                    } else {
                        nfb.setContentText("at all day");
                    }
                    nf = nfb.build();
                    nfm.notify(nid, nf);
                    nid ++;
                }
            } else {
                Log.d(TAG, "no events");
            }
            assert cursor != null;
            cursor.close();
        }

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
