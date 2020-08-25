package com.qiscus.qiscuschat.ui.chat

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.ViewGroup
import android.view.Window
import com.qiscus.integrations.multichannel_sample.R
import kotlinx.android.synthetic.main.dialog_delete_message.*

@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class DialogDeleteMessage(
    context: Context,
    val view: OnDeleteSelectedMessage
) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_delete_message)
        window.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        window.setBackgroundDrawable(null)
        listener()
    }

    private fun listener() {
        btn_cancel.setOnClickListener { dismiss() }

        btn_yes.setOnClickListener {
            view.onDeleteMessage()
            dismiss()
        }
    }

    interface OnDeleteSelectedMessage {
        fun onDeleteMessage()
    }
}