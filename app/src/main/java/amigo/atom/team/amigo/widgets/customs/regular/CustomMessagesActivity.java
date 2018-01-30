package amigo.atom.team.amigo.widgets.customs.regular;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.stfalcon.chatkit.messages.MessageHolders;
import com.stfalcon.chatkit.messages.MessageInput;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

import amigo.atom.team.amigo.R;
import amigo.atom.team.amigo.common.fixtures.MessagesFixtures;
import amigo.atom.team.amigo.common.model.Message;
import amigo.atom.team.amigo.common.demo.DemoMessagesActivity;
import amigo.atom.team.amigo.widgets.customs.regular.holders.messages.CustomIncomingImageMessageViewHolder;
import amigo.atom.team.amigo.widgets.customs.regular.holders.messages.CustomIncomingTextMessageViewHolder;
import amigo.atom.team.amigo.widgets.customs.regular.holders.messages.CustomOutcomingImageMessageViewHolder;
import amigo.atom.team.amigo.widgets.customs.regular.holders.messages.CustomOutcomingTextMessageViewHolder;
import amigo.atom.team.amigo.utils.AppUtils;


public class CustomMessagesActivity extends DemoMessagesActivity
        implements MessagesListAdapter.OnMessageLongClickListener<Message>,
        MessageInput.InputListener,
        MessageInput.AttachmentsListener {

    public static void open(Context context) {
        context.startActivity(new Intent(context, CustomMessagesActivity.class));
    }

    private MessagesList messagesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_holder_messages);

        messagesList = (MessagesList) findViewById(R.id.messagesList);
        initAdapter();

        MessageInput input = (MessageInput) findViewById(R.id.input);
        input.setInputListener(this);
        input.setAttachmentsListener(this);
    }

    @Override
    public boolean onSubmit(CharSequence input) {
        messagesAdapter.addToStart(
                MessagesFixtures.getTextMessage(input.toString()), true);
        return true;
    }

    @Override
    public void onAddAttachments() {
        messagesAdapter.addToStart(MessagesFixtures.getImageMessage(), true);
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
        super.messagesAdapter.setLoadMoreListener(this);
        messagesList.setAdapter(super.messagesAdapter);
    }
}
