package ru.zipta.dailybriefing;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Date;
import java.util.HashSet;


/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    private static final String TAG = "MainActivityFragment";

    public static final Uri CALENDARS_URI = Uri.parse("content://com.android.calendar/calendars");
    private final static Uri EVENTS_URI = Uri.parse("content://com.android.calendar/events");
    private final static Uri TASKS_URI = Uri.parse("content://org.dayup.gtask.data/tasks");
    public static final int GT_TITLE = 1;
    public static final int GT_DATE = 3;

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Context context = this.getActivity().getBaseContext();
        ContentResolver cr = context.getContentResolver();

        //gtasks

        Cursor cursor;

        cursor = cr.query(TASKS_URI, null, null, new String[]{"-1", "true"}, null);

        String title = null;
        Date date = null;

        if (cursor != null) {
            while (cursor.moveToNext()) {
                title = cursor.getString(GT_TITLE);
                date = new Date(cursor.getLong(GT_DATE));
                Log.d(TAG, "gtasks title: " + title + " date " + date.toString());
            }
        } else {
            Log.d(TAG, "no tasks");
        }

        cursor.close();

        //calendar

        cursor = cr.query(CALENDARS_URI,
                (new String[]{"_id"}), null, null, null);

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

        for (String id : calendarIds) {

            long now = new Date().getTime();
            long tomorrow = now + DateUtils.DAY_IN_MILLIS * 8;

            cursor = cr.query(EVENTS_URI, new String[]{"title", "dtstart", "dtend", "allDay"},
                    "dtstart>=" + now
                            //+ " and dtend<" + tomorrow
                            + " and calendar_id=" + id, null, null);

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    title = cursor.getString(0);
                    date = new Date(cursor.getLong(1));
                    Log.d(TAG, "calendar title: " + title + " date " + date.toString());
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