package com.ai2app.teampoker.db;

import com.ai2app.teampoker.comm.Poker;
import com.ai2app.teampoker.comm.Robot;
import com.google.firebase.firestore.ServerTimestamp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PokerRoom implements Serializable{
    public static final int ROOM_USE=0,ROOM_FINISH=1;
    private String createUid;
    private List<PokerUser> users;
    private Map<String,Boolean> joins;
    @ServerTimestamp
    private Date create;
    @ServerTimestamp
    private Date end;
    private int status = ROOM_USE;
    private String roomInKey;
    private int gameCount;

    public PokerRoom() {
    }
    public PokerRoom(String createUid,String userName,String userImage,String robotName) {
        this.createUid =createUid;
        this.users = new ArrayList<>();
        this.joins = new HashMap<>();
        joins.put(createUid,true);
        users.add(new PokerUser(createUid,PokerUser.USER,userName,userImage,Robot.NAME+"0"));
        this.gameCount=1;
        addRobotUser(robotName);
    }

    private void addRobotUser(String rName){
        for(int i = 1; i< Poker.mixUser; i++){
            String uid = Robot.NAME+i;
            String robotName = rName +i;
            users.add(new PokerUser(uid,PokerUser.ROBOT,robotName,Robot.LOGO_NAME,null));
        }
    }

    public void replaceRobot(String uid,String userName,String userLogo){
        for(int i=0;i<users.size();i++){
            PokerUser user = users.get(i);
            if(user.getType() == PokerUser.ROBOT) {
                users.set(i,new PokerUser(uid,PokerUser.USER,userName,userLogo,user.getUid()));
                joins.put(uid,true);
                joins.remove(user.getUid());
                break;
            }
        }
    }
    public void changeToRobot(String uid){
        for(int i=0;i<users.size();i++){
            PokerUser user = users.get(i);
            if(user.getUid().equals(uid)) {
                joins.put(uid,false);
                user.setUserChangeToRobot(user.getReplaceRobot());
                break;
            }
        }
    }

    public String getCreateUid() {
        return createUid;
    }

    public void setCreateUid(String createUid) {
        this.createUid = createUid;
    }

    public List<PokerUser> getUsers() {
        return users;
    }

    public void setUsers(List<PokerUser> users) {
        this.users = users;
    }

    public Map<String, Boolean> getJoins() {
        return joins;
    }

    public void setJoins(Map<String, Boolean> joins) {
        this.joins = joins;
    }


    public Date getCreate() {
        return create;
    }

    public void setCreate(Date create) {
        this.create = create;
    }



    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getRoomInKey() {
        return roomInKey;
    }

    public void setRoomInKey(String roomInKey) {
        this.roomInKey = roomInKey;
    }

    public Date getEnd() {
        end = null;
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }

    public int getGameCount() {
        return gameCount;
    }

    public void setGameCount(int gameCount) {
        this.gameCount = gameCount;
    }

    public void addGameCount(){
        this.gameCount = this.gameCount+1;
    }
}

