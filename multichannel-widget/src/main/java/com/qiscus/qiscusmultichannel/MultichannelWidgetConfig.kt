package com.qiscus.qiscusmultichannel

import android.content.SharedPreferences
import com.qiscus.qiscusmultichannel.util.MultichannelNotificationListener
import com.qiscus.qiscusmultichannel.util.QiscusChatLocal.getPref

/**
 * Created on : 05/08/19
 * Author     : Taufik Budi S
 * GitHub     : https://github.com/tfkbudi
 */
object MultichannelWidgetConfig {
    private var enableLog: Boolean = false
    private var isSessional: Boolean = false
    var multichannelNotificationListener: MultichannelNotificationListener? = null
    private var enableNotification: Boolean = true
    private var roomTitle: String? = null
    private var roomSubtitle: String? = null
    private var hardcodedAvatar: String? = null
    private var hideUIEvent: Boolean = false

    private fun edit(): SharedPreferences.Editor {
        return getPref().edit()
    }

    fun setEnableLog(enableLog: Boolean) = apply { this.enableLog = enableLog }
    fun isEnableLog() = enableLog
    fun isSessional() = isSessional
    fun setSessional(isSessional: Boolean) = apply { this.isSessional = isSessional }
    fun setNotificationListener(multichannelNotificationListener: MultichannelNotificationListener?) = apply { this.multichannelNotificationListener = multichannelNotificationListener }
    fun getNotificationListener() = multichannelNotificationListener
    fun setEnableNotification(enableNotification: Boolean) = apply { this.enableNotification = enableNotification }
    fun isEnableNotification() = enableNotification
    fun setRoomTitle(roomTitle: String?) = apply { this.roomTitle = roomTitle }
    fun getRoomTitle(): String? = roomTitle
    fun setRoomSubtitle(roomSubtitle: String?) = apply { this.roomSubtitle = roomSubtitle }
    fun getRoomSubtitle(): String? = roomSubtitle
    internal fun setChannelId(channelId: Int) {
        edit().putInt("channelId", channelId).apply()
    }

    internal fun getChannelId(): Int? {
        val channelId = getPref().getInt("channelId", 0)
        return if (channelId == 0) null else channelId
    }
    fun setHideUIEvent(isHidden: Boolean) = apply { this.hideUIEvent = isHidden }
    fun getHideUIEvent(): Boolean = hideUIEvent

    @Deprecated("move using configuration from dashboard")
    fun setHardcodedAvatar(hardcodedAvatar: String) = apply { this.hardcodedAvatar = hardcodedAvatar }
    @Deprecated("move using configuration from dashboard")
    fun getHardcodedAvatar(): String? = hardcodedAvatar
}