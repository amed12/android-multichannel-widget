package com.qiscus.qiscuschat.ui.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.qiscus.integrations.multichannel_sample.R
import com.qiscus.qiscuschat.MultichannelWidget
import com.qiscus.qiscuschat.util.showToast
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity(), LoginPresenterImpl.LoginListener {
    private lateinit var presenter: LoginPresenterImpl

    companion object {
        fun generateIntent(context: Context) {
            context.startActivity(Intent(context, LoginActivity::class.java))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        presenter = LoginPresenterImpl(this)

        btnLogin.setOnClickListener {
            errorText.visibility = View.INVISIBLE

            val userName = et_display_name.text.toString()
            val userEmail = et_user_email.text.toString()

            presenter.loginValidate(userName, userEmail)
        }

        btn_back.setOnClickListener { finish() }
    }

    override fun onDataValid(userName: String, userEmail: String) {
        val userProperties = mapOf("city" to "jogja", "job" to "customer")

        MultichannelWidget.instance.initiateChat(
            this, userName, userEmail, "", null, userProperties
        )
        finish()
    }

    override fun onIdNotValid(message: String) {
        errorText.text = message
        errorText.visibility = View.VISIBLE
    }

    override fun onError(message: String) {
        applicationContext.showToast(message)
    }

}
