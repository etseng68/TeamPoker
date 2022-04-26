package com.ai2app.teampoker.phone;

import android.app.DialogFragment;
import android.os.Bundle;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.ai2app.teampoker.R;
import com.ai2app.teampoker.db.PokerDb;
import com.ai2app.teampoker.db.PokerGame;
import com.ai2app.teampoker.db.PokerRoom;
import com.ai2app.teampoker.view.PhoneScoreView;

import java.util.ArrayList;
import java.util.List;

public class ScoreViewFragment extends DialogFragment implements
        View.OnClickListener {

    private static final String ARG_SHOW_TYPE = "showType";
    private static final String ARG_ROOM_KEY = "roomKey";
    private static final String ARG_ROOM = "pokerRoom";
    private static final String ARG_GAME_KEY = "GameKey";
    private static final String ARG_GAME = "pokerGame";
    private static final String ARG_UID = "uid";
    public static final int SHOW_NORMAL=0,SHOW_FINISH=1;

    private int[] mScoreLayoutRes = new int[]{R.id.phone_score_1,R.id.phone_score_2,
            R.id.phone_score_3,R.id.phone_score_4};
    private int[] mRankingIconRes = new int[]{R.drawable.ic_looks_one_white_24dp,
            R.drawable.ic_looks_two_white_24dp,R.drawable.ic_looks_3_white_24dp,
            R.drawable.ic_looks_4_white_24dp};

    private List<PhoneScoreView> mPhoneScoreViews;

    private String mRoomKey,mGameKey,mUid;
    private PokerGame mPokerGame;
    private PokerRoom mPokerRoom;
    private PokerDb mPokerDb;
    private TextView mKeyText;
    private ImageView mExitView;
    private int mShowType;


    public ScoreViewFragment() {
        // Required empty public constructor
    }

    public static ScoreViewFragment newInstance(int showType, String uid,
                                                String roomKey, PokerRoom pokerRoom,
                                                String gameKey, PokerGame pokerGame) {
        ScoreViewFragment fragment = new ScoreViewFragment();
        Bundle args = new Bundle();
        args.putString(ARG_UID,uid);
        args.putInt(ARG_SHOW_TYPE, showType);
        args.putString(ARG_ROOM_KEY,roomKey);
        args.putSerializable(ARG_ROOM, pokerRoom);
        args.putString(ARG_GAME_KEY,gameKey);
        args.putSerializable(ARG_GAME, pokerGame);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mShowType = getArguments().getInt(ARG_SHOW_TYPE,SHOW_NORMAL);
            mUid =getArguments().getString(ARG_UID);
            mRoomKey = getArguments().getString(ARG_ROOM_KEY);
            mPokerRoom = (PokerRoom) getArguments().getSerializable(ARG_ROOM);
            mGameKey = getArguments().getString(ARG_GAME_KEY);
            mPokerGame = (PokerGame) getArguments().getSerializable(ARG_GAME);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_score_view, container, false);
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (mPhoneScoreViews == null)
            mPhoneScoreViews = new ArrayList<>();
        for(int res:mScoreLayoutRes)
            mPhoneScoreViews.add((PhoneScoreView) view.findViewById(res));
        mKeyText = view.findViewById(R.id.phone_room_game_key);
        mKeyText.setText(mRoomKey+":"+mGameKey);
        mExitView = view.findViewById(R.id.phone_score_exit);
        mExitView.setOnClickListener(this);

    }
    @Override
    public void onStart() {
        super.onStart();
        getDialog().setCanceledOnTouchOutside(true);
        Window window = getDialog().getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;
        wlp.width = WindowManager.LayoutParams.MATCH_PARENT;
        wlp.height = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(wlp);
        setViewValue(mPokerGame);
    }

    @Override
    public void onClick(View v) {
        dismiss();
    }

    private void setViewValue(PokerGame pokerGame){
        if(mPokerGame != null) {
            for (int i = 0; i < pokerGame.getRanking().size(); i++) {
                String userId = pokerGame.getRanking().get(i);
                PokerGame.GameUser gameUser = pokerGame.getGameUserMap().get(userId);
                mPhoneScoreViews.get(i).setViewValues(mRankingIconRes[i],
                        gameUser.getUserName(),String.valueOf(gameUser.getScore()),gameUser.getLogo(),
                        gameUser.getGathers());
            }
        }
    }

}
