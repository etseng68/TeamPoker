package com.ai2app.teampoker.db;


import android.util.Log;

import androidx.annotation.NonNull;

import com.ai2app.teampoker.comm.Robot;
import com.ai2app.teampoker.comm.Tools;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class PokerDb{
    private static final String TAG="PokerStore";
    public static final int FAIL_TYPE_NONE=0,FAIL_TYPE_MANY=1,FAIL_TYPE_NOT_FOUND=2;
    public static final String POKER = "poker";
    public static final String MEMBER = "member";
    public static final String AD = "ad";
    public static final String GAME = "game";
    private static final String FAIL_GET = "failGet";
    private static final String FAIL_OTHER = "failOther";
    private FirebaseFirestore mFStore;

    private PokerRoom mPokerRoom;
    private PokerGame mPokerGame;

    public PokerDb() {}

    public interface OnMemberListener{
        void onComplete(String uid,Member member);
    }

    public interface OnPokerGameListener{
        void onComplete(String pokerGameKey,PokerGame pokerGame,int failType);
    }
    public interface OnGetRoomListener{
        void onComplete(String roomKey,PokerRoom pokerRoom);
    }
    public interface OnRoomCreateListener{
        void onComplete(String roomKey,String roomInKey);
    }

    public void createGame(final String roomKey, PokerRoom pokerRoom, String userMessage,
                           String coverMessage,final OnPokerGameListener onPokerGameListener){
        mPokerGame = new PokerGame(pokerRoom,userMessage,coverMessage);
        getFStore().collection(POKER+"/"+roomKey+"/"+GAME).add(mPokerGame)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        onPokerGameListener.onComplete(documentReference.getId(),
                                mPokerGame,FAIL_TYPE_NONE);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i(TAG, "onFailure: createGame room ="+roomKey);
                        onPokerGameListener.onComplete(null,
                                mPokerGame,FAIL_TYPE_NOT_FOUND);
                    }
                });
    }
    public void queryWaitGame(final String roomKey, final OnPokerGameListener onPokerGameListener){
        getFStore().collection(POKER+"/"+roomKey+"/"+GAME)
                .whereEqualTo("status", PokerGame.GAME_WAIT).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()){
                            if(task.getResult().size() == 1){
                                DocumentSnapshot snapshot = task.getResult().getDocuments().get(0);
                                onPokerGameListener.onComplete(snapshot.getId(),
                                        snapshot.toObject(PokerGame.class),FAIL_TYPE_NONE);
                            }else {
                                onPokerGameListener.onComplete(null,null,
                                        FAIL_TYPE_MANY);
                                Log.i(TAG, "joinGame to many:"+roomKey);
                            }
                        }else{
                            onPokerGameListener.onComplete(null,null,
                                    FAIL_TYPE_NOT_FOUND);
                            Log.i(TAG, "joinGame faile :"+roomKey);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }

    public void getPokerRoomFromInKey(final String roomInKey, final OnGetRoomListener onGetRoomListener){
        getRoomInKeyQuery(roomInKey).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()){
                           if(task.getResult().size() == 1){
                               DocumentSnapshot snapshot = task.getResult().getDocuments().get(0);
                               PokerRoom pokerRoom = snapshot.toObject(PokerRoom.class);
                               if(Tools.checkRoomInFull(pokerRoom) ||
                                       pokerRoom.getStatus() == PokerRoom.ROOM_FINISH)
                                   onGetRoomListener.onComplete(snapshot.getId(), null);
                               else
                                   onGetRoomListener.onComplete(snapshot.getId(), pokerRoom);
                           }else {
                               onGetRoomListener.onComplete(null, null);
                               Log.i(TAG, "getPokerRoomFromInKey to many:"+roomInKey);
                           }
                        }else{
                            onGetRoomListener.onComplete(null, null);
                            Log.i(TAG, "getPokerRoomFromInKey faile :"+roomInKey);
                        }
                    }
                });
    }
    public void setPokerRoom(String roomKey,PokerRoom pokerRoom){
        getFStore().collection(POKER).document(roomKey).set(pokerRoom);
    }
    public void getPokerRoom(final String roomKey, final OnGetRoomListener onGetRoomListener){
        getFStore().collection(POKER).document(roomKey).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if(documentSnapshot.exists()) {
                            PokerRoom pokerRoom = documentSnapshot.toObject(PokerRoom.class);
                            onGetRoomListener.onComplete(roomKey, pokerRoom);
                        }
                        else
                            onGetRoomListener.onComplete(roomKey,null);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i(TAG, "onFailure: getPokerRoom="+roomKey);
                onGetRoomListener.onComplete(roomKey,null);
            }
        });
    }
    public void queryRoom(String uid, final OnGetRoomListener onGetRoomListener){
        getFStore().collection(POKER)
                .whereEqualTo("status",PokerRoom.ROOM_USE)
                .whereEqualTo("joins."+uid,true)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            if(task.getResult().isEmpty()){
                                onGetRoomListener.onComplete(null,null);
                            }else{
                                List<DocumentSnapshot> docs = task.getResult().getDocuments();
                                Collections.sort(docs, new Comparator<DocumentSnapshot>() {
                                    @Override
                                    public int compare(DocumentSnapshot d1, DocumentSnapshot d2) {
                                        PokerRoom r1 = d1.toObject(PokerRoom.class);
                                        PokerRoom r2 = d2.toObject(PokerRoom.class);
                                        return r1.getCreate().compareTo(r2.getCreate());
                                    }
                                });
                                for(int i=0;i<docs.size();i++){
                                    PokerRoom pokerRoom = docs.get(i).toObject(PokerRoom.class);
                                    if(i == docs.size()-1)
                                        onGetRoomListener.onComplete(docs.get(i).getId(),
                                                pokerRoom);
                                    else{
                                        pokerRoom.setStatus(PokerRoom.ROOM_FINISH);
                                        docs.get(i).getReference().set(pokerRoom);
                                    }

                                }
                            }
                        }else {
                            Log.d(TAG, "onComplete:faile="+task.getException());
                            onGetRoomListener.onComplete(null,null);
                        }
                    }
                });
    }
    public void createRoom(PokerRoom pokerRoom,OnRoomCreateListener onRoomCreateListener){
        mPokerRoom = pokerRoom;
        queryRoomKey(getRandomRoomKey(),onRoomCreateListener);
    }
    private Query getRoomInKeyQuery(String roomInKey){
        return getFStore().collection(POKER)
                .whereEqualTo("status",PokerRoom.ROOM_USE).
                        whereEqualTo("roomInKey",roomInKey);
    }
    private void queryRoomKey(final String roomInKey, final OnRoomCreateListener onRoomCreateListener){
        getRoomInKeyQuery(roomInKey).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()){
                            if(task.getResult().isEmpty())
                                addNewRoom(roomInKey,onRoomCreateListener);
                            else
                                queryRoomKey(getRandomRoomKey(),onRoomCreateListener);
                        }else{
                            Log.i(TAG, "onComplete: createRoomInKey="+roomInKey);
                        }
                    }
                });
    }
    private void addNewRoom(final String roomInKey, final OnRoomCreateListener onRoomCreateListener){
        mPokerRoom.setRoomInKey(roomInKey);
        getFStore().collection(POKER).add(mPokerRoom)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference docRf) {
                        if(docRf != null ){
                            final String roomKey = docRf.getId();
                            //docRf.set(getDocReferenceData("roomInKey",roomInKey), SetOptions.merge())
                            docRf.set(mPokerRoom)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            onRoomCreateListener.onComplete(roomKey,roomInKey);
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.i(TAG, "onSuccess: set roomInKey failure");
                                        }
                                    });

                        }
                        else
                            Log.i(TAG, "onSuccess: doc or listener is null");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i(TAG,"createPokerRoomFromPhone:"+FAIL_OTHER+"="+e.getMessage());
                    }
                });
    }

    private String getRandomRoomKey(){
        Random random = new Random();
        return String.format("%04d",random.nextInt(10000));
    }

    public void getPokerGame(final String roomKey, final String gameKey,
                             final OnPokerGameListener onPokerGameListener){
        getFStore().collection(POKER+"/"+roomKey+"/"+GAME).document(gameKey).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        PokerGame pokerGame = documentSnapshot.toObject(PokerGame.class);
                        onPokerGameListener.onComplete(documentSnapshot.getId(),pokerGame,
                                FAIL_TYPE_NONE);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i(TAG, "onFailure: room="+roomKey+",game="+gameKey);
                    }
                });
    }
    public void queryPokerGame(String roomKey, final OnPokerGameListener onPokerGameListener){
        getFStore().collection(POKER+"/"+roomKey+"/"+GAME)
                .whereLessThan("status",PokerGame.GAME_OVER).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful() && task.getResult() != null){
                            List<DocumentSnapshot> docs = task.getResult().getDocuments();
                            if(docs.size() ==0){
                                onPokerGameListener.onComplete(null, null,
                                        FAIL_TYPE_NOT_FOUND);
                            }else if(docs.size() ==1){
                                onPokerGameListener.onComplete(docs.get(0).getId(),
                                        docs.get(0).toObject(PokerGame.class), FAIL_TYPE_NONE);
                            }else if(docs.size() >1) {
                                Collections.sort(docs, new Comparator<DocumentSnapshot>() {
                                    @Override
                                    public int compare(DocumentSnapshot d1, DocumentSnapshot d2) {
                                        PokerGame g1 = d1.toObject(PokerGame.class);
                                        PokerGame g2 = d2.toObject(PokerGame.class);
                                        return g1.getCreate().compareTo(g2.getCreate());
                                    }
                                });
                                for (int i = 0; i < docs.size(); i++) {
                                    PokerGame pokerGame = docs.get(i).toObject(PokerGame.class);
                                    if (i == docs.size() - 1)
                                        onPokerGameListener.onComplete(docs.get(i).getId(), pokerGame,
                                                FAIL_TYPE_NONE);
                                    else {
                                        pokerGame.setStatus(PokerGame.GAME_OVER);
                                        docs.get(i).getReference().set(pokerGame);
                                    }
                                }
                            }
                        }
                        else{
                            Log.d(TAG, "onComplete:faile "+ task.getException());
                            onPokerGameListener.onComplete(null, null,
                                    FAIL_TYPE_NOT_FOUND);
                        }
                    }
                });

    }
    public void setPokerRoomAndPokerGame(String roomKey,PokerRoom pokerRoom,String gameyKey,
                                         PokerGame pokerGame){
        setPokerRoom(roomKey,pokerRoom);
        setPokerGame(roomKey,gameyKey,pokerGame);
    }
    public void setPokerGame(String mPokerRoomKey, final String pokerGameKey, PokerGame pokerGame){
        getFStore().collection(POKER+"/"+mPokerRoomKey+"/"+GAME).document(pokerGameKey)
                .set(pokerGame)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "setPokerGame:onSuccess"+pokerGameKey);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i(TAG, "setPokerGame:onFailure"+e.getMessage());
                    }
                });
    }
    public void setMembersPoint(PokerGame pokerGame){
        for (Map.Entry entry : pokerGame.getGameUserMap().entrySet()) {
            String uid = entry.getKey().toString();
            final PokerGame.GameUser gameUser = (PokerGame.GameUser) entry.getValue();
            if(gameUser.getType() != PokerGame.GameUser.ROBOT) {
                getMember(uid, new OnMemberListener() {
                    @Override
                    public void onComplete(String uid, Member member) {
                        if (member != null && gameUser.getScore() >0) {
                            member.addPoint(gameUser.getDeduct());
                            setMember(uid, member, null);
                        } else
                            Log.d(TAG, "onComplete: member is null or score =0");
                    }
                });
            }
        }
    }

    public void setMember(final String uid, final Member member, final OnMemberListener onMemberListener){

        //Map<String,Object> memberMap =Tools.getMapFromClass(member,true);
        getFStore().collection(MEMBER).document(uid).set(member)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        if(onMemberListener != null)
                            onMemberListener.onComplete(uid,member);
                        else
                            Log.d(TAG, "setMember:onSuccess uie="+uid);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i(TAG, "createMember:onFailure"+e.getMessage());
                        if(onMemberListener != null)
                            onMemberListener.onComplete(uid,null);
                    }
                });
    }

    public void getMember(final String uid, final OnMemberListener onMemberListener){
        getFStore().collection(MEMBER).document(uid).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()){
                            onMemberListener.onComplete(uid,documentSnapshot.toObject(Member.class));
                        }
                        else {
                            //mOnMemberListener.onComplete(uid,null);
                            setMember(uid, new Member(), new OnMemberListener() {
                                @Override
                                public void onComplete(String uid, Member member) {
                                    onMemberListener.onComplete(uid,member);
                                }
                            });
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i(TAG, "getMember:onFailure"+e.getMessage());
                        onMemberListener.onComplete(uid,null);
                    }
                });
    }

    public void addMemberAdViewScore(String adUid, final int scoreBase, final String roomKey, final String gameKey){
        final String uid = FirebaseAuth.getInstance().getUid();
        getFStore().collection(MEMBER+"/"+uid+"/"+AD).document(adUid).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            DocumentSnapshot doc = task.getResult();
                            PokerAdView pokerAdView = null;
                            String key = String.valueOf(Calendar.getInstance().getTimeInMillis());
                            if(doc != null && doc.exists()){
                                pokerAdView=doc.toObject(PokerAdView.class);
                                pokerAdView.addAdMap(
                                        (pokerAdView.getAdMap().size()+1)*scoreBase,
                                        key,roomKey,gameKey);
                            }else {
                                pokerAdView = new PokerAdView(scoreBase, key,roomKey,gameKey);
                            }
                            doc.getReference().set(pokerAdView);
                            final long addScore =pokerAdView.getScoreSum();
                            getMember(uid, new OnMemberListener() {
                                @Override
                                public void onComplete(String uid, Member member) {
                                    member.addPoint(addScore);
                                    setMember(uid,member,null);
                                }
                            });
                        }
                        else
                            Log.d(TAG, "onComplete: task fail");
                    }
                });
    }


    private FirebaseFirestore getFStore(){
        if(mFStore == null)
            mFStore = FirebaseFirestore.getInstance();
        return mFStore;
    }
}
