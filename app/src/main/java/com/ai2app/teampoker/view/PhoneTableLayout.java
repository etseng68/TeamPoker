package com.ai2app.teampoker.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.ai2app.teampoker.R;
import com.ai2app.teampoker.comm.Poker;
import com.ai2app.teampoker.comm.PokerDrawable;
import com.ai2app.teampoker.comm.Tools;
import com.ai2app.teampoker.db.PokerGame;
import java.util.List;


public class PhoneTableLayout extends LinearLayout {
    private static final String TAG = "PhoneTableLayout";
    public static final int MAX_USER =3;
    private static final int[] mLogoRes= new int[]{R.id.logo_a,R.id.logo_b,R.id.logo_c};
    private static final int[] mMssageRes= new int[]{R.id.msg_a,R.id.msg_b,R.id.msg_c};
    private static final int[] mNameRes= new int[]{R.id.name_a,R.id.name_b,R.id.name_c};
    private static final int mCoverImageRes = R.id.cover_view,mCoverMessageRes = R.id.cover_txt;
    private static final int mCardShowLayoutRes = R.id.cards_show;
    private static final String mDefaultMaterialColorName = "Brown";
    private static final String mDefaultUserCircleColor = "Red_900";

    private ImageView[] mLogoImages;
    private TextView[] mNameTexts,mMsgTexts;
    private ImageView mCoverImage;
    private TextView mCoverText;
    private CardsShowLayout mCardsShowLayout;
    private OnClickListener mOnClickListener;
    private OnLongClickListener mOnLongClickListener;
    private int mCurrentColor ;

    private int mWellColor,mFloorColor,mTableColor;

    public PhoneTableLayout(Context context) {
        super(context);
        init(context,null);
    }
    public PhoneTableLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context,attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(mFloorColor);
        canvas.drawPath(getWallPath(),getWallPaint());
        canvas.drawPath(getTablePath(),getTablePaint());
    }
    private Path getWallPath(){
        Path path = new Path();
        path.moveTo(0,0);
        path.lineTo(getWidth(),0);
        path.lineTo(getWidth(),getHeight()/2);
        path.lineTo(0,getHeight()/2);
        path.lineTo(0,0);
        return path;
    }
    private Paint getWallPaint(){
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(mWellColor);
        return paint;
    }

    private Path getTablePath(){
        Path path = new Path();
        path.moveTo(0,getHeight());
        path.lineTo(getWidth()/3,getHeight()/4);
        path.lineTo(getWidth() /3 *2,getHeight()/4);
        path.lineTo(getWidth(),getHeight());
        path.lineTo(0,getHeight());
        return path;
    }
    private Paint getTablePaint(){
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(mTableColor);
        return paint;
    }
    private void init(Context context, AttributeSet attrs){
        setWillNotDraw(false);
        initView(context);
        if(attrs !=null){
            TypedArray typedArray = context.getTheme().obtainStyledAttributes
                    (attrs, R.styleable.PhoneTable,0,0);
            try {
                setViewValues(typedArray.getTextArray(R.styleable.PhoneTable_logosRes),
                        typedArray.getTextArray(R.styleable.PhoneTable_namesRes),
                        typedArray.getTextArray(R.styleable.PhoneTable_msgsRes),
                        typedArray.getTextArray(R.styleable.PhoneTable_cardsRes),
                        typedArray.getString(R.styleable.PhoneTable_coverRes),
                        typedArray.getString(R.styleable.PhoneTable_coverText));
                setMaterialColor(typedArray.getString(R.styleable.PhoneTable_materialColor));
                mCurrentColor =typedArray.getInteger(R.styleable.PhoneTable_userCircleColor,
                        PokerDrawable.getColorByName(context,mDefaultUserCircleColor));
                //mDrawable = PokerDrawable.getVectoreDrawable(context,R.styleable.PokerCard_imageSrc);
            }finally {
                typedArray.recycle();
            }
        }
    }
    private void initView(Context context){
        mOnClickListener = (OnClickListener)context;
        mOnLongClickListener = (OnLongClickListener) context;
        //mCurrentColor = Color.TRANSPARENT;
        inflate(context,R.layout.layout_phone_table,this);

        mLogoImages= new ImageView[MAX_USER];
        mNameTexts = new TextView[MAX_USER];
        mMsgTexts = new TextView[MAX_USER];
        for(int i=0;i<MAX_USER;i++){
            //PTData.User user =mPtData.getUsers().get(i);
            mLogoImages[i] = findViewById(mLogoRes[i]);
            mNameTexts[i]=findViewById(mNameRes[i]);
            mMsgTexts[i] =findViewById(mMssageRes[i]);
        }
        mCoverImage = findViewById(mCoverImageRes);
        mCoverImage.setOnClickListener(mOnClickListener);
        mCoverText =findViewById(mCoverMessageRes);
        mCoverText.setTextColor(Color.WHITE);
        mCardsShowLayout = findViewById(mCardShowLayoutRes);
        mCardsShowLayout.setOnClickListener(mOnClickListener);
    }
    private void setMaterialColor(String materialColorName){
        if(materialColorName == null)
            materialColorName = mDefaultMaterialColorName;
        mTableColor = PokerDrawable.getColorByName(getContext(),materialColorName+"_500");
        mWellColor = PokerDrawable.getColorByName(getContext(),materialColorName+"_300");
        mFloorColor = PokerDrawable.getColorByName(getContext(),materialColorName+"_100");
    }

    private void setViewValues(CharSequence[] logosRf, CharSequence[] namesRf,
                               CharSequence[] msgsRf, CharSequence[] cardsRf,
                               String coverImageRf,String coverMessageRf) {
        setViewValues(Tools.convertCharsToList(logosRf),Tools.convertCharsToList(namesRf),
                Tools.convertCharsToList(msgsRf),Tools.convertCharsToList(cardsRf),
                coverImageRf,coverMessageRf);
    }
    private void setViewValues(List<String> logos, List<String> names,List<String> messages,
                               List<String> cards, String coverImage,String coverMessage){
        for(int i=0;i<MAX_USER;i++){
            if(logos != null && logos.size()>0)
                mLogoImages[i].setImageResource(getRes(logos.get(i)));
            if(names != null && names.size()>0)
                mNameTexts[i].setText(names.get(i));
            if(messages != null && messages.size()>0)
                mMsgTexts[i].setText(messages.get(i));
        }
        mCardsShowLayout.setCardsRefs(cards);
        setCover(coverImage,coverMessage);
        reSetValue();
    }
    public void setViewValues(String uid,PokerGame pokerGame,int color){
        mCurrentColor = color;
        List<PokerGame.GameUser> users = Tools.getDeductSelf(uid, pokerGame);
        for (int i = 0; i < users.size(); i++) {
            PokerGame.GameUser user = users.get(i);
            int res = getRes(user.getLogo());
            if (res > 0) {
                if (pokerGame.getOrder() != null && pokerGame.getOrder().size()>0 &&
                        user.getUid() != null && pokerGame.getOrder().get(0).equals(user.getUid()))
                    mLogoImages[i].setImageDrawable(PokerDrawable.getCircleDrawable(
                            getContext(), res, getUserColor()));
                else
                    mLogoImages[i].setImageResource(res);
            }
            mNameTexts[i].setText(user.getUserName());
            mMsgTexts[i].setText(user.getMessage());
        }
        mCardsShowLayout.setCardsRefs(pokerGame.getTable().getShows());
        setCover(pokerGame,uid);
        reSetValue();

    }
    private void setCover(String imageName,String message){
        setCoverImage(imageName);
        setCoverMessage(message);
    }
    private void setCoverImage(String imageName){
        mCoverImage.setImageResource(getRes(imageName));
        mCoverImage.setTag(imageName);
    }
    private void setCoverMessage(String message){
        mCoverText.setText(message);
        if(message == null || message.isEmpty())
            mCoverText.setVisibility(GONE);
        else
            mCoverText.setVisibility(VISIBLE);
    }
    public ImageView getCoverImage(){
        return mCoverImage;
    }
    private int getRes(String ref){
        if (ref != null)
            return PokerDrawable.getDrawableId(getContext(),ref);
        else
            return 0;
    }

    private void setCover(PokerGame pokerGame,String uid){
        String cover = pokerGame.getTable().getCover();
        String message = pokerGame.getTable().getMessage();
        if(pokerGame.getPosition().get(0).equals(uid) && cover.contains("user") &&
                pokerGame.getStatus() == PokerGame.GAME_WAIT) {
            cover = Poker.COVER_WAIT;
            message = null;
        }
        setCover(cover,message);
    }
    public void reSetValue(){
        invalidate();
        requestLayout();
    }
    public int getUserColor() {
        if(mCurrentColor == 0)
            return PokerDrawable.getColorByName(getContext(), mDefaultUserCircleColor);
        else
            return getResources().getColor(mCurrentColor);
    }

}
