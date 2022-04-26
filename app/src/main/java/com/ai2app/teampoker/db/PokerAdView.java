package com.ai2app.teampoker.db;


import com.google.firebase.firestore.ServerTimestamp;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PokerAdView {
    public static final int AD_SMALL=1,AD_BIG=2,AD_FULL=3,AD_VEDIO=10;
    private long scoreSum;
    private Map<String,AD> adMap;

    public PokerAdView() {
    }

    public PokerAdView(long scoreSum, String key,String roomKey,String gameKey) {
        addAdMap(scoreSum,key,roomKey,gameKey);
    }

    public long getScoreSum() {
        return scoreSum;
    }

    public void setScoreSum(long scoreSum) {
        this.scoreSum = scoreSum;
    }

    public Map<String, AD> getAdMap() {
        return adMap;
    }

    public void setAdMap(Map<String, AD> adMap) {
        this.adMap = adMap;
    }
//    public void addAdMap(long scoreSum,String key){
//        this.scoreSum=scoreSum;
//        if(this.adMap == null)
//            this.adMap = new HashMap<>();
//        this.adMap.put(key,new AD());
//    }
    public void addAdMap(long scoreSum,String key,String roomKey,String gameKey){
        this.scoreSum=scoreSum;
        if(this.adMap == null)
            this.adMap = new HashMap<>();
        this.adMap.put(key,new AD(roomKey,gameKey));
    }

    //-----------------------------------------------------------------------
    public static class AD {
        @ServerTimestamp
        private Date time;
        private String roomKey;
        private String gameKey;
        public AD() {
        }

        public AD(String roomKey, String gameKey) {
            this.roomKey = roomKey;
            this.gameKey = gameKey;
        }

        public Date getTime() {
            return time;
        }

        public void setTime(Date time) {
            this.time = time;
        }

        public String getRoomKey() {
            return roomKey;
        }

        public void setRoomKey(String roomKey) {
            this.roomKey = roomKey;
        }

        public String getGameKey() {
            return gameKey;
        }

        public void setGameKey(String gameKey) {
            this.gameKey = gameKey;
        }
    }
}
