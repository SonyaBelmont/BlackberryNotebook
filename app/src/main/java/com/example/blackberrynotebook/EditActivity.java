package com.example.blackberrynotebook;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.net.Uri;
import android.os.*;
import android.provider.Settings;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import java.io.*;
import java.util.*;

public class EditActivity extends Activity {
    private static final int PICK=100;
    Store store; Store.Note note; EditText body; TextView event,people,alarm,done;LinearLayout images;
    @Override public void onCreate(Bundle b){super.onCreate(b);getWindow().setStatusBarColor(Ui.BG);getWindow().setNavigationBarColor(Ui.BG);store=new Store(this);long id=getIntent().getLongExtra("id",0);note=id>0?store.find(id):null;if(note==null){note=new Store.Note();note.created=System.currentTimeMillis();}render();}
    void render(){LinearLayout root=Ui.column(this);root.setBackgroundColor(Ui.BG);setContentView(root);
        LinearLayout top=Ui.row(this);Ui.pad(top,this,14);TextView back=Ui.text(this,"‹  Запись",23,Ui.FG);Ui.heading(back);top.addView(back,new LinearLayout.LayoutParams(0,-2,1));back.setOnClickListener(v->finish());TextView when=Ui.text(this,Ui.date(note.created),12,Ui.MUTED);top.addView(when);root.addView(top);
        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);LinearLayout content=Ui.column(this);Ui.pad(content,this,16);scroll.addView(content);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        body=new EditText(this);body.setText(note.body);body.setTextColor(Ui.FG);body.setHintTextColor(Ui.MUTED);body.setTextSize(18);body.setGravity(Gravity.TOP);body.setHint("Что нужно записать?");body.setBackgroundColor(android.graphics.Color.TRANSPARENT);body.setInputType(android.text.InputType.TYPE_CLASS_TEXT|android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE|android.text.InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);body.setMinLines(9);body.setPadding(0,0,0,Ui.dp(this,20));content.addView(body,new LinearLayout.LayoutParams(-1,-2));
        TextView optional=Ui.text(this,"ДОПОЛНИТЕЛЬНО",12,Ui.MUTED);content.addView(optional);
        event=field(content,"Дата и время",()->chooseDate(false));people=field(content,"Участники",this::askPeople);alarm=field(content,"Будильник",this::alarmDialog);
        TextView photoBtn=field(content,"Фотографии  ＋",this::choosePhoto);images=Ui.row(this);images.setGravity(Gravity.LEFT);images.setPadding(0,Ui.dp(this,7),0,Ui.dp(this,12));HorizontalScrollView hs=new HorizontalScrollView(this);hs.addView(images);content.addView(hs);loadPhotos();
        done=Ui.button(this,"Сделано");LinearLayout.LayoutParams dlp=new LinearLayout.LayoutParams(-1,-2);dlp.topMargin=Ui.dp(this,12);content.addView(done,dlp);done.setOnClickListener(v->{note.done=!note.done;update();persist();});
        update();if(note.id==0){body.requestFocus();body.postDelayed(()->{InputMethodManager imm=(InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);imm.showSoftInput(body,InputMethodManager.SHOW_IMPLICIT);},180);}
    }
    TextView field(LinearLayout parent,String label,Runnable action){TextView t=Ui.text(this,label,16,Ui.FG);t.setBackground(Ui.shape(Ui.CARD,12,this));Ui.pad(t,this,14);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.topMargin=Ui.dp(this,8);parent.addView(t,p);t.setOnClickListener(v->action.run());return t;}
    void update(){event.setText("Дата и время:  "+Ui.date(note.eventAt));people.setText("Участники:  "+(note.people.isEmpty()?"Добавить":note.people));alarm.setText("Будильник:  "+(note.alarmOn?(Ui.date(note.alarmAt)+(note.comment.isEmpty()?"":" · "+note.comment)):"Выключен"));done.setText(note.done?"Вернуть в работу":"Сделано");done.setBackground(Ui.shape(note.done?Ui.CARD:Ui.PURPLE,16,this));}
    void chooseDate(boolean forAlarm){Calendar c=Calendar.getInstance();long existing=forAlarm?note.alarmAt:note.eventAt;if(existing>0)c.setTimeInMillis(existing);
        new DatePickerDialog(this,(picker,year,month,day)->{Calendar selected=Calendar.getInstance();selected.set(year,month,day,c.get(Calendar.HOUR_OF_DAY),c.get(Calendar.MINUTE),0);
            new TimePickerDialog(this,(p,h,m)->{selected.set(Calendar.HOUR_OF_DAY,h);selected.set(Calendar.MINUTE,m);selected.set(Calendar.SECOND,0);if(forAlarm){note.alarmAt=selected.getTimeInMillis();note.alarmOn=true;}else note.eventAt=selected.getTimeInMillis();update();persist();},selected.get(Calendar.HOUR_OF_DAY),selected.get(Calendar.MINUTE),true).show();
        },c.get(Calendar.YEAR),c.get(Calendar.MONTH),c.get(Calendar.DAY_OF_MONTH)).show();
    }
    void askPeople(){EditText input=new EditText(this);input.setSingleLine(false);input.setText(note.people);input.setHint("Например, Анна, Сергей");new AlertDialog.Builder(this).setTitle("Участники").setView(input).setPositiveButton("Сохранить",(d,w)->{note.people=input.getText().toString().trim();update();persist();}).setNegativeButton("Отмена",null).show();}
    void alarmDialog(){LinearLayout wrap=Ui.column(this);Ui.pad(wrap,this,14);Switch toggle=new Switch(this);toggle.setText("Будильник включён");toggle.setChecked(note.alarmOn);wrap.addView(toggle);EditText comment=new EditText(this);comment.setSingleLine(false);comment.setHint("Комментарий к будильнику");comment.setText(note.comment);wrap.addView(comment);
        AlertDialog d=new AlertDialog.Builder(this).setTitle("Будильник").setView(wrap).setPositiveButton("Применить",(dialog,which)->{note.comment=comment.getText().toString().trim();note.alarmOn=toggle.isChecked();if(note.alarmOn && note.alarmAt<=0)note.alarmAt=System.currentTimeMillis()+3600000;update();persist();if(note.alarmOn)chooseDate(true);}).setNeutralButton("Выбрать дату",(dialog,which)->{note.comment=comment.getText().toString().trim();note.alarmOn=true;persist();chooseDate(true);}).setNegativeButton("Отмена",null).create();d.show();}
    void choosePhoto(){Intent i=new Intent(Intent.ACTION_GET_CONTENT);i.setType("image/*");i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);startActivityForResult(Intent.createChooser(i,"Выберите фотографии"),PICK);}
    @Override protected void onActivityResult(int req,int result,Intent data){super.onActivityResult(req,result,data);if(req!=PICK||result!=RESULT_OK||data==null)return;if(data.getClipData()!=null){for(int j=0;j<data.getClipData().getItemCount();j++)copyPhoto(data.getClipData().getItemAt(j).getUri());}else if(data.getData()!=null)copyPhoto(data.getData());loadPhotos();}
    void copyPhoto(Uri uri){try{persist();if(note.id==0){Toast.makeText(this,"Сначала напишите текст заметки",Toast.LENGTH_SHORT).show();return;}String name="photo_"+note.id+"_"+System.nanoTime()+".jpg";File f=new File(getFilesDir(),name);try(InputStream in=getContentResolver().openInputStream(uri);OutputStream out=new FileOutputStream(f)){byte[] buf=new byte[8192];int n;while((n=in.read(buf))!=-1)out.write(buf,0,n);}store.addPhoto(note.id,name);}catch(Exception e){Toast.makeText(this,"Не удалось добавить фото",Toast.LENGTH_SHORT).show();}}
    void loadPhotos(){if(images==null)return;images.removeAllViews();for(String name:store.photos(note.id)){File f=new File(getFilesDir(),name);Bitmap bmp=BitmapFactory.decodeFile(f.getAbsolutePath());if(bmp==null)continue;ImageView v=new ImageView(this);v.setImageBitmap(bmp);v.setScaleType(ImageView.ScaleType.CENTER_CROP);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(Ui.dp(this,82),Ui.dp(this,82));p.rightMargin=Ui.dp(this,8);images.addView(v,p);v.setOnClickListener(w->{ImageView large=new ImageView(this);large.setImageBitmap(bmp);new AlertDialog.Builder(this).setView(large).setPositiveButton("Закрыть",null).show();});}}
    void persist(){if(body==null)return;note.body=body.getText().toString();if(note.id==0 && note.body.trim().isEmpty())return;store.save(note);ReminderReceiver.schedule(this,note);}
    @Override protected void onPause(){persist();super.onPause();}
    @Override public void onBackPressed(){persist();super.onBackPressed();}
}
