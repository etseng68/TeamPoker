package com.ai2app.teampoker.help;


import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.ai2app.teampoker.R;

public class AboutFragment extends DialogFragment implements View.OnClickListener{

    private static final String TAG="AboutFragment";
    private Button mCancelBtn;
    public AboutFragment() {
    }


    public static AboutFragment newInstance() {
        AboutFragment fragment = new AboutFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_about, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mCancelBtn=view.findViewById(R.id.about_cancel);
        mCancelBtn.setOnClickListener(this);
        getDialog().setTitle("About");
    }

    @Override
    public void onClick(View v) {
        dismiss();
    }
}
