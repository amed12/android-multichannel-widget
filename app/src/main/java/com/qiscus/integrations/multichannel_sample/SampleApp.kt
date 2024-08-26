package com.qiscus.integrations.multichannel_sample

import android.content.Context
import androidx.multidex.MultiDexApplication
import com.google.firebase.FirebaseApp
import com.qiscus.qiscusmultichannel.MultichannelWidget
import com.qiscus.qiscusmultichannel.MultichannelWidgetConfig
import com.qiscus.qiscusmultichannel.util.MultichannelNotificationListener
import com.qiscus.qiscusmultichannel.util.PNUtil
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
                .setEnableNotification(true)
                .setNotificationListener(object : MultichannelNotificationListener {

                    override fun handleMultichannelListener(context: Context?, qiscusComment: QMessage?) {
                        // show your notification here
                        if (context != null && qiscusComment != null) {
                            PNUtil.showPn(context, qiscusComment)
                        }
                    }

                })
                .setRoomTitle("Bot name")
                .setRoomSubtitle("Custom subtitle")
                .setHideUIEvent(true)
                .setHardcodedAvatar("https://d1edrlpyc25xu0.cloudfront.net/cee-8xj32ozyfbnka0arz/image/upload/XBOSht7_hR/bebi.jpeg")

        MultichannelWidget.setup(this, ConstCore.qiscusCore1(), "cee-8xj32ozyfbnka0arz", configMultichannel, "user1")
        //MultichannelWidget.setup(this, ConstCore.qiscusCore1(), "karm-gzu41e4e4dv9fu3f", configMultichannel, "user1")
    }
}