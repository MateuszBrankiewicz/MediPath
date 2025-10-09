package com.medipath.core.services

import com.medipath.core.responses.SearchResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface SearchService {
    @GET("/api/search/{query}")
    suspend fun search(
        @Path("query") query: String, 
        @Query("type") type: String, 
        @Query("city") city: String? = null, 
        @Query("specialisations") specialisations: String? = null
    ): Response<SearchResponse>
}