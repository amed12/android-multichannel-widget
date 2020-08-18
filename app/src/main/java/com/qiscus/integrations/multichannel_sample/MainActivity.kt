package com.qiscus.integrations.multichannel_sample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.qiscus.integrations.multichannel_sample.service.FirebaseServices
import com.qiscus.qiscusmultichannel.MultichannelWidget
import com.qiscus.qiscusmultichannel.MultichannelWidgetConfig
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val userProperties = mapOf("city" to "jogja", "job" to "developer")

        FirebaseServices().getCurrentDeviceToken()

        btnOpen.setOnClickListener {
            MultichannelWidget.instance.initiateChat(this, ConstCore.qiscusCore1(), "taufik dev", "taufik@qiscus.net","", null, userProperties)
        }

        btnOpen2.setOnClickListener {
            MultichannelWidget.instance.initiateChat(this, ConstCore.qiscusCore2(), "taufik dev2", "taufik2@qiscus.net","", null, userProperties)
        }
    }
}
