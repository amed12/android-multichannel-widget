package com.qiscus.qiscusmultichannel.data.repository.impl

import com.qiscus.qiscusmultichannel.MultichannelWidget
import com.qiscus.qiscusmultichannel.MultichannelWidgetConfig
import com.qiscus.qiscusmultichannel.data.model.DataInitialChat
import com.qiscus.qiscusmultichannel.data.model.UserProperties
import com.qiscus.qiscusmultichannel.data.repository.ChatroomRepository
import com.qiscus.qiscusmultichannel.data.repository.response.ResponseInitiateChat
import com.qiscus.qiscusmultichannel.util.Const
import com.qiscus.sdk.chat.core.QiscusCore
import com.qiscus.sdk.chat.core.QiscusCore.OnSendMessageListener
import com.qiscus.sdk.chat.core.data.model.QMessage
import com.qiscus.sdk.chat.core.data.remote.QiscusApi
import com.qiscus.sdk.chat.core.data.remote.QiscusPusherApi
import org.json.JSONObject
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

/**
 * Created on : 05/08/19
 * Author     : Taufik Budi S
 * GitHub     : https://github.com/tfkbudi
 */
class ChatroomRepositoryImpl : ChatroomRepository {

    fun sendComment(
        roomId: Long,
        message: QMessage,
        onSuccess: (QMessage) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        Const.qiscusCore()!!.sendMessage(message, object : OnSendMessageListener {
            override fun onSending(qiscusComment: QMessage) {
                Const.qiscusCore()!!.dataStore.addOrUpdate(qiscusComment)
            }

            override fun onSuccess(qiscusComment: QMessage) {
                if (qiscusComment.chatRoomId == roomId) {
                    onSuccess(qiscusComment)
                }
            }

            override fun onFailed(t: Throwable, qiscusComment: QMessage) {
                t.printStackTrace()
                if (qiscusComment.chatRoomId == roomId) {
                    onError(t)
                }
            }
        })
    }

    fun publishCustomEvent(
        roomId: Long,
        data: JSONObject
    ) {
        Const.qiscusCore()?.pusherApi?.publishCustomEvent(roomId, data)
    }

    fun subscribeCustomEvent(
        roomId: Long
    ) {
        Const.qiscusCore()?.pusherApi?.subsribeCustomEvent(roomId)
    }

    fun initiateChat(
        name: String,
        userId: String,
        avatar: String?,
        extras: String,
        userProp: List<UserProperties>,
        responseInitiateChat: (ResponseInitiateChat) -> Unit,
        onError: (Throwable) -> Unit
    ) {


        val appID = Const.qiscusCore()?.appId

        Const.qiscusCore()?.api?.jwtNonce
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({
                MultichannelWidget.instance.component.qiscusChatRepository.initiateChat(
                    DataInitialChat(
                        appID.toString(),
                        userId,
                        name,
                        avatar,
                        it.nonce,
                        null,
                        extras,
                        userProp
                    ), {
                        it.data.isSessional?.let {
                            MultichannelWidgetConfig.setSessional(true)
                        }
                        responseInitiateChat(it)
                    }, {
                        onError(it)
                    })
            }, {
                onError(it)
            })
    }
}