package com.ai2app.teampoker.comm;

import android.util.Log;

import com.ai2app.teampoker.R;
import com.ai2app.teampoker.db.PokerGame;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Poker {
    private static final String TAG ="Poker";
    public static final int card =52;
    public static final int hand=6;
    public static final int fistShow =4;
    public static final int mixUser=4;
    public static final String[] marks = new String[]{"c","d","h","s"};
    public static final String[] numberStrs = new String[]{"1","2","3","4","5","6","7","8","9","a","b","c","d"};
    public static final String[] markScoreTen= new String[]{"a","b","c","d"};
    public static final String COVER_WAIT="gr",COVER_BLANK="ga",COVER_HIDE="gr";
    public static final int ACT_THROW=0,ACT_THROW_SELECT=1,ACT_GET=2,ACT_GET_SELECT=3;
    public static final int SCORE_TOTOAL=220,SCORE_AVG=55;

    public Poker() {
    }

    public static String getNumStr(String card) {
        return card.substring(1);
    }
    public static String getMark(String card) {
        return card.substring(0,1);
    }
    public static int getNum(String card){
        if(Arrays.asList(markScoreTen).contains(getNumStr(card)))
            return 10;
        else
            return Integer.parseInt(getNumStr(card));
    }

    public static int getScore(String card){
        int score=0;
        if(card.equals("s1"))
            score=10;
        else if (getMark(card).equals("d") || getMark(card).equals("h")) {
            if(Arrays.asList(markScoreTen).contains(getNumStr(card)))
                score = 10;
            else if (getNum(card) == 1)
                score = 20;
            else
                score= getNum(card);
        }

        return score;
    }
    public static List<String> getCards(){
        List<String> cards = new ArrayList<>();
        for(String mark:marks){
            for(String numberStr:numberStrs)
                cards.add(mark+numberStr);
        }
        return cards;
    }

    public static List<String> getRandomCards(){
        List<String> cards = Poker.getCards();
        List<String> randomCards = new ArrayList<>();
        Random random = new Random();
        for(int i=0;i<Poker.card;i++){
            int index = random.nextInt(cards.size());
            randomCards.add(cards.get(index));
            cards.remove(index);
        }
        return randomCards;
    }
    public static boolean checkCardsPair(String card1,String card2){
        boolean check = false;
        String numStr1 = getNumStr(card1);
        String numStr2 = getNumStr(card2);
        List<String> tenMark = Arrays.asList(markScoreTen);
        boolean markCheck1 = tenMark.contains(numStr1);
        boolean markCheck2 = tenMark.contains(numStr2);
        if(markCheck1 && markCheck2 && numStr1.equals(numStr2))
            check = true;
        else if(!markCheck1 && !markCheck2 && getNum(card1)+ getNum(card2) == 10)
            check = true;
        return check;
    }
    public static String getCardPairScoreString(String card1,String card2){
        return card1+":"+card2+","+getScore(card1,card2);
    }
    public static int getScore(String card1,String card2){
        int sum =  getScore(card1)+getScore(card2);
        if(sum == 29 || sum == 19 || sum == 9)
            sum++;
        return sum;
    }
    public static boolean checkCoverPairInCards(String cover,List<String> cards){
        boolean check =false;
        if(!cover.equals("gr")) {
            for (String card : cards) {
                check = checkCardsPair(cover, card);
                if (check) break;
            }
        }
        else
            Log.d(TAG, "checkCoverPairInCards: cover="+cover);
        Log.d(TAG, "checkCoverPairInCards: check="+check);
        return check;
    }
    public static int getCardPairScore(String cardPair){
        return Integer.parseInt(cardPair.split(",")[1]);
    }
}
