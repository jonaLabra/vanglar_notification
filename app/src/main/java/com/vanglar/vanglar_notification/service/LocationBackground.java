package com.vanglar.vanglar_notification.service;

import android.app.IntentService;
import android.content.Intent;
import android.location.Location;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class LocationBackground extends IntentService {
    public LocationBackground() {
        super("Ubicacion");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
    double lat = intent.getDoubleExtra("Latitud",0.0);
    double lon = intent.getDoubleExtra("Longitud",0.0);
    double Distancia = CalcularDistancia(lat,lon,18.636472040975022,-99.161394102612);
    Intent i = new Intent("distance");
    i.putExtra("Distancia",Distancia);
        LocalBroadcastManager.getInstance(this).sendBroadcast(i);
    }

    private double CalcularDistancia(double latOrigen,double lonOrigen,double latDestino,double lonDestino) {
        Location location = new Location("Mi Ubicacion");
        location.setLatitude(latOrigen);  //latitud
        location.setLongitude(lonOrigen); //longitud
        Location location2 = new Location("Destino");
        location2.setLatitude(latDestino);  //latitud
        location2.setLongitude(lonDestino); //longitud
        float distance = location.distanceTo(location2);
        return (distance / 1000) ;
        /*float[] results = new float[1];
        Location.distanceBetween(latOrigen,lonOrigen,latDestino,lonDestino,results);
        return results[0];*/
    }
}
