package com.qiscus.qiscusmultichannel.ui.chat.viewholder
import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import com.qiscus.qiscusmultichannel.MultichannelWidget
import com.qiscus.qiscusmultichannel.R
import com.qiscus.qiscusmultichannel.ui.chat.ChatRoomActivity
import com.qiscus.qiscusmultichannel.ui.chat.ChatRoomFragment
import com.qiscus.qiscusmultichannel.ui.chat.viewholder.ChatButtonView.ChatButtonClickListener
import com.qiscus.qiscusmultichannel.util.Const
import com.qiscus.sdk.chat.core.QiscusCore
import com.qiscus.sdk.chat.core.data.model.QMessage
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*


class ButtonMessageVH(
    itemView: View) :
    BaseViewHolder(itemView) {
    private val textContent: TextView
    private val textTime: TextView
    private val buttonsContainer: ViewGroup
    private val messageContainer: ViewGroup
    private var context : Context? = null
    override fun bind(context: Context, comment: QMessage) {
        super.bind(context, comment)
        this.context = context
        try {
            val obj = JSONObject(comment.payload)
            setUpButtons(obj.getJSONArray("buttons"), comment)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        textContent.text = comment.text
        //textTime.text = comment.time.toString()
    }

    @SuppressLint("NewApi")
    private fun setUpButtons(buttons: JSONArray, comment: QMessage) {
        buttonsContainer.removeAllViews()
        val size = buttons.length()
        if (size < 1) {
            return
        }
        val buttonViews: MutableList<ChatButtonView> = ArrayList()
        for (i in 0 until size) {
            try {
                val jsonButton = buttons.getJSONObject(i)
                val type = jsonButton.optString("type", "")
                val payload = JSONObject(jsonButton.get("payload").toString())
                var postbackText = jsonButton.getString("postback_text")
                if (postbackText.isEmpty() == true) {
                    postbackText = jsonButton.getString("label")
                }
                if ("postback" == type) {
                    val button =
                        ChatButtonView(buttonsContainer.context, jsonButton)
                    button.setChatButtonClickListener(object : ChatButtonClickListener {
                        override fun onChatButtonClick(jsonButton: JSONObject?) {
                            val postBackMessage = QMessage.generatePostBackMessage(
                                comment.chatRoomId,
                                postbackText,
                                payload
                            )
                            sendComment(postBackMessage)
                        }
                    })
                    buttonViews.add(button)
                } else if ("link" == type) {
                    val button =
                        ChatButtonView(buttonsContainer.context, jsonButton)
                    button.setChatButtonClickListener(object : ChatButtonClickListener {
                        override fun onChatButtonClick(jsonButton: JSONObject?) {
                            openLink(
                                jsonButton?.optJSONObject("payload")?.optString("url")!!
                            )
                        }
                    })
                    buttonViews.add(button)
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
        for (i in buttonViews.indices) {
            buttonViews[i].button!!.setTextColor(
                ContextCompat.getColor(
                    Const.qiscusCore()?.getApps()!!,
                    R.color.qiscus_black_mc
                )
            )
            buttonsContainer.addView(buttonViews[i])
        }
        buttonsContainer.visibility = View.VISIBLE
    }

    @SuppressLint("ResourceType")
    private fun openLink(url: String) {
        CustomTabsIntent.Builder()
            .setToolbarColor(
                ContextCompat.getColor(
                    Const.qiscusCore()?.getApps()!!,
                    R.color.qiscus_left_bubble_text_mc
                )
            )
            .setShowTitle(true)
            .addDefaultShareMenuItem()
            .enableUrlBarHiding()
            .build()
            .launchUrl(buttonsContainer.context, Uri.parse(url))
    }

    private fun sendComment(comment: QMessage) {
        MultichannelWidget.instance.component.chatroomRepository.sendComment(
            comment.chatRoomId,
            comment,
            {
                it
                if (context != null) {
                    (context as ChatRoomActivity).updateCommentVH(it)
                }
            },
            {
                it
                if (context != null) {
                    val db = Const.qiscusCore()?.getDataStore()?.getComment(comment.uniqueId)!!
                    (context as ChatRoomActivity).updateCommentVH(db)
                }
            })
    }

    init {
        buttonsContainer = itemView.findViewById(R.id.buttons_container)
        messageContainer = itemView.findViewById(R.id.message)
        textContent = itemView.findViewById(R.id.contents)
        textTime = itemView.findViewById(R.id.date)
    }
}

