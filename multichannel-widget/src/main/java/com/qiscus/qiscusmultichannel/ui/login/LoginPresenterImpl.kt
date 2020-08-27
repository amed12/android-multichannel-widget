package com.qiscus.qiscusmultichannel.ui.login

class LoginPresenterImpl(val listener: LoginListener) {

    fun loginValidate(userName: String, userEmail: String) {
        if (userEmail == "" || userName == "") {
            listener.onError("Display name or Email cannot be empty")
            return
        }

        var mailAddress = ""
        var index = userEmail.lastIndexOf('.')
        if (index > 0) index = userEmail.lastIndexOf("@", index)
        if (index > 0) mailAddress = userEmail.substring(index)

        if (mailAddress.contains('@') && mailAddress.contains(".")) {
            listener.onDataValid(userName, userEmail)
        } else {
            listener.onIdNotValid("Please use valid email")
        }
    }

    interface LoginListener {
        fun onDataValid(userName: String, userEmail: String)

        fun onIdNotValid(message: String)

        fun onError(message: String)
    }
}