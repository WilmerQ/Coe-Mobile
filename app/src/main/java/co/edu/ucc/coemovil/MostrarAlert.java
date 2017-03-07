package co.edu.ucc.coemovil;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.view.ContextThemeWrapper;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.JsonSyntaxException;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class MostrarAlert extends Activity {

    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    int i;
    AlertDialog alert = null;
    public LocationManager mLocationManager;
    private Handler mHandler = new Handler();

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mostrar_alert);

        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        Log.d("COEMOVIL", "MostrarAlert oncreate");
        i = getIntent().getExtras().getInt("id");
        switch (i) {
            case 1: {
                Log.d("COEMOVIL", "Permiso gps desactivado");
                if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == -1) {
                    if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                            Log.d("COEMOVIL", "primero");
                            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                        } else {
                            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                            Log.d("COEMOVIL", "primera Vez");
                        }
                    }
                }
                break;
            }
            case 2: {
                Log.d("COEMOVIL", "gps desactivado");
                AlertNoGps();
                break;
            }
            case 3: {
                String url = getIntent().getExtras().getString("url");
                new EnviarRespuestaServidor().execute(url);
                break;
            }
        }
    }

    private void AlertNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.URTransparent));
        builder.setMessage("El sistema GPS esta desactivado, ¿Desea activarlo?")
                .setCancelable(false)
                .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                        getApplicationContext().stopService(new Intent(getApplicationContext(), MyService.class));
                        finishAndRemoveTask();
                    }
                });
        alert = builder.create();
        alert.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onRestart() {
        Log.d("COEMOVIL", "onRestart");
        if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            getApplicationContext().startService(new Intent(getApplicationContext(), MyService.class));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                finishAndRemoveTask();
            } else {
                finish();
            }
        } else {
            mostrarMensaje("Debe Encender el GPS", Toast.LENGTH_SHORT);
            AlertNoGps();
        }
        super.onRestart();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Log.d("COEMOVIL", "---------------------onRequestPermissionsResult");
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == -1) {
            Log.d("COEMOVIL", "---------------------onRequestPermissionsResult: if");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                finishAndRemoveTask();
            } else {
                finish();
            }
        } else {
            Log.d("COEMOVIL", "---------------------onRequestPermissionsResult: else");
            Intent intent1 = new Intent(this, MyService.class);
            this.startService(intent1);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                finishAndRemoveTask();
            } else {
                finish();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void mostrarMensaje(final String mensaje, final int duracion) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getBaseContext(), mensaje, duracion).show();
            }
        });
    }

    public class EnviarRespuestaServidor extends AsyncTask<String, String, Boolean> {

        OkHttpClient client = new OkHttpClient();
        Response response = null;

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                Request request = new Request.Builder()
                        .url(params[0])
                        .get()
                        .addHeader("cache-control", "no-cache")
                        .build();
                String res = null;
                //client.setConnectTimeout(10, TimeUnit.SECONDS);
                //client.setReadTimeout(10, TimeUnit.SECONDS);
                response = client.newCall(request).execute();
                if (response.code() == 200) {
                    res = response.body().string();
                    Log.e("COEMOVIL", "Ok: " + res);
                    publishProgress(res);
                    return true;
                } else {
                    res = response.body().string();
                    Log.e("COEMOVIL", "Error: " + res);
                    publishProgress("Error: " + res);
                    return false;
                }
            } catch (IOException e) {
                e.printStackTrace();
                publishProgress("Error: " + e.getMessage());
                return null;
            }
        }

        @Override
        protected void onProgressUpdate(String... values) {
            if (!(values == null)) {
                mostrarMensaje(" " + values[0], Toast.LENGTH_SHORT);
            }
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Boolean aVoid) {
            try {
                if (aVoid == null) {
                    mostrarMensaje("Su ubicacion No pudo ser trasmitida, por favor couminiquese con soporte", Toast.LENGTH_SHORT);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        finishAndRemoveTask();
                    } else {
                        finish();
                    }
                } else if (!aVoid) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        finishAndRemoveTask();
                    } else {
                        finish();
                    }
                } else {
                    getApplicationContext().stopService(new Intent(getApplicationContext(), MyService.class));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        finishAndRemoveTask();
                    } else {
                        finish();
                    }
                }
            } catch (JsonSyntaxException e) {
                e.printStackTrace();
            }
            super.onPostExecute(aVoid);
        }
    }
}
