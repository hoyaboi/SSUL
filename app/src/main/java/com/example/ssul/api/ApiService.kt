package com.example.ssul.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

// API 요청 인터페이스
interface ApiService {
    @GET("/pubs")
    fun getStores(
        @Query("college") college: String,
        @Query("major") degree: String
    ): Call<List<StoreResponse>>

    @GET("/pubs/{storeId}")
    fun getStoreInfo(
        @Path("storeId") storeId: Int,
        @Query("college") college: String,
        @Query("major") degree: String
    ): Call<StoreInfoResponse>
}