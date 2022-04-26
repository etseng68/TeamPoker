package com.ai2app.teampoker.ad;

import android.app.Dialog;
import android.content.Intent;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.ai2app.teampoker.R;
import com.ai2app.teampoker.comm.AdTools;
import com.ai2app.teampoker.comm.PokerActivity;
import com.ai2app.teampoker.comm.PokerDialog;
import com.ai2app.teampoker.db.Member;
import com.ai2app.teampoker.db.PokerGame;
import com.ai2app.teampoker.db.PokerRoom;
import com.ai2app.teampoker.db.PokerUser;
import com.ai2app.teampoker.phone.PhoneMainActivity;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;

import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.firebase.auth.FirebaseAuth;

public class InterstitialAdActivity extends AppCompatActivity {
    private static final String TAG = "VideoAdActivity";
    private static final int AD_RES= R.string.ad_unit_id_phone_game_new;
    private InterstitialAd mInterstitialAd;
    private Dialog mPb;
    private int mActType;
    private String mRoomKey,mGameKey;
    private PokerGame mPokerGame;
    private PokerRoom mPokerRoom;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setArgData(getIntent());
        //MobileAds.initialize(this, getString(R.string.ad_unit_id));
        MobileAds.initialize(this, initializationStatus -> {

        });
/*        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(AD_RES));
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        mInterstitialAd.setAdListener(adListener);*/
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(this, getString(AD_RES), adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        super.onAdFailedToLoad(loadAdError);
                        Log.i(TAG, loadAdError.getMessage());
                        mInterstitialAd = null;
                    }

                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        super.onAdLoaded(interstitialAd);
                        mInterstitialAd = interstitialAd;
                    }
                });
        mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdClicked() {
                super.onAdClicked();
                Log.d("TAG", "The ad clicked.");
            }

            @Override
            public void onAdDismissedFullScreenContent() {
                super.onAdDismissedFullScreenContent();
                Log.d("TAG", "The ad was dismissed.");
                intentPhone();
            }

            @Override
            public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                super.onAdFailedToShowFullScreenContent(adError);
                Log.d("TAG", "The ad failed to show.");
            }

            @Override
            public void onAdImpression() {
                super.onAdImpression();
                Log.d("TAG", "The ad impression.");
            }

            @Override
            public void onAdShowedFullScreenContent() {
                super.onAdShowedFullScreenContent();
                Log.d("TAG", "The ad was shown.");
                getPb().dismiss();
                if (mInterstitialAd != null) {
                    mInterstitialAd.show(InterstitialAdActivity.this);
                } else {
                    Log.d("TAG", "The interstitial ad wasn't ready yet.");
                }
            }
        });
    }
    private void setArgData(Intent intent) {
        if (intent != null) {
            mActType = intent.getIntExtra(PokerActivity.ARG_ACTION_TYPE, PokerUser.PLAYER);
            mRoomKey = intent.getStringExtra(PokerActivity.ARG_ROOM_KEY);
            mPokerRoom = (PokerRoom) intent.getSerializableExtra(PokerActivity.ARG_ROOM);
            mGameKey = intent.getStringExtra(PokerActivity.ARG_GAME_KEY);
            mPokerGame = (PokerGame) intent.getSerializableExtra(PokerActivity.ARG_GAME);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getPb().show();
    }

    /*private AdListener adListener = new AdListener(){
        @Override
        public void onAdClosed() {
            super.onAdClosed();
            Log.d(TAG, "onAdClosed: ");
            intentPhone();
            //AdTools.saveReward(getApplicationContext(),AD_RES,mRoomKey,mGameKey);
        }

        @Override
        public void onAdFailedToLoad(int i) {
            super.onAdFailedToLoad(i);
            Log.d(TAG, "onAdFailedToLoad: ");
            getPb().dismiss();
            intentPhone();
        }

        @Override
        public void onAdLeftApplication() {
            super.onAdLeftApplication();
            Log.d(TAG, "onAdLeftApplication: ");
        }

        @Override
        public void onAdOpened() {
            super.onAdOpened();
            Log.d(TAG, "onAdOpened: ");
        }

        @Override
        public void onAdLoaded() {
            Log.d(TAG, "onAdLoaded: ");
            getPb().dismiss();
            super.onAdLoaded();
            mInterstitialAd.show();
        }

    };*/

    private void intentPhone(){
        Intent intent = new Intent(this, PhoneMainActivity.class);
        intent.putExtra(PokerActivity.ARG_ACTION_TYPE,mActType);
        intent.putExtra(PokerActivity.ARG_ROOM_KEY,mRoomKey);
        intent.putExtra(PokerActivity.ARG_ROOM,mPokerRoom);
        intent.putExtra(PokerActivity.ARG_GAME_KEY,mGameKey);
        intent.putExtra(PokerActivity.ARG_GAME,mPokerGame);
        startActivity(intent);
        finish();
    }
    private Dialog getPb(){
        if(mPb == null)
            mPb = PokerDialog.getProgressDialog(this,
                    PokerDialog.progressDialogView(this,null,null));
        return mPb;
    }
}
