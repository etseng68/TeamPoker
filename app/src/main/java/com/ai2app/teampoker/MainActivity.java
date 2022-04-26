package com.ai2app.teampoker;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;

import com.ai2app.teampoker.comm.PokerActivity;
import com.ai2app.teampoker.comm.PokerDialog;
import com.ai2app.teampoker.db.Member;
import com.ai2app.teampoker.db.PokerDb;
import com.ai2app.teampoker.help.AboutFragment;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private final static String TAG = "MainActivity";
    private static final int RC_SIGN_IN = 100;
    private Button mPhoneBtn;
    private PokerDb mPokerDb;
    private String mUid;
    private Dialog mPb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);
        initToolbar();
        initView();
        initAd();
        checkSignIn();
    }
    private void initToolbar(){
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }
    private void initView(){
        mPhoneBtn=findViewById(R.id.main_phone_btn);
        mPhoneBtn.setOnClickListener(this);
        mPhoneBtn.setEnabled(false);
    }
    private void initAd(){
        //MobileAds.initialize(this, getString(R.string.ad_unit_id));
        MobileAds.initialize(this, initializationStatus -> { });
        AdView adView = findViewById(R.id.main_ad_view_big);
        adView.loadAd(new AdRequest.Builder().build());
        adView.setAdListener(new AdListener(){
            @Override
            public void onAdClosed() {
                super.onAdClosed();
//                AdTools.saveReward(getApplicationContext(), R.string.ad_unit_id_main_banner_big,
//                        null,null);
            }

        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.main_bar_sign_out:
                signOut();
                return true;
            case R.id.main_bar_about:
                AboutFragment aboutFragment = AboutFragment.newInstance();
                aboutFragment.setStyle(DialogFragment.STYLE_NORMAL,
                        android.R.style.Theme_Holo_Light_Dialog);
                aboutFragment.show(getSupportFragmentManager(),"about");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(View view) {
        startActivity(new Intent(MainActivity.this, PokerActivity.class));
    }


    private void checkSignIn(){
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if(auth.getCurrentUser() == null) {
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(
                                    //Arrays.asList(new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build()))
                                    Arrays.asList(new AuthUI.IdpConfig.GoogleBuilder().build()))
                            .build(),
                    RC_SIGN_IN);
        }else {
            getPb().show();
            getPokerDb().getMember(getUid(), new PokerDb.OnMemberListener() {
                @Override
                public void onComplete(String uid, Member member) {
                    getPb().dismiss();
                    if(member != null) {
                        setButtonEnable();
                    }else {
                        Toast.makeText(getApplicationContext(), "member create error"
                                , Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }
    }
    private void signOut(){
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {
                            startActivity(new Intent(MainActivity.this,
                                    MainActivity.class));
                        }
                        finish();
                    }
                });
    }
    private void setButtonEnable(){
        mPhoneBtn.setEnabled(true);
    }
    private String getUid(){
        if(mUid == null)
            mUid = FirebaseAuth.getInstance().getUid();
        return mUid;
    }
    private PokerDb getPokerDb(){
        if(mPokerDb == null)
            mPokerDb = new PokerDb();
        return mPokerDb;
    }
    private Dialog getPb(){
        if(mPb == null)
            mPb = PokerDialog.getProgressDialog(this,
                    PokerDialog.progressDialogView(this,null,null));
        return mPb;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {// Successfully signed in
                Log.i(TAG,"signed in successfully");
                checkSignIn();
            } else {// Sign in failed
                Log.i(TAG,response.getError().getErrorCode() +":"+ response.getError().getMessage());
/*                if (response == null) { // User pressed back button
                    Log.i(TAG,"pressed back button");
                    return;
                }
                if (response.getErrorCode() == ErrorCodes.NO_NETWORK) {
                    showMessage(getString(R.string.no_internet_connection));
                    Log.i(TAG,"no_internet_connection");
                    return;
                }
                if (response.getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                    Log.i(TAG,"unknown_error");
                    return;
                }*/
            }
            Log.i(TAG,"unknown_sign_in_response");
        }
    }
    private void showMessage(String message){
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show();
    }

}
