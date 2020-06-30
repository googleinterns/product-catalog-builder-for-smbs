package com.googleinterns.smb.common;

import com.googleinterns.smb.pojo.DirectionResponse;
import com.googleinterns.smb.pojo.OrderStatus;
import com.googleinterns.smb.pojo.SendBidRequest;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Singleton implementation for handling API calls and to cache after costly network operations.
 */
public class APIHandler {

    private static final String DIRECTIONS_API_BASE = "https://maps.googleapis.com";
    private static final String CONSUMER_API_BASE = "https://kirana-g.uc.r.appspot.com/";

    private Retrofit mRetrofit;
    // Mapping for multiple API handlers, for each base API
    private static Map<String, APIHandler> mApiHandlers = new HashMap<>();

    private APIHandler(String baseUrl) {
        mRetrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    private static APIHandler getInstance(String baseUrl) {
        // Get API handler for this base API
        if (mApiHandlers.get(baseUrl) == null) {
            APIHandler apiHandler = new APIHandler(baseUrl);
            mApiHandlers.put(baseUrl, apiHandler);
        }
        return mApiHandlers.get(baseUrl);
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

    public static DirectionService getDirectionService() {
        return getInstance(DIRECTIONS_API_BASE).mRetrofit.create(DirectionService.class);
    }

    public interface ConsumerService {
        @POST("/sendBid")
        Call<Void> sendBid(@Body SendBidRequest sendBidRequest);

        String DISPATCHED_STATUS_MESSAGE = "packageDispatched";
        String DELIVERED_STATUS_MESSAGE = "packageDelivered";

        @POST("/updateOrderStatus")
        Call<Void> notifyOrderStatus(@Body OrderStatus orderStatus);
    }

    public static ConsumerService getConsumerService() {
        return getInstance(CONSUMER_API_BASE).mRetrofit.create(ConsumerService.class);
    }
}
