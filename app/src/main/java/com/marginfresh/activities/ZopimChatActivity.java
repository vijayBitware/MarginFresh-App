package com.marginfresh.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.zendesk.logger.Logger;
import com.zopim.android.sdk.api.Chat;
import com.zopim.android.sdk.api.ZopimChat;
import com.zopim.android.sdk.chatlog.ZopimChatLogFragment;
import com.zopim.android.sdk.prechat.ChatListener;
import com.zopim.android.sdk.prechat.ZopimChatFragment;
import com.zopim.android.sdk.widget.ChatWidgetService;

/**
 * Created by bitware on 12/2/18.
 */

public class ZopimChatActivity extends AppCompatActivity implements ChatListener {
    private static final String LOG_TAG = "ZopimChatActivity";
    private static final String EXTRA_CHAT_CONFIG = "CHAT_CONFIG";
    private static final String STATE_CHAT_INITIALIZED = "CHAT_INITIALIZED";
    private Chat mChat;
    private boolean mChatInitialized = false;

    public ZopimChatActivity() {
    }

    public static void startActivity(Context context, ZopimChat.SessionConfig config) {
        Intent startChat = new Intent(context, com.zopim.android.sdk.prechat.ZopimChatActivity.class);
        startChat.putExtra("CHAT_CONFIG", config);
        context.startActivity(startChat);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(com.zopim.android.sdk.R.layout.zopim_chat_activity);
        Toolbar toolbar = (Toolbar)this.findViewById(com.zopim.android.sdk.R.id.toolbar);
        this.setSupportActionBar(toolbar);
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if(savedInstanceState != null) {
            this.mChatInitialized = savedInstanceState.getBoolean("CHAT_INITIALIZED", false);
            this.mChat = ZopimChat.resume(this);
            this.finish();
        } else {
            if(this.getIntent() != null) {
                String action = this.getIntent().getAction();
                if("zopim.action.RESUME_CHAT".equals(action)) {
                    Logger.v("ZopimChatActivity", "Resume chat request received", new Object[0]);
                }
            }

            this.mChat = ZopimChat.resume(this);
            this.mChatInitialized = !this.mChat.hasEnded();
            FragmentManager manager;
            if(!this.mChat.hasEnded()) {
                Logger.v("ZopimChatActivity", "Resuming chat log", new Object[0]);
                manager = this.getSupportFragmentManager();
                if(manager.findFragmentByTag(ZopimChatLogFragment.class.getName()) == null) {
                    ZopimChatLogFragment chatLogFragment = new ZopimChatLogFragment();
                    FragmentTransaction transaction = manager.beginTransaction();
                    transaction.add(com.zopim.android.sdk.R.id.chat_fragment_container, chatLogFragment, ZopimChatLogFragment.class.getName());
                    transaction.commit();
                }

            } else {
                Logger.v("ZopimChatActivity", "Starting new chat", new Object[0]);
                manager = this.getSupportFragmentManager();
                if(manager.findFragmentByTag(ZopimChatFragment.class.getName()) == null) {
                    ZopimChat.SessionConfig config = null;
                    if(this.getIntent() != null && this.getIntent().hasExtra("CHAT_CONFIG")) {
                        config = (ZopimChat.SessionConfig)this.getIntent().getSerializableExtra("CHAT_CONFIG");
                    }

                    ZopimChatFragment chatFragment = config != null?ZopimChatFragment.newInstance(config):new ZopimChatFragment();
                    FragmentTransaction transaction = manager.beginTransaction();
                    transaction.add(com.zopim.android.sdk.R.id.chat_fragment_container, chatFragment, ZopimChatFragment.class.getName());
                    transaction.commit();
                }

            }
        }
    }

    protected void onStart() {
        super.onStart();
        this.stopService(new Intent(this, ChatWidgetService.class));
    }

    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("CHAT_INITIALIZED", this.mChatInitialized);
    }

    protected void onDestroy() {
        Logger.v("ZopimChatActivity", "Activity destroyed", new Object[0]);
        super.onDestroy();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if(16908332 == item.getItemId()) {
            this.finish();
            return super.onOptionsItemSelected(item);
        } else {
            return false;
        }
    }

    public void onChatLoaded(Chat chat) {
        this.mChat = chat;
    }

    public void onChatInitialized() {
        this.mChatInitialized = true;
    }

    public void onChatEnded() {
        this.finish();
    }

}

