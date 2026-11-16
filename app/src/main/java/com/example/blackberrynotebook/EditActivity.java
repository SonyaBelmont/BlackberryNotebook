package com.example.blackberrynotebook;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.net.Uri;
import android.os.*;
import android.text.InputType;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import java.io.*;

public class EditActivity extends Activity {
    private static final int PICK=100;
    Store store; Store.Note note; EditText body; TextView whenNeed,people,alarm; LinearLayout images; TextView saveBtn; TextView stateText;

    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        getWindow().setStatusBarColor(Ui.EDIT_BG);
        getWindow().setNavigationBarColor(Ui.EDIT_BG);
        store=new Store(this);
        long id=getIntent().getLongExtra("id",0);
        note=id>0?store.find(id):null;
        if(note==null){note=new Store.Note();note.created=System.currentTimeMillis();}
        render();
    }

    void render(){
        LinearLayout root=Ui.column(this);root.setBackgroundColor(Ui.EDIT_BG);setContentView(root);

        LinearLayout top=Ui.row(this);Ui.pad(top,this,14);
        TextView back=Ui.text(this,"‹  Запись",23,Ui.FG);Ui.heading(back);
        top.addView(back,new LinearLayout.LayoutParams(0,-2,1));
        back.setOnClickListener(v->finish());
        TextView menu=Ui.iconButton(this,"⋮");
        top.addView(menu,new LinearLayout.LayoutParams(Ui.dp(this,44),Ui.dp(this,40)));
        menu.setOnClickListener(this::showNoteMenu);
        root.addView(top);

        stateText=Ui.text(this,"",12,Ui.MUTED);
        LinearLayout.LayoutParams stp=new LinearLayout.LayoutParams(-1,-2);stp.leftMargin=Ui.dp(this,16);stp.rightMargin=Ui.dp(this,16);root.addView(stateText,stp);

        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);
        LinearLayout content=Ui.column(this);Ui.pad(content,this,16);scroll.addView(content);
        root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));

        body=new EditText(this);
        body.setText(note.body);
        body.setTextColor(Ui.FG);
        body.setHintTextColor(Ui.MUTED);
        body.setTextSize(18);
        body.setGravity(Gravity.TOP);
        body.setHint("Что нужно записать?");
        body.setBackgroundColor(Color.TRANSPARENT);
        body.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_MULTI_LINE|InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        body.setMinLines(9);
        body.setPadding(0,0,0,Ui.dp(this,20));
        content.addView(body,new LinearLayout.LayoutParams(-1,-2));

        TextView created=Ui.text(this,"Создано: "+Ui.date(note.created),12,Ui.MUTED);
        LinearLayout.LayoutParams cr=new LinearLayout.LayoutParams(-1,-2);cr.bottomMargin=Ui.dp(this,10);content.addView(created,cr);

        TextView optional=Ui.text(this,"ДОПОЛНИТЕЛЬНО",12,Ui.MUTED);content.addView(optional);
        whenNeed=field(content,"Когда нужно",this::askWhenNeed);
        people=field(content,"Участники",this::askPeople);
        alarm=field(content,"Будильник",this::alarmDialog);
        TextView photoBtn=field(content,"Фотографии  ＋",this::choosePhoto);
        images=Ui.row(this);images.setGravity(Gravity.LEFT);images.setPadding(0,Ui.dp(this,7),0,Ui.dp(this,12));
        HorizontalScrollView hs=new HorizontalScrollView(this);hs.addView(images);content.addView(hs);
        loadPhotos();

        saveBtn=Ui.button(this,"Сохранить");
        LinearLayout.LayoutParams slp=new LinearLayout.LayoutParams(-1,-2);slp.topMargin=Ui.dp(this,12);content.addView(saveBtn,slp);
        saveBtn.setOnClickListener(v->{persist();finish();});

        update();
        if(note.id==0){
            body.requestFocus();
            body.postDelayed(()->{InputMethodManager imm=(InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);if(imm!=null)imm.showSoftInput(body,InputMethodManager.SHOW_IMPLICIT);},180);
        }
    }

    void showNoteMenu(View anchor){
        PopupMenu menu=new PopupMenu(this,anchor);
        menu.getMenu().add(0,1,0,note.done?"Вернуть в работу":"Отметить как сделано");
        menu.setOnMenuItemClickListener(item->{
            if(item.getItemId()==1){
                note.done=!note.done;
                update();
                persist();
                return true;
            }
            return false;
        });
        menu.show();
    }

    TextView field(LinearLayout parent,String label,Runnable action){
        TextView t=Ui.text(this,label,16,Ui.FG);
        t.setBackground(Ui.shape(android.graphics.Color.argb(170,18,18,22),12,this));
        Ui.pad(t,this,14);
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.topMargin=Ui.dp(this,8);
        parent.addView(t,p);
        t.setOnClickListener(v->action.run());
        return t;
    }

    void update(){
        whenNeed.setText("Когда нужно:  "+(note.needWhen.trim().isEmpty()?"Не указано":note.needWhen.trim()));
        people.setText("Участники:  "+(note.people.isEmpty()?"Не указаны":note.people));
        alarm.setText("Будильник:  "+(note.alarmOn?(Ui.date(note.alarmAt)+(note.comment.isEmpty()?"":" · "+note.comment)):"Выключен"));
        stateText.setText(note.done?"Статус: сделано":"Статус: активна");
    }

    void askWhenNeed(){
        EditText input=new EditText(this);
        input.setSingleLine(false);
        input.setMinLines(2);
        input.setHint("Например: завтра утром, до конца недели");
        input.setText(note.needWhen);
        new AlertDialog.Builder(this).setTitle("Когда нужно")
            .setView(input)
            .setPositiveButton("Сохранить",(d,w)->{note.needWhen=input.getText().toString().trim();update();persist();})
            .setNegativeButton("Отмена",null).show();
    }

    void askPeople(){
        EditText input=new EditText(this);
        input.setSingleLine(false);
        input.setText(note.people);
        input.setHint("Например, Анна, Сергей");
        new AlertDialog.Builder(this).setTitle("Участники")
            .setView(input)
            .setPositiveButton("Сохранить",(d,w)->{note.people=input.getText().toString().trim();update();persist();})
            .setNegativeButton("Отмена",null).show();
    }

    void alarmDialog(){
        LinearLayout wrap=Ui.column(this);Ui.pad(wrap,this,14);
        Switch toggle=new Switch(this);toggle.setText("Будильник включён");toggle.setChecked(note.alarmOn);wrap.addView(toggle);
        TextView current=Ui.text(this,note.alarmAt>0?"Время: "+Ui.date(note.alarmAt):"Время не выбрано",14,Ui.MUTED);
        LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-1,-2);cp.topMargin=Ui.dp(this,8);wrap.addView(current,cp);
        TextView choose=Ui.outlineButton(this,"Выбрать дату и время");
        LinearLayout.LayoutParams chp=new LinearLayout.LayoutParams(-1,-2);chp.topMargin=Ui.dp(this,10);wrap.addView(choose,chp);
        EditText comment=new EditText(this);comment.setSingleLine(false);comment.setHint("Комментарий к будильнику");comment.setText(note.comment);
        LinearLayout.LayoutParams ec=new LinearLayout.LayoutParams(-1,-2);ec.topMargin=Ui.dp(this,10);wrap.addView(comment,ec);

        final long[] picked={note.alarmAt};
        choose.setOnClickListener(v->{
            pickAlarmDateTime(ms->{picked[0]=ms;current.setText("Время: "+Ui.date(ms));});
        });

        new AlertDialog.Builder(this).setTitle("Будильник")
            .setView(wrap)
            .setPositiveButton("Применить",(dialog,which)->{
                note.comment=comment.getText().toString().trim();
                note.alarmOn=toggle.isChecked();
                note.alarmAt=picked[0];
                if(note.alarmOn && note.alarmAt<=0) note.alarmAt=System.currentTimeMillis()+3600000L;
                update();
                persist();
            })
            .setNeutralButton("Выключить",(dialog,which)->{note.alarmOn=false;note.alarmAt=0;note.comment="";update();persist();})
            .setNegativeButton("Отмена",null).show();
    }

    interface TimeCallback { void onPicked(long ms); }
    void pickAlarmDateTime(TimeCallback callback){
        java.util.Calendar c=java.util.Calendar.getInstance();
        long existing=note.alarmAt;
        if(existing>0)c.setTimeInMillis(existing);
        new DatePickerDialog(this,(picker,year,month,day)->{
            java.util.Calendar selected=java.util.Calendar.getInstance();
            selected.set(year,month,day,c.get(java.util.Calendar.HOUR_OF_DAY),c.get(java.util.Calendar.MINUTE),0);
            new TimePickerDialog(this,(p,h,m)->{
                selected.set(java.util.Calendar.HOUR_OF_DAY,h);
                selected.set(java.util.Calendar.MINUTE,m);
                selected.set(java.util.Calendar.SECOND,0);
                callback.onPicked(selected.getTimeInMillis());
            },selected.get(java.util.Calendar.HOUR_OF_DAY),selected.get(java.util.Calendar.MINUTE),true).show();
        },c.get(java.util.Calendar.YEAR),c.get(java.util.Calendar.MONTH),c.get(java.util.Calendar.DAY_OF_MONTH)).show();
    }

    void choosePhoto(){
        Intent i=new Intent(Intent.ACTION_GET_CONTENT);i.setType("image/*");i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);
        startActivityForResult(Intent.createChooser(i,"Выберите фотографии"),PICK);
    }

    @Override protected void onActivityResult(int req,int result,Intent data){
        super.onActivityResult(req,result,data);
        if(req!=PICK||result!=RESULT_OK||data==null)return;
        if(data.getClipData()!=null){for(int j=0;j<data.getClipData().getItemCount();j++)copyPhoto(data.getClipData().getItemAt(j).getUri());}
        else if(data.getData()!=null)copyPhoto(data.getData());
        loadPhotos();
    }

    void copyPhoto(Uri uri){
        try{
            persist();
            if(note.id==0){Toast.makeText(this,"Сначала напишите текст заметки",Toast.LENGTH_SHORT).show();return;}
            String name="photo_"+note.id+"_"+System.nanoTime()+".jpg";
            File f=new File(getFilesDir(),name);
            try(InputStream in=getContentResolver().openInputStream(uri);OutputStream out=new FileOutputStream(f)){
                byte[] buf=new byte[8192];int n;while((n=in.read(buf))!=-1)out.write(buf,0,n);
            }
            store.addPhoto(note.id,name);
        }catch(Exception e){Toast.makeText(this,"Не удалось добавить фото",Toast.LENGTH_SHORT).show();}
    }

    void loadPhotos(){
        if(images==null)return;
        images.removeAllViews();
        for(String name:store.photos(note.id)){
            File f=new File(getFilesDir(),name);
            Bitmap bmp=BitmapFactory.decodeFile(f.getAbsolutePath());
            if(bmp==null)continue;
            ImageView v=new ImageView(this);v.setImageBitmap(bmp);v.setScaleType(ImageView.ScaleType.CENTER_CROP);
            LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(Ui.dp(this,82),Ui.dp(this,82));p.rightMargin=Ui.dp(this,8);images.addView(v,p);
            v.setOnClickListener(w->{ImageView large=new ImageView(this);large.setImageBitmap(bmp);new AlertDialog.Builder(this).setView(large).setPositiveButton("Закрыть",null).show();});
        }
    }

    void persist(){
        if(body==null)return;
        note.body=body.getText().toString();
        if(note.id==0 && note.body.trim().isEmpty()) return;
        store.save(note);
        ReminderReceiver.schedule(this,note);
    }

    @Override protected void onPause(){persist();super.onPause();}
    @Override public void onBackPressed(){persist();super.onBackPressed();}
}
