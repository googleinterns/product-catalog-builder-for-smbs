package com.googleinterns.smb.common;

import com.googleinterns.smb.model.DirectionResponse;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Singleton implementation for handling API calls and to cache after costly network operations.
 */
public class APIHandler {

    private static final String DIRECTIONS_API_BASE = "https://maps.googleapis.com";

    private Retrofit retrofit;
    // Mapping for multiple API handlers, for each base API
    private static Map<String, APIHandler> apiHandlers = new HashMap<>();

    private APIHandler(String baseUrl) {
        retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    private static APIHandler getInstance(String baseUrl) {
        // Get API handler for this base API
        if (apiHandlers.get(baseUrl) == null) {
            APIHandler apiHandler = new APIHandler(baseUrl);
            apiHandlers.put(baseUrl, apiHandler);
        }
        return apiHandlers.get(baseUrl);
    }

    /**
     * Google Directions API interface for fetching route information
     */
    public interface DirectionService {
        @GET("/maps/api/directions/json")
        Call<DirectionResponse> getRoute(
                @Query("origin") String origin,
                @Query("destination") String destination,
                @Query("key") String apiKey
        );
    }

    public static DirectionService getDirectionsAPIInterface() {
        return getInstance(DIRECTIONS_API_BASE).retrofit.create(DirectionService.class);
    }
}
