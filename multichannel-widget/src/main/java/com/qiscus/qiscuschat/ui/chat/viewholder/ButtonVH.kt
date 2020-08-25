package com.qiscus.qiscuschat.ui.chat.viewholder

import android.annotation.SuppressLint
import android.net.Uri
import android.view.View
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import com.qiscus.integrations.multichannel_sample.R
import com.qiscus.qiscuschat.MultichannelWidget
import com.qiscus.qiscuschat.ui.view.ButtonView
import com.qiscus.sdk.chat.core.QiscusCore
import com.qiscus.sdk.chat.core.data.model.QiscusComment
import kotlinx.android.synthetic.main.item_button_mc.view.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class ButtonVH(itemView: View) : BaseViewHolder(itemView) {

    override fun bind(comment: QiscusComment) {
        super.bind(comment)
        try {
            setUpButtons(comment.roomId, JSONObject(comment.extraPayload).getJSONArray("buttons"))
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        itemView.tv_contents.text = comment.message
        itemView.tv_time.text = comment.time.toString()
    }

    @SuppressLint("NewApi")
    private fun setUpButtons(roomId: Long, buttons: JSONArray) {
        itemView.buttons_container.removeAllViews()
        val size = buttons.length()
        if (size < 1) {
            return
        }
        val buttonViews: MutableList<ButtonView> = ArrayList()
        for (i in 0 until size) {
            try {
                val jsonButton = buttons.getJSONObject(i)
                val type = jsonButton.optString("type", "")
                if ("postback" == type) {
                    val button = ButtonView(itemView.buttons_container.context, jsonButton)
                    button.setChatButtonClickListener {
                        try {
                            val payload = JSONObject(it.optString("payload"))
                            val postBack = it.optString("postback_text")
                            sendComment(
                                QiscusComment.generatePostBackMessage(
                                    roomId,
                                    postBack,
                                    payload.toString()
                                )
                            )
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }
                    buttonViews.add(button)
                } else if ("link" == type) {
                    val button = ButtonView(itemView.buttons_container.context, jsonButton)
                    button.setChatButtonClickListener { jsonButton1 ->
                        openLink(jsonButton1.optJSONObject("payload").optString("url"))
                    }
                    buttonViews.add(button)
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }

        for (i in buttonViews.indices) {
            buttonViews[i].button.setTextColor(
                ContextCompat.getColor(
                    QiscusCore.getApps(),
                    R.color.qiscus_left_bubble_text_mc
                )
            )
            if (i == buttonViews.size - 1) {
                buttonViews[i].button.background =
                    itemView.context.getDrawable(R.drawable.qiscus_rounded_left_chat_bg_mc)
            } else {
                buttonViews[i].button.setBackgroundColor(
                    ContextCompat.getColor(
                        QiscusCore.getApps(),
                        R.color.qiscus_left_bubble_mc
                    )
                )
            }
            itemView.buttons_container.addView(buttonViews[i])
        }
        itemView.buttons_container.visibility = View.VISIBLE
    }

    private fun sendComment(comment: QiscusComment) {
        MultichannelWidget.instance.component.chatroomRepository.sendComment(
            comment.roomId,
            comment,
            {
                it
            },
            {
                it
            })
    }

    private fun openLink(url: String) {
        CustomTabsIntent.Builder()
            .setToolbarColor(ContextCompat.getColor(QiscusCore.getApps(), R.color.colorPrimary))
            .setShowTitle(true)
            .addDefaultShareMenuItem()
            .enableUrlBarHiding()
            .build()
            .launchUrl(itemView.buttons_container.context, Uri.parse(url))
    }
}