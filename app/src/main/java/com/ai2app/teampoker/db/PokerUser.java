package com.ai2app.teampoker.db;


import com.ai2app.teampoker.comm.Robot;

import java.io.Serializable;

public class PokerUser implements Serializable {
    public static final int CREATOR=0,PLAYER=1;
    public static final int ROBOT=0,USER=1;
    private int type; //0:robot 1:user
    private String uid;
    private String userName;
    private String logo;
    private String replaceRobot;
    private long deduct;

    public PokerUser() {
    }

    public PokerUser(String uid,int type, String userName, String logo,String replaceRobot) {
        setUserData(uid,type,userName,logo);
        this.replaceRobot = replaceRobot;
    }

    public void setUserData(String uid,int type, String userName, String logo){
        this.uid =uid;
        this.type = type;
        this.userName = userName;
        this.logo = logo;
    }
    public void setRobotReplace(int type, String userName, String logo,String replaceRobot){
        this.type = type;
        this.userName = userName;
        this.logo = logo;
        this.replaceRobot =replaceRobot;
    }
    public void setUserChangeToRobot(String userName){
        setUserData(userName,ROBOT,userName, Robot.LOGO_NAME);
        this.replaceRobot =null;
    }
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getReplaceRobot() {
        return replaceRobot;
    }

    public void setReplaceRobot(String replaceRobot) {
        this.replaceRobot = replaceRobot;
    }

    public long getDeduct() {
        return deduct;
    }

    public void setDeduct(long deduct) {
        this.deduct = deduct;
    }
    public void addDeduct(long deduct) {
        this.deduct = this.deduct +deduct;
    }
//    public long getScore() {
//        return deduct;
//    }
//
//    public void setScore(long score) {
//        this.score = score;
//    }
//    public void addScore(long score) {
//        this.score = this.score+score;
//    }
}
