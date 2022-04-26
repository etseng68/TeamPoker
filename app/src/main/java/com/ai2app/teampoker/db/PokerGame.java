package com.ai2app.teampoker.db;


import com.ai2app.teampoker.R;
import com.ai2app.teampoker.comm.Poker;
import com.ai2app.teampoker.comm.Robot;
import com.ai2app.teampoker.comm.Tools;
import com.google.firebase.firestore.ServerTimestamp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PokerGame implements Serializable{

    public static final int GAME_WAIT=0,GAME_RUN=1,GAME_FINISH=2,GAME_OVER=3;
    private Table table;
    private Map<String,GameUser> gameUserMap;
    private List<String> position =new ArrayList<>();
    private List<String> ranking =new ArrayList<>();
    private List<String> order = new ArrayList<>();
    private List<Integer> actions =new ArrayList<>();
    private int status = GAME_WAIT;

    @ServerTimestamp
    private Date create;
    @ServerTimestamp
    private Date end;
    private String nextGameKey;

    public PokerGame() {}

    public PokerGame(PokerRoom pokerRoom, String userMessage,String coverMessage){
        this.table = new Table();
        for(PokerUser pokerUser:pokerRoom.getUsers()){
            addGameUser(pokerUser.getUid(),pokerUser,userMessage);
            if(pokerUser.getUid() == pokerRoom.getCreateUid())
                this.table.setCoverAndMessage(pokerUser.getLogo(),coverMessage);
        }
    }
    public void initPokerGame(String coverMessage){
        initOrder();
        initUserAction();
        getTable().init(Poker.getRandomCards());
        getTable().setCoverAndMessage(
                getGameUserMap().get(getOrder().get(0)).getLogo(),coverMessage);
        initHand();
        initTable();
        initMessage();
        this.status = GAME_RUN;
    }
    public void initUserAction(){
        if(this.actions.size()>0)
            this.actions.clear();
        for(int act: Tools.acts)
            this.actions.add(act);
    }
    private void initOrder(){
        int count = (Poker.card - Poker.hand * Poker.mixUser - Poker.fistShow)/Poker.mixUser;
        if(this.order.size() >0)
            this.order.clear();
        for(int i =0; i<count;i++){
            for(String key:position)
                order.add(key);
        }
    }

    private void initTable(){
        for(int i =0;i<Poker.fistShow;i++) {
            String card = shiftTableHideCard();
            if(card != null)
                this.table.shows.add(card);
        }
    }
    private void initHand(){
        for(int i=0;i<Poker.hand;i++){
            for(GameUser user:gameUserMap.values()){
                String card = shiftTableHideCard();
                if(card != null)
                user.getHands().add(card);

            }
        }
    }
    private void initMessage(){
        for(GameUser gameUser:getGameUserMap().values()){
            gameUser.setMessage(String.valueOf(gameUser.getScore()));
        }
    }
    private String shiftTableHideCard(){
        String card = null;
        if(getTable().getHides().size()>0) {
            card = getTable().getHides().get(0);
            getTable().getHides().remove(0);
        }
        return card;
    }
    public void addGather(String uid,String cardPair){
        GameUser gameUser = this.getGameUserMap().get(uid);
        gameUser.getGathers().add(cardPair);
        gameUser.addScore(Poker.getCardPairScore(cardPair));
        gameUser.setMessage(String.valueOf(gameUser.getScore()));
        reSetRanking();
    }
    private void reSetRanking(){
        this.ranking = Tools.reSetRanking(this.getGameUserMap());
    }

    public void addGameUser(String uid,PokerUser pokerUser,String message){
        if(gameUserMap == null)
            gameUserMap = new HashMap<>();
        gameUserMap.put(uid,new GameUser(pokerUser,message));
        positionUser(uid);
    }
    public void changeToRobot(String uid,String robotUidPreWord,String robotLocalName,String changeMessage){
        GameUser gameUser = gameUserMap.remove(uid);
        String robotUid = gameUser.getReplaceRobot();
        String robotIndex = robotUid.replace(robotUidPreWord,"").trim();
        gameUserMap.put(robotUid,getChangeRobotFromUser(gameUser,
                robotLocalName+robotIndex, changeMessage));
        replaceUserData(uid,robotUid);
    }
    private GameUser getChangeRobotFromUser(GameUser gameUser,String robotLocalNameAddIndex,String changeMessage){
        String robotId =gameUser.getReplaceRobot();
        gameUser.setLogo(Robot.LOGO_NAME);
        gameUser.setMessage(gameUser.getUserName()+changeMessage+robotLocalNameAddIndex);
        gameUser.setUserName(robotLocalNameAddIndex);
        gameUser.setReplaceRobot(null);
        gameUser.setType(PokerUser.ROBOT);
        gameUser.setUid(robotId);
        return gameUser;
    }
    public void replaceGameUser(String uid,PokerUser pokerUser,String message){
        gameUserMap.remove(pokerUser.getReplaceRobot());
        gameUserMap.put(uid,new GameUser(pokerUser,message));
        replaceUserData(pokerUser.getReplaceRobot(),uid);
    }
    private void replaceUserData(String originalKey,String newKey){
        if(getPosition() != null)
            Collections.replaceAll(getPosition(),originalKey,newKey);
        if(getOrder() != null)
            Collections.replaceAll(getOrder(),originalKey,newKey);
        if(getRanking() != null)
            Collections.replaceAll(getRanking(),originalKey,newKey);
    }
    private void positionUser(String userKey){
        position.add(userKey);
    }


    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }


    public Map<String, GameUser> getGameUserMap() {
        return gameUserMap;
    }

    public void setGameUserMap(Map<String, GameUser> gameUserMap) {
        this.gameUserMap = gameUserMap;
    }

    public List<String> getPosition() {
        return position;
    }

    public void setPosition(List<String> position) {
        this.position = position;
    }

    public List<String> getRanking() {
        return ranking;
    }

    public void setRanking(List<String> ranking) {
        this.ranking = ranking;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Date getCreate() {
        return create;
    }

    public void setCreate(Date create) {
        this.create = create;
    }

    public Date getEnd() {
        end = null;
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }

    public List<String> getOrder() {
        return order;
    }

    public void setOrder(List<String> order) {
        this.order = order;
    }

    public List<Integer> getActions() {
        return actions;
    }

    public void setActions(List<Integer> actions) {
        this.actions = actions;
    }

    public String getNextGameKey() {
        return nextGameKey;
    }

    public void setNextGameKey(String nextGameKey) {
        this.nextGameKey = nextGameKey;
    }

    public void setDeduct(int scoreAvg){
        for (Map.Entry entry : getGameUserMap().entrySet()) {
            PokerGame.GameUser gameUser = (PokerGame.GameUser) entry.getValue();
            gameUser.setDeduct(gameUser.getScore()-scoreAvg);
        }
    }
    //-----------------------------------------------------------------------------------------
    public static class Table implements Serializable{
        private String cover;
        private List<String> hides ;
        private List<String> shows ;
        private String message;

        public Table() {
            this.cover = Poker.COVER_WAIT;
            this.message = "";
        }

        public void init(List<String> hides) {
            this.hides = hides;
            this.shows = new ArrayList<>();
        }
        public String getCover() {
            return cover;
        }

        public void setCover(String cover) {
            this.cover = cover;
        }

        public List<String> getHides() {
            return hides;
        }

        public void setHides(List<String> hides) {
            this.hides = hides;
        }

        public List<String> getShows() {
            return shows;
        }

        public void setShows(List<String> shows) {
            this.shows = shows;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
                this.message = message;
        }

        public void setCoverAndMessage(String cover,String message){
            this.cover= cover;
            this.message =message;
        }
    }
    //-----------------------------------------------------------------------------------------
    public static class GameUser extends PokerUser implements Serializable{

        private List<String> hands = new ArrayList<>();
        private List<String> gathers = new ArrayList<>(); //p8-h2:2;
        private int score;
        private String message;

        public GameUser() {
        }

        public GameUser(PokerUser pokerUser,String message) {
            setRobotReplace(pokerUser.getType(),pokerUser.getUserName(),pokerUser.getLogo(),
                    pokerUser.getReplaceRobot());
            this.message = message;
        }

        public List<String> getHands() {
            return hands;
        }

        public void setHands(List<String> hands) {
            this.hands = hands;
        }


        public List<String> getGathers() {
            return gathers;
        }

        public void setGathers(List<String> gathers) {
            this.gathers = gathers;
        }


        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public int getScore() {
            return score;
        }

        public void setScore(int score) {
            this.score = score;
        }
        public void addScore(int score){
            this.score = this.score+score;
        }

    }

}
