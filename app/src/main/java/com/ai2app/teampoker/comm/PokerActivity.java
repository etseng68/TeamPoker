package com.ai2app.teampoker.comm;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;

import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ai2app.teampoker.R;
import com.ai2app.teampoker.db.Member;
import com.ai2app.teampoker.db.PokerDb;
import com.ai2app.teampoker.db.PokerGame;
import com.ai2app.teampoker.db.PokerRoom;
import com.ai2app.teampoker.db.PokerUser;
import com.ai2app.teampoker.phone.JoinDialogFragment;
import com.ai2app.teampoker.phone.PhoneMainActivity;
import com.ai2app.teampoker.phone.UserChoiceActivity;
import com.google.firebase.auth.FirebaseAuth;

public class PokerActivity extends AppCompatActivity implements JoinDialogFragment.OnJoinListener{
    private final static String TAG = "PokerActivity";

    public static final String ARG_MEMBER = "member";
    public static final String ARG_USER_LOGO = "userLogo";
    public static final String ARG_USER_NAME = "userName";
    public static final String ARG_ROOM_KEY = "roomKey";
    public static final String ARG_ROOM = "pokerRoom";
    public static final String ARG_GAME_KEY = "gameKey";
    public static final String ARG_GAME = "pokerGame";
    public static final String ARG_ACTION_TYPE = "actionType";

    private int mActType=-1;
    private String mUserName,mUserLogo;
    private String mRoomKey,mGameKey;
    private Dialog mPb;
    private String mUid;
    private PokerDb mPokerDb;
    private PokerGame mPokerGame;
    private PokerRoom mPokerRoom;
    private Member mMember;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setArgData(getIntent());
    }
    private void setArgData(Intent intent){
        if(intent != null){
            mActType = intent.getIntExtra(ARG_ACTION_TYPE,-1);
            mUserName = intent.getStringExtra(ARG_USER_NAME);
            mUserLogo = intent.getStringExtra(ARG_USER_LOGO);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        queryMember();
    }
    private void queryMember(){
        getPb().show();
        getPokerDb().getMember(getUid(), new PokerDb.OnMemberListener() {
            @Override
            public void onComplete(String uid, final Member member) {
                getPb().dismiss();
                if(member != null){
                    mMember = member;
                    if(mMember.getGameRoom() != null && !mMember.getGameRoom().isEmpty()){
                        getPb().show();
                        getPokerDb().getPokerRoom(mMember.getGameRoom(), new PokerDb.OnGetRoomListener() {
                            @Override
                            public void onComplete(String roomKey, PokerRoom pokerRoom) {
                                getPb().dismiss();
                                if(pokerRoom != null && pokerRoom.getStatus() != PokerRoom.ROOM_FINISH){
                                    setPokerRoomAndQueryGame(roomKey,pokerRoom);
                                }else {
                                    mMember.setGameRoom(null);
                                    getPokerDb().setMember(getUid(),mMember,null);
                                    queryRoom();
                                }
                            }
                        });
                    }
                    else
                        queryRoom();
                }
                else {
                    Toast.makeText(getApplicationContext(), getString(R.string.phone_member_null),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void queryRoom(){
        getPb().show();
        getPokerDb().queryRoom(getUid(), new PokerDb.OnGetRoomListener() {
            @Override
            public void onComplete(String roomKey, PokerRoom pokerRoom) {
                getPb().dismiss();
                if(roomKey == null && pokerRoom == null){
                    if(mActType >-1 && mUserName != null && mUserLogo != null)
                        createOrJoinRoom();
                    else
                        intentUserChoice();
                } else {
                    setPokerRoomAndQueryGame(roomKey,pokerRoom);
                }
            }
        });
    }
    private void setPokerRoomAndQueryGame(String roomKey,PokerRoom pokerRoom){
        if (pokerRoom.getCreateUid() != null && pokerRoom.getJoins() != null
                && pokerRoom.getJoins().get(getUid())) {
            mActType = PokerUser.PLAYER;
            if (pokerRoom.getCreateUid().equals(getUid()))
                mActType = PokerUser.CREATOR;
            PokerUser pokerUser = Tools.getPokerUser(pokerRoom, getUid());
            mUserName = pokerUser.getUserName();
            mUserLogo = pokerUser.getLogo();
            mRoomKey = roomKey;
            mPokerRoom = pokerRoom;
            queryGame(roomKey,pokerRoom);
        } else {
            Log.d(TAG, "onComplete: pokerRoom.getCreateUid() is null");
            queryRoom();
        }
    }
    private void createOrJoinRoom(){
        if(mActType == PokerUser.CREATOR) {
            getPb().show();
            final PokerRoom pokerRoom = new PokerRoom(getUid(),mUserName,mUserLogo,
                    getString(R.string.robot_name));
            getPokerDb().createRoom(pokerRoom, new PokerDb.OnRoomCreateListener() {
                @Override
                public void onComplete(String roomKey, String roomInKey) {
                    getPb().dismiss();
                    mRoomKey = roomKey;
                    setMemberRoomKey(roomKey);
                    createGame(roomKey,pokerRoom);
                }
            });
        }
        else
          showInputPinCodeDialog();
    }

    private void createGame(String roomKey,PokerRoom pokerRoom){
        mPokerRoom = pokerRoom;
        getPb().show();
        getPokerDb().createGame(roomKey,pokerRoom,getString(R.string.message_join),
                getString(R.string.phone_cover_start), new PokerDb.OnPokerGameListener() {
            @Override
            public void onComplete(String pokerGameKey, PokerGame pokerGame, int failType) {
                getPb().dismiss();
                if(failType == PokerDb.FAIL_TYPE_NONE){
                    mGameKey= pokerGameKey;
                    mPokerGame = pokerGame;
                    intentPhoneMain();
                }else
                    Log.d(TAG, "onComplete: createGame="+failType);
            }

        });
    }
    private void queryGame(String roomKey, final PokerRoom pokerRoom){
        getPb().show();
        getPokerDb().queryPokerGame(roomKey, new PokerDb.OnPokerGameListener() {
            @Override
            public void onComplete(String pokerGameKey, PokerGame pokerGame, int failType) {
                getPb().dismiss();
                if(failType != PokerDb.FAIL_TYPE_NONE){
                //if(pokerGameKey == null && pokerGame == null){
                    if(mActType == PokerUser.CREATOR)
                        createGame(mRoomKey, mPokerRoom);
                    else
                        joinGame(mRoomKey,pokerRoom,pokerGameKey, pokerGame);
                }else{
                    mGameKey= pokerGameKey;
                    mPokerGame = pokerGame;
                    intentPhoneMain();
                }
            }
        });
    }

    private void showInputPinCodeDialog(){
        JoinDialogFragment joinDialogFragment = JoinDialogFragment.newInstance();
        joinDialogFragment.show(getFragmentManager(),"join");
    }

    @Override
    public void onJoinRoom(String roomKey, PokerRoom pokerRoom,String gameKey,PokerGame pokerGame) {
        if(roomKey !=null && pokerRoom !=null) {
            joinGame(roomKey, pokerRoom,gameKey,pokerGame);
        }else
            finish();
    }
    private void setMemberRoomKey(String roomKey){
        if(mMember != null){
            mMember.setGameRoom(roomKey);
            getPokerDb().setMember(getUid(),mMember,null);
        }
    }
    public void joinGame(final String roomKey, PokerRoom pokerRoom,String gameKey,PokerGame pokerGame){
        pokerRoom.replaceRobot(getUid(),mUserName,mUserLogo);
        pokerGame.getTable().setCoverAndMessage(mUserLogo,getString(R.string.phone_cover_join));
        pokerGame.replaceGameUser(getUid(),Tools.getPokerUser(pokerRoom,getUid()),
                getString(R.string.phone_user_message_join));
        getPokerDb().setPokerRoomAndPokerGame(roomKey,pokerRoom,gameKey,pokerGame);
        mRoomKey =roomKey;
        mPokerRoom = pokerRoom;
        mGameKey = gameKey;
        mPokerGame = pokerGame;
        setMemberRoomKey(roomKey);
        intentPhoneMain();
    }


    private void intentPhoneMain(){
        Intent intent = new Intent(PokerActivity.this, PhoneMainActivity.class);
        intent.putExtra(ARG_MEMBER,mMember);
        intent.putExtra(ARG_ACTION_TYPE,mActType);
        intent.putExtra(ARG_ROOM_KEY,mRoomKey);
        intent.putExtra(ARG_ROOM,mPokerRoom);
        intent.putExtra(ARG_GAME_KEY,mGameKey);
        intent.putExtra(ARG_GAME,mPokerGame);
        startActivity(intent);
        finish();
    }
    private void intentUserChoice(){
        Intent intent = new Intent(PokerActivity.this, UserChoiceActivity.class);
        startActivity(intent);
        finish();
    }

    private Dialog getPb(){
        if(mPb == null)
            mPb = PokerDialog.getProgressDialog(this,
                    PokerDialog.progressDialogView(this,null,null));
        return mPb;
    }
    private String getUid(){
        if(mUid == null)
            mUid = FirebaseAuth.getInstance().getUid();
        return mUid;
    }
    public PokerDb getPokerDb(){
        if(mPokerDb == null);
        mPokerDb = new PokerDb();
        return mPokerDb;
    }


}
