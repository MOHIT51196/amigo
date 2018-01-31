package amigo.atom.team.amigo.widgets.customs.regular;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.stfalcon.chatkit.messages.MessageHolders;
import com.stfalcon.chatkit.messages.MessageInput;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

import java.util.ArrayList;

import amigo.atom.team.amigo.R;
import amigo.atom.team.amigo.common.fixtures.MessagesFixtures;
import amigo.atom.team.amigo.common.model.Message;
import amigo.atom.team.amigo.common.demo.MessagesActivity;
import amigo.atom.team.amigo.widgets.customs.regular.holders.messages.CustomIncomingImageMessageViewHolder;
import amigo.atom.team.amigo.widgets.customs.regular.holders.messages.CustomIncomingTextMessageViewHolder;
import amigo.atom.team.amigo.widgets.customs.regular.holders.messages.CustomOutcomingImageMessageViewHolder;
import amigo.atom.team.amigo.widgets.customs.regular.holders.messages.CustomOutcomingTextMessageViewHolder;
import amigo.atom.team.amigo.utils.AppUtils;

import static amigo.atom.team.amigo.common.fixtures.MessagesFixtures.getTextMessage;
import static android.os.Build.VERSION_CODES.M;


public class CustomMessagesActivity extends MessagesActivity
        implements MessagesListAdapter.OnMessageLongClickListener<Message>,
        MessageInput.InputListener,
        MessageInput.AttachmentsListener {

    private DatabaseReference dbRef;
    private StorageReference storageRef;


    public static void open(Context context) {
        context.startActivity(new Intent(context, CustomMessagesActivity.class));
    }

    private MessagesList messagesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_holder_messages);

        dbRef = FirebaseDatabase.getInstance().getReference();
        storageRef = FirebaseStorage.getInstance().getReference();

        messagesList = (MessagesList) findViewById(R.id.messagesList);
        initAdapter();

        MessageInput input = (MessageInput) findViewById(R.id.input);
        input.setInputListener(this);
        input.setAttachmentsListener(this);
    }

    @Override
    public boolean onSubmit(CharSequence input) {
        Message msg = MessagesFixtures.getTextMessage(input.toString());
        messagesAdapter.addToStart(msg
                , true);

//        dbRef.child("chat").child(msg.getId()).setValue(msg);
        return true;
    }

    @Override
    public void onAddAttachments() {
//        messagesAdapter.addToStart(MessagesFixtures.getImageMessage(), true);
    }

    @Override
    public void onMessageLongClick(Message message) {
        AppUtils.showToast(this, R.string.on_log_click_message, false);
    }

    private void initAdapter() {
        MessageHolders holdersConfig = new MessageHolders()
                .setIncomingTextConfig(
                        CustomIncomingTextMessageViewHolder.class,
                        R.layout.item_custom_incoming_text_message)
                .setOutcomingTextConfig(
                        CustomOutcomingTextMessageViewHolder.class,
                        R.layout.item_custom_outcoming_text_message)
                .setIncomingImageConfig(
                        CustomIncomingImageMessageViewHolder.class,
                        R.layout.item_custom_incoming_image_message)
                .setOutcomingImageConfig(
                        CustomOutcomingImageMessageViewHolder.class,
                        R.layout.item_custom_outcoming_image_message);

        super.messagesAdapter = new MessagesListAdapter<>(super.senderId, holdersConfig, super.imageLoader);
        super.messagesAdapter.setOnMessageLongClickListener(this);
//        super.messagesAdapter.setLoadMoreListener(this);
        messagesList.setAdapter(super.messagesAdapter);

        new MessageLoaderTask().execute();
    }

    private class MessageLoaderTask extends AsyncTask<Void, Void, Void>{


        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Void doInBackground(Void... params) {
            dbRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    int flag = 0;

                    for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                        messagesAdapter.clear();

                        if(flag == 0){
                            messagesAdapter.addToStart(snapshot.getValue(Message.class), true);
                            flag++;
                        }

                        for(int i=0; i<10; i++) {
                            ArrayList<Message> messages = new ArrayList<>();
                            Message msg = snapshot.getValue(Message.class);
                            messages.add(msg);
                            messagesAdapter.addToEnd(messages, false);
                            messagesAdapter.notifyDataSetChanged();
                        }
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {}
            });

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
        }
    }
}
