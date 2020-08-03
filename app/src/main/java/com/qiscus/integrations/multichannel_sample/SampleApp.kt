package com.qiscus.integrations.multichannel_sample

import android.content.Context
import androidx.multidex.MultiDexApplication
import com.qiscus.qiscusmultichannel.MultichannelWidget
import com.qiscus.qiscusmultichannel.MultichannelWidgetConfig
import com.qiscus.qiscusmultichannel.util.MultichannelNotificationListener
import com.qiscus.sdk.chat.core.QiscusCore
import com.qiscus.sdk.chat.core.data.model.QMessage

/**
 * Created on : 2020-02-28
 * Author     : Taufik Budi S
 * Github     : https://github.com/tfkbudi
 */
class SampleApp: MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()

        //just 1 in 1 lifecircle
        ConstCore.setCore()

        val configMultichannel: MultichannelWidgetConfig =
            MultichannelWidgetConfig.setEnableLog(BuildConfig.DEBUG)
                .setNotificationListener(null)
        MultichannelWidget.setup(this, ConstCore.qiscusCore1(), "karm-gzu41e4e4dv9fu3f", configMultichannel, "user1")
        MultichannelWidget.setup(this, ConstCore.qiscusCore2(), "karm-gzu41e4e4dv9fu3f", configMultichannel, "user2")
    }
}