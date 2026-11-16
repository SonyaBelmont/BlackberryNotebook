package com.example.blackberrynotebook;

import android.content.*;
import android.database.*;
import android.database.sqlite.*;
import java.util.*;

public class Store extends SQLiteOpenHelper {
    Store(Context c) { super(c,"notes.db",null,1); }
    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE notes(id INTEGER PRIMARY KEY AUTOINCREMENT, body TEXT NOT NULL DEFAULT '', created INTEGER NOT NULL, done INTEGER DEFAULT 0, event_at INTEGER DEFAULT 0, people TEXT DEFAULT '', alarm_at INTEGER DEFAULT 0, alarm_comment TEXT DEFAULT '', alarm_on INTEGER DEFAULT 0)");
        db.execSQL("CREATE TABLE photos(id INTEGER PRIMARY KEY AUTOINCREMENT, note_id INTEGER NOT NULL, filename TEXT NOT NULL)");
    }
    @Override public void onUpgrade(SQLiteDatabase db,int oldV,int newV) {}
    static class Note {
        long id,created,eventAt,alarmAt;
        String body="",people="",comment="";
        boolean done,alarmOn;
    }
    private Note parse(Cursor c) {
        Note n=new Note(); n.id=c.getLong(c.getColumnIndexOrThrow("id")); n.created=c.getLong(c.getColumnIndexOrThrow("created"));
        n.body=c.getString(c.getColumnIndexOrThrow("body"));n.done=c.getInt(c.getColumnIndexOrThrow("done"))!=0;
        n.eventAt=c.getLong(c.getColumnIndexOrThrow("event_at")); n.people=c.getString(c.getColumnIndexOrThrow("people"));
        n.alarmAt=c.getLong(c.getColumnIndexOrThrow("alarm_at")); n.comment=c.getString(c.getColumnIndexOrThrow("alarm_comment"));
        n.alarmOn=c.getInt(c.getColumnIndexOrThrow("alarm_on"))!=0; return n;
    }
    List<Note> list(String query) {
        List<Note> out=new ArrayList<>();
        String search="%"+query+"%";
        try(Cursor c=getReadableDatabase().rawQuery("SELECT * FROM notes WHERE body LIKE ? OR people LIKE ? OR alarm_comment LIKE ? ORDER BY created DESC,id DESC",new String[]{search,search,search}))){while(c.moveToNext())out.add(parse(c));}
        return out;
    }
    Note find(long id) {
        try(Cursor c=getReadableDatabase().rawQuery("SELECT * FROM notes WHERE id=?",new String[]{String.valueOf(id)}))){return c.moveToFirst()?parse(c):null;}
    }
    long save(Note n){
        ContentValues v=new ContentValues(); v.put("body",n.body);v.put("created",n.created);v.put("done",n.done?1:0);
        v.put("event_at",n.eventAt);v.put("people",n.people);v.put("alarm_at",n.alarmAt);v.put("alarm_comment",n.comment);v.put("alarm_on",n.alarmOn?1:0);
        if(n.id==0) n.id=getWritableDatabase().insertOrThrow("notes",null,v);
        else getWritableDatabase().update("notes",v,"id=?",new String[]{String.valueOf(n.id)});
        return n.id;
    }
    void addPhoto(long id,String name){ContentValues v=new ContentValues();v.put("note_id",id);v.put("filename",name);getWritableDatabase().insert("photos",null,v);}
    List<String> photos(long id){List<String> out=new ArrayList<>();try(Cursor c=getReadableDatabase().rawQuery("SELECT filename FROM photos WHERE note_id=? ORDER BY id",new String[]{String.valueOf(id)}))){while(c.moveToNext())out.add(c.getString(0));}return out;}
}
