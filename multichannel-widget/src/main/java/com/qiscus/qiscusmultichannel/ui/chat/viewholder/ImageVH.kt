package com.qiscus.qiscusmultichannel.ui.chat.viewholder

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.FileProvider
import androidx.core.util.PatternsCompat
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.qiscus.nirmana.Nirmana
import com.qiscus.qiscusmultichannel.R
import com.qiscus.qiscusmultichannel.util.Const
import com.qiscus.qiscusmultichannel.util.DateUtil
import com.qiscus.qiscusmultichannel.util.webView.WebViewHelper
import com.qiscus.sdk.chat.core.QiscusCore
import com.qiscus.sdk.chat.core.data.model.QMessage
import com.qiscus.sdk.chat.core.util.QiscusDateUtil
import org.json.JSONObject
import java.io.File
import java.util.*
import java.util.regex.Matcher

/**
 * Created on : 22/08/19
 * Author     : Taufik Budi S
 * GitHub     : https://github.com/tfkbudi
 */
class ImageVH(itemView: View) : BaseViewHolder(itemView) {
    private val thumbnail: ImageView = itemView.findViewById(R.id.thumbnail)
    private val message: TextView = itemView.findViewById(R.id.message)
    private val sender: TextView? = itemView.findViewById(R.id.sender)
    private val dateOfMessage: TextView? = itemView.findViewById(R.id.dateOfMessage)

    override fun bind(context: Context, comment: QMessage) {
        super.bind(context, comment)

        try {
            val content = JSONObject(comment.payload)
            val url = content.getString("url")
            val caption = content.getString("caption")
            val filename = content.getString("file_name")

            if (url.startsWith("http")) { //We have sent it
                showSentImage(comment, url)
            } else { //Still uploading the image
                showSendingImage(url)
            }

            if (caption.isEmpty()) {
                message.visibility = View.GONE
            } else {
                message.visibility = View.VISIBLE
                message.text = caption
            }

            val chatRoom = Const.qiscusCore()?.getDataStore()?.getChatRoom(comment.chatRoomId)

            sender?.visibility = if (chatRoom!!.type == "group") View.VISIBLE else View.GONE

            dateOfMessage?.text = DateUtil.toFullDate(comment.timestamp)

            thumbnail.setOnClickListener {
                dialodViewImage(url, comment.sender.name, caption, comment.timestamp)
                /*val localPath = QiscusCore.getDataStore().getLocalPath(comment.id)
                if (localPath != null) {
                    itemView.context.showToast("Image already in the gallery")

                } else {
                    downloadFile(comment, filename, url)
                }*/
            }
            setUpLinks()
        } catch (t: Throwable) {

        }

    }

    override fun setNeedToShowDate(showDate: Boolean) {
        dateOfMessage?.visibility = if (showDate) View.VISIBLE else View.GONE
    }

    private fun showSendingImage(url: String) {
        val localPath = File(url)
        showLocalImage(localPath)
    }

    private fun showSentImage(comment: QMessage, url: String) {
        val localPath = Const.qiscusCore()?.getDataStore()?.getLocalPath(comment.id)
        localPath?.let { showLocalImage(it) } ?: Nirmana.getInstance().get()
            .setDefaultRequestOptions(
                RequestOptions()
                    .placeholder(R.drawable.ic_qiscus_add_image)
                    .error(R.drawable.ic_qiscus_add_image)
                    .dontAnimate()
                    .transforms(CenterCrop(), RoundedCorners(16))

            )
            .load(url)
            .into(thumbnail)
    }

    private fun showLocalImage(localPath: File) {
        Nirmana.getInstance().get()
            .setDefaultRequestOptions(
                RequestOptions()
                    .placeholder(R.drawable.ic_qiscus_add_image)
                    .error(R.drawable.ic_qiscus_add_image)
                    .dontAnimate()
                    .transforms(CenterCrop(), RoundedCorners(16))
            )
            .load(localPath)
            .into(thumbnail)
    }

    private fun dialodViewImage(
        localPath: String,
        sender: String,
        description: String,
        date: Date
    ) {
        val mCotext = itemView.context
        val mDialog =
            LayoutInflater.from(mCotext).inflate(R.layout.image_dialog_view_mc, null)

        val imageView = mDialog.findViewById<ImageView>(R.id.ivDialogView)
        val ibDialogView = mDialog.findViewById<ImageButton>(R.id.ibDialogView)
        val tvSender = mDialog.findViewById<TextView>(R.id.tv_view_sender)
        val tvDescription = mDialog.findViewById<TextView>(R.id.tv_view_description)
        val tvDate = mDialog.findViewById<TextView>(R.id.tv_view_date)
        /*val btnShare = mDialog.findViewById<ImageButton>(R.id.ibShareDialogView)*/
        tvSender.text = sender
        tvDescription.text = description
        tvDate.text = QiscusDateUtil.toFullDateFormat(date)

        Nirmana.getInstance().get()
            .load(localPath)
            .into(imageView)
        val dialogBuilder = AlertDialog.Builder(mCotext, R.style.CustomeDialogFull)
            .setView(mDialog)
        val dialog = dialogBuilder.show()

        /*btnShare.setOnClickListener {
            //shareImage()
        }*/
        ibDialogView.setOnClickListener {
            dialog.dismiss()
        }
    }

    fun shareImage(fileImage: File) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "image/jpg"
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(fileImage))
        } else {

            Const.qiscusCore()?.getApps()?.packageName + ".qiscus.sdk.provider"
            intent.putExtra(
                Intent.EXTRA_STREAM,
                FileProvider.getUriForFile(
                    itemView.context,
                    Const.qiscusCore()?.getApps()?.packageName + ".qiscus.sdk.provider",
                    fileImage
                )
            )
        }
    }

    @SuppressLint("DefaultLocale", "RestrictedApi")
    private fun setUpLinks() {
        val text = message.text.toString().toLowerCase()
        val matcher: Matcher = PatternsCompat.AUTOLINK_WEB_URL.matcher(text)
        while (matcher.find()) {
            val start: Int = matcher.start()
            if (start > 0 && text[start - 1] == '@') {
                continue
            }
            val end: Int = matcher.end()
            clickify(start, end, object : ClickSpan.OnClickListener {
                override fun onClick() {
                    var url = text.substring(start, end)
                    if (!url.startsWith("http")) {
                        url = "http://$url"
                    }
                    WebViewHelper.launchUrl(itemView.context, Uri.parse(url))
                }
            })
        }
    }

    private fun clickify(start: Int, end: Int, listener: ClickSpan.OnClickListener) {
        val text: CharSequence = message.text.toString()
        val span = ClickSpan(listener)
        if (start == -1) {
            return
        }
        if (text is Spannable) {
            (text as Spannable).setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        } else {
            val s: SpannableString = SpannableString.valueOf(text)
            s.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            message.text = s
        }
    }

    private class ClickSpan(private val listener: OnClickListener?) :
        ClickableSpan() {

        interface OnClickListener {
            fun onClick()
        }

        override fun onClick(widget: View) {
            listener?.onClick()
        }

    }
}