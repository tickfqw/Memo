package com.example.tick.memo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by tick on 2016/4/11.
 */
public class AlarmNote extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
        String messageTitle=intent.getStringExtra("messageTitle");
        String messageContent=intent.getStringExtra("messageContent");
        Intent in=new Intent();
        in.setClass(context,Alarm.class);
        in.putExtra("messageTitle", messageTitle);
        in.putExtra("messageContent", messageContent);
        in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(in);
    }
}
