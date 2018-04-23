package com.demo.bobble.keyboard.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;

import com.demo.bobble.keyboard.Model.TextWordModel;
import com.demo.bobble.keyboard.database.DataProvider;
import com.demo.bobble.keyboard.database.KeywordsUtil;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;

//Alarm receiver to send request to server in 10 minutes

public class HttpRequestAlarm extends BroadcastReceiver {

    private static final int REQUEST_CODE = 0;

    private Context context;

    private int timeInterval = 1;

    public HttpRequestAlarm() {
    }

    public HttpRequestAlarm(Context context, int timeInterval) {
        this.context = context;
        this.timeInterval = timeInterval;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        FirebaseAnalytics analytics = FirebaseAnalytics.getInstance(context);
        KeywordsUtil util = new KeywordsUtil(context);

        Cursor cursor = util.getUnsyncedWords();
        ArrayList<TextWordModel> textList = new ArrayList<>();
        if(cursor != null && cursor.moveToFirst()) {
            while (cursor.moveToNext()) {
                TextWordModel model = new TextWordModel();

                Long id = cursor.getLong(cursor
                        .getColumnIndex(DataProvider.EnglishSuggest._ID));
                String word = cursor.getString(cursor
                        .getColumnIndex(DataProvider.EnglishSuggest.KEYWORD));

                model.setId(id);
                model.setWord(word);
                textList.add(model);

                //Sending the firebase event to track the typed words

                Bundle bundle = new Bundle();
                bundle.putString("KBWord", word);
                analytics.logEvent("KB_user_type", bundle);
                Log.d("HttpRequestAlarm", "HttpRequestAlarm msg : "+word);
            }
            updateSyncState(textList,util);
        }

    }

    //update the flag of the tracked words from Suggest DB

    private void updateSyncState(ArrayList<TextWordModel> list , KeywordsUtil util){
        for(TextWordModel model : list){
            util.updateSyncState(model.getId());
        }
    }

    public void start() {
        Intent intentAlarm = new Intent(context, HttpRequestAlarm.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                REQUEST_CODE, intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis(), timeInterval * 1000, pendingIntent);
    }

    public void stop() {
        Intent intentAlarm = new Intent(context, HttpRequestAlarm.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                REQUEST_CODE, intentAlarm, 0);
        AlarmManager alarmManager = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }
}