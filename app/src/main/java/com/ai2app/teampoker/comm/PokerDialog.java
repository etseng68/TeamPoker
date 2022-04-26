package com.ai2app.teampoker.comm;

import android.app.Activity;
import android.app.Dialog;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.ai2app.teampoker.R;

public class PokerDialog {
    public static Dialog getProgressDialog(Activity activity,View dialogLayoutView){
        return new AlertDialog.Builder(activity)
                .setView(dialogLayoutView)
                .setCancelable(false)
                .create();
    }
    public static View progressDialogView(Activity activity,String title, String message){
        String msg = "Loading...";

        LayoutInflater inflater = activity.getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_progress,null);
        if(title != null && !title.isEmpty())
            ((TextView)view.findViewById(R.id.dialog_pb_title)).setText(title);
        if(message != null && !message.isEmpty())
            msg = message;
        ((TextView)view.findViewById(R.id.dialog_pb_message)).setText(msg);
        return view;
    }
}
