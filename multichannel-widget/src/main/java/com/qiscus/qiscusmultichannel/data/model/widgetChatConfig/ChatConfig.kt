package com.qiscus.qiscusmultichannel.data.model.widgetChatConfig

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ChatConfig(
    val customerServiceName: String? = null,
    val customerServiceAvatar: String? = null
) : Parcelable