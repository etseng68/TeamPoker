package com.ai2app.teampoker.comm;


import com.ai2app.teampoker.db.PokerGame;
import com.ai2app.teampoker.db.PokerRoom;
import com.ai2app.teampoker.db.PokerUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class Tools {
    private static final String TAG ="Tools" ;

    public static final int[] acts = new int[]{Poker.ACT_THROW,Poker.ACT_THROW_SELECT,
            Poker.ACT_GET,Poker.ACT_GET_SELECT};

    public static PokerGame handleUserAction(String uid, PokerGame pokerGame, String selectCard,
                                             String[] msgs){
            switch (pokerGame.getActions().get(0)) {
                case Poker.ACT_THROW:
                    pokerGame.getGameUserMap().get(uid).getHands().remove(selectCard);
                    pokerGame = handleUserCard(pokerGame, selectCard);
                    break;
                case Poker.ACT_THROW_SELECT:
                    pokerGame = handleUserSelect(uid, pokerGame, selectCard, Poker.COVER_HIDE);
                    break;
                case Poker.ACT_GET:
                    pokerGame = shiftTableHideCard(pokerGame);
                    pokerGame = handleUserCard(pokerGame, pokerGame.getTable().getCover());
                    break;
                case Poker.ACT_GET_SELECT:
                    pokerGame = handleUserSelect(uid, pokerGame, selectCard, Poker.COVER_BLANK);
                    break;
            }
            if (pokerGame.getActions().size() == 0) {
                pokerGame.initUserAction();
                pokerGame.getOrder().remove(0);
            }
            pokerGame.getTable().setMessage(null);
            if(pokerGame.getTable().getCover() == Poker.COVER_BLANK && pokerGame.getOrder().size()>0){
                pokerGame.getTable().setCover(
                        pokerGame.getGameUserMap().get(pokerGame.getOrder().get(0)).getLogo());
                pokerGame.getTable().setMessage(msgs[Poker.ACT_THROW]);
            }
        return pokerGame;
    }

    private static PokerGame handleUserCard(PokerGame pokerGame,String selectCard){
        pokerGame.getTable().setCover(selectCard);
        boolean pairCheck = Poker.checkCoverPairInCards(pokerGame.getTable().getCover(),
                pokerGame.getTable().getShows());
        if(!pairCheck){
            pokerGame.getTable().getShows().add(pokerGame.getTable().getCover());
            String cover;
            if(pokerGame.getActions().get(0) == Poker.ACT_THROW)
                cover = Poker.COVER_HIDE;
            else
                cover = Poker.COVER_BLANK;
            pokerGame.getTable().setCover(cover);
            pokerGame.getActions().remove(0);
        }
        pokerGame.getActions().remove(0);
        return pokerGame;
    }
    private static PokerGame handleUserSelect(String uid,PokerGame pokerGame,String selectCard,
                                              String cover){
        pokerGame.addGather(uid,
                Poker.getCardPairScoreString(pokerGame.getTable().getCover(),selectCard));
        pokerGame.getTable().getShows().remove(selectCard);
        pokerGame.getTable().setCover(cover);
        pokerGame.getActions().remove(0);
        return pokerGame;
    }
    public static PokerGame shiftTableHideCard(PokerGame pokerGame){
        if(pokerGame.getTable().getHides().size()>0) {
            pokerGame.getTable().setCover(pokerGame.getTable().getHides().get(0));
            pokerGame.getTable().getHides().remove(0);
        }
        return pokerGame;
    }

    public static List<PokerGame.GameUser> getDeductSelf(String uid,PokerGame pokerGame){
        List<PokerGame.GameUser> deductUsers = null;
        List<String> positions = pokerGame.getPosition();
        if(positions != null && positions.size()>0) {
            deductUsers = new ArrayList<>();
            int x = positions.indexOf(uid);
            for (int i = 1; i < positions.size(); i++) {
                x++;
                if (x > positions.size() - 1) x = 0;
                PokerGame.GameUser gameUser = pokerGame.getGameUserMap().get(positions.get(x));
                gameUser.setUid(positions.get(x));
                deductUsers.add(gameUser);
            }
        }
        return deductUsers;
    }
    public static PokerUser getPokerUser(PokerRoom pokerRoom, String uid){
        PokerUser user = null;
        for(PokerUser pokerUser:pokerRoom.getUsers()){
            if(pokerUser.getUid().equals(uid)) {
                user = pokerUser;
                break;
            }
        }
        return user;
    }
    public static PokerRoom setPokerUser(PokerRoom pokerRoom, PokerUser pokerUser){
        for (int i = 0; i < pokerRoom.getUsers().size(); i++) {
            PokerUser user =pokerRoom.getUsers().get(i);
            if(user.equals(pokerUser.getUid())) {
                pokerRoom.getUsers().set(i,pokerUser);
                break;
            }
        }
        return pokerRoom;
    }

    public static List<String> reSetRanking(Map<String,PokerGame.GameUser> gameUserMap){
        List<UserRanking> userRankings = new ArrayList<>();
        List<String> newRankings = new ArrayList<>();
        for(Map.Entry entry:gameUserMap.entrySet())
            userRankings.add(new UserRanking(entry.getKey().toString(),
                    (int)((PokerGame.GameUser)entry.getValue()).getScore()));

        Collections.sort(userRankings, new Comparator<UserRanking>() {
            @Override
            public int compare(UserRanking u1, UserRanking u2) {
                return u2.getScore()-u1.getScore();
            }
        });

        for(UserRanking userRanking:userRankings)
            newRankings.add(userRanking.getUid());
        return newRankings;
    }

    public static List<String> convertCharsToList(CharSequence[] sequences){
        List<String> cards = null;
        if (sequences != null && sequences.length >0) {
            cards = new ArrayList<>();
            for (int i = 0; i < sequences.length; i++) {
                cards.add(i, sequences[i].toString());
            }
        }
        return cards;
    }
    public static List<String> convertCharsToList(String[] sequences){
        List<String> cards = null;
        if (sequences != null && sequences.length >0) {
            cards = new ArrayList<>();
            for (int i = 0; i < sequences.length; i++) {
                cards.add(i, sequences[i]);
            }
        }
        return cards;
    }
    public static String[] convertCharsToArrary(CharSequence[] sequences){
        String[] cards = null;
        if (sequences != null && sequences.length >0) {
            cards = new String[sequences.length];
            for (int i = 0; i < sequences.length; i++) {
                cards[i]= sequences[i].toString();
            }
        }
        return cards;
    }
    public static String convertCharsToString(CharSequence sequence){
        if (sequence != null)
            return sequence.toString();
        else
            return null;
    }

    public static boolean checkRoomInFull(PokerRoom pokerRoom){
        boolean check = true;
        for (PokerUser pokerUser : pokerRoom.getUsers()) {
            if (pokerUser.getType() == PokerUser.ROBOT) {
                check = false;
                break;
            }
        }
        return check;
    }
    //----------------------------------------------------------------------------------------------
    private static class UserRanking{
        private String uid;
        private int score;
        public UserRanking(String uid,int score){
            this.uid = uid;
            this.score =score;
        }

        public String getUid() {
            return uid;
        }

        public void setUid(String uid) {
            this.uid = uid;
        }

        public int getScore() {
            return score;
        }

        public void setScore(int score) {
            this.score = score;
        }
    }

}
