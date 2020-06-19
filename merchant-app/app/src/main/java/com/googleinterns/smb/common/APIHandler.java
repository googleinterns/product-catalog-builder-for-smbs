package com.googleinterns.smb.common;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public class APIHandler {

    /**
     * Google Directions API interface for fetching route information
     */
    public interface DirectionsAPIInterface {
        @GET("/maps/api/directions/json")
        Call<String> getRoute(
                @Query("origin") String origin,
                @Query("destination") String destination,
                @Query("key") String apiKey
        );
    }

}
