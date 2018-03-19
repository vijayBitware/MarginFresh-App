package com.marginfresh.ZendeskLiveChat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.marginfresh.R;
import com.marginfresh.activities.DrawerActivity;
import com.zopim.android.sdk.api.Chat;
import com.zopim.android.sdk.api.ZopimChat;
import com.zopim.android.sdk.chatlog.ZopimChatLogFragment;
import com.zopim.android.sdk.embeddable.ChatActions;
import com.zopim.android.sdk.prechat.ChatListener;
import com.zopim.android.sdk.prechat.PreChatForm;
import com.zopim.android.sdk.prechat.ZopimChatFragment;
import com.zopim.android.sdk.widget.ChatWidgetService;

/**
 * Created by bitware on 1/2/18.
 */

public class LiveChatActivity extends AppCompatActivity implements ChatListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sample_pre_chat_activity);


        // use toolbar as action bar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // orientation change
        if (savedInstanceState != null) {
            return;
        }

        /**
         * If starting activity while the chat widget is actively presented the activity will resume the current chat
         */
        boolean widgetWasActive = stopService(new Intent(this, ChatWidgetService.class));
        if (widgetWasActive) {
            resumeChat();
            return;
        }

        /**
         * We've received an intent request to resume the existing chat.
         * Resume the chat via {@link com.zopim.android.sdk.api.ZopimChat#resume(android.support.v4.app.FragmentActivity)} and
         * start the {@link ZopimChatLogFragment}
         */
        if (getIntent() != null) {
            String action = getIntent().getAction();
            if (ChatActions.ACTION_RESUME_CHAT.equals(action)) {
                resumeChat();
                return;
            }
        }

        /**
         * Attempt to resume chat. If there is an active chat it will be resumed.
         */
        Chat chat = ZopimChat.resume(this);
        if (!chat.hasEnded()) {
            resumeChat();
            return;
        }

        /**
         * Start a new chat
         */
        {
            // set pre chat fields as mandatory
            PreChatForm preChatForm = new PreChatForm.Builder()
                    .name(PreChatForm.Field.REQUIRED_EDITABLE)
                    .email(PreChatForm.Field.REQUIRED_EDITABLE)
                    .phoneNumber(PreChatForm.Field.REQUIRED_EDITABLE)
                    .department(PreChatForm.Field.REQUIRED_EDITABLE)
                    .message(PreChatForm.Field.REQUIRED_EDITABLE)
                    .build();
            // build chat config
            ZopimChat.SessionConfig config = new ZopimChat.SessionConfig()
                    .preChatForm(preChatForm);
            // prepare chat fragment
            ZopimChatFragment fragment = ZopimChatFragment.newInstance(config);
            // show fragment
            FragmentManager manager = getSupportFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.replace(R.id.chat_fragment_container, fragment, ZopimChatFragment.class.getName());
            transaction.commit();
        }
    }

    /**
     * Resumes the chat and loads the {@link ZopimChatLogFragment}
     */
    private void resumeChat() {

        FragmentManager manager = getSupportFragmentManager();
        // find the retained fragment
        if (manager.findFragmentByTag(ZopimChatLogFragment.class.getName()) == null) {
            ZopimChatLogFragment chatLogFragment = new ZopimChatLogFragment();

            FragmentTransaction transaction = manager.beginTransaction();
            transaction.add(R.id.chat_fragment_container, chatLogFragment, ZopimChatLogFragment.class.getName());
            transaction.commit();
        }
    }

    @Override
    public void onChatLoaded(Chat chat) {
        // TODO
    }

    @Override
    public void onChatInitialized() {
        // TODO
    }

    @Override
    public void onChatEnded() {
        // TODO
       // Toast.makeText(LiveChatActivity.this,"Chat Ended..",Toast.LENGTH_SHORT).show();
        startActivity(new Intent(LiveChatActivity.this,DrawerActivity.class));
    }
}
