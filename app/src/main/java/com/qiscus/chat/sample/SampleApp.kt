package com.qiscus.chat.sample

import androidx.multidex.MultiDexApplication
import com.qiscus.chat.sample.utils.NotificationListener
import com.qiscus.qiscusmultichannel.MultichannelWidget
import com.qiscus.qiscusmultichannel.MultichannelWidgetConfig

/**
 * Created on : 2020-02-28
 * Author     : Taufik Budi S
 * Github     : https://github.com/tfkbudi
 */
class SampleApp : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()

        val configMultichannel: MultichannelWidgetConfig =
            MultichannelWidgetConfig.setNotificationListener(NotificationListener())

        MultichannelWidget.setup(
            this,
            "rir-i1gavwtrrzsh7vk6h",
            configMultichannel
        )
    }

}