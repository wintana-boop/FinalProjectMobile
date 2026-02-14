package com.example.finalproject.data.repository

import com.example.finalproject.data.model.QuoteResponse
import retrofit2.Response
import retrofit2.http.GET

interface QuoteApi {

    @GET("random")
    suspend fun getRandomQuote(): Response<QuoteResponse>
}