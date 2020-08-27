package com.qiscus.qiscusmultichannel.ui.view;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.qiscus.qiscusmultichannel.R;

import org.json.JSONObject;

public class ButtonView extends FrameLayout implements View.OnClickListener {
    private TextView button;
    private JSONObject jsonButton;
    private ChatButtonClickListener chatButtonClickListener;

    public ButtonView(Context context, JSONObject jsonButton) {
        super(context);
        this.jsonButton = jsonButton;
        injectViews();
        initLayout();
    }

    private void injectViews() {
        inflate(getContext(), R.layout.view_chat_button, this);
        button = findViewById(R.id.button);
    }

    private void initLayout() {
        button.setText(jsonButton.optString("label", "Button"));
        button.setOnClickListener(this);
    }

    public TextView getButton() {
        return button;
    }

    public void setChatButtonClickListener(ChatButtonClickListener chatButtonClickListener) {
        this.chatButtonClickListener = chatButtonClickListener;
    }

    @Override
    public void onClick(View v) {
        chatButtonClickListener.onChatButtonClick(jsonButton);
    }

    public interface ChatButtonClickListener {
        void onChatButtonClick(JSONObject jsonButton);
    }
}
