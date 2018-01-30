package amigo.atom.team.amigo.views.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import amigo.atom.team.amigo.R;
import amigo.atom.team.amigo.widgets.customs.regular.CustomDialogsActivity;
import amigo.atom.team.amigo.widgets.customs.regular.CustomMessagesActivity;


public class ChatFragment extends Fragment {
    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.view = inflater.inflate(R.layout.fragment_chat, container, false);

        final FloatingActionButton btnGoChat = (FloatingActionButton) view.findViewById(R.id.btnGoChat);


        btnGoChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().startActivity(new Intent(getActivity(), CustomDialogsActivity.class));
            }
        });
        return this.view;
    }
}
