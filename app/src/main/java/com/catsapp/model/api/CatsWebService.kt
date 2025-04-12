package com.catsapp.model.api

import android.util.Log
import com.catsapp.model.Cat
import com.catsapp.model.mapToCat
import com.swordhealth.catsapp.BuildConfig
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private const val TAG = "CatsWebService"

class CatsWebService {
    private var api: CatsApi

    init {
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("x-api-key", BuildConfig.CAT_API_KEY)
                    .build()
                chain.proceed(request)
            }
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.thecatapi.com/v1/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        api = retrofit.create(CatsApi::class.java)
    }

    suspend fun getCats(): List<Cat> {
        return try {
            api.getCats().mapNotNull {
                if (it.image == null) null
                else it.mapToCat()
            }
        } catch (e: Exception) {
            Log.d(TAG, "getCats | Exception caught=$e")
            emptyList()
        }
    }

    suspend fun getFavouriteCats(): List<FavouriteCatResponse> {
        return try {
            api.getFavouriteCats()
        } catch (e: Exception) {
            Log.d(TAG, "getFavouriteCats | Exception caught=$e")
            emptyList()
        }
    }
}