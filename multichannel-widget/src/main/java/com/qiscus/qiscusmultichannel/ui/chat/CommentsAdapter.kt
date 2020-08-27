@file:Suppress("DUPLICATE_LABEL_IN_WHEN")

package com.qiscus.qiscusmultichannel.ui.chat

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.qiscus.qiscusmultichannel.R
import com.qiscus.qiscusmultichannel.ui.chat.viewholder.*
import com.qiscus.sdk.chat.core.data.model.QiscusComment
import com.qiscus.sdk.chat.core.util.QiscusAndroidUtil
import com.qiscus.sdk.chat.core.util.QiscusDateUtil

/**
 * Created on : 19/08/19
 * Author     : Taufik Budi S
 * GitHub     : https://github.com/tfkbudi
 */
@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
class CommentsAdapter(val context: Context) :
    SortedRecyclerViewAdapter<QiscusComment, BaseViewHolder>() {

    private var lastDeliveredCommentId: Long = 0
    private var lastReadCommentId: Long = 0
    private var selectedComment: QiscusComment? = null

    override val itemClass: Class<QiscusComment>
        get() = QiscusComment::class.java

    override fun compare(item1: QiscusComment, item2: QiscusComment): Int = when {
        (item2 == item1) -> 0  //Same comments
        (item2.id == -1L && item1.id == -1L) -> item2.time.compareTo(item1.time) //Not completed comments
        (item2.id != -1L && item1.id != -1L) -> QiscusAndroidUtil.compare(
            item2.id,
            item1.id
        ) //Completed comments
        (item2.id == -1L) -> 1
        (item1.id == -1L) -> -1
        else -> item2.time.compareTo(item1.time)
    }

    override fun getItemViewType(position: Int): Int {
        val comment = data.get(position)
        return when (comment.type) {
            QiscusComment.Type.TEXT -> if (comment.isMyComment) TYPE_MY_TEXT else TYPE_OPPONENT_TEXT
            QiscusComment.Type.REPLY -> if (comment.isMyComment) TYPE_MY_REPLY else TYPE_OPPONENT_REPLY
            QiscusComment.Type.VIDEO -> if (comment.isMyComment) TYPE_MY_VIDEO else TYPE_OPPONENT_VIDEO
            QiscusComment.Type.IMAGE -> if (comment.isMyComment) TYPE_MY_IMAGE else TYPE_OPPONENT_IMAGE
            QiscusComment.Type.FILE -> if (comment.isMyComment) TYPE_MY_FILE else TYPE_OPPONENT_FILE
            QiscusComment.Type.LINK -> if (comment.isMyComment) TYPE_MY_TEXT else TYPE_OPPONENT_TEXT
            QiscusComment.Type.SYSTEM_EVENT -> TYPE_EVENT
            QiscusComment.Type.CARD -> TYPE_CARD
            QiscusComment.Type.CAROUSEL -> TYPE_CAROUSEL
            QiscusComment.Type.BUTTONS -> TYPE_BUTTONS
            else -> TYPE_NOT_SUPPORT
        }
    }

    private fun getView(parent: ViewGroup, viewType: Int): View = when (viewType) {
        TYPE_MY_TEXT -> LayoutInflater.from(context)
            .inflate(R.layout.item_my_text_comment_mc, parent, false)
        TYPE_OPPONENT_TEXT -> LayoutInflater.from(context)
            .inflate(R.layout.item_opponent_text_comment_mc, parent, false)
        TYPE_MY_VIDEO -> LayoutInflater.from(context)
            .inflate(R.layout.item_my_video_comment_mc, parent, false)
        TYPE_OPPONENT_VIDEO -> LayoutInflater.from(context)
            .inflate(R.layout.item_opponent_video_comment_mc, parent, false)
        TYPE_MY_IMAGE -> LayoutInflater.from(context)
            .inflate(R.layout.item_my_image_comment_mc, parent, false)
        TYPE_OPPONENT_IMAGE -> LayoutInflater.from(context)
            .inflate(R.layout.item_opponent_image_comment_mc, parent, false)
        TYPE_MY_REPLY -> LayoutInflater.from(context)
            .inflate(R.layout.item_my_reply_mc, parent, false)
        TYPE_OPPONENT_REPLY -> LayoutInflater.from(context)
            .inflate(R.layout.item_opponent_reply_mc, parent, false)
        TYPE_EVENT -> LayoutInflater.from(context)
            .inflate(R.layout.item_event_mc, parent, false)
        TYPE_MY_FILE -> LayoutInflater.from(context)
            .inflate(R.layout.item_my_file_mc, parent, false)
        TYPE_OPPONENT_FILE -> LayoutInflater.from(context)
            .inflate(R.layout.item_opponent_file_mc, parent, false)
        TYPE_CARD -> LayoutInflater.from(context)
            .inflate(R.layout.item_card_mc, parent, false)
        TYPE_CAROUSEL -> LayoutInflater.from(context)
            .inflate(R.layout.item_carousel_mc, parent, false)
        TYPE_BUTTONS -> LayoutInflater.from(context)
            .inflate(R.layout.item_button_mc, parent, false)
        else -> LayoutInflater.from(context)
            .inflate(R.layout.item_message_not_supported_mc, parent, false)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder =
        when (viewType) {
            TYPE_MY_TEXT, TYPE_OPPONENT_TEXT -> TextVH(getView(parent, viewType))
            TYPE_MY_VIDEO, TYPE_OPPONENT_VIDEO -> VideoVH(getView(parent, viewType))
            TYPE_MY_IMAGE, TYPE_OPPONENT_IMAGE -> ImageVH(getView(parent, viewType))
            TYPE_MY_REPLY, TYPE_OPPONENT_REPLY -> ReplyVH(getView(parent, viewType))
            TYPE_EVENT -> EventVH(getView(parent, viewType))
            TYPE_MY_FILE, TYPE_OPPONENT_FILE -> FileVH(getView(parent, viewType))
            TYPE_CARD -> CardVH(getView(parent, viewType))
            TYPE_CAROUSEL -> CarouselVH(getView(parent, viewType))
            else -> NoSupportVH(getView(parent, viewType))
        }

    override fun getItemCount(): Int = data.size()

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.bind(data.get(position))
        holder.pstn = position

        if (data[position].isMyComment
            || (position + 1 < data.size()
                    && data[position].senderEmail == data[position + 1].senderEmail)
        ) {
            holder.setNeedToShowName(null, false)
        } else {
            holder.setNeedToShowName(data[position].senderAvatar, true)
        }

        if (position == data.size() - 1) {
            holder.setNeedToShowFirstIndicator(true)
            holder.setNeedToShowDate(true)
        } else {
            holder.setNeedToShowFirstIndicator(false)
            holder.setNeedToShowDate(
                !QiscusDateUtil.isDateEqualIgnoreTime(
                    data.get(position).time,
                    data.get(position + 1).time
                )
            )
        }

        setOnClickListener(holder.itemView, position)
    }

    override fun addOrUpdate(comments: List<QiscusComment>) {

        for (comment in comments) {
            val index = findPosition(comment)
            if (comment.isDeleted) {
                if (index > -1) {
                    remove(comment)
                }
                return
            }

            if (index == -1) {
                data.add(comment)
            } else {
                data.updateItemAt(index, comment)
            }
        }
        notifyDataSetChanged()
    }

    override fun addOrUpdate(comment: QiscusComment) {
        val index = findPosition(comment)
        if (comment.isDeleted) {
            if (index > -1) {
                remove(comment)
            }
            return
        }

        if (index == -1) {
            data.add(comment)
        } else {
            data.updateItemAt(index, comment)
        }
        notifyDataSetChanged()
    }

    override fun remove(comment: QiscusComment) {
        data.remove(comment)
        notifyDataSetChanged()
    }

    fun setSelectedComment(comment: QiscusComment) = apply { this.selectedComment = comment }

    fun getSelectedComment() = selectedComment

    fun clearSelected() {
        val size = data.size()
        for (i in size - 1 downTo 0) {
            if (data.get(i).isSelected) {
                data.get(i).isSelected = false
            }
        }
        notifyDataSetChanged()
    }

    fun updateLastDeliveredComment(lastDeliveredCommentId: Long) {
        this.lastDeliveredCommentId = lastDeliveredCommentId
        updateCommentState()
        notifyDataSetChanged()
    }

    fun updateLastReadComment(lastReadCommentId: Long) {
        this.lastReadCommentId = lastReadCommentId
        this.lastDeliveredCommentId = lastReadCommentId
        updateCommentState()
        notifyDataSetChanged()
    }

    private fun updateCommentState() {
        val size = data.size()
        for (i in 0 until size) {
            if (data.get(i).state > QiscusComment.STATE_SENDING) {
                if (data.get(i).id <= lastReadCommentId) {
                    if (data.get(i).state == QiscusComment.STATE_READ) {
                        break
                    }
                    data.get(i).state = QiscusComment.STATE_READ
                } else if (data.get(i).id <= lastDeliveredCommentId) {
                    if (data.get(i).state >= QiscusComment.STATE_DELIVERED) {
                        break
                    }
                    data.get(i).state = QiscusComment.STATE_DELIVERED
                }
            }
        }
    }

    fun getLatestSentComment(): QiscusComment? {
        val size = data.size()
        for (i in 0 until size) {
            val comment = data.get(i)
            if (comment.state >= QiscusComment.STATE_ON_QISCUS) {
                return comment
            }
        }
        return null
    }

    interface RecyclerViewItemClickListener {
        fun onItemClick(view: View, position: Int)

        fun onItemLongClick(view: View, position: Int)
    }

    private val TYPE_NOT_SUPPORT = 0
    private val TYPE_MY_TEXT = 1
    private val TYPE_OPPONENT_TEXT = 2
    private val TYPE_MY_IMAGE = 3
    private val TYPE_OPPONENT_IMAGE = 4
    private val TYPE_MY_FILE = 5
    private val TYPE_OPPONENT_FILE = 6
    private val TYPE_MY_REPLY = 7
    private val TYPE_OPPONENT_REPLY = 8
    private val TYPE_EVENT = 9
    private val TYPE_CARD = 10
    private val TYPE_CAROUSEL = 11
    private val TYPE_BUTTONS = 12
    private val TYPE_MY_VIDEO = 13
    private val TYPE_OPPONENT_VIDEO = 14
}