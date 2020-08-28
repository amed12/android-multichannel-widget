package com.qiscus.qiscusmultichannel.ui.chat

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.*
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.annotation.NonNull
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.qiscus.jupuk.JupukBuilder
import com.qiscus.jupuk.JupukConst
import com.qiscus.nirmana.Nirmana
import com.qiscus.qiscusmultichannel.MultichannelWidget
import com.qiscus.qiscusmultichannel.R
import com.qiscus.qiscusmultichannel.ui.chat.image.SendImageConfirmationActivity
import com.qiscus.qiscusmultichannel.ui.loading.LoadingActivity
import com.qiscus.qiscusmultichannel.ui.view.QiscusChatScrollListener
import com.qiscus.qiscusmultichannel.ui.webView.WebViewHelper
import com.qiscus.qiscusmultichannel.util.*
import com.qiscus.sdk.chat.core.QiscusCore
import com.qiscus.sdk.chat.core.data.local.QiscusCacheManager
import com.qiscus.sdk.chat.core.data.model.QiscusChatRoom
import com.qiscus.sdk.chat.core.data.model.QiscusComment
import com.qiscus.sdk.chat.core.data.model.QiscusComment.Type.*
import com.qiscus.sdk.chat.core.data.model.QiscusPhoto
import com.qiscus.sdk.chat.core.data.remote.QiscusPusherApi
import com.qiscus.sdk.chat.core.util.QiscusAndroidUtil
import com.qiscus.sdk.chat.core.util.QiscusFileUtil
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.fragment_chat_room_mc.*
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.util.*

/**
 * Created on : 16/08/19
 * Author     : Taufik Budi S
 * GitHub     : https://github.com/tfkbudi
 */
class ChatRoomFragment : Fragment(), QiscusChatScrollListener.Listener,
    ChatRoomPresenter.ChatRoomView, QiscusPermissionsUtil.PermissionCallbacks,
    DialogDeleteMessage.OnDeleteSelectedMessage {

    private val CAMERA_PERMISSION = arrayOf(
        "android.permission.CAMERA",
        "android.permission.WRITE_EXTERNAL_STORAGE",
        "android.permission.READ_EXTERNAL_STORAGE"
    )
    private val FILE_PERMISSION = arrayOf(
        "android.permission.WRITE_EXTERNAL_STORAGE",
        "android.permission.READ_EXTERNAL_STORAGE"
    )
    protected val TAKE_PICTURE_REQUEST = 3
    protected val RC_CAMERA_PERMISSION = 128
    private val RC_FILE_PERMISSION = 130
    protected val SEND_PICTURE_CONFIRMATION_REQUEST = 4
    private val IMAGE_GALLERY_REQUEST = 7
    protected val GET_TEMPLATE = 5
    private lateinit var ctx: Context
    private lateinit var commentsAdapter: CommentsAdapter
    private var qiscusChatRoom: QiscusChatRoom? = null
    private lateinit var presenter: ChatRoomPresenter
    private var commentSelectedListener: CommentSelectedListener? = null
    private var userTypingListener: OnUserTypingListener? = null
    private var selectedComment: QiscusComment? = null
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var rvMessage: RecyclerView
    private var newMessageUnread: ArrayList<QiscusComment> = ArrayList()
    private var isTyping = false
    private var isSessional = false

    companion object {
        const val CHATROOM_KEY = "chatroom_key"
        const val SESSIONA_KEY = "session_key"

        fun newInstance(qiscusChatRoom: QiscusChatRoom, isSessional: Boolean): ChatRoomFragment {
            val chatRoomFragment = ChatRoomFragment()
            val bundle = Bundle()
            bundle.putParcelable(CHATROOM_KEY, qiscusChatRoom)
            bundle.putBoolean(SESSIONA_KEY, isSessional)
            chatRoomFragment.arguments = bundle
            return chatRoomFragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_chat_room_mc, container, false)
        rvMessage = view.findViewById(R.id.rvMessage) as RecyclerView
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initRecyclerMessage()

        arguments?.let {
            qiscusChatRoom = it.getParcelable(CHATROOM_KEY)
            isSessional = it.getBoolean(SESSIONA_KEY)
        }

        if (qiscusChatRoom == null) {
            throw RuntimeException("please provide qiscus chat room")
        }

        setViewEnabled(btnSend, false)
        changeImageSendTint(btnSend, R.color.qiscus_grey_mc)

        btnSend.setOnClickListener {
            tvFirstIndicator.visibility = View.GONE
            sendingComment()
        }

        btn_new_room.setOnClickListener {
            generateNewChatRoom()
        }
        btn_new_message.setOnClickListener {
            rvMessage.smoothScrollToPosition(0)
            showNewMessageButton(null)
        }
        btnCancelReply.setOnClickListener { rootViewSender.visibility = View.GONE }

        qiscusChatRoom?.let {
            presenter = ChatRoomPresenter(it)
            presenter.attachView(this)
            presenter.loadComments(20)

//            create a new chat room after resolved
            if (isSessional) {
                showNewChatButton(it.options.getBoolean("is_resolved"))
            }
        }

        btnAttachmentOptions.setOnClickListener { showAttachmentDialog() }

        etMessage.afterTextChangedDelayed({
            notifyServerTyping(true)
        }, {
            when {
                it.isNotEmpty() -> {
                    setViewEnabled(btnSend, true)
                    changeImageSendTint(btnSend, R.color.colorPrimary)
                }
                else -> {
                    setViewEnabled(btnSend, false)
                    changeImageSendTint(btnSend, R.color.qiscus_grey_mc)
                }
            }
        }, {
            notifyServerTyping(false)
        })
        requestFilePermission()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (context is ChatRoomActivity) {
            ctx = context
        }

        if (activity is CommentSelectedListener) {
            commentSelectedListener = activity as CommentSelectedListener
            userTypingListener = activity as OnUserTypingListener
        }
    }

    override fun onResume() {
        super.onResume()
        QiscusCacheManager.getInstance().setLastChatActivity(true, qiscusChatRoom!!.id)
        QiscusPusherApi.getInstance().subscribeChatRoom(qiscusChatRoom)
        notifyLatestRead()
    }

    override fun onPause() {
        super.onPause()
        QiscusPusherApi.getInstance().unsubsribeChatRoom(qiscusChatRoom)
        QiscusCacheManager.getInstance().setLastChatActivity(false, qiscusChatRoom!!.id)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        notifyLatestRead()
        presenter.detachView()
    }

    private fun initRecyclerMessage() {
        layoutManager = LinearLayoutManager(ctx)
        layoutManager.reverseLayout = true
        layoutManager.stackFromEnd = true
        rvMessage.layoutManager = layoutManager
        rvMessage.setHasFixedSize(true)
        rvMessage.addOnScrollListener(QiscusChatScrollListener(layoutManager, this))
        commentsAdapter = CommentsAdapter(ctx)
        rvMessage.adapter = commentsAdapter

        commentsAdapter.setOnItemClickListener(object :
            CommentsAdapter.RecyclerViewItemClickListener {
            override fun onItemClick(view: View, position: Int) {
                handleItemClick(commentsAdapter.data[position])
            }

            override fun onItemLongClick(view: View, position: Int) {
                toggleSelectedComment(commentsAdapter.data[position])
            }

        })
    }

    private fun handleItemClick(comment: QiscusComment) {
        if (!comment.isSelected) {
            when (comment.type) {
                FILE -> {
                    val obj = JSONObject(comment.extraPayload)
                    val url = obj.getString("url")
                    val fileName = obj.getString("file_name")
                    presenter.downloadFile(comment, url, fileName)
                }
                REPLY -> {
                    val position: Int = commentsAdapter.findPosition(comment.replyTo)
                    if (position >= 0) {
                        rvMessage.scrollToPosition(position)
                        highlightComment(commentsAdapter.data[position])
                    }
                }
                else -> {
                    // do nothing
                }
            }
        }
        clearSelectedComment()
    }

    private fun highlightComment(comment: QiscusComment) {
        comment.isSelected = true
        commentsAdapter.addOrUpdate(comment)
        commentsAdapter.setSelectedComment(comment)

        QiscusAndroidUtil.runOnUIThread({ clearSelectedComment() }, 2000)
    }

    private fun sendingComment() {
        if (!TextUtils.isEmpty(etMessage.text)) {
            if (rootViewSender.isVisible) {
                selectedComment?.let {
                    presenter.sendReplyComment(etMessage.text.toString(), it)
                }
                rootViewSender.visibility = View.GONE
                selectedComment = null
            } else {
                sendComment(etMessage.text.toString())
            }

            etMessage.setText("")
        }
    }

    private fun openCamera() {
        if (QiscusPermissionsUtil.hasPermissions(ctx, CAMERA_PERMISSION)) {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (intent.resolveActivity(ctx.packageManager) != null) {
                var photoFile: File? = null
                try {
                    photoFile = QiscusImageUtil.createImageFile()
                } catch (ex: IOException) {
                    ctx.showToast(getString(R.string.qiscus_chat_error_failed_write_mc))
                }

                if (photoFile != null) {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile))
                    } else {
                        intent.putExtra(
                            MediaStore.EXTRA_OUTPUT,
                            FileProvider.getUriForFile(
                                ctx,
                                QiscusCore.getApps().packageName,
                                photoFile
                            )
                        )
                    }
                    startActivityForResult(intent, TAKE_PICTURE_REQUEST)
                }

            }
        } else {
            requestCameraPermission()
        }
    }

    private fun pickImage() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
//        intent.type = "image/*"
        intent.type = "*/*"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            intent.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*", "video/*"))
        }
        startActivityForResult(intent, IMAGE_GALLERY_REQUEST)
    }

    private fun openGallery() {
        if (QiscusPermissionsUtil.hasPermissions(ctx, FILE_PERMISSION)) {
            pickImage()
        } else {
            requestFilePermission()
        }
    }

    private fun openFile() {
        if (QiscusPermissionsUtil.hasPermissions(ctx, FILE_PERMISSION)) {

            JupukBuilder().setMaxCount(1)
                .setColorPrimary(ContextCompat.getColor(ctx, R.color.colorPrimary))
                .setColorPrimaryDark(ContextCompat.getColor(ctx, R.color.colorPrimaryDark))
                .setColorAccent(ContextCompat.getColor(ctx, R.color.colorAccent))
                .pickDoc(this)
        } else {
            requestFilePermission()
        }
    }

    private fun requestCameraPermission() {
        if (!QiscusPermissionsUtil.hasPermissions(ctx, CAMERA_PERMISSION)) {
            QiscusPermissionsUtil.requestPermissions(
                this, getString(R.string.qiscus_permission_request_title_mc),
                RC_CAMERA_PERMISSION, CAMERA_PERMISSION
            )
        }
    }

    private fun requestFilePermission() {
        if (!QiscusPermissionsUtil.hasPermissions(ctx, FILE_PERMISSION)) {
            QiscusPermissionsUtil.requestPermissions(
                this, getString(R.string.qiscus_permission_request_title_mc),
                RC_FILE_PERMISSION, FILE_PERMISSION
            )
        }
    }


    private fun bindReplyView(origin: QiscusComment) {
        originSender.text = origin.sender
        when (origin.type) {
            IMAGE -> {
                originImage.visibility = View.VISIBLE
                Nirmana.getInstance().get()
                    .load(origin.attachmentUri)
                    .into(originImage)
                originContent.text = origin.caption
            }
            VIDEO -> {
                originImage.visibility = View.VISIBLE
                Nirmana.getInstance().get()
                    .load(origin.attachmentUri)
                    .into(originImage)
                originContent.text = origin.caption
            }
            FILE -> {
                originContent.text = origin.attachmentName
                originImage.visibility = View.GONE
            }
            else -> {
                originImage.visibility = View.GONE
                originContent.text = origin.message
            }
        }
    }

    fun toggleSelectedComment(comment: QiscusComment) {
        if (comment.type != SYSTEM_EVENT && comment.type != BUTTONS
            && comment.type != CARD && comment.type != CAROUSEL
        ) {
            comment.isSelected = true
            commentsAdapter.addOrUpdate(comment)
            commentsAdapter.setSelectedComment(comment)
            commentSelectedListener?.onCommentSelected(comment)
        }
    }

    fun clearSelectedComment() {
        commentSelectedListener?.onClearSelectedComment(true)
        commentsAdapter.clearSelected()
    }

    private fun sendComment(message: String) {
        clearSelectedComment()
        presenter.sendComment(message)
    }

    fun deleteComment() {
        activity?.let { DialogDeleteMessage(it, this).show() }
    }

    fun replyComment() {
        clearSelectedComment()
        selectedComment = commentsAdapter.getSelectedComment()
        rootViewSender.visibility = if (selectedComment == null) View.GONE else View.VISIBLE
        selectedComment?.let { bindReplyView(it) }
    }

    fun copyComment() {
        clearSelectedComment()
        commentsAdapter.getSelectedComment()?.let {
            val textCopied = when (it.type) {
                FILE -> it.attachmentName
                IMAGE -> it.caption
                CARD -> {
                    val title = JSONObject(it.extraPayload).getString("title")
                    val description = JSONObject(it.extraPayload).getString("description")
                    title + "\n" + description
                }
                else -> it.message
            }
            val clipboard = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(
                getString(R.string.qiscus_chat_activity_label_clipboard_mc),
                textCopied
            )
            clipboard.primaryClip = clip

            ctx.showToast(getString(R.string.qiscus_copied_message_mc))
        }

    }

    override fun onTopOffListMessage() {
        loadMoreComments()
    }

    override fun onMiddleOffListMessage() {
    }

    override fun onBottomOffListMessage() {
        newMessageUnread.clear()
        newMessagePanel.visibility = View.GONE
    }

    private fun loadMoreComments() {
        if (progressBar.visibility == View.GONE && commentsAdapter.itemCount > 0) {
            val comment = commentsAdapter.data.get(commentsAdapter.itemCount - 1)
            if (comment.id == -1L || comment.commentBeforeId > 0) {
                presenter.loadOlderCommentThan(comment)
            }
        }
    }

    override fun initRoomData(comments: List<QiscusComment>, qiscusChatRoom: QiscusChatRoom) {
        this.qiscusChatRoom = qiscusChatRoom
        commentsAdapter.addOrUpdate(comments)
        tvFirstIndicator.visibility =
            if (commentsAdapter.data.size() > 0) View.GONE else View.VISIBLE
        rvMessage.scrollToPosition(0)
    }

    override fun onLoadMoreComments(comments: List<QiscusComment>) {
        commentsAdapter.addOrUpdate(comments)
    }

    override fun onSuccessSendComment(comment: QiscusComment) {
        commentsAdapter.addOrUpdate(comment)
        rvMessage.smoothScrollToPosition(0)
    }

    override fun onFailedSendComment(comment: QiscusComment) {
        commentsAdapter.addOrUpdate(comment)
    }

    override fun onNewComment(comment: QiscusComment) {
        tvFirstIndicator.visibility = View.GONE
        commentsAdapter.addOrUpdate(comment)
        if ((rvMessage.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition() <= 2) {
            rvMessage.smoothScrollToPosition(0)
        }
        showNewMessageButton(comment)
    }

    override fun showLoading() {
        progressBar.visibility = View.VISIBLE
    }

    override fun dismissLoading() {
        progressBar.visibility = View.GONE
    }

    override fun onCommentDeleted(comment: QiscusComment) {
        commentsAdapter.remove(comment)
    }

    override fun onSendingComment(comment: QiscusComment) {
        commentsAdapter.addOrUpdate(comment)
        rvMessage.smoothScrollToPosition(0)
    }

    override fun updateLastDeliveredComment(lastDeliveredCommentId: Long) {
        commentsAdapter.updateLastDeliveredComment(lastDeliveredCommentId)
    }

    override fun updateLastReadComment(lastReadCommentId: Long) {
        commentsAdapter.updateLastReadComment(lastReadCommentId)
    }

    override fun updateComment(comment: QiscusComment) {
        commentsAdapter.addOrUpdate(comment)
    }

    override fun onUserTyping(email: String?, isTyping: Boolean) {
        userTypingListener?.onUserTyping(email, isTyping)
    }

    override fun onFileDownloaded(file: File, mimeType: String?) {
        val intent = Intent(Intent.ACTION_VIEW)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            intent.setDataAndType(Uri.fromFile(file), mimeType)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        } else {
            intent.setDataAndType(
                FileProvider.getUriForFile(
                    ctx,
                    QiscusCore.getApps().packageName,
                    file
                ), mimeType
            )
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            showError(getString(R.string.qiscus_chat_error_failed_open_file))
        }

    }

    override fun showNewChatButton(it: Boolean) {
        if (it) {
            MultichannelWidget.instance.component.chatroomRepository.getSession(
                QiscusCore.getAppId(),
                {
                    isSessional = it
                    if (it) {
                        newChatPanel.visibility = View.VISIBLE
                        messageInputPanel.visibility = View.GONE
                    } else {
                        newChatPanel.visibility = View.GONE
                        messageInputPanel.visibility = View.VISIBLE
                    }
                }) { error(it) }

        } else {
            newChatPanel.visibility = View.GONE
            messageInputPanel.visibility = View.VISIBLE
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showNewMessageButton(comment: QiscusComment?) {
        comment?.let {
            var isAdd = true
            if (it.isMyComment) {
                rvMessage.smoothScrollToPosition(0)
                isAdd = false
            } else {
                for (commentUnread in newMessageUnread)
                    if (commentUnread.id == it.id) isAdd = false
            }
            if (isAdd) newMessageUnread.add(it)
        }

        val count = newMessageUnread.size
        if (count > 0 && layoutManager.findFirstVisibleItemPosition() > 1) {
            btn_new_message.text = "$count New Message"
            newMessagePanel.visibility = View.VISIBLE
        } else {
            newMessagePanel.visibility = View.GONE
        }
    }

    override fun refreshComments() {
        dismissLoading()
        MultichannelWidget.instance.openChatRoomMultichannel()
        activity?.finish()
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {

    }

    private fun notifyLatestRead() {
        val qiscusComment = commentsAdapter.getLatestSentComment()
        if (qiscusComment != null && qiscusChatRoom != null) {
            QiscusPusherApi.getInstance()
                .markAsRead(qiscusChatRoom!!.id, qiscusComment.id)
        }
    }

    private fun notifyServerTyping(typing: Boolean) {
        if (isTyping != typing) {
            QiscusPusherApi.getInstance().publishTyping(qiscusChatRoom!!.id, typing)
            isTyping = typing
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == TAKE_PICTURE_REQUEST && resultCode == Activity.RESULT_OK) {
            try {
                val imageFile =
                    QiscusFileUtil.from(Uri.parse(QiscusCacheManager.getInstance().lastImagePath))
                val qiscusPhoto = QiscusPhoto(imageFile)

                val intent =
                    SendImageConfirmationActivity.generateIntent(ctx, qiscusChatRoom!!, qiscusPhoto)
                startActivityForResult(intent, SEND_PICTURE_CONFIRMATION_REQUEST)

            } catch (e: Exception) {
                ctx.showToast(getString(R.string.qiscus_chat_error_failed_read_picture_mc))
                e.printStackTrace()
            }

        } else if (requestCode == SEND_PICTURE_CONFIRMATION_REQUEST && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                showError(getString(R.string.qiscus_chat_error_failed_open_picture_mc))
                return
            }

            val caption = data.getStringExtra(SendImageConfirmationActivity.EXTRA_CAPTIONS)
            val qiscusPhoto =
                data.getParcelableExtra<QiscusPhoto>(SendImageConfirmationActivity.EXTRA_PHOTOS)
            if (qiscusPhoto != null) {
                presenter.sendFile(qiscusPhoto.photoFile, caption)
            } else {
                showError(getString(R.string.qiscus_chat_error_failed_read_picture_mc))
            }
        } else if (requestCode == GET_TEMPLATE && resultCode == Activity.RESULT_OK) {
            data?.let {
                val template = it.getStringExtra("template")
                sendComment(template)
            }
        } else if (requestCode == IMAGE_GALLERY_REQUEST && resultCode == Activity.RESULT_OK) {
            try {
                val imageFile = QiscusFileUtil.from(data?.data!!)
                val qiscusPhoto = QiscusPhoto(imageFile)
                startActivityForResult(
                    SendImageConfirmationActivity.generateIntent(
                        ctx,
                        qiscusChatRoom!!, qiscusPhoto
                    ),
                    SEND_PICTURE_CONFIRMATION_REQUEST
                )
            } catch (e: Exception) {
                showError("Failed to open image file!")
            }
        } else if (requestCode == JupukConst.REQUEST_CODE_DOC) {
            val paths = data?.getStringArrayListExtra(JupukConst.KEY_SELECTED_DOCS)
            if (paths != null && paths.isNotEmpty()) {
                presenter.sendFile(File(paths[0]))
            }
        }

    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        QiscusPermissionsUtil.checkDeniedPermissionsNeverAskAgain(
            this, getString(R.string.qiscus_permission_message_mc),
            R.string.qiscus_grant_mc, R.string.qiscus_denny_mc, perms
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        @NonNull permissions: Array<String>,
        @NonNull grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        QiscusPermissionsUtil.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults,
            this
        )
    }

    @SuppressLint("InflateParams")
    private fun showAttachmentDialog() {
        val dialogView = layoutInflater.inflate(R.layout.bottom_sheet_attachment_mc, null)
        val dialog = BottomSheetDialog(ctx, R.style.AppBottomSheetDialogTheme)
        dialog.setContentView(dialogView)
        dialog.show()

        val btnTakePhoto = dialogView.findViewById(R.id.linTakePhoto) as LinearLayout
        val btnImageGalery = dialogView.findViewById(R.id.linImageGallery) as LinearLayout
        val btnDocument = dialogView.findViewById(R.id.linDocument) as LinearLayout
        btnTakePhoto.visibility = View.GONE
        btnImageGalery.setOnClickListener {
            dialog.dismiss()
            selectImage()
        }

        btnTakePhoto.setOnClickListener {
            dialog.dismiss()
            openCamera()
        }

        btnDocument.setOnClickListener {
            dialog.dismiss()
            openFile()
        }
    }

    private fun selectImage() {

        val builder = AlertDialog.Builder(context)
        val items = arrayOf<CharSequence>(
            "Take Photo", "Choose from Library",
            "Cancel"
        )
        builder.setItems(items) { _, which ->
            when (which) {
                0 -> openCamera()
                1 -> openGallery()
            }
        }
        builder.show()
    }

    override fun showError(message: String) {
        ctx.showToast(message)
    }

    override fun openWebview(url: String) {
        WebViewHelper.launchUrl(ctx, Uri.parse(url))
    }

    override fun onDestroy() {
        super.onDestroy()
        QiscusCacheManager.getInstance().setLastChatActivity(false, 0)
        presenter.detachView()
        clearFindViewByIdCache()
    }

    private fun setViewEnabled(view: View, enabled: Boolean) {
        view.isEnabled = enabled
        view.isClickable = enabled
        view.isFocusable = enabled
    }

    private fun changeImageSendTint(imageView: ImageView, color: Int) {
        activity?.let {
            ContextCompat.getColor(it, color)
        }?.let {
            imageView.setColorFilter(it, PorterDuff.Mode.SRC_ATOP)
        }
    }

    override fun onDeleteMessage() {
        clearSelectedComment()
        commentsAdapter.getSelectedComment()?.let {
            presenter.deleteComment(it)
        }
    }

    private fun generateNewChatRoom() {
        val account = QiscusCore.getQiscusAccount()
        QiscusChatLocal.setRoomId(0)
        LoadingActivity.generateIntent(
            ctx,
            account.username,
            QiscusChatLocal.getUserId(),
            QiscusChatLocal.getAvatar(),
            QiscusChatLocal.getExtras(),
            QiscusChatLocal.getUserProps()
        )
        activity?.finish()
    }

    interface CommentSelectedListener {
        fun onCommentSelected(selectedComment: QiscusComment)
        fun onClearSelectedComment(status: Boolean)
    }

    interface OnUserTypingListener {
        fun onUserTyping(email: String?, isTyping: Boolean)
    }
}