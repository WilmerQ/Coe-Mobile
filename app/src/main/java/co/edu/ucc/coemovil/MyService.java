package co.edu.ucc.coemovil;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import co.edu.ucc.coemovil.clases.Conexion;
import co.edu.ucc.coemovil.clases.Coordenadas;
import co.edu.ucc.coemovil.clases.Usuario;
import co.edu.ucc.coemovil.clases.Util;

/**
 * @author wilme
 * @see android.app.Service
 * class tipo Service encargada de recibir la accion "si" de la notificacion.
 * se encarga de consultar los permisos de ubicacion
 * verifica la disponibilidad del gps
 * si los permisos estan desactivados envia la accion 1 a {@link MostrarAlert}
 * <p>
 * si el gps esta desactivado envia la accion 2 a {@link MostrarAlert}
 * <p>
 * cuando la ubicacion tiene una precision por debajo de 10 metros, se envia la ubicacion y la accion 3 a {@link MostrarAlert}
 */
public class MyService extends Service {

    public MyService() {
    }

    public static Boolean isRunning = false;

    public LocationManager mLocationManager;
    public LocationUpdaterListener mLocationListener;
    public Location previousBestLocation = null;
    NotificationManager notifyMgr;
    int i;
    Handler mHandler;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mLocationListener = new LocationUpdaterListener();
        notifyMgr = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        mHandler = new Handler();
        Log.d("COEMOVIL", "onCreate");
        super.onCreate();
    }


    Runnable mHandlerTask = new Runnable() {
        @Override
        public void run() {
            if (!isRunning) {
                startListening();
                Log.d("COEMOVIL", "mHandlerTask");
            }
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // mHandlerTask.run();
        try {
            i = intent.getExtras().getInt("id");
            Log.d("COEMOVIL", "MyService:Cancel_noti id " + i);
            notifyMgr.cancel(i);
            Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
            getApplicationContext().sendBroadcast(it);

        } catch (Exception e) {
            e.printStackTrace();
        }
        startListening();
        Log.d("COEMOVIL", "onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        stopListening();
        mHandler.removeCallbacks(mHandlerTask);
        super.onDestroy();
    }

    /**
     * se inicia la ejecucion para capturar la pocicion.
     */
    private void startListening() {
        Log.d("COEMOVIL", "startListening before");

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == -1) {
            Log.d("COEMOVIL", "permiso ACCESS_FINE_LOCATION aun no asignado");
            Intent intent = new Intent(this, MostrarAlert.class);
            intent.putExtra("id", 1);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            onDestroy();

        } else {
            if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                Log.d("COEMOVIL", "Gps apagado");
                Intent intent = new Intent(this, MostrarAlert.class);
                intent.putExtra("id", 2);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                onDestroy();
            }


            if (mLocationManager.getAllProviders().contains(LocationManager.GPS_PROVIDER))
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
        }

        isRunning = true;
    }

    /**
     * se ejecuta la parada de la toma de ubicaciones.
     */
    private void stopListening() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationManager.removeUpdates(mLocationListener);
        }
        isRunning = false;

    }


    public class LocationUpdaterListener implements LocationListener {
        Coordenadas coordenadas = new Coordenadas();

        @Override
        public void onLocationChanged(Location location) {
            Log.d("COEMOVIL", "onLocationChanged " + location.getLatitude() + " : " + location.getLongitude() + " : " + location.getAccuracy());

            if (location.getAccuracy() < 10) {
                Log.d("COEMOVIL", "Ubicacion con 10 metros de precision");
                Gson gson = new Gson();
                coordenadas.setAndroidID(DameIdApk(getApplicationContext()));
                coordenadas.setIdDispostitivo(getRegistrationId(getApplicationContext()));
                coordenadas.setIdPeticion(Long.valueOf(i));
                coordenadas.setLatitud(location.getLatitude());
                coordenadas.setLongitud(location.getLongitude());
                String Url = "http://" + Conexion.getLocalhost() + ":" + Conexion.getPuerto() + "/coe/webresources/EquipoTrabajo/" + gson.toJson(coordenadas) + "?tipo=tipo";
                Log.d("COEMOVIL", "URL: " + Url);
                Intent intent = new Intent(getApplicationContext(), MostrarAlert.class);
                intent.putExtra("id", 3);
                intent.putExtra("url", Url);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                stopListening();
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderDisabled(String provider) {
            stopListening();
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

    }

    public String DameIdApk(Context context) {
        return Settings.Secure.getString(getBaseContext().getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(Util.PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.d("COEMOVIL", "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing registration ID is not guaranteed to work with
        // the new app version.
        int registeredVersion = prefs.getInt(Util.PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.d("COEMOVIL", "App version changed.");
            return "";
        }
        return registrationId;
    }

    private SharedPreferences getGCMPreferences(Context context) {
        // This sample app persists the registration ID in shared preferences, but
        // how you store the registration ID in your app is up to you.
        return getSharedPreferences(Principal.class.getSimpleName(),
                Context.MODE_PRIVATE);
    }

    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    private void mostrarMensaje(final String mensaje, final int duracion) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getBaseContext(), mensaje, duracion).show();
            }
        });
    }


}







