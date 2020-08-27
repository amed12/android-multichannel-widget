package com.qiscus.qiscusmultichannel.ui.chat.viewholder

import android.annotation.SuppressLint
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import com.qiscus.qiscusmultichannel.R
import com.qiscus.sdk.chat.core.QiscusCore
import com.qiscus.sdk.chat.core.data.model.QiscusComment
import java.io.File

class VideoVH(itemView: View) : ImageVH(itemView), ImageVH.DownloadFileListener {
    private val play: ImageView? = itemView.findViewById(R.id.iv_play)
    private val duration: TextView? = itemView.findViewById(R.id.tv_duration)
    private val thumbnail: ImageView = itemView.findViewById(R.id.thumbnail)

    override fun bind(comment: QiscusComment) {
        isVideoMode(this)
        onDownloaded(QiscusCore.getDataStore().getLocalPath(comment.id))
        super.bind(comment)

        if (comment.isMyComment && comment.isDownloading) {
            play?.visibility = View.VISIBLE
        }

        thumbnail.setOnClickListener {
            val qiscusFile = QiscusCore.getDataStore().getLocalPath(comment.id)
            if (qiscusFile != null) {
                try {
                    val newFile = File(qiscusFile.absolutePath)
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(newFile.absolutePath)
                        )
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        intent.setDataAndType(Uri.fromFile(newFile), "video/*")
                        itemView.context.startActivity(intent)
                    } else {
                        intentProvider(newFile)
                    }
                } catch (e: Exception) {
                    Toast.makeText(
                        itemView.context,
                        "Cannot Open Video",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

    }

    private fun setDurationVideo(path: String): String? {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(path)
        val time: String = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        val timeInMillisec = time.toLong()

        val duration = timeInMillisec / 1000
        val hours = duration / 3600
        val remainingMinutes = (duration - hours * 3600) / 60
        val remainingSeconds = duration - (hours * 3600 + remainingMinutes * 60)

        var minutes = remainingMinutes.toString()
        var seconds = remainingSeconds.toString()

        if (minutes == "0") {
            minutes = "00"
        } else if (minutes.length < 2) {
            minutes = "0$minutes"
        }
        if (seconds == "0") {
            seconds = "00"
        } else if (seconds.length < 2) {
            seconds = "0$seconds"
        }
        return "$minutes:$seconds"
    }

    private fun intentProvider(file: File) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val uri = FileProvider.getUriForFile(
            itemView.context,
            QiscusCore.getApps().packageName + ".qiscus.sdk.provider",
            file
        )
        intent.setDataAndType(uri, "video/*")
        val pm = itemView.context.packageManager
        if (intent.resolveActivity(pm) != null) {
            itemView.context.startActivity(intent)
        } else {
            Toast.makeText(
                itemView.context,
                "Cannot Open Video",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onDownloaded(localPath: File?) {
        if (localPath != null) {
            play?.visibility = View.VISIBLE
            duration?.text = setDurationVideo(localPath.path)
        } else {
            play?.visibility = View.GONE
            duration?.text = "00:00"
        }
    }
}