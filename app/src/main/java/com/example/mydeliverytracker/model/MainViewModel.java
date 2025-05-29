package com.example.mydeliverytracker.model;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.mydeliverytracker.repository.MapRepository;
import com.google.android.gms.maps.model.LatLng;

public class MainViewModel extends ViewModel {
    private MapRepository repository = new MapRepository();
    private MutableLiveData<Route> route = new MutableLiveData<>();

    public void fetchRoute(LatLng pickup, LatLng dropOff) {
        repository.getRoute(pickup, dropOff).observeForever(route::setValue);
    }

    public LiveData<Route> getRoute() {
        return route;
    }
}