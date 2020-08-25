package com.qiscus.qiscuschat.data.repository

import com.google.gson.JsonObject
import com.qiscus.qiscuschat.data.model.DataInitialChat
import com.qiscus.qiscuschat.data.model.widgetChatConfig.ChatConfig
import com.qiscus.qiscuschat.data.repository.response.ResponseInitiateChat
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class QiscusChatRepository(val api: QiscusChatApi.Api) {

    fun initiateChat(
        dataInitialChat: DataInitialChat,
        onSuccess: (ResponseInitiateChat) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        api.getNonce(dataInitialChat).enqueue(object : Callback<ResponseInitiateChat?> {
            override fun onFailure(call: Call<ResponseInitiateChat?>, t: Throwable) {
                onError(t)
            }

            override fun onResponse(
                call: Call<ResponseInitiateChat?>,
                response: Response<ResponseInitiateChat?>
            ) {
                if (response.isSuccessful) {
                    val result = response.body()?.data?.let {
                        ResponseInitiateChat(it)
                    }
                    result?.let {
                        onSuccess(it)
                    }

                } else {
                    onError(Throwable("Error get data from api"))
                }
            }
        })
    }

    fun getSession(
        appCode: String,
        onSuccess: (Boolean) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        api.getSession(appCode).enqueue(object : Callback<JsonObject?> {
            override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
                onError(t)
            }

            override fun onResponse(call: Call<JsonObject?>, response: Response<JsonObject?>) {
                if (response.isSuccessful) {
                    var isSessional = false
                    val result = response.body()?.getAsJsonObject("data")?.let {
                        isSessional = it.get("is_sessional").asBoolean
                    }
                    result?.let {
                        onSuccess(isSessional)
                    }

                } else {
                    onError(Throwable("Error get data from api"))
                }
            }

        })
    }

    fun getWidgetChatConfig(
        appCode: String,
        onSuccess: (ChatConfig) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        api.getChatConfig(appCode).enqueue(object : Callback<JsonObject?> {
            override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
                onError(t)
            }

            override fun onResponse(call: Call<JsonObject?>, response: Response<JsonObject?>) {
                val data = response.body()?.getAsJsonObject("data")?.getAsJsonObject("widget")
                    ?.getAsJsonObject("variables")

                data?.let {
                    onSuccess(
                        ChatConfig(
                            it.get("customerServiceName").asString,
                            it.get("customerServiceAvatar").asString
                        )
                    )
                }
            }
        })
    }


}
