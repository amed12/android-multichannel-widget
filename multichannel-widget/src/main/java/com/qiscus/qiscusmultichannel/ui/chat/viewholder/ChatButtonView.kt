package com.qiscus.qiscusmultichannel.ui.chat.viewholder
import android.content.Context
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import com.qiscus.qiscusmultichannel.R
import org.json.JSONObject

class ChatButtonView(context: Context, private val jsonButton: JSONObject) :
    FrameLayout(context), View.OnClickListener {
    var button: TextView? = null
        private set
    private var chatButtonClickListener: ChatButtonClickListener? = null
    private fun injectViews() {
        View.inflate(context, R.layout.item_chat_button, this)
        button = findViewById(R.id.button)
    }

    private fun initLayout() {
        button!!.text = jsonButton.optString("label", "Button")
        button!!.setOnClickListener(this)
    }

    fun setChatButtonClickListener(chatButtonClickListener: ChatButtonClickListener?) {
        this.chatButtonClickListener = chatButtonClickListener
    }

    override fun onClick(v: View) {
        chatButtonClickListener!!.onChatButtonClick(jsonButton)
    }

    interface ChatButtonClickListener {
        fun onChatButtonClick(jsonButton: JSONObject?)
    }

    init {
        injectViews()
        initLayout()
    }
}
