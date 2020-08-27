package com.qiscus.qiscusmultichannel.ui.chat

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.request.RequestOptions
import com.qiscus.nirmana.Nirmana
import com.qiscus.qiscusmultichannel.MultichannelWidget
import com.qiscus.qiscusmultichannel.R
import com.qiscus.sdk.chat.core.QiscusCore
import com.qiscus.sdk.chat.core.data.model.QiscusChatRoom
import com.qiscus.sdk.chat.core.data.model.QiscusComment
import com.qiscus.sdk.chat.core.data.model.QiscusComment.Type
import com.qiscus.sdk.chat.core.data.model.QiscusComment.Type.*
import com.qiscus.sdk.chat.core.data.remote.QiscusApi
import com.qiscus.sdk.chat.core.data.remote.QiscusPusherApi
import com.qiscus.sdk.chat.core.event.QiscusCommentReceivedEvent
import com.qiscus.sdk.chat.core.event.QiscusUserStatusEvent
import com.qiscus.sdk.chat.core.util.QiscusAndroidUtil
import com.qiscus.sdk.chat.core.util.QiscusDateUtil
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.activity_chat_room_mc.*
import kotlinx.android.synthetic.main.toolbar_menu_selected_comment_mc.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers


class ChatRoomActivity : AppCompatActivity(), ChatRoomFragment.CommentSelectedListener,
    ChatRoomFragment.OnUserTypingListener {

    lateinit var qiscusChatRoom: QiscusChatRoom
    private val users: MutableSet<String> = HashSet()
    private var memberList: String = ""
    private var isSessional = false

    companion object {
        var IS_ACTIVE = false
        val CHATROOM_KEY = "chatroom_key"

        fun generateIntent(
            context: Context,
            qiscusChatRoom: QiscusChatRoom
        ): Intent {

            val intent = Intent(context, ChatRoomActivity::class.java)
//            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.putExtra(CHATROOM_KEY, qiscusChatRoom)
            context.startActivity(intent)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_room_mc)
        /* setSupportActionBar(toolbar_selected_comment)
          supportActionBar?.title = qiscusChatRoom.name
          toolbar.setNavigationIcon(R.drawable.ic_back)
          toolbar.setNavigationOnClickListener { finish() }
         */

        qiscusChatRoom = intent.getParcelableExtra(CHATROOM_KEY)

        if (!this::qiscusChatRoom.isInitialized) {
            finish()
            return
        }

        btn_back.setOnClickListener { finish() }

        MultichannelWidget.instance.component.chatroomRepository.getSession(
            QiscusCore.getAppId(),
            {
                isSessional = it
                supportFragmentManager.beginTransaction()
                    .replace(
                        R.id.fragmentContainer,
                        ChatRoomFragment.newInstance(qiscusChatRoom, it),
                        ChatRoomFragment::class.java.name
                    )
                    .commit()
            }) { error(it) }


        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        btn_action_copy.setOnClickListener { getChatFragment().copyComment() }
        btn_action_delete.setOnClickListener { getChatFragment().deleteComment() }
        btn_action_reply.setOnClickListener { getChatFragment().replyComment() }
        btn_action_reply_cancle.setOnClickListener { getChatFragment().clearSelectedComment() }
        setBarInfo()

//        tvTitle.text = qiscusChatRoom.name
    }

    override fun onResume() {
        super.onResume()
        IS_ACTIVE = true
        bindRoomData()
    }

    private fun getChatFragment(): ChatRoomFragment {
        return supportFragmentManager.findFragmentByTag(ChatRoomFragment::class.java.name) as ChatRoomFragment
    }

    override fun onCommentSelected(selectedComment: QiscusComment) {
        if (toolbar_selected_comment.visibility == View.VISIBLE
            || (qiscusChatRoom.options.optBoolean("is_resolved")
                    && isSessional)
        ) {
            toolbar_selected_comment.visibility = View.GONE
            getChatFragment().clearSelectedComment()
        } else {
            btn_action_delete.visibility =
                if (selectedComment.isMyComment) View.VISIBLE else View.GONE

            val caption = selectedComment.caption != ""
            btn_action_copy.visibility = when {
                selectedComment.type == TEXT || selectedComment.type == REPLY -> View.VISIBLE
                selectedComment.type == FILE && caption -> View.VISIBLE
                selectedComment.type == IMAGE && caption -> View.VISIBLE
                selectedComment.type == VIDEO && caption -> View.VISIBLE
                else -> View.GONE
            }

            toolbar_selected_comment.visibility = View.VISIBLE
        }
    }

    override fun onClearSelectedComment(status: Boolean) {
        toolbar_selected_comment.visibility = View.INVISIBLE
    }

    override fun onUserTyping(email: String?, isTyping: Boolean) {
        tvMemberList?.let {
            it.text = if (isTyping) {
                QiscusAndroidUtil.runOnUIThread({ it.text = memberList }, 5000)
                "typing..."
            } else {
                memberList
            }
        }
    }

    private fun bindRoomData() {
        MultichannelWidget.instance.component.chatroomRepository.getChatWidgetConfig(
            QiscusCore.getAppId(),
            {
                Nirmana.getInstance().get()
                    .load(it.customerServiceAvatar)
                    .apply(
                        RequestOptions()
                            .dontAnimate()
                            .placeholder(R.drawable.ic_default_avatar)
                            .error(R.drawable.ic_default_avatar)
                    )
                    .into(avatar)

                tvTitle.text = it.customerServiceName
            }, { error(it) }
        )

        for (member in qiscusChatRoom.member) {
            if (member.email != MultichannelWidget.instance.getQiscusAccount().email) {
                users.add(member.email)
                QiscusPusherApi.getInstance().subscribeUserOnlinePresence(member.email)
            }
        }
    }

    private fun setBarInfo() {
        val listMember: ArrayList<String> = arrayListOf()
        QiscusApi.getInstance().getChatRoomInfo(qiscusChatRoom.id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { chatRoom ->
                chatRoom.member.forEach {
                    listMember.add(it.username)
                }
                this.memberList = listMember.joinToString()
                tvMemberList.text = memberList
            }
    }

    @Subscribe
    fun onUserStatusChanged(event: QiscusUserStatusEvent) {
        val last = QiscusDateUtil.getRelativeTimeDiff(event.lastActive)
        if (users.contains(event.user)) {
            //tvSubtitle?.text = if (event.isOnline) "Online" else "Last seen $last"
        }
    }

    @Subscribe
    fun onMessageReceived(event: QiscusCommentReceivedEvent) {
        when (event.qiscusComment.type) {
            Type.SYSTEM_EVENT -> setBarInfo()
        }

    }

    override fun onStop() {
        super.onStop()
        IS_ACTIVE = false
    }

    override fun onDestroy() {
        super.onDestroy()
        for (user in users) {
            QiscusPusherApi.getInstance().unsubscribeUserOnlinePresence(user)
        }
        EventBus.getDefault().unregister(this)
        clearFindViewByIdCache()
    }

    @Subscribe
    fun onCommentReceivedEvent(event: QiscusCommentReceivedEvent) {
        if (event.qiscusComment.roomId == qiscusChatRoom.id) {
            if (event.qiscusComment.type == SYSTEM_EVENT) {
                QiscusApi.getInstance().getChatRoomInfo(qiscusChatRoom.id)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { chatRoom ->
                        chatRoom.options.getBoolean("is_resolved").let {
                            MultichannelWidget.instance.component.chatroomRepository.getSession(
                                QiscusCore.getAppId(),
                                {
                                    isSessional = it
                                }) { error(it) }
                        }
                    }
            }
        }
    }
}
