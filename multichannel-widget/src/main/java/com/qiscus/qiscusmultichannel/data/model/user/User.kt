package com.qiscus.qiscusmultichannel.data.model.user

import java.io.Serializable

data class User(
    var userId: String = "",
    var name: String = "",
    var avatar: String = "",
    val userProperties: Map<String, String>? = null
) : Serializable