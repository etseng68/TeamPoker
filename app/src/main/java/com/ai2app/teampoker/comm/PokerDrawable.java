package com.ai2app.teampoker.comm;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Build;

import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import com.ai2app.teampoker.R;

import java.lang.reflect.Field;
import java.util.Arrays;

public class PokerDrawable {
    private final static String TAG = "PokerDrawable";

    public static float[] getCardScaleData(String card){
        float[] scaleData = new float[]{25f/147f,60f/242f,80f,40f};
        if(Arrays.asList(Poker.markScoreTen).contains(Poker.getNumStr(card))){
            if(Poker.getNumStr(card).equals("a"))
                scaleData = new float[]{25f/152f,60f/220f,20f,80f};
            else
                scaleData = new float[]{25f/195f,60f/283f,30f,90f};
        }
        return scaleData;
    }

    public static Bitmap getBitmapFromDrawable(Context context, String card,float rotate,
                                               float paintW, int color){

        Drawable d = context.getDrawable(getDrawableId(context,card));
        float[] sWHtXY = getCardScaleData(card);
        final int dw =d.getIntrinsicWidth();
        final int dh =d.getIntrinsicHeight();

        float[] whxy = getDrawableRotateData(rotate,dw,dh,sWHtXY[0],sWHtXY[1],sWHtXY[2],sWHtXY[3]);

        Bitmap b = Bitmap.createBitmap((int)whxy[0], (int)whxy[1], Bitmap.Config.RGB_565);
        Canvas c = new Canvas(b);
        c.save();
        c.rotate(rotate);
        c.translate(whxy[2], whxy[3]);

        d.setBounds(0,0,dw,dh);
        d.draw(c);
        c.restore();
        c.drawRect(0,0,whxy[0]-1,whxy[1]-1,getCardRectPaint(paintW,color));

        return b;
    }
    private static Paint getCardRectPaint(float w,int color){
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(w); //0.5f
        paint.setColor(color); //Color.BLACK
        return paint;
    }

    private static float[] getDrawableRotateData(float rotate,int dw,int dh,float sw,float sh,
                                                 float tx,float ty){
        //rw=80,rh=40
        float[] whxy = new float[]{0f,0f,0f,0f};
        switch ((int)rotate){
            case 0:
                whxy[0] = dw * sw;
                whxy[1] = dh * sh;
                whxy[2] = -tx * sw;
                whxy[3] = -ty * sh;
                break;
            case 90:
                whxy[0] = dh * sh;
                whxy[1] = dw * sw;
                whxy[2] = -tx * sw;
                whxy[3] = -whxy[0] - (ty * sh);
                break;
            case -90:
                whxy[0] = dh * sh;
                whxy[1] = dw * sw;
                whxy[2] = -whxy[1] - (tx * sw);
                whxy[3] = -ty * sh;
                break;
            case 180:
                whxy[0] = dw * sw;
                whxy[1] = dh * sh;
                whxy[2] = -whxy[0] - tx * sw;
                whxy[3] = -whxy[1] - ty * sh;
                break;
        }
        return whxy;
    }
    public static RoundedBitmapDrawable getCircleDrawable(Context context, int cardRes, int color){
        Drawable d = context.getDrawable(cardRes);
        Bitmap bitmap = Bitmap.createBitmap(d.getIntrinsicWidth(),d.getIntrinsicHeight(), Bitmap.Config.RGB_565);
        Canvas c = new Canvas(bitmap);
        c.drawColor(color);
        d.setBounds(0,0,c.getWidth(),c.getHeight());
        d.draw(c);

        RoundedBitmapDrawable cirle =
                RoundedBitmapDrawableFactory.create(context.getResources(),bitmap);
        cirle.getPaint().setAntiAlias(true);
        cirle.setCornerRadius(Math.max(bitmap.getWidth(),bitmap.getHeight()));
        return cirle;
    }
    public static int getDrawableId(Context context, String vName){
       int res = getDrawableId(context,"drawable",vName);
       if (res == 0)
           res = getDrawableId(context,"mipmap",vName);
        return res;
    }
    public static int getColorByName(Context context,String cName){
        int colorId = 0;
        try {
            Class res = R.color.class;
            Field field = res.getField( cName );
            colorId = field.getInt(null);
        } catch ( Exception e ) {
            e.printStackTrace();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.getResources().getColor(colorId,null);
        }
        else{
            return context.getResources().getColor(colorId);
        }
    }
    private static int getDrawableId(Context context,String defType,String vName){
        return context.getResources().getIdentifier(vName,defType,context.getPackageName());
    }

}
