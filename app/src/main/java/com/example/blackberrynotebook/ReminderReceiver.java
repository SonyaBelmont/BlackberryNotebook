package com.example.blackberrynotebook;

import android.app.*;
import android.content.*;
import android.media.RingtoneManager;
import android.os.*;

public class ReminderReceiver extends BroadcastReceiver {
    static final String CHANNEL="note_alarm_channel";

    static void channel(Context ctx){
        if(Build.VERSION.SDK_INT>=26){
            NotificationChannel ch=new NotificationChannel(CHANNEL,"Будильники блокнота",NotificationManager.IMPORTANCE_HIGH);
            ch.setDescription("Будильники и напоминания блокнота");
            ch.enableVibration(true);
            ch.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            ch.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM),null);
            ((NotificationManager)ctx.getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(ch);
        }
    }

    static PendingIntent pending(Context ctx,long id){
        Intent intent=new Intent(ctx,ReminderReceiver.class);
        intent.putExtra("id",id);
        return PendingIntent.getBroadcast(ctx,(int)id,intent,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
    }

    static void schedule(Context ctx,Store.Note note){
        if(note.id==0)return;
        AlarmManager am=(AlarmManager)ctx.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pi=pending(ctx,note.id);
        am.cancel(pi);
        if(!note.alarmOn||note.alarmAt<=System.currentTimeMillis())return;
        if(Build.VERSION.SDK_INT>=31 && !am.canScheduleExactAlarms()) am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,note.alarmAt,pi);
        else am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,note.alarmAt,pi);
    }

    @Override public void onReceive(Context ctx,Intent intent){
        long id=intent.getLongExtra("id",-1);
        Store.Note note=new Store(ctx).find(id);
        if(note==null||!note.alarmOn)return;
        channel(ctx);

        Intent alertIntent=new Intent(ctx,AlarmAlertActivity.class);
        alertIntent.putExtra("id",id);
        alertIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        PendingIntent full=PendingIntent.getActivity(ctx,(int)id,alertIntent,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);

        String title=note.comment.isEmpty()?"Будильник блокнота":note.comment;
        String content=note.body.length()>140?note.body.substring(0,140)+"…":note.body;

        Notification n=new Notification.Builder(ctx,CHANNEL)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(new Notification.BigTextStyle().bigText(content))
            .setFullScreenIntent(full,true)
            .setContentIntent(full)
            .setCategory(Notification.CATEGORY_ALARM)
            .setAutoCancel(false)
            .setOngoing(true)
            .build();

        NotificationManager nm=(NotificationManager)ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify((int)id,n);
        try{ctx.startActivity(alertIntent);}catch(Exception ignored){}
    }
}
