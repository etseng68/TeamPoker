package com.ai2app.teampoker.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.ai2app.teampoker.comm.PokerDrawable;

import java.util.List;

public class ScorePairCardView extends View{
    private static final int CARDPAIR_COUNT=9;
    private static final int CARDPAIR_MARGIN=32;
    private static final int CARD_MARGIN=8;

    private static final int CARDPAIR_ROTATE_COUNT=9;
    private static final int CARDPAIR_ROTATE_MARGIN=24;
    private static final int CARD_ROTATE_MARGIN=6;

    private List<CardPair> mCardPairs ;
    private Paint mScorePaint;
    private float mRotate=0;

    public ScorePairCardView(Context context) {
        super(context);
    }
    public ScorePairCardView(Context context, List<CardPair> cardPairs, float rotate) {
        super(context);
        setCardPairs(cardPairs);
        this.mRotate = rotate;
    }
    public ScorePairCardView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }
    public List<CardPair> getCardPairs() {
        return mCardPairs;
    }

    public void setCardPairs(List<CardPair> cardPairs) {
        this.mCardPairs = cardPairs;
        invalidate();
    }
    @Override
    protected void onDraw(Canvas canvas) {
        if (mCardPairs == null) {
            super.onDraw(canvas);
        }
        else{
            int pairCount,pairMargin,cardMargin;
            if(mRotate ==90 || mRotate ==-90){
                pairCount = CARDPAIR_ROTATE_COUNT;
                pairMargin = CARDPAIR_ROTATE_MARGIN;
                cardMargin = CARD_ROTATE_MARGIN;
            }else {
                pairCount = CARDPAIR_COUNT;
                pairMargin = CARDPAIR_MARGIN;
                cardMargin = CARD_MARGIN;
            }
            drawScoreCardOrder(canvas,mCardPairs,mRotate,pairCount,pairMargin, cardMargin,
                    getWidth(),getHeight());
        }
    }

    private void drawScoreCardOrder(Canvas canvas, List<CardPair> cardPairs, float rotate,
                                    int maxCount, int pairMargin, int cardMargin,
                                    int viewW, int viewH){
        float pairSize = getPairSize(rotate,viewW,viewH,maxCount,cardPairs.size());
        float position;
        float[] pairWH = getPairWidthHeight(rotate,viewW,viewH,pairSize,pairMargin,cardMargin);
        int[] viewWH = new int[]{viewW,viewH};
        int measureCount = getCount(cardPairs.size(),maxCount);
        for (int i = 0; i < cardPairs.size(); i++) {
            int order = getOrder(rotate,i,cardPairs.size());
            String[] cards = cardPairs.get(order).getCards();
            position = getPosition(rotate,order,pairMargin,pairSize,measureCount);
            for (int j = 0; j < cards.length; j++) {
                drawScoreCardPair(j,canvas,rotate,viewWH,pairWH,position,cards);
                if (j == 0)
                    position = position + (pairSize / 2) - (cardMargin / 2);
            }

            drawScoreText(i,canvas,cardPairs,rotate,viewWH,pairWH,pairSize, measureCount,cardMargin);
        }
    }
    private void drawScoreCardPair(int index,Canvas canvas,float rotate, int[] viewWH,
                                   float[] pairWH,float position,String[] cards ){
        int cardOrder = getOrder(rotate,index,cards.length);
        Bitmap bitmap = getCardSmallBitmap(cards[cardOrder],pairWH,rotate);
        float[] cardXY = getDrawCardXY(rotate,position,viewWH);
        canvas.drawBitmap(bitmap, cardXY[0], cardXY[1], null);
    }
    private void drawScoreText(int index, Canvas canvas, List<CardPair> cardPairs, float rotate,
                               int[] viewWH, float[] pairWH, float pairS,
                               int measureCount, int cardMargin){
        float[] swh=getScoreWH(rotate,viewWH,pairS);
        String scoreStr = getScoreText(rotate, index, cardPairs);
        RectF rectF = getScoreRectF(rotate,index,swh,pairWH,measureCount,cardMargin);
        float[] rxy = getScoreRotateXY(rotate,swh,index);
        float[] dxy = getScoreDrawXY(rotate,index,scoreStr,swh,pairWH,measureCount,
                cardPairs.size(),cardMargin);
        drawScore(canvas,scoreStr,rectF,rotate,rxy,dxy,index);
    }

    //--------------------------------------------------------------------------------------
    // card pair def
    private int getOrder(float rotate,int i,int size){
        int order ;
        if(rotate == -90 || rotate == 180){
            order = size-i-1;
        }else{
            order =i;
        }
        return order;
    }
    private float getPosition(float rotate,int i,int pairMargin,float pairS,int count){
        float position=0;
        if(rotate == 0f || rotate == 90)
            position = (pairS * i) + (pairMargin / 2);
        else
            position = (count -i - 1) * pairS + (pairMargin / 2);
        return position;
    }

    private float[] getPairWidthHeight(float rotate,int viewW,int viewH,float pairS,
                                       int pairMargin,int cardMargin){
        float wh[]= new float[]{0f,0f};
        if(rotate ==90f || rotate == -90){
            wh[0] = viewW / 3 * 2 ;
            wh[1] = (pairS - pairMargin - cardMargin) / 2;
        }else{
            wh[0] = (pairS - pairMargin - cardMargin) / 2;
            wh[1] = viewH / 3 * 2 ;
        }
        return wh;
    }
    private float[] getDrawCardXY(float rotate,float position,int[] viewWH){
        int viewW = viewWH[0],viewH = viewWH[1];
        float xy[] = new float[]{0f,0f};
        switch ((int)rotate){
            case 90:
                xy[0] = viewW / 3;
                xy[1] = position;
                break;
            case 180:
                xy[0] = position;
                xy[1] = viewH / 3;
                break;
            case -90:
                xy[0] = 0;
                xy[1] = position;
                break;
            case 0:
                xy[0] = position;
                xy[1] = 0;
                break;
        }
        return xy;
    }
    private int getCount(int carPairCount,int maxCount){
        if(maxCount >= carPairCount)
            return maxCount;
        else
            return carPairCount;
    }
    private float getPairSize(float rotate,int w,int h,int maxCount,int pairs){
        float pairSize;
        int wh;
        if(rotate == 90f || rotate == -90f)
            wh = h;
        else
            wh = w;

        if (pairs > maxCount)
            pairSize = wh / pairs;
        else
            pairSize = wh / maxCount;
        return pairSize;
    }
    private Bitmap getCardSmallBitmap(String card,float[] pairWH,float rotate){
        float pairW = pairWH[0],pairH = pairWH[1];
        Matrix matrix = new Matrix();
        float scaleW ,scaleH ;
        Bitmap bitmap =  PokerDrawable.getBitmapFromDrawable(getContext(),card,rotate,
                0.5f, Color.BLACK);
        scaleW =  pairW / (float) bitmap.getWidth();
        scaleH =  pairH / (float) bitmap.getHeight();
        matrix.setScale(scaleW, scaleH);
        return Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),
                matrix,true);
    }
    //--------------------------------------------------------------------------------------
    // score def
    private void drawScore(Canvas canvas,String scoreStr,RectF rectF,
                           float rotate,float[] rotateXY, float[] drawXY,
                           int index){
        canvas.drawRect(rectF, getScorePaintBackgroundColor(index));
        canvas.save();
        canvas.rotate(rotate, rotateXY[0], rotateXY[1]);
        canvas.drawText(scoreStr, drawXY[0], drawXY[1], getScorePaint(scoreStr));
        canvas.restore();
    }

    private String getScoreText(float rotate,int index,List<CardPair> cardPairs){
        int order=index;
        if(rotate == -90){
            order = cardPairs.size() - index -1;
        }
        return cardPairs.get(order).getScoreStr();
    }
    private float[] getScoreRotateXY (float rotate,float[] swh,int index){
        float rx = swh[0] / 2;
        float ry = (swh[1] * index) + (swh[1] / 2);
        switch ((int)rotate){
            case 90:
                break;
            case -90:
                break;
            case 180:
                rx = swh[0] / 2;
                ry = swh[1] / 2;
                break;
            default:
                rx =0f;
                ry =0f;
                break;
        }
        return new float[]{rx,ry};
    }
    private float[] getScoreWH (float rotate,int[] viewWH, float pairS){
        int viewW = viewWH[0],viewH= viewWH[1];
        float sw,sh;
        if(rotate == 90 || rotate == -90) {
            sw = viewW /3;
            sh = pairS;
        }
        else{
            sw = pairS;
            sh = viewH /3;
        }
        return new float[]{sw,sh};
    }

    private float[] getScoreDrawXY(float rotate,int index,String scoreStr,
                                   float[] swh,float[] pairWH,
                                   int measureCount,int pairsCount,int cardMargin){
        float sw=swh[0],sh=swh[1];
        float pairW = pairWH[0],pairH = pairWH[1];
        float dx,dy;
        float[] textXY = getScoreXY(scoreStr, sw,sh);;
        switch ((int)rotate) {
            case 90:
                dx = textXY[0];
                dy = cardMargin / 2 + sh * index + textXY[1];
                break;
            case -90:
                dx = textXY[0]-(measureCount-pairsCount)* sh;
                dy = pairW  + cardMargin / 2 + sh * index + textXY[1];
                break;
            case 180:
                dx = sw * index + textXY[0] - (measureCount-1) * sw;
                dy = textXY[1];
                break;
            default:
                dx = sw * index + textXY[0];
                dy = pairH + cardMargin / 2 + textXY[1];
                break;
        }
        return new float[]{dx,dy};
    }
    private RectF getScoreRectF(float rotate,int index, float[] swh, float[] pairWH,
                                int measureCount,int cardMargin){
        float sw=swh[0],sh=swh[1];
        float pairW = pairWH[0],pairH = pairWH[1];
        float left=0,top=0,right=0,bottom=0;
        switch ((int)rotate){
            case 90:
                left = cardMargin / 2;
                top =  sh * index;
                right = left + sw ;
                bottom = top + sh;
                break;
            case -90:
                left = pairW  + (cardMargin / 2);
                top =  (measureCount - index -1)  * sh;
                right = left + sw;
                bottom = top + sh;
                break;
            case 180:
                left = (measureCount - index -1)  * sw;
                top =  cardMargin/2;
                right = left +sw;
                bottom = sh ;
                break;
            default:
                left = (sw * index)+1;
                top =  pairH +(cardMargin/2) ;
                right = (sw*(index+1))-1;
                bottom = top + sh;
                break;
        }
        return new RectF(left , top, right, bottom);
    }
    private float[] getScoreXY(String scoreStr,float w,float h){ //cardpair : width, height
        float[] textXY= new float[]{0f,0f,0f,0f};
        Rect rect = new Rect();
        Paint paint = getScorePaint(scoreStr);
        paint.getTextBounds(scoreStr,0,scoreStr.length(),rect);
        textXY[0] = (w-rect.width())/2;

        Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        textXY[1] = (h-fontMetrics.bottom+fontMetrics.top)/2-fontMetrics.top;
        textXY[2] = rect.width();
        textXY[3] = rect.height();
        return textXY;
    }

    private Paint getScorePaint(String score){
        if(mScorePaint == null) {
            mScorePaint = new Paint();
            mScorePaint.setStrokeWidth(2);
            mScorePaint.setTextSize(64f);
            mScorePaint.setTextAlign(Paint.Align.LEFT);
        }
        if (Integer.parseInt(score) > 0)
            mScorePaint.setColor(Color.RED);
        else
            mScorePaint.setColor(Color.BLACK);
        return mScorePaint;
    }

    private Paint getScorePaintBackgroundColor(int index){
        int mod = index % 2;
        int color;
        if(mod == 0)
            color = Color.WHITE;
        else
            color = Color.GRAY;
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);
        paint.setAlpha(100);
        return paint;
    }

    //======================================================================================
    public static class CardPair{
        private String card1;
        private String card2;
        private int score;

        public CardPair() {
        }

        public CardPair(String card1, String card2, int score) {
            this.card1 = card1;
            this.card2 = card2;
            this.score = score;
        }

        public String getCard1() {
            return card1;
        }

        public void setCard1(String card1) {
            this.card1 = card1;
        }

        public String getCard2() {
            return card2;
        }

        public void setCard2(String card2) {
            this.card2 = card2;
        }

        public int getScore() {
            return score;
        }

        public void setScore(int score) {
            this.score = score;
        }

        public String getScoreStr(){
            return String.valueOf(score);
        }
        public void setScore(String scoreStr) {
            this.score = Integer.parseInt(scoreStr);
        }
        public String[] getCards(){
            return new String[]{card1,card2};
        }
    }
}
