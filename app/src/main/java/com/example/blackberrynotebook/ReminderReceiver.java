package com.example.blackberrynotebook;

import android.app.*;
import android.content.*;
import android.os.*;
import android.media.RingtoneManager;

public class ReminderReceiver extends android.content.BroadcastReceiver {
    static final String CHANNEL="note_alarm_channel";
    static void channel(Context ctx){if(Build.VERSION.SDK_INT>=26){NotificationChannel ch=new NotificationChannel(CHANNEL,"Будильники блокнота",NotificationManager.IMPORTANCE_HIGH);ch.setDescription("Уведомления по установленным будильникам");ch.enableVibration(true);ch.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM),null);((NotificationManager)ctx.getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(ch);}}
    static PendingIntent pending(Context ctx,long id){Intent intent=new Intent(ctx,ReminderReceiver.class);intent.putExtra("id",id);return PendingIntent.getBroadcast(ctx,(int)id,intent,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);}
    static void schedule(Context ctx,Store.Note note){if(note.id==0)return;AlarmManager am=(AlarmManager)ctx.getSystemService(Context.ALARM_SERVICE);PendingIntent pi=pending(ctx,note.id);am.cancel(pi);if(!note.alarmOn||note.alarmAt<=System.currentTimeMillis())return;
        if(Build.VERSION.SDK_INT>=31 && !am.canScheduleExactAlarms()){am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,note.alarmAt,pi);}else am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,note.alarmAt,pi);
    }
    @Override public void onReceive(Context ctx,Intent intent){long id=intent.getLongExtra("id",-1);Store.Note note=new Store(ctx).find(id);if(note==null||!note.alarmOn)return;channel(ctx);Intent open=new Intent(ctx,EditActivity.class);open.putExtra("id",id);open.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);PendingIntent p=PendingIntent.getActivity(ctx,(int)id,open,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        String title=note.comment.isEmpty()?"Напоминание из блокнота":note.comment;String content=note.body.length()>140?note.body.substring(0,140)+"…":note.body;
        Notification n=new Notification.Builder(ctx,CHANNEL).setSmallIcon(android.R.drawable.ic_lock_idle_alarm).setContentTitle(title).setContentText(content).setStyle(new Notification.BigTextStyle().bigText(content)).setContentIntent(p).setAutoCancel(true).setCategory(Notification.CATEGORY_ALARM).build();
        ((NotificationManager)ctx.getSystemService(Context.NOTIFICATION_SERVICE)).notify((int)id,n);
        note.alarmOn=false;new Store(ctx).save(note);
    }
}
