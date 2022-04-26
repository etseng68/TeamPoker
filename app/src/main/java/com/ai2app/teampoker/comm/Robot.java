package com.ai2app.teampoker.comm;

import com.ai2app.teampoker.db.PokerGame;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Robot {
    public static final String LOGO_NAME= "ic_launcher";
    public static final String NAME="robot";

    private static String handleCoverCard(PokerGame pokerGame){
        List<CardIa> cardIas = new ArrayList<>();
        String cover = pokerGame.getTable().getCover();
        String selectCard = null;
        for(String card:pokerGame.getTable().getShows()){
            int score;
            if(Poker.checkCardsPair(cover,card)) {
                score = Poker.getScore(cover, card);
                cardIas.add(new CardIa(card, cover, score));
            }
        }
        if(cardIas.size() == 1)
            selectCard= cardIas.get(0).getHand();
        else if(cardIas.size() > 1)
            selectCard= getResultCard(cardIas).getHand();
        return selectCard;
    }
    private static String handleHandCard(String robotUid,PokerGame pokerGame){
        String selectCard;
        if(pokerGame.getTable().getShows().size()>0) {
            List<CardIa> cardIas = new ArrayList<>();
            for (String hand : pokerGame.getGameUserMap().get(robotUid).getHands()) {
                for (String show : pokerGame.getTable().getShows()) {
                    int score = 0;
                    if (Poker.checkCardsPair(hand, show))
                        score = Poker.getScore(hand, show);
                    cardIas.add(new CardIa(hand, show, score));
                }
            }
            CardIa cardIa = getResultCard(cardIas);
            selectCard = cardIa.getHand();
        }else{
            List<String> selectList = pokerGame.getGameUserMap().get(robotUid).getHands();
            Collections.sort(selectList, new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    return Poker.getScore(o1) - Poker.getScore(o2);
                }
            });
            selectCard = selectList.get(0);
        }
        return selectCard;
    }
    private static CardIa getResultCard(List<CardIa> cardIas){
        Collections.sort(cardIas, new Comparator<CardIa>() {
            @Override
            public int compare(CardIa o1, CardIa o2) {
                int c;
                 c = o2.getScore()-o1.getScore();
                if(c == 0)
                    c = Poker.getScore(o1.getHand())-Poker.getScore(o2.getHand());
                return c;
            }
        });
        return cardIas.get(0);
    }
    public static PokerGame robotAction(PokerGame pokerGame,String[] msgs){
        String robotUid= pokerGame.getOrder().get(0);
        String selectCard= null;
        switch (pokerGame.getActions().get(0)){
                case Poker.ACT_THROW:
                    selectCard = handleHandCard(robotUid,pokerGame);
                    break;
                case Poker.ACT_THROW_SELECT:
                    selectCard = handleCoverCard(pokerGame);
                    break;
                case Poker.ACT_GET:
                    selectCard = null;
                    break;
                case Poker.ACT_GET_SELECT:
                    selectCard = handleCoverCard(pokerGame);
                    break;
            }
            return Tools.handleUserAction(robotUid,pokerGame, selectCard,msgs);

    }
    //-------------------------------------------------------------------------------------
    public static class CardIa{
        private String hand;
        private String show;
        private int score;

        public CardIa(String hand, String show, int score) {
            this.hand = hand;
            this.show = show;
            this.score = score;
        }

        public String getHand() {
            return hand;
        }

        public void setHand(String hand) {
            this.hand = hand;
        }

        public String getShow() {
            return show;
        }

        public void setShow(String show) {
            this.show = show;
        }

        public int getScore() {
            return score;
        }

        public void setScore(int score) {
            this.score = score;
        }
    }
}
