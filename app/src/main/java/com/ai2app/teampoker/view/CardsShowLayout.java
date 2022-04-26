package com.ai2app.teampoker.view;


import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.ai2app.teampoker.R;
import com.ai2app.teampoker.comm.PokerDrawable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CardsShowLayout extends ViewGroup {
    private static final String TAG="CardsShowLayout";
    private List<String> mCardsRefs;
    private int mLayoutType = -1;

    public CardsShowLayout(Context context) {
        super(context);
        init(context,null,null,-1);
    }
    public CardsShowLayout(Context context,List<String> cardsRefs,int layoutType) {
        super(context);
        init(context,null,cardsRefs,layoutType);
    }

    public CardsShowLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context,attrs,null,-1);
    }
    public CardsShowLayout(Context context, AttributeSet attrs,List<String> cardsRefs,int layoutType) {
        super(context, attrs);
        init(context,attrs,cardsRefs,layoutType);
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int childCount = getChildCount();
        if(mLayoutType >0) {
            Map<String,CardLayout> layoutMap = CardShowType.getCardLayout(
                    mLayoutType,mCardsRefs, getMeasuredWidth(),getMeasuredHeight());
            if (layoutMap != null && layoutMap.size() == childCount) {
                CardLayout cl ;
                for (int i = 0; i < childCount; i++) {
                    ImageView child = (ImageView) getChildAt(i);
                        cl = layoutMap.get(child.getTag().toString());
                    if (cl != null)
                        child.layout(cl.left, cl.top, cl.right, cl.bottom);
                }
            }
        }
    }

    private void init(Context context, AttributeSet attrs,List<String> cardsRefs,int layoutType){
        if(layoutType >=0 && cardsRefs != null){
            mCardsRefs = cardsRefs;
            mLayoutType = layoutType;
        }else if(attrs != null){
            TypedArray typedArray = context.getTheme().obtainStyledAttributes
                    (attrs, R.styleable.PhoneTable, 0, 0);
            try {
                CharSequence[] charCards = typedArray.getTextArray(R.styleable.PhoneTable_cardsRes);
                if (charCards != null) {
                    mCardsRefs = new ArrayList<>();
                    for (int i = 0; i < charCards.length; i++)
                        mCardsRefs.add(i,charCards[i].toString());
                }
                mLayoutType = typedArray.getInt(R.styleable.PhoneTable_cardArrangeTyp, -1);
            } finally {
                typedArray.recycle();
            }
        }
        createImageView();
    }
    private void createImageView(){
        removeAllViews();
        if(mCardsRefs != null ){
            for(int i=0;i<mCardsRefs.size();i++){
                addView(getNewImageView(mCardsRefs.get(i)), i, getChildLayoutParams());
            }
        }
    }

    private ImageView getNewImageView(String card){
        final ImageView imageView = new ImageView(getContext());
        imageView.setTag(card);
        imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        imageView.setImageResource(PokerDrawable.getDrawableId(getContext(),card));
        imageView.setAdjustViewBounds(true);
        imageView.setOnClickListener((OnClickListener)getContext());
        imageView.setOnLongClickListener((OnLongClickListener)getContext());
        return imageView;
    }
    private ViewGroup.LayoutParams getChildLayoutParams(){
        LayoutParams layout =  new LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT);
        return layout;
    }

    public void setCardsRefs(List<String> cardsRefs) {
        this.mCardsRefs = cardsRefs;
        createImageView();
        invalidate();
        requestLayout();
    }

    //==========================================================================================
    public static class CardLayout{
        public int left;
        public int top;
        public int right;
        public int bottom;
        public int rotate;

        public CardLayout(){

        }
        public CardLayout(int left,int top,int right,int bottom,int rotate) {
            setLayout(left,top,right,bottom,rotate);
        }

        public void setLayout(int left,int top,int right,int bottom,int rotate){
            this.left =left;
            this.top=top;
            this.right = right;
            this.bottom = bottom;
            this.rotate = rotate;
        }

    }
    //==========================================================================================
    public static class CardShowType {
        public static final int TYPE_TABLE=0,TYPE_TABLE_HAND=1,TYPE_HAND=2;

        public static Map<String,CardsShowLayout.CardLayout> getCardLayout(int type
                , List<String> cards, int width, int height){
            Map<String,CardsShowLayout.CardLayout> cardLayoutMap = null;
            switch (type) {
                case TYPE_TABLE:
                    cardLayoutMap= getTableShowCardLayout(cards, width, height);
                    break;
                case TYPE_TABLE_HAND:
                    cardLayoutMap= getHandShowCardLayout(cards, width, height);
                    break;
                case TYPE_HAND:
                    cardLayoutMap= getHandCardLayout(cards, width, height);
                    break;
            }
            return cardLayoutMap;
        }

        public static Map<String,CardsShowLayout.CardLayout> getTableShowCardLayout(
                List<String> cards, int width,int height){
            Map<String,CardsShowLayout.CardLayout> cardLayoutMap = null;
            if(cards != null && width>0 && height>0) {
                cardLayoutMap = new HashMap<>();
                final int margin = 16;
                int l = 0, t = 0, r = 0, b = 0;
                for(int index=0;index<cards.size();index++) {
                    if (cards.size() == 1) {
                        r = width; b = height;
                        cardLayoutMap.put(cards.get(index),new CardsShowLayout.CardLayout(l,t,r,b,0));
                    } else {
                        int column = (int) Math.sqrt(index - 1) + 1;
                        int w = width / column;
                        int h = height / column - margin * (column - 2) / 2;
                        for (int i = 0; i < index; i++) {
                            int x = i / column;
                            int y = i % column;
                            l = w * y;
                            r = l + w;
                            t = h * x + margin * x;
                            b = t + h - margin;
                            cardLayoutMap.put(cards.get(index),new CardsShowLayout.CardLayout(l,t,r,b,0));
                        }
                    }

                }
            }
            return cardLayoutMap;
        }

        private static int[] getRowData(int index,int[] columns){
            int row=0,rowCount=0,position=0;
            for (int i = 0; i < columns.length; i++) {
                rowCount = columns[i]-rowCount;
                if(columns[i]>index) {
                    row = i;
                    break;
                }
            }
            if(row == 0)
                position = index;
            else
                position = index -columns[row-1];
            return new int[]{row,rowCount,position};
        }

        public static Map<String,CardsShowLayout.CardLayout> getHandShowCardLayout(
                List<String> cards, int width,int height){
            final int defRow =2,defColumn =4;
            Map<String,CardsShowLayout.CardLayout> cardLayoutMap = null;
            int count,w;
            if(cards != null && width>0 && height>0) {
                count=cards.size();
                int[] columns =getColumns(count,defColumn);
                final int margin = 16;
                final int sw = width / (3 * 3);
                final int[] ws = new int[]{5,7};
                final int[] lefts = new int[]{2,1};
                final int h = height / defRow;
                int l , t , r, b;
                cardLayoutMap = new HashMap<>();
                for(int index=0;index<count;index++) {
                    int[] rowDatas = getRowData(index,columns);
                    w = sw * ws[rowDatas[0]] / rowDatas[1];
                    l = sw * lefts[rowDatas[0]] + w * rowDatas[2] + (margin/2);
                    r = l + w - (margin/2);
                    t = rowDatas[0] * h ;
                    b = t + h ;
                    cardLayoutMap.put(cards.get(index),new CardsShowLayout.CardLayout(l,t,r,b,0));
                }
            }
            return cardLayoutMap;
        }
        public static Map<String,CardsShowLayout.CardLayout> getHandCardLayout(
                List<String> cards, int width,int height){
            Map<String,CardsShowLayout.CardLayout> cardLayoutMap = null;
            if(cards != null && width>0 && height>0) {
                final int margin = 32;
                final float scale =(float) height / (float) (width * 10 / 6);
                int w,h,left,count,l,t,r,b;
                cardLayoutMap = new HashMap<>();
                h = height;
                w = (int)(width * scale);
                count = cards.size();
                left = (width - w * count )/2 ;
                if(left<0)
                    left = ( width - w * count)/(count -1);
                for(int index=0;index<count;index++) {
                    l = left + w * index + margin;
                    r = l + w - margin;
                    if(left<0) {
                        l = (left * index) + w * index ;
                        r = l + w -(margin/2);
                    }
                    t =0;
                    b = t + h;
                    cardLayoutMap.put(cards.get(index),new CardsShowLayout.CardLayout(l,t,r,b,0));
                }
            }
            return cardLayoutMap;
        }

        private static int[] getColumns(int cardCount,int defColumn){
            int[] columns=new int[2];
            int div = cardCount /2 ,mod =cardCount % 2;
            if(cardCount <= 10){
                columns[0]=defColumn;
                columns[1]=columns[0]+defColumn+2;
            }else {
                if (mod > 0) {
                    columns[0] = div;
                    columns[1] = columns[0]+div+1;
                }else {
                    columns[0] = div - 1;
                    columns[1] = columns[0]+div + 1;
                }
            }
            return columns;
        }

    }
}
