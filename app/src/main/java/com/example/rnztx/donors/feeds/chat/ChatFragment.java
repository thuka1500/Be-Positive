package com.example.rnztx.donors.feeds.chat;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import com.example.rnztx.donors.R;
import com.example.rnztx.donors.models.ChatMessage;
import com.example.rnztx.donors.models.utils.Constants;
import com.example.rnztx.donors.models.utils.Utilities;
import com.firebase.client.Firebase;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatFragment extends Fragment {

    ArrayAdapter mArrayAdapter;
    private static final String LOG_TAG = ChatFragment.class.getSimpleName();
    @Bind(R.id.img_send_chat_message) ImageButton imgSendChatMessage;
    @Bind(R.id.edtx_new_message) EditText edtxNewMesssage;
    @Bind(R.id.list_view_chats) ListView listViewChats;
    ArrayList<String> mList ;
    String mDonorId = "879322342";
    Firebase mFirebaseNewMessage;

    public ChatFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mList = new ArrayList<>();
        mArrayAdapter = new ArrayAdapter(getActivity(),R.layout.chat_items,mList);
        Bundle arguments = getActivity().getIntent().getExtras();
        if (arguments.containsKey(Constants.EXTRA_TARGET_USERID)){
            mDonorId = arguments.getString(Constants.EXTRA_TARGET_USERID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.chat_fragment, container, false);
        ButterKnife.bind(this,rootView);
        listViewChats.setAdapter(mArrayAdapter);

        Firebase firebase = new Firebase(Constants.FIREBASE_URL).child(Constants.FIREBASE_LOCATION_CHATMESSAGES);
        mFirebaseNewMessage = firebase.child(Utilities.getChatId(mDonorId));


        return rootView;
    }

    @OnClick(R.id.img_send_chat_message)
    public void onMessageSend(){
        String newMessage = edtxNewMesssage.getText().toString();
        ChatMessage newMessageObj = new ChatMessage(Utilities.getUserId(),newMessage);
        String uniqueKey = mFirebaseNewMessage.push().getKey();
        mFirebaseNewMessage.child(uniqueKey).setValue(newMessageObj);
        Log.e(LOG_TAG,"message send");
    }

}
