package com.ai2app.teampoker.db;


import com.ai2app.teampoker.comm.Poker;
import com.google.firebase.firestore.ServerTimestamp;

import java.io.Serializable;
import java.util.Date;

public class Member implements Serializable{
    public String name;
    @ServerTimestamp
    public Date create;
    @ServerTimestamp
    public Date modify;
    public String gameRoom;
    public long point = 0;
    public boolean showPokerHelp =false;

    public Member() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getCreate() {
        return create;
    }

    public void setCreate(Date create) {
        this.create = create;
    }

    public Date getModify() {
        modify = null;
        return modify;
    }
    public void setModify(Date modify) {
        this.modify = modify;
    }

    public String getGameRoom() {
        return gameRoom;
    }

    public void setGameRoom(String gameRoom) {
        this.gameRoom = gameRoom;
    }

    public long getPoint() {
        return point;
    }

    public void setPoint(long point) {
        this.point = point;
    }
    public void addPoint(long point){
        this.point = this.point +point;
    }
//    public long getScore() {
//        return score;
//    }
//
//    public void setScore(long score) {
//        this.score = score;
//    }
//
//    public void addScore(long score){
//
//        this.score = this.score+score;
//    }

    public boolean isShowPokerHelp() {
        return showPokerHelp;
    }

    public void setShowPokerHelp(boolean showPokerHelp) {
        this.showPokerHelp = showPokerHelp;
    }
}
