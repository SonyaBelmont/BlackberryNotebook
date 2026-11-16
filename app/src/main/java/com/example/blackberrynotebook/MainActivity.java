package com.example.blackberrynotebook;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.os.*;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import android.text.*;
import java.util.*;

public class MainActivity extends Activity {
    Store store; LinearLayout list; EditText search; LinearLayout searchWrap; TextView searchBtn;

    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        getWindow().setStatusBarColor(Ui.BG);
        getWindow().setNavigationBarColor(Ui.BG);
        store=new Store(this);
        ReminderReceiver.channel(this);
        if(Build.VERSION.SDK_INT>=33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},42);
        render();
    }

    @Override protected void onResume(){super.onResume();if(list!=null){((View)findViewById(1001)).setBackground(Ui.listBackground(this));populate();}}

    void render(){
        LinearLayout root=Ui.column(this);
        root.setId(1001);
        root.setBackground(Ui.listBackground(this));
        Ui.pad(root,this,16);
        setContentView(root);

        LinearLayout heading=Ui.row(this);
        TextView name=Ui.text(this,"Блокнот",28,Ui.FG);Ui.heading(name);
        heading.addView(name,new LinearLayout.LayoutParams(0,-2,1));
        TextView palette=Ui.iconButton(this,"◐");
        LinearLayout.LayoutParams ip=new LinearLayout.LayoutParams(Ui.dp(this,44),Ui.dp(this,40));ip.rightMargin=Ui.dp(this,8);
        heading.addView(palette,ip);
        palette.setOnClickListener(v->themeDialog());
        searchBtn=Ui.iconButton(this,"⌕");
        heading.addView(searchBtn,new LinearLayout.LayoutParams(Ui.dp(this,44),Ui.dp(this,40)));
        searchBtn.setOnClickListener(v->toggleSearch());
        root.addView(heading);

        TextView add=Ui.button(this,"＋  Новая заметка");
        LinearLayout.LayoutParams alp=new LinearLayout.LayoutParams(-1,Ui.dp(this,54));alp.topMargin=Ui.dp(this,14);
        root.addView(add,alp);
        add.setOnClickListener(v->startActivity(new Intent(this,EditActivity.class)));

        searchWrap=Ui.column(this);
        searchWrap.setVisibility(View.GONE);
        search=new EditText(this);
        search.setSingleLine(true);
        search.setTextSize(16);
        search.setTextColor(Ui.FG);
        search.setHintTextColor(Ui.MUTED);
        search.setHint("Поиск по записям");
        search.setBackground(Ui.shape(android.graphics.Color.argb(165,16,16,19),14,this));
        search.setPadding(Ui.dp(this,14),0,Ui.dp(this,14),0);
        LinearLayout.LayoutParams slp=new LinearLayout.LayoutParams(-1,Ui.dp(this,48));slp.topMargin=Ui.dp(this,12);
        searchWrap.addView(search,slp);
        root.addView(searchWrap);

        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);
        list=Ui.column(this);scroll.addView(list);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,0,1);lp.topMargin=Ui.dp(this,12);root.addView(scroll,lp);

        search.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int st,int c,int af){}public void onTextChanged(CharSequence s,int st,int before,int count){populate();}public void afterTextChanged(Editable e){}});
        populate();
    }

    void toggleSearch(){
        boolean show=searchWrap.getVisibility()!=View.VISIBLE;
        searchWrap.setVisibility(show?View.VISIBLE:View.GONE);
        if(show){
            search.requestFocus();
            search.postDelayed(()->{InputMethodManager imm=(InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);if(imm!=null)imm.showSoftInput(search,InputMethodManager.SHOW_IMPLICIT);},140);
        } else {
            search.setText("");
            View v=getCurrentFocus();
            if(v!=null){InputMethodManager imm=(InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);if(imm!=null)imm.hideSoftInputFromWindow(v.getWindowToken(),0);}            
            populate();
        }
    }

    void themeDialog(){
        int current=Ui.getThemeIndex(this);
        new AlertDialog.Builder(this)
            .setTitle("Фон списка")
            .setSingleChoiceItems(Ui.THEME_NAMES,current,(d,which)->{
                Ui.setThemeIndex(this,which);
                ((View)findViewById(1001)).setBackground(Ui.listBackground(this));
                populate();
                d.dismiss();
            }).setNegativeButton("Отмена",null).show();
    }

    void populate(){
        if(list==null)return;
        list.removeAllViews();
        String q=search==null?"":search.getText().toString().trim();
        List<Store.Note> notes=store.list(q);
        if(notes.isEmpty()){
            TextView empty=Ui.text(this,"Пока нет записей. Нажмите «Новая заметка», чтобы начать.",16,Ui.MUTED);
            Ui.pad(empty,this,20);
            list.addView(empty);
            return;
        }
        for(Store.Note n:notes){
            LinearLayout card=Ui.column(this);
            card.setBackground(Ui.shape(Ui.CARD,17,this));
            Ui.pad(card,this,15);
            card.setAlpha(n.done?0.44f:1f);

            String body=n.body.trim();
            String previewText=(body.length()>190?body.substring(0,190)+"…":body);
            if(n.done) previewText="Сделано  ·  "+previewText;
            TextView preview=Ui.text(this,previewText,17,Ui.FG);
            preview.setMaxLines(4);
            preview.setEllipsize(android.text.TextUtils.TruncateAt.END);
            card.addView(preview);

            ArrayList<String> metaParts=new ArrayList<>();
            metaParts.add(Ui.date(n.created));
            if(!n.needWhen.trim().isEmpty()) metaParts.add("Когда нужно: "+n.needWhen.trim());
            if(n.alarmOn) metaParts.add("◷ "+Ui.date(n.alarmAt));
            if(!n.people.trim().isEmpty()) metaParts.add(n.people.trim());
            TextView meta=Ui.text(this,join(metaParts,"   ·   "),12,Ui.MUTED);
            LinearLayout.LayoutParams mlp=new LinearLayout.LayoutParams(-1,-2);mlp.topMargin=Ui.dp(this,10);card.addView(meta,mlp);

            LinearLayout.LayoutParams clp=new LinearLayout.LayoutParams(-1,-2);clp.bottomMargin=Ui.dp(this,10);list.addView(card,clp);
            card.setOnClickListener(v->{Intent i=new Intent(this,EditActivity.class);i.putExtra("id",n.id);startActivity(i);});
        }
    }

    String join(List<String> values,String sep){StringBuilder sb=new StringBuilder();for(int i=0;i<values.size();i++){if(i>0)sb.append(sep);sb.append(values.get(i));}return sb.toString();}
}
