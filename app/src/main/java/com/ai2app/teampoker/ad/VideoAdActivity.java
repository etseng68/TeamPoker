package com.ai2app.teampoker.ad;

import android.content.Intent;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.ai2app.teampoker.R;
import com.ai2app.teampoker.comm.AdTools;
import com.ai2app.teampoker.comm.PokerActivity;
import com.ai2app.teampoker.phone.UserChoiceActivity;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;


//public class VideoAdActivity extends AppCompatActivity implements RewardedVideoAdListener {
public class VideoAdActivity extends AppCompatActivity {
    private static final String TAG = "VideoAdActivity";
    private static final int AD_RES= R.string.ad_unit_id_phone_game_start;
    //private RewardedVideoAd mRewardedVideoAd;
    private RewardedAd mRewardedAd;
    private int mActType=-1;
    private String mUserName,mUserLogo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setArgData(getIntent());
        //MobileAds.initialize(this, getString(R.string.ad_unit_id));
        MobileAds.initialize(this, initializationStatus -> {

        });
       /* mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this);
        mRewardedVideoAd.setRewardedVideoAdListener(this);
        AdRequest adRequest = new AdRequest.Builder().build();
        mRewardedVideoAd.loadAd(getString(AD_RES),adRequest);*/
        AdRequest adRequest = new AdRequest.Builder().build();
        RewardedAd.load(this, getString(AD_RES), adRequest,
                new RewardedAdLoadCallback() {
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        super.onAdFailedToLoad(loadAdError);
                        Log.d(TAG, loadAdError.getMessage());
                        mRewardedAd = null;
                    }

                    @Override
                    public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                        super.onAdLoaded(rewardedAd);
                        mRewardedAd = rewardedAd;
                        showAd();
                    }
                });
        mRewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdClicked() {
                super.onAdClicked();
                Log.d(TAG, "Ad clicked.");
            }

            @Override
            public void onAdDismissedFullScreenContent() {
                super.onAdDismissedFullScreenContent();
                Log.d(TAG, "Ad was shown.");
                mRewardedAd = null;
            }

            @Override
            public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                super.onAdFailedToShowFullScreenContent(adError);
                Log.d(TAG, "Ad failed to show.");
            }

            @Override
            public void onAdImpression() {
                super.onAdImpression();
            }

            @Override
            public void onAdShowedFullScreenContent() {
                super.onAdShowedFullScreenContent();
            }
        });
    }
    private void setArgData(Intent intent){
        if(intent != null){
            mActType = intent.getIntExtra(PokerActivity.ARG_ACTION_TYPE,-1);
            mUserName = intent.getStringExtra(PokerActivity.ARG_USER_NAME);
            mUserLogo = intent.getStringExtra(PokerActivity.ARG_USER_LOGO);
        }
    }

    @Override
    protected void onResume() {
        //mRewardedVideoAd.resume(this);
        super.onResume();
    }
    @Override
    public void onPause() {
        //mRewardedVideoAd.pause(this);
        super.onPause();
    }

    @Override
    public void onDestroy() {
        //mRewardedVideoAd.destroy(this);
        super.onDestroy();
    }
/*    @Override
    public void onRewardedVideoAdLoaded() {
        Log.d(TAG, "onRewardedVideoAdLoaded: ");
        if(mRewardedVideoAd.isLoaded())
            mRewardedVideoAd.show();
    }

    @Override
    public void onRewardedVideoAdOpened() {
        Log.d(TAG, "onRewardedVideoAdOpened: ");
    }

    @Override
    public void onRewardedVideoStarted() {
        Log.d(TAG, "onRewardedVideoStarted: ");
    }

    @Override
    public void onRewardedVideoAdClosed() {
        Log.d(TAG, "onRewardedVideoAdClosed: ");
        intentPhone();
    }

    @Override
    public void onRewarded(RewardItem rewardItem) {
        Log.d(TAG, "onRewarded: ");
        AdTools.saveReward(getApplicationContext(),AD_RES,null,null);
    }

    @Override
    public void onRewardedVideoAdLeftApplication() {
        Log.d(TAG, "onRewardedVideoAdLeftApplication: ");
    }

    @Override
    public void onRewardedVideoAdFailedToLoad(int i) {
        Log.d(TAG, "onRewardedVideoAdFailedToLoad: ");
        intentPhone();
    }*/
    private void showAd(){
        if(mRewardedAd !=  null){
            mRewardedAd.show(this, rewardItem -> {
                Log.d(TAG, "The user earned the reward.");
                int rewardAmount = rewardItem.getAmount();
                String rewardType = rewardItem.getType();
                intentPhone(rewardAmount,rewardType);
            });
        }else{
            Log.d(TAG, "The rewarded ad wasn't ready yet.");
        }
    }
    private void intentPhone(int rewardAmount,String rewardType){
        Intent intent = new Intent(this, PokerActivity.class);
        intent.putExtra(PokerActivity.ARG_ACTION_TYPE,mActType);
        intent.putExtra(PokerActivity.ARG_USER_NAME,mUserName);
        intent.putExtra(PokerActivity.ARG_USER_LOGO,mUserLogo);
        startActivity(intent);
        finish();
    }
}
