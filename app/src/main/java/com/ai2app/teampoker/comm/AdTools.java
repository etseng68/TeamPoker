package com.ai2app.teampoker.comm;


import android.content.Context;
import android.widget.Toast;

import com.ai2app.teampoker.R;
import com.ai2app.teampoker.db.PokerAdView;
import com.ai2app.teampoker.db.PokerDb;
import com.google.firebase.auth.FirebaseAuth;

public class AdTools {

    public static int getAdScore(int adRes){
        int baseScore=0;
        switch (adRes){
            case R.string.ad_unit_id_main_banner_big:
                baseScore = PokerAdView.AD_BIG;
                break;
            case R.string.ad_unit_id_user_choice_top:
                baseScore = PokerAdView.AD_SMALL;
                break;
            case R.string.ad_unit_id_user_choice_bottom:
                baseScore = PokerAdView.AD_SMALL;
                break;
            case R.string.ad_unit_id_phone_game_new:
                baseScore = PokerAdView.AD_FULL;
                break;
            case R.string.ad_unit_id_phone_game_start:
                baseScore = PokerAdView.AD_VEDIO;
                break;
        }
        return baseScore;
    }
    public static void saveReward(Context context,int adRes,String roomKey,String gameKey){
        PokerDb pokerDb = new PokerDb();
        String adUid=context.getString(adRes).split("/")[1];
        int ad_score=AdTools.getAdScore(adRes);
        pokerDb.addMemberAdViewScore(adUid, ad_score,roomKey,gameKey);
        Toast.makeText(context,
                context.getString(R.string.ad_reward)+ad_score+
                        context.getString(R.string.ad_point)+" !",
                Toast.LENGTH_SHORT).show();

    }
}
