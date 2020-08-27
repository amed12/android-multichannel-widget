package com.qiscus.qiscusmultichannel.data.repository

import com.google.gson.JsonObject
import com.qiscus.qiscusmultichannel.MultichannelWidget
import com.qiscus.qiscusmultichannel.data.model.DataInitialChat
import com.qiscus.qiscusmultichannel.data.repository.response.ResponseInitiateChat
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

/**
 * base url IT = https://qismo.qiscus.com/
 * base url Multichannel = https://multichannel.qiscus.com/
 */

object QiscusChatApi {
    fun create(): Api {
        val logInterceptor = HttpLoggingInterceptor()
        logInterceptor.level = HttpLoggingInterceptor.Level.BODY

        val client = OkHttpClient.Builder()
        if (MultichannelWidget.config.isEnableLog()) {
            client.addInterceptor(logInterceptor)
        }

        val retrofit: Retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://multichannel.qiscus.com/")
            .client(client.build())
            .build()
        return retrofit.create(Api::class.java)
    }

    interface Api {
        @Headers("Content-Type: application/json")
        @POST("api/v1/qiscus/initiate_chat")
        fun getNonce(@Body dataInitialChat: DataInitialChat): Call<ResponseInitiateChat>

        @POST("api/v1/qiscus/initiate_chat")
        fun initiateChat(@Body dataInitialChat: DataInitialChat): Call<ResponseInitiateChat>

        @GET("{appCode}/get_session")
        fun getSession(@Path("appCode") appCode: String): Call<JsonObject>

        @GET("api/v1/app/config/public-widget/{appCode}")
        fun getChatConfig(@Path("appCode") appCode: String): Call<JsonObject>
    }
}

