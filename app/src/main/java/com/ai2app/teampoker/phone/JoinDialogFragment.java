package com.ai2app.teampoker.phone;


import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.ai2app.teampoker.R;
import com.ai2app.teampoker.db.PokerGame;
import com.ai2app.teampoker.db.PokerRoom;
import com.ai2app.teampoker.db.PokerDb;

import java.util.ArrayList;
import java.util.Arrays;

public class JoinDialogFragment extends DialogFragment implements
        View.OnClickListener {
    private static final String TAG = "JoinDialogFragment";
    private final Integer[] mEditRes = new Integer[]{R.id.dialog_edit1,R.id.dialog_edit2,
            R.id.dialog_edit3,R.id.dialog_edit4};
    private ArrayList<EditText> mInputPinEdits;
    private Button mSaveBtn,mCancleBtn;
    private TextView mMsgText;
    private OnJoinListener mOnJoinListener;
    private PokerDb mPokerDb;
    private StringBuilder mPin = new StringBuilder("0000");

    public interface OnJoinListener{
        void onJoinRoom(String roomKey,PokerRoom pokerRoom,String gameKey,PokerGame pokerGame);
    }

    public JoinDialogFragment() {
    }

    public static JoinDialogFragment newInstance() {
        JoinDialogFragment fragment = new JoinDialogFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_join_dialog,container);
    }

    @Override
    public void onStart() {
        super.onStart();
        getDialog().setCanceledOnTouchOutside(true);
        Window window = getDialog().getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;
        wlp.width = WindowManager.LayoutParams.MATCH_PARENT;
        wlp.height = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(wlp);
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mInputPinEdits = new ArrayList<>();
        for(int i=0;i<mEditRes.length;i++) {
            EditText editText = view.findViewById(mEditRes[i]);
            editText.addTextChangedListener(textWatcher);
            editText.setSelectAllOnFocus(true);
            mInputPinEdits.add(editText);
        }
        mSaveBtn = view.findViewById(R.id.dialog_save);
        mSaveBtn.setOnClickListener(this);
        mCancleBtn = view.findViewById(R.id.dialog_cancel);
        mCancleBtn.setOnClickListener(this);
        mMsgText = view.findViewById(R.id.dialog_message);

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(context instanceof OnJoinListener){
            mOnJoinListener = (OnJoinListener) context;
        }else {
            throw new RuntimeException(context.toString() + " must implement OnJoinListener");
        }
    }

    @Override
    public void onClick(View v) {
        int vid = v.getId();
        if(vid == R.id.dialog_save){
            checkPinCode(mPin.toString());
        }else {
            mOnJoinListener.onJoinRoom(null,null,null,null);
            dismiss();
        }
    }
    private PokerDb getPokerDb(){
        if(mPokerDb == null)
            mPokerDb = new PokerDb();
        return mPokerDb;
    }
    private void checkPinCode(final String code){
        getPokerDb().getPokerRoomFromInKey(code, new PokerDb.OnGetRoomListener() {
            @Override
            public void onComplete(final String roomKey, final PokerRoom pokerRoom) {
                if(roomKey != null){
                    if(pokerRoom == null)
                        mMsgText.setText(code+getString(R.string.join_dialog_message_full));
                    else {
                        getPokerDb().queryWaitGame(roomKey, new PokerDb.OnPokerGameListener() {
                            @Override
                            public void onComplete(String pokerGameKey, PokerGame pokerGame, int failType) {
                                if(failType == PokerDb.FAIL_TYPE_NONE) {
                                    mOnJoinListener.onJoinRoom(roomKey, pokerRoom, pokerGameKey, pokerGame);
                                    dismiss();
                                }else
                                    mMsgText.setText(R.string.join_dialog_message_run);
                            }
                        });
                    }
                }
                else {
                    mMsgText.setText(R.string.join_dialog_message_pin);
                }
            }
        });
    }

    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            Log.d(TAG, "beforeTextChanged: ");
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if(count == 1) {
                int index = Arrays.asList(mEditRes).indexOf(getView().findFocus().getId());
                if(index >=0) {
                    mPin.setCharAt(index, s.charAt(0));
                    mInputPinEdits.get(index).clearFocus();
                    index++;
                    if (index < 4)
                        mInputPinEdits.get(index).requestFocus();
                }
                else
                    mMsgText.setText(R.string.join_dialog_message_edit);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
            Log.d(TAG, "afterTextChanged: ");
        }
    };
}
