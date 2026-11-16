package com.example.blackberrynotebook;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.os.*;
import android.graphics.*;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import android.text.*;
import java.util.*;

public class MainActivity extends Activity {
    Store store; LinearLayout list; EditText search;
    @Override public void onCreate(Bundle b){super.onCreate(b);getWindow().setStatusBarColor(Ui.BG);getWindow().setNavigationBarColor(Ui.BG);store=new Store(this);ReminderReceiver.channel(this);if(Build.VERSION.SDK_INT>=33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},42);render();}
    @Override protected void onResume(){super.onResume();if(list!=null)populate();}
    void render(){
        LinearLayout root=Ui.column(this);root.setBackgroundColor(Ui.BG);Ui.pad(root,this,16);setContentView(root);
        LinearLayout heading=Ui.row(this);TextView name=Ui.text(this,"Блокнот",28,Ui.FG);Ui.heading(name);heading.addView(name,new LinearLayout.LayoutParams(0,-2,1));
        TextView plus=Ui.button(this,"＋");heading.addView(plus,new LinearLayout.LayoutParams(Ui.dp(this,54),Ui.dp(this,52)));plus.setOnClickListener(v->startActivity(new Intent(this,EditActivity.class)));
        root.addView(heading);
        search=new EditText(this);search.setSingleLine(true);search.setTextSize(16);search.setTextColor(Ui.FG);search.setHintTextColor(Ui.MUTED);search.setHint("Поиск по записям");search.setBackground(Ui.shape(Ui.CARD,14,this));search.setPadding(Ui.dp(this,14),0,Ui.dp(this,14),0);
        LinearLayout.LayoutParams slp=new LinearLayout.LayoutParams(-1,Ui.dp(this,48));slp.topMargin=Ui.dp(this,14);root.addView(search,slp);
        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);list=Ui.column(this);scroll.addView(list);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,0,1);lp.topMargin=Ui.dp(this,12);root.addView(scroll,lp);
        search.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int st,int c,int af){}public void onTextChanged(CharSequence s,int st,int before,int count){populate();}public void afterTextChanged(Editable e){}});populate();
    }
    void populate(){if(list==null)return;list.removeAllViews();String q=search==null?"":search.getText().toString().trim();List<Store.Note> notes=store.list(q);
        if(notes.isEmpty()){TextView empty=Ui.text(this,"Пока нет записей. Нажмите ＋, чтобы начать.",16,Ui.MUTED);Ui.pad(empty,this,20);list.addView(empty);return;}
        for(Store.Note n:notes){LinearLayout card=Ui.column(this);card.setBackground(Ui.shape(Ui.CARD,17,this));Ui.pad(card,this,15);card.setAlpha(n.done?0.46f:1f);
            String body=n.body.trim();TextView preview=Ui.text(this,(n.done?"Сделано  ·  ":"")+(body.length()>180?body.substring(0,180)+"…":body),17,Ui.FG);preview.setMaxLines(4);preview.setEllipsize(android.text.TextUtils.TruncateAt.END);card.addView(preview);
            String aux=Ui.date(n.created)+(n.alarmOn?"   ◷ "+Ui.date(n.alarmAt):"")+(n.people.isEmpty()?"":"   · "+n.people);
            TextView meta=Ui.text(this,aux,12,Ui.MUTED);LinearLayout.LayoutParams mlp=new LinearLayout.LayoutParams(-1,-2);mlp.topMargin=Ui.dp(this,10);card.addView(meta,mlp);
            LinearLayout.LayoutParams clp=new LinearLayout.LayoutParams(-1,-2);clp.bottomMargin=Ui.dp(this,10);list.addView(card,clp);
            card.setOnClickListener(v->{Intent i=new Intent(this,EditActivity.class);i.putExtra("id",n.id);startActivity(i);});
        }
    }
}
