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

    static final String[] THEME_NAMES={"Чистый чёрный","Черничная дымка","Ночная черника","Графит и ягода","Кровоглотик"};

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
        switch(index){
            case 0: {
                GradientDrawable g=new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,new int[]{Color.rgb(7,7,9),Color.rgb(12,12,14)});
                return g;
            }
            case 1: {
                LayerDrawable layer=new LayerDrawable(new Drawable[]{
                    new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{Color.rgb(16,13,21),Color.rgb(28,20,40),Color.rgb(11,10,15)}),
                    radial(a,0.22f,0.18f,0.50f,Color.argb(125,117,84,180),Color.TRANSPARENT),
                    radial(a,0.82f,0.20f,0.42f,Color.argb(75,90,64,140),Color.TRANSPARENT),
                    radial(a,0.55f,0.88f,0.56f,Color.argb(55,55,36,96),Color.TRANSPARENT)
                });
                return layer;
            }
            case 2: {
                LayerDrawable layer=new LayerDrawable(new Drawable[]{
                    new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,new int[]{Color.rgb(8,9,12),Color.rgb(18,14,27),Color.rgb(8,7,10)}),
                    radial(a,0.75f,0.18f,0.36f,Color.argb(90,143,102,214),Color.TRANSPARENT),
                    radial(a,0.18f,0.82f,0.48f,Color.argb(70,82,58,132),Color.TRANSPARENT)
                });
                return layer;
            }
            case 3: {
                LayerDrawable layer=new LayerDrawable(new Drawable[]{
                    new GradientDrawable(GradientDrawable.Orientation.BL_TR,new int[]{Color.rgb(12,12,14),Color.rgb(24,22,30),Color.rgb(10,10,12)}),
                    radial(a,0.10f,0.12f,0.52f,Color.argb(58,110,88,168),Color.TRANSPARENT),
                    radial(a,0.86f,0.72f,0.45f,Color.argb(85,116,76,188),Color.TRANSPARENT)
                });
                return layer;
            }
            default:
                return new BitmapDrawable(a.getResources(),bloodlickerBitmap(a));
        }
    }

    private static Drawable radial(Activity a,float x,float y,float radius,int inner,int outer){
        int size=dp(a,900);
        ShapeDrawable d=new ShapeDrawable(new OvalShape());
        Paint p=d.getPaint();
        p.setShader(new RadialGradient(size*x,size*y,size*radius,new int[]{inner,outer},new float[]{0f,1f},Shader.TileMode.CLAMP));
        d.setIntrinsicWidth(size);d.setIntrinsicHeight(size);
        return d;
    }

    private static Bitmap bloodlickerBitmap(Activity a){
        int w=1080,h=1920;
        Bitmap bmp=Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);
        Canvas c=new Canvas(bmp);
        Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
        LinearGradient bg=new LinearGradient(0,0,w,h,new int[]{Color.rgb(8,8,11),Color.rgb(24,15,35),Color.rgb(7,7,9)},new float[]{0f,0.55f,1f},Shader.TileMode.CLAMP);
        p.setShader(bg);c.drawRect(0,0,w,h,p);
        p.setShader(null);
        p.setColor(Color.argb(70,128,96,185));c.drawCircle(w*0.23f,h*0.22f,w*0.32f,p);
        p.setColor(Color.argb(45,76,54,118));c.drawCircle(w*0.77f,h*0.82f,w*0.28f,p);
        Paint silhouette=new Paint(Paint.ANTI_ALIAS_FLAG);
        silhouette.setColor(Color.argb(95,15,8,18));
        Path body=new Path();
        body.moveTo(w*0.70f,h*0.22f);
        body.cubicTo(w*0.90f,h*0.28f,w*0.94f,h*0.54f,w*0.78f,h*0.70f);
        body.cubicTo(w*0.63f,h*0.84f,w*0.45f,h*0.78f,w*0.38f,h*0.59f);
        body.cubicTo(w*0.31f,h*0.40f,w*0.47f,h*0.22f,w*0.70f,h*0.22f);
        c.drawPath(body,silhouette);
        Paint detail=new Paint(Paint.ANTI_ALIAS_FLAG);
        detail.setColor(Color.argb(46,175,135,235));
        detail.setStrokeWidth(18f);detail.setStyle(Paint.Style.STROKE);
        Path spine=new Path();spine.moveTo(w*0.60f,h*0.28f);spine.cubicTo(w*0.70f,h*0.38f,w*0.74f,h*0.54f,w*0.61f,h*0.70f);c.drawPath(spine,detail);
        Paint eye=new Paint(Paint.ANTI_ALIAS_FLAG);eye.setColor(Color.argb(160,188,150,255));c.drawCircle(w*0.66f,h*0.41f,8,eye);
        Paint mist=new Paint(Paint.ANTI_ALIAS_FLAG);mist.setColor(Color.argb(22,255,255,255));
        for(int i=0;i<8;i++) c.drawRoundRect(new RectF(-120,h*(0.12f*i+0.08f),w+120,h*(0.12f*i+0.11f)),80,80,mist);
        return bmp;
    }
}
