package com.vanglar.vanglar_notification;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.vanglar.vanglar_notification.service.LocationBackground;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener{
    TextView miUbicacion, Calculo;
    private double latitudActual = 0.0,longitudActual = 0.0;
    private TextToSpeech myTTS;   // Define the TTS objecy
    private int MY_DATA_CHECK_CODE = 0;
    double Distancia = 0.0;
    boolean flag = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        miUbicacion = findViewById(R.id.txtUbicacion);
        Calculo = findViewById(R.id.txtCalculo);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,}, 1000);
        } else {
            locationStart();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter("distance");
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver,intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
         @Override
         public void onReceive(Context context, Intent intent) {
             Distancia = intent.getDoubleExtra("Distancia",0.0);
             Log.d("Distancia", "onReceive: "+Distancia);
             Calculo.setText("Estas a: " + String.format("%.2f",Distancia) + " km");
             if (Distancia <= 1.00 && flag) {
                 SpeakOn();
             } else if (Distancia >= 1.00 && !flag){
                 Log.d("Load", "Run: Buscando lugar de destino...");
                 flag = true;
             }else{
                 Log.d("Load", "Run: Buscando lugar de destino...");
             }
         }
     };
    private void speakWords(String speech) {
        System.out.println(speech + " TTSTTTS");
        myTTS.speak(speech, TextToSpeech.QUEUE_FLUSH, null);
    }
    private void SpeakOn(){
        myTTS = new TextToSpeech(this, this);
        Intent checkTTSIntent = new Intent();
        checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);
        flag = false;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MY_DATA_CHECK_CODE) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                //the user has the necessary data - create the TTS
                myTTS = new TextToSpeech(this, this);
            } else {
                //no data - install it now
                Intent installTTSIntent = new Intent();
                installTTSIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installTTSIntent);
            }
        }
    }

    //setup TTS
    public void onInit(int initStatus) {
        //check for successful instantiation
        if (initStatus == TextToSpeech.SUCCESS) {
            if (myTTS.isLanguageAvailable(Locale.US) == TextToSpeech.LANG_AVAILABLE)
                myTTS.setLanguage(Locale.US);
           // Log.d("SPEAKER", "onInit: Corriendo el speaker");
           speakWords("Estas a '" + String.format("%.2f", Distancia) + "' kilometros del destino");
        } else if (initStatus == TextToSpeech.ERROR) {
            Toast.makeText(this, "Sorry! Text To Speech failed...", Toast.LENGTH_LONG).show();
        }
    }

    private void locationStart() {
        LocationManager mlocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Localizacion Local = new Localizacion();
        Local.setMainActivity(this);
        final boolean gpsEnabled = mlocManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!gpsEnabled) {
            Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(settingsIntent);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,}, 1000);
            return;
        }
        mlocManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, (LocationListener) Local);
        mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, (LocationListener) Local);

    }
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 1000) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationStart();
                return;
            }
        }
    }
    public void setLocation(Location loc) {
        //Obtener la direccion de la calle a partir de la latitud y la longitud
        if (loc.getLatitude() != 0.0 && loc.getLongitude() != 0.0) {
            try {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                List<Address> list = geocoder.getFromLocation(
                        loc.getLatitude(), loc.getLongitude(), 1);
                if (!list.isEmpty()) {
                    Address DirCalle = list.get(0);
                    Log.d("Direccion Actual", DirCalle.getAddressLine(0));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    /* Aqui empieza la Clase Localizacion */
    public class Localizacion implements LocationListener {
        MainActivity mainActivity;
        public MainActivity getMainActivity() {
            return mainActivity;
        }
        public void setMainActivity(MainActivity mainActivity) {
            this.mainActivity = mainActivity;
        }
        @Override
        public void onLocationChanged(Location loc) {
            // Este metodo se ejecuta cada vez que el GPS recibe nuevas coordenadas
            // debido a la deteccion de un cambio de ubicacion
            latitudActual = loc.getLatitude();
            longitudActual = loc.getLongitude();
            this.mainActivity.setLocation(loc);
            miUbicacion.setText("Latitud: " + latitudActual + "\n" + "Longitud: " + longitudActual);
            Intent i = new Intent(getApplicationContext(), LocationBackground.class);
            i.putExtra("Latitud", latitudActual);
            i.putExtra("Longitud",longitudActual);
            startService(i);
        }
        @Override
        public void onProviderDisabled(String provider) {
            // Este metodo se ejecuta cuando el GPS es desactivado
        }
        @Override
        public void onProviderEnabled(String provider) {
            // Este metodo se ejecuta cuando el GPS es activado
        }
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            switch (status) {
                case LocationProvider.AVAILABLE:
                    Log.d("debug", "LocationProvider.AVAILABLE");
                    break;
                case LocationProvider.OUT_OF_SERVICE:
                    Log.d("debug", "LocationProvider.OUT_OF_SERVICE");
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    Log.d("debug", "LocationProvider.TEMPORARILY_UNAVAILABLE");
                    break;
            }
        }
    }


}