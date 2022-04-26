package com.ai2app.teampoker.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;

import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.ai2app.teampoker.R;
import com.ai2app.teampoker.comm.Poker;
import com.ai2app.teampoker.comm.PokerDrawable;
import com.ai2app.teampoker.comm.Tools;

import java.util.List;

public class PhoneScoreView extends LinearLayout{
    private static final String TAG = "PhoneScoreView";
    private ImageView mLogoImage,mPlaceImage;
    private TextView mNameText,mScoreText,mDeductText;
    private ScoreLayout mScoreLayout;
    private String mName,mScore;
    private List<String> mPairs;
    private Drawable mLogo,mPlace;
    public PhoneScoreView(Context context) {
        super(context);
        init(context,null);
    }

    public PhoneScoreView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context,attrs);
    }

    private void init(Context context, AttributeSet attrs){
        if(attrs !=null){
            TypedArray typedArray = context.getTheme().obtainStyledAttributes
                    (attrs, R.styleable.PhoneScoreCard,0,0);
            try {
                mName = Tools.convertCharsToString(
                        typedArray.getText(R.styleable.PhoneScoreCard_scoreName));
                mScore = Tools.convertCharsToString(
                        typedArray.getText(R.styleable.PhoneScoreCard_scoreScore));
                mPairs = Tools.convertCharsToList(
                        typedArray.getTextArray(R.styleable.PhoneScoreCard_scorePairs));
                mLogo = typedArray.getDrawable(R.styleable.PhoneScoreCard_scoreLogo);
                mPlace= typedArray.getDrawable(R.styleable.PhoneScoreCard_scorePlace);
            }finally {
                typedArray.recycle();
            }
            initView(context);
        }
    }
    private void initView(Context context){
        inflate(context,R.layout.layout_phone_score,this);
        mNameText=findViewById(R.id.score_name);
        mScoreText=findViewById(R.id.score_num);
        mDeductText=findViewById(R.id.score_deduct);
        mLogoImage=findViewById(R.id.score_logo);
        mPlaceImage=findViewById(R.id.score_place);
        mScoreLayout = findViewById(R.id.score_pairs);
        setViewValues();
    }
    private void setViewValues(){
        mNameText.setText(mName);
        mScoreText.setText(mScore);
        setDeductText(mScore);
        mPlaceImage.setImageDrawable(mPlace);
        mLogoImage.setImageDrawable(mLogo);
        mScoreLayout.setCardPairs(mPairs);
    }
    public void setViewValues(int placeRes,String name,String score,String logo,List<String> pairs){
        mName = name;
        mScore =score;
        mLogo = getResources().getDrawable(PokerDrawable.getDrawableId(getContext(),logo));
        mPlace = getResources().getDrawable(placeRes);
        mPairs = pairs;
        setViewValues();
    }
    private void setDeductText(String score){
        int s =0,deduct=0;
        if (score != null)
            s = Integer.parseInt(score);
        deduct = s-Poker.SCORE_AVG;
        int color =getResources().getColor(R.color.DarkGray);
        if(deduct>0) {
            color =getResources().getColor(R.color.DarkRed);
        }
        mDeductText.setText(String.valueOf(deduct));
        //mDeductText.setTextColor(color);
        mDeductText.setBackgroundColor(color);
    }

}
