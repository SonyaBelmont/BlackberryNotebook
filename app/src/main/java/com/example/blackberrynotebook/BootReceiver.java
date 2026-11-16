package com.example.blackberrynotebook;

import android.content.*;
public class BootReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context ctx,Intent intent){if(Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())){Store store=new Store(ctx);for(Store.Note n:store.list(""))ReminderReceiver.schedule(ctx,n);}}
}
