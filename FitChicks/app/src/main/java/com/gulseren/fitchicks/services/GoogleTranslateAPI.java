package com.gulseren.fitchicks.services;



import com.gulseren.fitchicks.model.TranslateResponse;

import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GoogleTranslateAPI {
    @GET(".")
    Single<TranslateResponse> translate(
            @Query("q") String text,
            @Query("target") String targetLanguage,
            @Query("source") String sourceLanguage,
            @Query("key") String apiKey
                    );
}
