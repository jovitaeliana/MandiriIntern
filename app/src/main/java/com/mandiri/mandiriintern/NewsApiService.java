package com.mandiri.mandiriintern;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface NewsApiService {
    @GET("top-headlines")
    Call<NewsResponse> getTopHeadlines(
            @Query("country") String country,
            @Query("category") String category,
            @Query("q") String query,
            @Query("page") int page,
            @Query("pageSize") int pageSize
    );

    @GET("everything")
    Call<NewsResponse> searchAllNews(
            @Query("q") String query,
            @Query("language") String language
    );
}