package com.example.blackberrynotebook;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.graphics.drawable.shapes.OvalShape;
import android.view.*;
import android.widget.*;
import java.text.*;
import java.util.*;

final class Ui {
    static final String PREFS="blackberry_notebook_prefs";
    static final String KEY_THEME="list_theme";

    static final int BG=Color.rgb(20,18,24);
    static final int EDIT_BG=Color.rgb(8,8,10);
    static final int CARD=Color.argb(228,32,29,38);
    static final int FG=Color.rgb(241,237,244);
    static final int MUTED=Color.rgb(187,180,195);
    static final int PURPLE=Color.rgb(146,117,189);
    static final int PURPLE_DARK=Color.rgb(98,73,138);

    static final String[] THEME_NAMES={"Чистый чёрный","Черничная дымка","Аметистовый шёлк","Волшебный сад","Кровоглотик","Ночной шёлк","Тихий сад","Сиреневое утро"};

    static int dp(Activity a,float v){return (int)(a.getResources().getDisplayMetrics().density*v+0.5f);}    
    static GradientDrawable shape(int color,int radius,Activity a){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(a,radius));return d;}
    static GradientDrawable strokeShape(int color,int stroke,int strokeColor,int radius,Activity a){GradientDrawable d=shape(color,radius,a);d.setStroke(dp(a,stroke),strokeColor);return d;}
    static TextView text(Activity a,String value,int sp,int color){TextView t=new TextView(a);t.setText(value);t.setTextColor(color);t.setTextSize(sp);return t;}
    static LinearLayout column(Activity a){LinearLayout l=new LinearLayout(a);l.setOrientation(LinearLayout.VERTICAL);return l;}
    static LinearLayout row(Activity a){LinearLayout l=new LinearLayout(a);l.setOrientation(LinearLayout.HORIZONTAL);l.setGravity(Gravity.CENTER_VERTICAL);return l;}
    static String date(long ms){return ms<=0?"Не задано":new SimpleDateFormat("dd MMM, HH:mm",new Locale("ru")).format(new Date(ms));}
    static void pad(View v,Activity a,int p){int x=dp(a,p);v.setPadding(x,x,x,x);}    
    static TextView button(Activity a,String s){TextView t=text(a,s,16,FG);t.setGravity(Gravity.CENTER);t.setBackground(shape(PURPLE,16,a));pad(t,a,13);return t;}
    static TextView outlineButton(Activity a,String s){TextView t=text(a,s,16,FG);t.setGravity(Gravity.CENTER);t.setBackground(strokeShape(Color.TRANSPARENT,1,Color.argb(100,200,190,215),16,a));pad(t,a,13);return t;}
    static TextView iconButton(Activity a,String s){TextView t=text(a,s,20,FG);t.setGravity(Gravity.CENTER);t.setBackground(shape(Color.argb(115,38,34,46),14,a));t.setPadding(dp(a,10),dp(a,8),dp(a,10),dp(a,8));return t;}
    static void heading(TextView t){t.setTypeface(null,Typeface.BOLD);}    

    static int getThemeIndex(Context c){return c.getSharedPreferences(PREFS,Context.MODE_PRIVATE).getInt(KEY_THEME,2);}    
    static void setThemeIndex(Context c,int v){c.getSharedPreferences(PREFS,Context.MODE_PRIVATE).edit().putInt(KEY_THEME,v).apply();}

    static Drawable listBackground(Activity a){return backgroundFor(a,getThemeIndex(a));}

    static Drawable backgroundFor(Activity a,int index){
        if(index==0){
            return new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{Color.rgb(6,6,8),Color.rgb(12,11,14)});
        }
        int drawableId;
        switch(index){
            case 1: drawableId=R.drawable.wallpaper_blueberry_mist; break;
            case 2: drawableId=R.drawable.wallpaper_amethyst_silk; break;
            case 3: drawableId=R.drawable.wallpaper_magic_garden; break;
            case 4: drawableId=R.drawable.wallpaper_bloodlicker; break;
            case 5: drawableId=R.drawable.wallpaper_satin_wave; break;
            case 6: drawableId=R.drawable.wallpaper_midnight_herbarium; break;
            case 7: drawableId=R.drawable.wallpaper_lilac_dawn; break;
            default: drawableId=R.drawable.wallpaper_blueberry_mist; break;
        }
        BitmapFactory.Options options=new BitmapFactory.Options();
        options.inPreferredConfig=Bitmap.Config.RGB_565;
        options.inSampleSize=1;
        Bitmap bmp=BitmapFactory.decodeResource(a.getResources(),drawableId,options);
        if(bmp==null)return shape(BG,0,a);
        BitmapDrawable bg=new BitmapDrawable(a.getResources(),bmp);
        bg.setGravity(Gravity.FILL);
        return bg;
    }
}
