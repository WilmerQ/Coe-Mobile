package co.edu.ucc.coemovil;

import android.app.NotificationManager;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import co.edu.ucc.coemovil.clases.Conexion;
import co.edu.ucc.coemovil.clases.Usuario;

public class VerAlertas extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {

    Spinner spinnerRangos;
    MapFragment mapFragment;
    ProgressBar progressBar;
    List<String> ejemplo = new LinkedList<>();
    NotificationManager notifyMgr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ver_alertas);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ejemplo.add("- Selecione un rango");
        ejemplo.add("ultima hora");
        ejemplo.add("ultimas seis horas");
        ejemplo.add("ultimo dia");
        ejemplo.add("ultima semana");
        ejemplo.add("todas");
        notifyMgr = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        //int i = intent.getExtras().getInt("id");
        //Log.d("COEMOVIL", "Cancel_notificacion: id " + i);
        //notifyMgr.cancel(i);

        spinnerRangos = (Spinner) findViewById(R.id.spinnerListaRangos);
        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.fragmentVerMapaAlertas);
        progressBar = (ProgressBar) findViewById(R.id.progressBarVerAlertas);
        progressBar.setVisibility(View.INVISIBLE);

        ArrayAdapter<String> adaptador = new ArrayAdapter<>(getBaseContext(), R.layout.spinner_personalizado_item, ejemplo);
        spinnerRangos.setAdapter(adaptador);

        spinnerRangos.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!parent.getSelectedItem().toString().equals("- Selecione un rango")) {
                    Log.d("SAT", "item selecionado " + parent.getSelectedItem());
                    mapFragment.onStart();
                    GoogleMap map = mapFragment.getMap();
                    map.clear();
                    Gson g = new GsonBuilder().create();
                    String urlTemp = "http://" + Conexion.getLocalhost() + ":" + Conexion.getPuerto() + "/coe/webresources/auxiliar/" + g.toJson(parent.getSelectedItem());
                    Log.d("COEMOVIL", "url: " + urlTemp);
                    new solicitarAlertas().execute(urlTemp);
                    //mostrarMensaje("Solicitud de posicionamiento enviada, los usuarios apareceran en el mapa segun estos compartar su ubicacion", Toast.LENGTH_LONG);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

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
        getMenuInflater().inflate(R.menu.ver_alertas, menu);
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

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

    }

    public class solicitarAlertas extends AsyncTask<String, String, Boolean> {

        OkHttpClient client = new OkHttpClient();
        Response response = null;
        /**
         * The Gson.
         */
        Gson gson = new Gson();

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                Request request = new Request.Builder()
                        .url(params[0])
                        .get()
                        .addHeader("cache-control", "no-cache")
                        .build();
                String res = null;
                client.setConnectTimeout(10, TimeUnit.SECONDS);
                client.setReadTimeout(10, TimeUnit.SECONDS);
                response = client.newCall(request).execute();
                if (response.code() == 200) {
                    res = response.body().string();
                    publishProgress("A la Espera " + res);
                    return true;
                } else {
                    res = response.body().string();
                    Log.e("CoeMovil", "Error: " + res);
                    publishProgress("Error: " + res);
                    //  cambiarEstadoVisual(true);
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
                //mostrarMensaje(" " + values[0], Toast.LENGTH_LONG);
            }
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Boolean aVoid) {
            try {
                if (aVoid == null) {
                    //cambiarEstadoVisual(true);
                } else if (!aVoid) {
                    //cambiarEstadoVisual(true);
                } else {

                    //cambiarEstadoVisual(true);
                }
            } catch (JsonSyntaxException e) {
                //cambiarEstadoVisual(true);
                e.printStackTrace();
            }
            super.onPostExecute(aVoid);
        }
    }
}
