package com.qiscus.qiscusmultichannel.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.qiscus.qiscusmultichannel.MultichannelWidget
import com.qiscus.qiscusmultichannel.ui.chat.ChatRoomActivity
import com.qiscus.sdk.chat.core.data.model.QiscusChatRoom
import com.qiscus.sdk.chat.core.data.model.QiscusComment

/**
 * @author Yuana andhikayuana@gmail.com
 * @since Aug, Tue 14 2018 12.40
 **/
class NotificationClickReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val qiscusComment: QiscusComment = intent.getParcelableExtra("data")
        QiscusChatLocal.setRoomId(qiscusComment.roomId)
//        MultichannelWidget.instance.openChatRoomMultichannel()
        MultichannelWidget.instance.openChatRoomById(qiscusComment.roomId,
            { generateIntent(context, it) }, { it.printStackTrace() })
    }

    private fun generateIntent(context: Context, qiscusChatRoom: QiscusChatRoom) {
        val intent = Intent(context, ChatRoomActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.putExtra(ChatRoomActivity.CHATROOM_KEY, qiscusChatRoom)
        context.startActivity(intent)
    }
}