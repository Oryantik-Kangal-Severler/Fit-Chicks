package com.gulseren.fitchicks.services;



import com.gulseren.fitchicks.model.FoodResponse;

import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface FoodAPI {
    @GET("nutrition")
    Single<FoodResponse> getData(
            @Query("query") String foodQuery,
            @Header("X-Api-Key") String apiKey
    );
}
