package com.example.mydeliverytracker.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.mydeliverytracker.api.DirectionsApiService;
import com.example.mydeliverytracker.api.DirectionsResponse;
import com.example.mydeliverytracker.model.Route;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;
import com.google.maps.android.PolyUtil;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
public class MapRepository {
    private DirectionsApiService apiService;

    public MapRepository() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://maps.googleapis.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(DirectionsApiService.class);
    }

    public LiveData<Route> getRoute(LatLng pickup, LatLng dropOff) {
        MutableLiveData<Route> routeData = new MutableLiveData<>();
        String origin = pickup.latitude + "," + pickup.longitude;
        String destination = dropOff.latitude + "," + dropOff.longitude;

        apiService.getDirections(origin, destination, "YOUR_API_KEY").enqueue(new Callback<DirectionsResponse>() {
            @Override
            public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    DirectionsResponse routeResponse = response.body();
                    List<LatLng> points = new ArrayList<>();
                    String eta = routeResponse.routes.get(0).legs.get(0).duration.text;
                    double distance = routeResponse.routes.get(0).legs.get(0).distance.value / 1000.0;

                    for (DirectionsResponse.Step step : routeResponse.routes.get(0).legs.get(0).steps) {
                        points.addAll(PolyUtil.decode(step.polyline.points));
                    }
                    routeData.postValue(new Route(points, eta, distance));
                }
            }

            @Override
            public void onFailure(Call<DirectionsResponse> call, Throwable t) {
                // Handle error
            }
        });
        return routeData;
    }
}