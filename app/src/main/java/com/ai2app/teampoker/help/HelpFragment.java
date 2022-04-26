package com.ai2app.teampoker.help;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.ai2app.teampoker.R;


public class HelpFragment extends DialogFragment implements View.OnClickListener {

    private static final String ARG_TYPE = "type";
    public static final int TYPE_USER_CHOICE=0,TYPE_PHONE_MAIN=1;

    private int mType;

    public HelpFragment() {
        // Required empty public constructor
    }

    public static HelpFragment newInstance(int type) {
        HelpFragment fragment = new HelpFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_TYPE, type);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mType = getArguments().getInt(ARG_TYPE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        int fragmentLayout = R.layout.fragment_help_user_choice;
        if(mType == TYPE_PHONE_MAIN)
            fragmentLayout = R.layout.fragment_help_phone_main;
        return inflater.inflate(fragmentLayout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ImageView closeImage  = view.findViewById(R.id.help_close);
        closeImage.setOnClickListener(this);
        getDialog().getWindow().getAttributes().alpha=0.7f;
    }



    @Override
    public void onClick(View v) {
        dismiss();
    }
}
