package com.qiscus.qiscuschat

import com.qiscus.qiscuschat.util.MultichannelNotificationListener

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

    fun setEnableLog(enableLog: Boolean) = apply { MultichannelWidgetConfig.enableLog = enableLog }
    fun isEnableLog() = enableLog
    fun isSessional() =
        isSessional

    fun setSessional(isSessional: Boolean) =
        apply { MultichannelWidgetConfig.isSessional = isSessional }

    fun setNotificationListener(multichannelNotificationListener: MultichannelNotificationListener?) =
        apply {
            MultichannelWidgetConfig.multichannelNotificationListener =
                multichannelNotificationListener
        }

    fun getNotificationListener() =
        multichannelNotificationListener

    fun setEnableNotification(enableNotification: Boolean) =
        apply { MultichannelWidgetConfig.enableNotification = enableNotification }

    fun isEnableNotification() =
        enableNotification

}