package com.ai2app.teampoker.phone;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.ai2app.teampoker.R;
import com.ai2app.teampoker.ad.VideoAdActivity;
import com.ai2app.teampoker.comm.AdTools;
import com.ai2app.teampoker.comm.PokerActivity;
import com.ai2app.teampoker.comm.PokerDrawable;
import com.ai2app.teampoker.db.PokerDb;
import com.ai2app.teampoker.db.PokerUser;
import com.ai2app.teampoker.help.HelpFragment;
import com.ai2app.teampoker.help.AboutFragment;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class UserChoiceActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = "UserChoiceActivity";
    private static final int USER_COUNT = 20;
    private ArrayList<Fragment> mFragments;
    private ViewPager mViewPager;
    private Button mJoinBtn,mCreateBtn;
    private String mUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_user_choice);
        initToolbar();
        initAd();
        initView();
        initFragment();
    }
    private void initToolbar(){
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }
    private void initView(){
        mViewPager = findViewById(R.id.user_viewpager);
        mJoinBtn = findViewById(R.id.joinBtn);
        mJoinBtn.setOnClickListener(this);
        mCreateBtn = findViewById(R.id.createBtn);
        mCreateBtn.setOnClickListener(this);
    }
    private void initAd(){
        //MobileAds.initialize(this, getString(R.string.ad_unit_id));
        MobileAds.initialize(this, initializationStatus -> { });
        AdView adViewTop = findViewById(R.id.adView_top);
        adViewTop.setAdListener(new AdListener(){
            @Override
            public void onAdClosed() {
                super.onAdClosed();
                saveAdScore(R.string.ad_unit_id_user_choice_top);
            }
        });
        AdView adViewBottom = findViewById(R.id.adView_bottom);
        adViewBottom.setAdListener(new AdListener(){
            @Override
            public void onAdClosed() {
                super.onAdClosed();
                saveAdScore(R.string.ad_unit_id_user_choice_bottom);
            }
        });
        adViewTop.loadAd(new AdRequest.Builder().build());
        adViewBottom.loadAd(new AdRequest.Builder().build());
    }
    private void saveAdScore(int adRes){
        //AdTools.saveReward(this,adRes,null,null);
    }
    private void initFragment(){
        mFragments = new ArrayList<>();
        String[] names = getResources().getStringArray(R.array.user_name);
        for(int i=0;i<USER_COUNT;i++){
            mFragments.add(UserFragment.newInstance("user"+(i+1),names[i]));
        }
        mViewPager.setAdapter(new UserJoinAdapter(getSupportFragmentManager(),mFragments));
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_user_choice, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.user_choice_bar_about:
                AboutFragment aboutFragment = AboutFragment.newInstance();
                aboutFragment.setStyle(DialogFragment.STYLE_NORMAL,
                        android.R.style.Theme_Holo_Light_Dialog);
                aboutFragment.show(getSupportFragmentManager(), "about");
                return true;
            case android.R.id.home:
                onBackPressed();
                finish();
                return true;
            case R.id.user_choice_bar_help:
                HelpFragment helpFragment = HelpFragment.newInstance(HelpFragment.TYPE_USER_CHOICE);
                helpFragment.setStyle(DialogFragment.STYLE_NORMAL,
                        android.R.style.Theme_Holo_NoActionBar);
                helpFragment.show(getSupportFragmentManager(),"help");
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(View view) {
        int vid = view.getId();
        int actType =PokerUser.PLAYER;
        switch (vid){
            case R.id.createBtn:
                actType = PokerUser.CREATOR;
                break;
            case R.id.joinBtn:
                actType = PokerUser.PLAYER;
                break;
        }
        UserFragment fragment = (UserFragment)((UserJoinAdapter)mViewPager.getAdapter())
                .getItem(mViewPager.getCurrentItem());
        intentPhone(actType,fragment.getUserName(), fragment.getLogo());
    }

    private void intentPhone(int act,String userName,String userLogo){
        Intent intent = new Intent(this, VideoAdActivity.class);
        intent.putExtra(PokerActivity.ARG_ACTION_TYPE,act);
        intent.putExtra(PokerActivity.ARG_USER_NAME,userName);
        intent.putExtra(PokerActivity.ARG_USER_LOGO,userLogo);
        startActivity(intent);
        finish();
    }

    private String getUid(){
        if(mUid == null)
            mUid = FirebaseAuth.getInstance().getUid();
        return mUid;
    }
    //---------------------------------------------------------------------------------
    public static class UserJoinAdapter extends FragmentPagerAdapter {
        private static final String TAG = "UserJoinAdapter";
        private ArrayList<Fragment> nFragmentses;
        public UserJoinAdapter(FragmentManager fm, ArrayList<Fragment> fragments) {
            super(fm);
            this.nFragmentses = fragments;
        }

        @Override
        public Fragment getItem(int position) {
            return nFragmentses.get(position);
        }

        @Override
        public int getCount() {
            return nFragmentses.size();
        }
    }
    //----------------------------------------------------------------------------------
    public static class UserFragment extends Fragment{
        private static final String TAG = "UserFragment";
        private String nUserLogo;
        private String nUserName;
        private ImageView nUserImage;
        private TextView nUserNameText;
        public static UserFragment newInstance(String logo,String name){
            UserFragment fragment = new UserFragment();
            Bundle args = new Bundle();
            args.putString(PokerActivity.ARG_USER_LOGO,logo);
            args.putString(PokerActivity.ARG_USER_NAME,name);
            fragment.setArguments(args);
            return fragment;
        }
        public UserFragment() {
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            nUserLogo = getArguments().getString(PokerActivity.ARG_USER_LOGO);
            nUserName = getArguments().getString(PokerActivity.ARG_USER_NAME);
            Log.i(TAG, "onCreate: ");
        }

        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_user,container,false);
        }

        @Override
        public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            nUserImage = view.findViewById(R.id.user_image);
            nUserImage.setImageResource(PokerDrawable.getDrawableId(getActivity(),nUserLogo));
            nUserNameText = view.findViewById(R.id.user_name);
            nUserNameText.setText(nUserName);
        }
        public String getUserName(){
            return nUserName;
        }
        public String getLogo(){
            return nUserLogo;
        }
    }
}
