package com.ai2app.teampoker.phone;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;

import android.util.Log;
import android.view.DragEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;

import com.ai2app.teampoker.R;
import com.ai2app.teampoker.ad.InterstitialAdActivity;
import com.ai2app.teampoker.help.HelpFragment;
import com.ai2app.teampoker.help.AboutFragment;
import com.ai2app.teampoker.view.CardsShowLayout;
import com.ai2app.teampoker.comm.Poker;
import com.ai2app.teampoker.comm.PokerActivity;
import com.ai2app.teampoker.comm.PokerDialog;
import com.ai2app.teampoker.comm.PokerDrawable;
import com.ai2app.teampoker.db.Member;
import com.ai2app.teampoker.db.PokerGame;
import com.ai2app.teampoker.db.PokerRoom;
import com.ai2app.teampoker.comm.Robot;
import com.ai2app.teampoker.comm.Tools;
import com.ai2app.teampoker.db.PokerDb;
import com.ai2app.teampoker.db.PokerUser;
import com.ai2app.teampoker.view.PhoneTableLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.Map;

public class PhoneMainActivity extends AppCompatActivity implements
        View.OnClickListener,View.OnLongClickListener{
    private final static String TAG = "PhoneMainActivity";
    public final static int CURRENT_COLOR = R.color.Red_900;
    public final static long VIBRATOR_SEC =200l;
    public final static int BAR_ACT_EXIT=0,BAR_ACT_NEW=1;
    private FirebaseFirestore mFireStore = FirebaseFirestore.getInstance();
    private int mActType;
    private String mUid;
    private PokerDb mPokerDb;
    private String mRoomKey,mGameKey;
    private PhoneTableLayout mPhoneTable;
    private CardsShowLayout mPhoneHand;
    private TextView mRoomKeyText,mNameText,mPlayCountText,mRoomScoreText,mScoreText;
    private ImageView mLogoView;
    private PokerGame mPokerGame;
    private PokerRoom mPokerRoom;
    private CardDragListener mCardDragListener;
    private Dialog mPb;
    private ListenerRegistration mRoomListenerRegistration;
    private ListenerRegistration mGameListenerRegistration;
    private String[] mCoverMessage;
    private LinearLayout mMessageLayout;
    private Vibrator mVibrator;
    private Member mMember;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_phone_main);
        setArgData(getIntent());
        initToolBar();
        initView();
    }
    private void initToolBar(){
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        int menuLayoutRes = R.menu.menu_phone_plyer;
        if(mActType == PokerUser.CREATOR)
            menuLayoutRes = R.menu.menu_phone_creator;
        getMenuInflater().inflate(menuLayoutRes, menu);
        return true;

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
//                showAlertMessage(BAR_ACT_EXIT,getString(R.string.phone_alert_exit_title),
//                        getString(R.string.phone_alert_exit_body));
                return true;
            case R.id.phone_bar_ranking:
                showScoreViewDialog(ScoreViewFragment.SHOW_NORMAL);
                return true;
            case R.id.phone_bar_new_game:
                String message=getString(R.string.phone_alert_new_body_finish_no);
                if(mPokerGame.getStatus() >= PokerGame.GAME_FINISH)
                    message=getString(R.string.phone_alert_new_body_finish);
                showAlertMessage(BAR_ACT_NEW,getString(R.string.phone_alert_new_title), message);
                return true;
            case R.id.phone_bar_about:
                AboutFragment aboutFragment = AboutFragment.newInstance();
                aboutFragment.show(getSupportFragmentManager(), "about");
                return true;
            case R.id.phone_bar_help:
                HelpFragment helpFragment = HelpFragment.newInstance(HelpFragment.TYPE_PHONE_MAIN);
                helpFragment.setStyle(DialogFragment.STYLE_NORMAL,
                        android.R.style.Theme_Holo_NoActionBar);
                helpFragment.show(getSupportFragmentManager(),"help");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setToolBarTitle(){
        if(mMember != null)
            getSupportActionBar().setTitle(getString(R.string.my_score)+":"+mMember.getPoint());
    }

    private void setArgData(Intent intent){
        if(intent != null){
            mActType = intent.getIntExtra(PokerActivity.ARG_ACTION_TYPE,PokerUser.PLAYER);
            mRoomKey = intent.getStringExtra(PokerActivity.ARG_ROOM_KEY);
            mPokerRoom = (PokerRoom) intent.getSerializableExtra(PokerActivity.ARG_ROOM);
            mGameKey = intent.getStringExtra(PokerActivity.ARG_GAME_KEY);
            mPokerGame = (PokerGame) intent.getSerializableExtra(PokerActivity.ARG_GAME);
            mMember =(Member) intent.getSerializableExtra(PokerActivity.ARG_MEMBER);
            mCoverMessage = getResources().getStringArray(R.array.coverMessage);
        }
    }
    private void getMember(){
            getPb().show();
            getPokerDb().getMember(getUid(), new PokerDb.OnMemberListener() {
                @Override
                public void onComplete(String uid, Member member) {
                    getPb().dismiss();
                    if(member != null) {
                        mMember = member;
                        setToolBarTitle();
                    }
                    else
                        Toast.makeText(getApplicationContext(), R.string.phone_member_null,Toast.LENGTH_SHORT).show();
                }
            });

    }

    @Override
    protected void onResume() {
        super.onResume();
        setViewValues();
    }

    private void initView(){
        if(mPokerRoom != null && mPokerGame != null) {
            mCardDragListener = new CardDragListener();
            mRoomKeyText = findViewById(R.id.phone_room_in_key);
            mRoomKeyText.setText(mPokerRoom.getRoomInKey());
            mRoomKeyText.setOnClickListener(this);

            mPhoneTable = findViewById(R.id.phone_table);
            mPhoneTable.setOnClickListener(this);
            mPhoneTable.setOnDragListener(mCardDragListener);

            mPhoneHand = findViewById(R.id.phone_hand);
            mPhoneHand.setOnLongClickListener(this);

            mMessageLayout = findViewById(R.id.phone_message_layout);
            PokerGame.GameUser gameUser = mPokerGame.getGameUserMap().get(getUid());
            mLogoView = findViewById(R.id.phone_logo);
            mLogoView.setImageResource(PokerDrawable.getDrawableId(this, gameUser.getLogo()));
            mNameText = findViewById(R.id.phone_name);
            mNameText.setText(gameUser.getUserName());

            mPlayCountText = findViewById(R.id.phone_room_play_count);
            mRoomScoreText = findViewById(R.id.phone_room_sum_score);
            mScoreText = findViewById(R.id.phone_play_score);
            setPhoneMessage();
            //mScoreText.setText(String.valueOf(gameUser.getScore()));

        }
    }
    private void setViewValues(){
        getMember();
        setToolBarTitle();
        gameListener();
        roomListener();
    }
    private void gameListener(){
        if(mGameListenerRegistration == null) {
            mGameListenerRegistration = mFireStore
                    .collection(PokerDb.POKER + "/" + mRoomKey + "/" + PokerDb.GAME).document(mGameKey)
                    .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(DocumentSnapshot snapshot,
                                            FirebaseFirestoreException e) {
                            if (e != null) {
                                Log.i(TAG, "gameListener():Listen failed.", e);
                                return;
                            }
                            if (snapshot != null && snapshot.exists()) {
                                PokerGame pokerGame = snapshot.toObject(PokerGame.class);
                                setPhoneViewGroup(pokerGame);
                            } else {
                                Log.i(TAG, "gameListener():Current data: null");
                            }
                        }
                    });
        }
    }
    private void roomListener(){
        if(mRoomListenerRegistration == null) {
            mRoomListenerRegistration = mFireStore.collection(PokerDb.POKER).document(mRoomKey)
                    .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(DocumentSnapshot snapshot, FirebaseFirestoreException e) {
                            if (e != null) {
                                Log.i(TAG, "roomListener():Listen failed.", e);
                                return;
                            }
                            if (snapshot != null && snapshot.exists()) {
                                mPokerRoom = snapshot.toObject(PokerRoom.class);
                                setPhoneMessage();
                                //mRoomScoreText.setText(Tools.getPokerUser(mPokerRoom,getUid()).getScore());
                                if(mPokerRoom.getStatus() == PokerRoom.ROOM_FINISH)
                                    finish();
                            } else {
                                Log.i(TAG, "gameListener():Current data: null");
                            }
                        }
                    });
        }
    }

    private void setPhoneViewGroup(PokerGame pokerGame){
        mPokerGame = pokerGame;
        setPhoneMessage();
        getPb().dismiss();
        if (pokerGame != null ) {
            switch (pokerGame.getStatus()){
                case PokerGame.GAME_WAIT:
                    setPhoneLayouts(pokerGame);
                    break;
                case PokerGame.GAME_RUN:
                    setPhoneLayouts(pokerGame);
                    if(pokerGame.getOrder().size() == 0){
                        pokerGame.setStatus(PokerGame.GAME_FINISH);
                        savePokerGameToFireStore(pokerGame);
                    }
                    break;
                case PokerGame.GAME_FINISH:
                    setPhoneLayouts(pokerGame);
                    showScoreViewDialog(ScoreViewFragment.SHOW_FINISH);
                    //mPokerGame.setDeduct(Poker.SCORE_AVG);
                    break;
                case PokerGame.GAME_OVER:
                    if(!mPokerRoom.getCreateUid().equals(getUid())) {
                        if (pokerGame.getNextGameKey() != null) {
                            mGameListenerRegistration.remove();
                            getPb().show();
                            getPokerDb().getPokerGame(mRoomKey, mPokerGame.getNextGameKey(),
                                    new PokerDb.OnPokerGameListener() {
                                        @Override
                                        public void onComplete(String pokerGameKey, PokerGame pokerGame, int failType) {
                                            getPb().dismiss();
                                            if (failType == PokerDb.FAIL_TYPE_NONE)
                                                intentPhone(pokerGameKey, pokerGame);
                                            else
                                                Log.d(TAG, "onComplete: fail" + failType);
                                        }
                                    });

                        } else
                            Toast.makeText(this, R.string.phone_game_key_null, Toast.LENGTH_SHORT).show();
                    }

                    break;
            }

        }

    }
    private void showScoreViewDialog(int type){
        if(getFragmentManager().findFragmentByTag("score") == null){
            ScoreViewFragment scoreViewFragment =
                    ScoreViewFragment.newInstance(type, getUid(), mRoomKey, mPokerRoom, mGameKey, mPokerGame);
            scoreViewFragment.show(getFragmentManager(),"score");
        }
        else
            Log.d(TAG, "showScoreViewDialog: is show");
    }
    private void setPhoneLayouts(PokerGame pokerGame){
        setPhoneTable(pokerGame);
        setPhoneHand(pokerGame);
        if (pokerGame.getOrder() != null && pokerGame.getOrder().size() > 0){
            robotAction(pokerGame);
            setVibrator(pokerGame);
            //setPhoneMessage(pokerGame);
        }
    }
    private void setVibrator(PokerGame pokerGame){
        if(mVibrator == null)
            mVibrator = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);
        if(pokerGame.getOrder().size()>0 && pokerGame.getOrder().get(0).equals(getUid()) &&
                pokerGame.getActions().get(0) == Poker.ACT_THROW)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mVibrator.vibrate(VibrationEffect.createOneShot(VIBRATOR_SEC,20));
            }else
                mVibrator.vibrate(VIBRATOR_SEC);
    }
    private void setPhoneTable(PokerGame pokerGame){
        //mPhoneTable.setPtData(getUid(),pokerGame,CURRENT_COLOR);
        mPhoneTable.setViewValues(getUid(),pokerGame,CURRENT_COLOR);
    }
    private void setPhoneHand(PokerGame pokerGame){
        PokerGame.GameUser user = pokerGame.getGameUserMap().get(getUid());
        if(user != null && user.getHands() != null) {
            mPhoneHand.setCardsRefs(user.getHands());
        }
    }

//    private void setPhoneMessage(PokerGame pokerGame){
//        int backColor = Color.TRANSPARENT;
//        int fontColor = Color.BLACK;
//        int scoreImageRes = R.drawable.ic_chat_black_24dp;
//        if(pokerGame.getOrder().size()>0 && pokerGame.getOrder().get(0).equals(getUid())) {
//            backColor = getResources().getColor(CURRENT_COLOR);
//            fontColor = Color.WHITE;
//            scoreImageRes = R.drawable.ic_chat_white_24dp;
//        }
//        mMessageLayout.setBackgroundColor(backColor);
//        //mMessage.setText(pokerGame.getGameUserMap().get(getUid()).getMessage());
//        setMyScore();
//        mNameText.setTextColor(fontColor);
////        mMessage.setTextColor(fontColor);
////        mMessage.setCompoundDrawablesWithIntrinsicBounds(messageImageRes,0,0,0);
//    }
    private void setPhoneMessage(){
        int backColor = Color.TRANSPARENT;
        int fontColor = Color.BLACK;
        int scoreImageRes = R.drawable.ic_stars_black_24dp;
        int countImageRes =R.drawable.ic_playlist_add_check_black_24dp;
        int sumImageRes =R.drawable.ic_playlist_add_black_24dp;
        long score =0,sum =0;
        int count=0;
        if(mPokerRoom != null){
            count = mPokerRoom.getGameCount();
            if(mPokerRoom.getUsers() != null && mPokerRoom.getUsers().size()>0) {
                PokerUser pokerUser = Tools.getPokerUser(mPokerRoom,getUid());
                if(pokerUser != null )
                    sum = pokerUser.getDeduct();
            }
        }
        if(mPokerGame != null) {
            score = mPokerGame.getGameUserMap().get(getUid()).getScore();
            if(mPokerGame.getOrder() != null && mPokerGame.getOrder().size()>0
                    && mPokerGame.getOrder().get(0).equals(getUid())){
                backColor = getResources().getColor(CURRENT_COLOR);
                fontColor = Color.WHITE;
                countImageRes =R.drawable.ic_playlist_add_check_white_24dp;
                sumImageRes =R.drawable.ic_playlist_add_white_24dp;
                scoreImageRes = R.drawable.ic_stars_white_24dp;
            }
        }
        mMessageLayout.setBackgroundColor(backColor);
        mNameText.setTextColor(fontColor);
        mPlayCountText.setText(String.valueOf(count));
        mPlayCountText.setTextColor(fontColor);
        mPlayCountText.setCompoundDrawablesWithIntrinsicBounds(countImageRes,0,0,0);

        mRoomScoreText.setText(String.valueOf(sum));
        mRoomScoreText.setTextColor(fontColor);
        mRoomScoreText.setCompoundDrawablesWithIntrinsicBounds(sumImageRes,0,0,0);

        mScoreText.setText(String.valueOf(score));
        mScoreText.setTextColor(fontColor);
        mScoreText.setCompoundDrawablesWithIntrinsicBounds(scoreImageRes,0,0,0);
    }
    private void robotAction(PokerGame pokerGame){
        PokerGame.GameUser gameUser = pokerGame.getGameUserMap().get(pokerGame.getOrder().get(0));
        if(pokerGame.getPosition().get(0).equals(getUid()) &&
                gameUser.getType() == PokerUser.ROBOT){
            pokerGame=Robot.robotAction(pokerGame,mCoverMessage);
            savePokerGameToFireStore(pokerGame);
        }
    }

    private String getUid(){
        if (mUid == null)
            mUid = FirebaseAuth.getInstance().getUid();
        return mUid;
    }
    private void showMessage(String message){
        Toast.makeText(this, message,Toast.LENGTH_LONG).show();
    }

    public void coverAction(View view){
        switch (mPokerGame.getStatus()){
            case PokerGame.GAME_WAIT:
                if(mActType == PokerUser.CREATOR && mPokerGame.getPosition().get(0).equals(getUid()))
                    mPokerGame.initPokerGame(mCoverMessage[0]);
                else
                    Log.d(TAG, "coverAction: Is not you. Must wait creator start!");
                break;
            case PokerGame.GAME_RUN:
                String orderId = mPokerGame.getOrder().get(0);
                PokerGame.GameUser gameUser = mPokerGame.getGameUserMap().get(orderId);
                if(view.getTag() != null && orderId.equals(getUid())) {
                    if(mPokerGame.getActions().get(0) == Poker.ACT_GET) {
                        mPokerGame = Tools.handleUserAction(getUid(), mPokerGame,
                                view.getTag().toString(), mCoverMessage);
                    }else{
                        Log.d(TAG, "coverAction: is not flop!" + gameUser.getUserName());
                    }
                }else
                    Log.d(TAG, "coverAction: is not you, Now is" + gameUser.getUserName());
                break;
            case PokerGame.GAME_OVER:
                break;
        }
        savePokerGameToFireStore(mPokerGame);
    }
    private void cardAction(View cardView){
        String card = cardView.getTag().toString();
        if(mPokerGame.getActions().get(0) == Poker.ACT_THROW_SELECT ||
                mPokerGame.getActions().get(0) == Poker.ACT_GET_SELECT) {
            if (Poker.checkCardsPair(mPokerGame.getTable().getCover(), card)) {
                mPokerGame = Tools.handleUserAction(getUid(), mPokerGame, card, mCoverMessage);
                savePokerGameToFireStore(mPokerGame);
            } else
                showMessage(getString(R.string.phone_alert_not_pair));
        }else {
            if(mPokerGame.getActions().get(0) == Poker.ACT_THROW)
                showMessage(getString(R.string.phone_alert_thorw_card));
            else if(mPokerGame.getActions().get(0) == Poker.ACT_GET)
                showMessage(getString(R.string.phone_alert_flop));
        }
    }
    private void savePokerGameToFireStore(PokerGame pokerGame){
        getPokerDb().setPokerGame(mRoomKey,mGameKey,pokerGame);
    }


    private void showAlertMessage(final int barAct, String title, String message){
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "onClick: cancel");
                    }
                })
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mPokerGame.setDeduct(Poker.SCORE_AVG);
                        switch (barAct){
                            case BAR_ACT_EXIT:
                                finish();
                                break;
                            case BAR_ACT_NEW:
                                createNewGame();
                                break;
                        }
                    }
                })
                .show();

    }
    private void createNewGame(){
        if(mPokerRoom.getCreateUid().equals(getUid())) {
            getPb().show();
            getPokerDb().createGame(mRoomKey, mPokerRoom, "0",
                    getString(R.string.phone_cover_start),
                    new PokerDb.OnPokerGameListener() {
                        @Override
                        public void onComplete(String pokerGameKey, PokerGame pokerGame, int failType) {
                            getPb().dismiss();
                            if(failType == PokerDb.FAIL_TYPE_NONE)
                                saveNewGame(pokerGameKey, pokerGame);
                            else
                                Log.d(TAG, "onComplete: fail"+failType);
                        }
            });
        }
        else
            Log.d(TAG, "createNewGame: not create user");
    }
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.cover_view:
                Log.i(TAG, "onClick: cover_view");
                if(mPokerGame != null)
                    coverAction(view);
                else
                    Log.d(TAG, "onClick: PokerGame not work!");
                    //mMessage.setText("PokerGame not work!");
                break;
            case R.id.phone_room_in_key:
                Log.d(TAG, "onClick: phone_room_in_key");
                break;
            default:
                int pid = ((ViewGroup)view.getParent()).getId();
                if(pid == R.id.cards_show){
                    Log.i(TAG, "onClick: cards_show");
                    cardAction(view);
                }
                else{
                    Log.i(TAG, "onClick: phone_hand");
                }
                break;
        }
    }
    @Override
    public boolean onLongClick(View view) {
        switch (view.getId()){
            case R.id.phone_table:
                Log.i(TAG, "onLongClick: phone_table");
                break;
            case R.id.cards_show:
                Log.i(TAG, "onLongClick: cards_show");
                break;
            case R.id.phone_hand:
                Log.i(TAG, "onLongClick: phone_hand");
                break;
            case R.id.cover_view:
                Log.i(TAG, "onLongClick: cover_view");
                break;
            default:
                Log.i(TAG, "onLongClick: defalut");
                if(view.getTag() != null){
                    int pid = ((ViewGroup)view.getParent()).getId();
                    if(pid == R.id.cards_show){
                        Log.i(TAG, "onClick: defalut:cards_show");
                    }else if(pid == R.id.phone_hand){
                        Log.i(TAG, "onClick: defalut:phone_hand");
                        setDragAndDrop(view);
                    }else{
                        Log.i(TAG, "onClick: defalut:other");
                    }
                }
                break;
        }
        return true;
    }
    private void setDragAndDrop(View view){
        String tag = view.getTag().toString();

        ClipData dragData = new ClipData(tag,new String[]{ClipDescription.MIMETYPE_TEXT_PLAIN},
                new ClipData.Item(tag));
        CardDragShadowBuilder builder = new CardDragShadowBuilder(view);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            view.startDragAndDrop(dragData,builder,view,0);
        else
            view.startDrag(dragData,builder,view,0);
    }

    public PokerDb getPokerDb(){
        if(mPokerDb == null);
            mPokerDb = new PokerDb();
        return mPokerDb;
    }

    @Override
    protected void onDestroy() {
        exit();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        showAlertMessage(BAR_ACT_EXIT,getString(R.string.phone_alert_exit_title),
                getString(R.string.phone_alert_exit_body));
        //super.onBackPressed();
    }

    private void saveAllUserPokerRomScores(PokerRoom pokerRoom, PokerGame pokerGame){
        if(mPokerGame.getStatus() > PokerGame.GAME_RUN) {
            for (PokerUser pokerUser : pokerRoom.getUsers()) {
                if (pokerUser.getType() != PokerUser.ROBOT) {
                    long deduct= pokerGame.getGameUserMap().get(pokerUser.getUid()).getDeduct();
                    pokerUser.addDeduct(deduct);
                }
                //mMember.addScore(mPokerGame.getGameUserMap().get(getUid()).getScore());
            }
            saveMembersPoint(pokerGame);
        }
        savePokerRoomToFireStore();
        //mRoomListenerRegistration.remove();

        //saveMember(getUid(),mMember);

    }
    private void exit(){
        if(mPokerRoom.getCreateUid().equals(getUid())){
            mPokerRoom.setStatus(PokerRoom.ROOM_FINISH);
            mMember.setGameRoom(null);
            mRoomListenerRegistration.remove();
            saveAllUserPokerRomScores(mPokerRoom,mPokerGame);
            mPokerGame.setStatus(PokerGame.GAME_OVER);
        }else{
            if(mPokerGame.getStatus() > PokerGame.GAME_RUN) {
                PokerUser pokerUser = Tools.getPokerUser(mPokerRoom,getUid());
                pokerUser.setDeduct(mPokerGame.getGameUserMap().get(getUid()).getDeduct());
                mPokerRoom= Tools.setPokerUser(mPokerRoom,pokerUser);
                mMember.addPoint(pokerUser.getDeduct());
            } else{
                mPokerRoom.changeToRobot(getUid());
                mPokerGame.changeToRobot(getUid(),Robot.NAME,getString(R.string.robot_name),
                        getString(R.string.phone_user_changed));
            }
            mRoomListenerRegistration.remove();
            savePokerRoomToFireStore();
            mMember.setGameRoom(null);
            saveMember(getUid(),mMember);
        }
        mGameListenerRegistration.remove();
        savePokerGameToFireStore(mPokerGame);
    }
    private void savePokerRoomToFireStore(){
        getPokerDb().setPokerRoom(mRoomKey,mPokerRoom);
    }

    private void saveMember(String uid,Member member){
        getPokerDb().setMember(uid,member,null);
    }
    private void saveMembersPoint(PokerGame pokerGame){
        getPokerDb().setMembersPoint(pokerGame);
    }
    private void saveNewGame(String newGameKey,PokerGame newPokerGame){
        if(mPokerRoom.getCreateUid().equals(getUid()) && newGameKey != null){
            mGameListenerRegistration.remove();
            mPokerGame.setStatus(PokerGame.GAME_OVER);
            mPokerGame.setNextGameKey(newGameKey);
            savePokerGameToFireStore(mPokerGame);
            mPokerRoom.addGameCount();
            saveAllUserPokerRomScores(mPokerRoom,mPokerGame);
            intentPhone(newGameKey,newPokerGame);
        }else{
//            mMessage.setText("Wait " +
//                    Tools.getPokerUser(mPokerRoom,mPokerRoom.getCreateUid()).getUserName()+
//                    " start game!");
            Log.d(TAG, "saveNewGame: wait user join");
        }

    }

    private void intentPhone(String gameKey,PokerGame pokerGame){
        Intent intent = new Intent(PhoneMainActivity.this, InterstitialAdActivity.class);
        intent.putExtra(PokerActivity.ARG_ACTION_TYPE,mActType);
        intent.putExtra(PokerActivity.ARG_ROOM_KEY,mRoomKey);
        intent.putExtra(PokerActivity.ARG_ROOM,mPokerRoom);
        intent.putExtra(PokerActivity.ARG_GAME_KEY,gameKey);
        intent.putExtra(PokerActivity.ARG_GAME,pokerGame);
        startActivity(intent);
    }

    private Dialog getPb(){
        if(mPb == null)
            mPb = PokerDialog.getProgressDialog(this,
                    PokerDialog.progressDialogView(this,null,null));
        return mPb;
    }

    //-----------------------------------------------------------------------------------
    protected class CardDragListener implements View.OnDragListener{
        @Override
        public boolean onDrag(View v, DragEvent event) {
            //ImageView v = (ImageView) view;

            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    Log.d(TAG, "onDrag: ACTION_DRAG_STARTED ="+ v.getTag());
                    ((View) event.getLocalState()).setVisibility(View.INVISIBLE);
                    return true;
                case DragEvent.ACTION_DRAG_ENTERED:
                    Log.d(TAG, "onDrag: ACTION_DRAG_ENTERED ="+ v.getTag());
                    return true;
                case DragEvent.ACTION_DRAG_LOCATION:
                    Log.d(TAG, "onDrag: ACTION_DRAG_LOCATION ="+ v.getTag());
                    return true;
                case DragEvent.ACTION_DRAG_EXITED:
                    Log.d(TAG, "onDrag: ACTION_DRAG_EXITED ="+ v.getTag());
                    return true;
                case DragEvent.ACTION_DROP:
                    Log.i(TAG, "onDrag: ACTION_DROP=");
                    if(mPokerGame.getOrder().get(0).equals(getUid()) &&
                            mPokerGame.getActions().get(0) == Poker.ACT_THROW) {
                    }
                    return true;

                case DragEvent.ACTION_DRAG_ENDED:
                    Log.d(TAG, "onDrag: ACTION_DRAG_ENDED");
                    if (event.getResult()) {
                        Log.i(TAG, "onDrag: ACTION_DRAG_ENDED=" + "The drop was handled.");
                        if(mPokerGame.getOrder().get(0).equals(getUid()) &&
                                mPokerGame.getActions().get(0) == Poker.ACT_THROW) {
                            View cardView = (View) event.getLocalState();
                            String card= cardView.getTag().toString();
                            cardView.setVisibility(View.INVISIBLE);
                            ((PhoneTableLayout)v).getCoverImage()
                                    .setImageResource(PokerDrawable.getDrawableId(v.getContext(), card));
                            savePokerGameToFireStore(
                                    Tools.handleUserAction(getUid(),mPokerGame,card,mCoverMessage));
                        }
                        else
                            ((View) event.getLocalState()).setVisibility(View.VISIBLE);
                    } else {
                        Log.i(TAG, "onDrag: ACTION_DRAG_ENDED=" + "The drop didn't work.");
                        ((View) event.getLocalState()).setVisibility(View.VISIBLE);
                    }
                    return true;
                default:
                    Log.e("DragDrop Example", "Unknown action type received by OnDragListener.");

                    break;
            }

            return false;
        }
    }
    //-----------------------------------------------------------------------------------
    private static class CardDragShadowBuilder extends View.DragShadowBuilder{
        private Drawable nShadow;
        public CardDragShadowBuilder(View view) {
            super(view);
            nShadow = ((ImageView)view).getDrawable().getConstantState().newDrawable().mutate();
        }

        @Override
        public void onProvideShadowMetrics(Point outShadowSize, Point outShadowTouchPoint) {
            int width =(int)((float)getView().getWidth()*0.75f);
            int height = (int)((float)getView().getHeight()*0.75f);
            nShadow.setBounds(0,0, width, height);
            outShadowSize.set(width, height);
            outShadowTouchPoint.set(width/2, height/2);
        }

        @Override
        public void onDrawShadow(Canvas canvas) {
            nShadow.draw(canvas);
        }
    }
}
