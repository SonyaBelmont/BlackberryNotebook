package com.example.blackberrynotebook;

import android.app.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
import java.text.*;
import java.util.*;

final class Ui {
    static final int BG=Color.rgb(20,18,24), CARD=Color.rgb(35,32,40), FG=Color.rgb(241,237,244), MUTED=Color.rgb(187,180,195), PURPLE=Color.rgb(146,117,189);
    static int dp(Activity a,float v){return (int)(a.getResources().getDisplayMetrics().density*v+0.5f);}
    static GradientDrawable shape(int color,int radius,Activity a){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(a,radius));return d;}
    static TextView text(Activity a,String value,int sp,int color){TextView t=new TextView(a);t.setText(value);t.setTextColor(color);t.setTextSize(sp);return t;}
    static LinearLayout column(Activity a){LinearLayout l=new LinearLayout(a);l.setOrientation(LinearLayout.VERTICAL);return l;}
    static LinearLayout row(Activity a){LinearLayout l=new LinearLayout(a);l.setOrientation(LinearLayout.HORIZONTAL);l.setGravity(Gravity.CENTER_VERTICAL);return l;}
    static String date(long ms){return ms<=0?"Не задано":new SimpleDateFormat("dd MMM, HH:mm",new Locale("ru")).format(new Date(ms));}
    static void pad(View v,Activity a,int p){int x=dp(a,p);v.setPadding(x,x,x,x);}
    static TextView button(Activity a,String s){TextView t=text(a,s,16,FG);t.setGravity(Gravity.CENTER);t.setBackground(shape(PURPLE,16,a));pad(t,a,13);return t;}
    static void heading(TextView t){t.setTypeface(null,Typeface.BOLD);}
}
