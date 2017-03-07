package co.edu.ucc.coemovil;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import co.edu.ucc.coemovil.Sqlite.parametroBD;
import co.edu.ucc.coemovil.clases.Conexion;
import co.edu.ucc.coemovil.clases.Dispositivo;
import co.edu.ucc.coemovil.clases.Usuario;
import co.edu.ucc.coemovil.clases.Util;

public class Principal extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, ActivityCompat.OnRequestPermissionsResultCallback {

    private static final String TAG = "coemovil";
    Handler mHandler = new Handler();
    GoogleCloudMessaging gcm;
    String regid;
    String msg;
    Context context;
    //llamando a bd
    parametroBD bd;


    Usuario usuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        bd = new parametroBD(this);
        usuario = (Usuario) getIntent().getExtras().getSerializable("usuarioDatos");
        context = getApplicationContext();
        Log.d("COEMOVIL", "id de la apk" + DameIdApk(context));
        if (!DispositivoRegistrado(this)) {
            new SendGcmToServer().execute("");
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.principal, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.miequipo_principal) {
            Intent intent = new Intent(Principal.this, Mapa_equipo.class);
            intent.putExtra("usuarioDatos", usuario);
            startActivity(intent);
        } else if (id == R.id.verAlertas) {
            Intent intent = new Intent(Principal.this, VerAlertas.class);
            intent.putExtra("usuarioDatos", usuario);
            startActivity(intent);
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGCMPreferences(context);
        int appVersion = getAppVersion(context);
        Log.d(TAG, "Saving regId on app version:-- " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(Util.PROPERTY_REG_ID, regId);
        editor.putInt(Util.PROPERTY_APP_VERSION, appVersion);
        editor.commit();
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

    private boolean DispositivoRegistrado(Context context) {
        Cursor cursor = bd.consultarDispositivo(DameIdApk(context));
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            cursor.close();
            return true;
        } else if (cursor.getCount() == 0) {
            Log.d(TAG, "Registration not found.");
            cursor.close();
            return false;
        } else {
            cursor.close();
            return false;
        }
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        Util.PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.d(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(Util.PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.d(TAG, "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing registration ID is not guaranteed to work with
        // the new app version.
        int registeredVersion = prefs.getInt(Util.PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.d(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }


    public String DameIdApk(Context context) {
        return Settings.Secure.getString(getBaseContext().getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public class SendGcmToServer extends AsyncTask<String, String, Boolean> {


        Gson gson = new Gson();
        OkHttpClient client = new OkHttpClient();
        Response response = null;

        @Override
        protected Boolean doInBackground(String... params) {
            Gson g = new GsonBuilder().create();
            Dispositivo dispositivo = new Dispositivo();
            try {
                if (gcm == null) {
                    gcm = GoogleCloudMessaging.getInstance(Principal.this);
                }
                regid = gcm.register(Util.SENDER_ID);
                msg = "Device registered, registration ID=" + regid;
                storeRegistrationId(context, regid);
            } catch (IOException ex) {
                msg = "Error :" + ex.getMessage();
                return null;
            }
            dispositivo.setAndroidID(DameIdApk(context));
            dispositivo.setTokenGoogle(regid);
            dispositivo.setUsuario(usuario);
            String url1 = "http://" + Conexion.getLocalhost() + ":" + Conexion.getPuerto() + "/coe/webresources/dispositivo/" + g.toJson(dispositivo);
            Log.d("COEMOVIL", "URL REGISTRO DISPOSITIVO: " + url1);
            try {
                Request request = new Request.Builder()
                        .url(url1)
                        .get()
                        .addHeader("cache-control", "no-cache")
                        .build();
                String res = null;
                client.setConnectTimeout(10, TimeUnit.SECONDS);
                client.setReadTimeout(10, TimeUnit.SECONDS);
                response = client.newCall(request).execute();
                if (response.code() == 200) {
                    res = response.body().string();
                    publishProgress("Resultado: " + res);
                    return true;
                } else {
                    res = response.body().string();
                    Log.e("CoeMovil", "Error: " + res);
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
                mostrarMensaje(" " + values[0], Toast.LENGTH_LONG);
            }
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
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
