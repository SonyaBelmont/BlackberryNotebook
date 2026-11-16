package com.example.blackberrynotebook;

import android.app.*;
import android.content.*;
import android.media.*;
import android.os.*;
import android.view.*;
import android.widget.*;

public class AlarmAlertActivity extends Activity {
    private Ringtone ringtone;
    private Vibrator vibrator;
    private long noteId;
    private Store store;

    @Override protected void onCreate(Bundle b){
        super.onCreate(b);
        getWindow().setStatusBarColor(Ui.EDIT_BG);
        getWindow().setNavigationBarColor(Ui.EDIT_BG);
        store=new Store(this);
        noteId=getIntent().getLongExtra("id",-1);
        Store.Note note=store.find(noteId);
        if(note==null){finish();return;}

        LinearLayout root=Ui.column(this);
        root.setBackgroundColor(Ui.EDIT_BG);
        Ui.pad(root,this,20);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        setContentView(root);

        TextView title=Ui.text(this,note.comment.isEmpty()?"Будильник":"Будильник: "+note.comment,24,Ui.FG);
        Ui.heading(title);
        title.setGravity(Gravity.CENTER);
        root.addView(title,new LinearLayout.LayoutParams(-1,-2));

        TextView body=Ui.text(this,note.body,18,Ui.FG);
        body.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams blp=new LinearLayout.LayoutParams(-1,-2);blp.topMargin=Ui.dp(this,18);root.addView(body,blp);

        TextView info=Ui.text(this,"Запись из Блокнота",13,Ui.MUTED);
        info.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams ilp=new LinearLayout.LayoutParams(-1,-2);ilp.topMargin=Ui.dp(this,10);root.addView(info,ilp);

        TextView open=Ui.outlineButton(this,"Открыть запись");
        LinearLayout.LayoutParams olp=new LinearLayout.LayoutParams(-1,-2);olp.topMargin=Ui.dp(this,22);root.addView(open,olp);
        open.setOnClickListener(v->{
            Intent i=new Intent(this,EditActivity.class);i.putExtra("id",noteId);startActivity(i);
        });

        TextView snooze=Ui.outlineButton(this,"Отложить на 5 минут");
        LinearLayout.LayoutParams slp=new LinearLayout.LayoutParams(-1,-2);slp.topMargin=Ui.dp(this,12);root.addView(snooze,slp);
        snooze.setOnClickListener(v->snooze());

        TextView stop=Ui.button(this,"Отключить");
        LinearLayout.LayoutParams stp=new LinearLayout.LayoutParams(-1,-2);stp.topMargin=Ui.dp(this,12);root.addView(stop,stp);
        stop.setOnClickListener(v->stopAlarm());

        startAlert();
    }

    void startAlert(){
        try{
            android.net.Uri uri=RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if(uri==null) uri=RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            ringtone=RingtoneManager.getRingtone(this,uri);
            if(ringtone!=null) ringtone.play();
        }catch(Exception ignored){}
        vibrator=(Vibrator)getSystemService(VIBRATOR_SERVICE);
        if(vibrator!=null){
            if(Build.VERSION.SDK_INT>=26) vibrator.vibrate(VibrationEffect.createWaveform(new long[]{0,500,300,500},0));
            else vibrator.vibrate(new long[]{0,500,300,500},0);
        }
    }

    void stopAlertHardware(){
        try{if(ringtone!=null && ringtone.isPlaying())ringtone.stop();}catch(Exception ignored){}
        try{if(vibrator!=null)vibrator.cancel();}catch(Exception ignored){}
        NotificationManager nm=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        if(nm!=null) nm.cancel((int)noteId);
    }

    void stopAlarm(){
        stopAlertHardware();
        Store.Note note=store.find(noteId);
        if(note!=null){note.alarmOn=false;store.save(note);}
        finish();
    }

    void snooze(){
        stopAlertHardware();
        Store.Note note=store.find(noteId);
        if(note!=null){note.alarmOn=true;note.alarmAt=System.currentTimeMillis()+5*60*1000L;store.save(note);ReminderReceiver.schedule(this,note);}        
        finish();
    }

    @Override public void onBackPressed(){}
    @Override protected void onDestroy(){stopAlertHardware();super.onDestroy();}
}
