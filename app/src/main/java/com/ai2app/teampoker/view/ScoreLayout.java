package com.ai2app.teampoker.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.ViewGroup;

import com.ai2app.teampoker.R;
import com.ai2app.teampoker.view.ScorePairCardView;

import java.util.ArrayList;
import java.util.List;

public class ScoreLayout extends ViewGroup{
    private static final String TAG="TableShowLayout";
    private List<String> mCardPairs;
    private ScorePairCardView mScorePairCardView;

    public ScoreLayout(Context context) {
        super(context);
        init(context,null);
    }

    public ScoreLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context,attrs);
    }

    private void init(Context context, AttributeSet attrs){
        TypedArray typedArray = context.getTheme().obtainStyledAttributes
                (attrs, R.styleable.TableView,0,0);
        try {
            mCardPairs = convertToList(typedArray.getTextArray(R.styleable.TableView_cardRes));
        }finally {
            typedArray.recycle();
        }
        createView();
    }
    private List<String> convertToList(CharSequence[] sequences){
        List<String> cards = null;
        if(sequences !=null){
            cards = new ArrayList<>();
            for(int i=0;i<sequences.length;i++){
                cards.add(i,sequences[i].toString());
            }
        }
        return cards;
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        measureChildren(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        setCarPairsLayout();
    }

    private void setCarPairsLayout(){
        if(mScorePairCardView != null){
            mScorePairCardView.layout(0, 0,
                    mScorePairCardView.getMeasuredWidth(), mScorePairCardView.getMeasuredHeight());
        }
    }

    private void createView(){
        removeAllViews();
        mScorePairCardView = getScorePairCardView();
        if(mScorePairCardView != null)
            addView(mScorePairCardView);
    }

    private ScorePairCardView getScorePairCardView(){
        List<ScorePairCardView.CardPair> cardPairs = getCardPairScores();
        ScorePairCardView cardView= null;
        if(cardPairs != null && cardPairs.size()>0) {
            cardView = new ScorePairCardView(getContext(), getCardPairScores(),0f);
            cardView.setTag(true);
        }
        return cardView;
    }

    private List<ScorePairCardView.CardPair> getCardPairScores(){
        List<ScorePairCardView.CardPair> cardPairs = null;
        if(mCardPairs != null){
            cardPairs = new ArrayList<>();
            for(int i=0;i<mCardPairs.size();i++){
                String cardPairStr=mCardPairs.get(i);
                cardPairs.add(i,new ScorePairCardView.CardPair(
                        cardPairStr.split(",")[0].split(":")[0],
                        cardPairStr.split(",")[0].split(":")[1],
                        Integer.parseInt(cardPairStr.split(",")[1])));
            }
        }
        return cardPairs;
    }

    public void setCardPairs(List<String> cardPairs) {
        this.mCardPairs = cardPairs;
        createView();
        invalidate();
        requestLayout();
    }

}
