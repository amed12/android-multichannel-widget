package com.qiscus.chat.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.qiscus.qiscusmultichannel.MultichannelWidget
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnOpen.setOnClickListener {
            val userProperties = mapOf("city" to "jogja", "job" to "customer")

            MultichannelWidget.instance.initiateChat(
                this, "Demo Customer",
                "demoCustomer@qiscus.net", "", null, userProperties
            )
        }

        btnLogin.setOnClickListener { MultichannelWidget.instance.loginChat(this) }

        btnLogout.setOnClickListener { MultichannelWidget.instance.logoutChat() }
    }
}
