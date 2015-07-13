package ru.zipta.dailybriefing;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;

public class MainActivityFragment extends Fragment {

    private static final String TAG = "MainActivityFragment";

    public static final Uri CALENDARS_URI = Uri.parse("content://com.android.calendar/calendars");
    private final static Uri EVENTS_URI = Uri.parse("content://com.android.calendar/events");
    private final static Uri TASKS_URI = Uri.parse("content://org.dayup.gtask.data/tasks");
    public static final int GT_TITLE = 1;
    public static final int GT_DATE = 3;
    public static final int CAL_TITLE = 0;
    public static final int CAL_DATE = 1;
    public static final int CAL_ALLDAY = 3;

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        int nid = 0;
        Context context = this.getActivity().getBaseContext();
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

        return inflater.inflate(R.layout.fragment_main, container, false);
    }
}
