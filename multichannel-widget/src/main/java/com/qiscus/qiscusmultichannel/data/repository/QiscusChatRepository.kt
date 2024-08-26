package com.qiscus.qiscusmultichannel.data.repository

import com.qiscus.qiscusmultichannel.data.model.DataInitialChat
import com.qiscus.qiscusmultichannel.data.repository.response.ResponseInitiateChat
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class QiscusChatRepository(val api: QiscusChatApi.Api) {

    fun initiateChat(
        dataInitialChat: DataInitialChat,
        onSuccess: (ResponseInitiateChat) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        api.initiateChat(dataInitialChat).enqueue(object : Callback<ResponseInitiateChat?> {
            override fun onFailure(call: Call<ResponseInitiateChat?>, t: Throwable) {
                onError(t)
            }

            override fun onResponse(
                call: Call<ResponseInitiateChat?>,
                response: Response<ResponseInitiateChat?>
            ) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        if (it.data.customerRoom == null) {
                            onError(Throwable("Customer room is empty"))
                        } else {
                            onSuccess(it)
                        }
                    }
                } else {
                    onError(Throwable("Error get data from api"))
                }
            }
        })
    }

    fun checkSessional(
        appCode: String,
        onSuccess: (ResponseInitiateChat) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        api.sessionalCheck(appCode).enqueue(object : Callback<ResponseInitiateChat> {
            override fun onFailure(call: Call<ResponseInitiateChat>, t: Throwable) {
                onError(t)
            }

            override fun onResponse(
                call: Call<ResponseInitiateChat>,
                response: Response<ResponseInitiateChat>
            ) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        onSuccess(it)
                    }
                } else {
                    onError(Throwable("Error get data from api"))
                }
            }

        })
    }
}
