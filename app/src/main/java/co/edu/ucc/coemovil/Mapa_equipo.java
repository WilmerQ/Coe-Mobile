package co.edu.ucc.coemovil;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
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

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import co.edu.ucc.coemovil.clases.Conexion;
import co.edu.ucc.coemovil.clases.EquipoTrabajo;
import co.edu.ucc.coemovil.clases.GcmObjetoRespuesta;
import co.edu.ucc.coemovil.clases.Usuario;

/**
 * {@link Mapa_equipo}
 * Clase encargada de mostrar un mapa de la api V2 de google maps.
 * se contara con una lista de grupos de trabajo.
 * <br/> los usuarios apareceranen el mapa segun trasmitan su ubicacion.
 *
 * @author wilme
 * @see AppCompatActivity
 * @see com.google.android.gms.maps.OnMapReadyCallback
 * see {@link android.support.design.widget.NavigationView.OnNavigationItemSelectedListener}
 */
public class Mapa_equipo extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {

    Handler mHandler = new Handler();
    List<EquipoTrabajo> equipoTrabajo;
    Spinner spinnerListaEquipos;
    MapFragment mapFragment;
    ProgressBar bar;
    Handler handler1;
    Usuario usuario;
    List<GcmObjetoRespuesta> gcmObjetoRespuesta;
    List<LatLng> ubicaciones;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapa_equipo);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        usuario = (Usuario) getIntent().getExtras().getSerializable("usuarioDatos");
        gcmObjetoRespuesta = new ArrayList<>();
        ubicaciones = new ArrayList<>();

        spinnerListaEquipos = (Spinner) findViewById(R.id.spinnerListaEquiposTrabajo);
        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.fragmentVerMapa);
        bar = (ProgressBar) findViewById(R.id.progressBarVerEquipos);
        handler1 = new Handler(Looper.getMainLooper());

        //cambiarEstadoVisual(false);
        new DescargarEquipos().execute();
        bar.setVisibility(View.INVISIBLE);

        spinnerListaEquipos.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!parent.getSelectedItem().toString().equals("--Selecione un Equipo")) {
                    Log.d("SAT", "item selecionado " + parent.getSelectedItem());
                    mapFragment.onStart();
                    GoogleMap map = mapFragment.getMap();
                    map.clear();
                    Gson g = new GsonBuilder().create();
                    String urlTemp = "http://" + Conexion.getLocalhost() + ":" + Conexion.getPuerto() + "/coe/webresources/EquipoTrabajo/" + g.toJson(parent.getSelectedItem().toString()) + "/" + g.toJson(usuario.getId());
                    Log.d("COEMOVIL", "url: " + urlTemp);
                    new PedirUbicacionCompañeros().execute(urlTemp);
                    mostrarMensaje("Solicitud de posicionamiento enviada, los usuarios apareceran en el mapa segun estos compartar su ubicacion", Toast.LENGTH_LONG);
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

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("actulizar-mapa"));
    }

    /**
     * @see BroadcastReceiver
     * variable tipo {@link BroadcastReceiver} encargado de recibir informacion en tiempo de ejecucion (una vez creada la task)
     * que es enviada desde la clase {@link MyService} y actualizar sin tener que reiniciar la tarea.
     */
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            GcmObjetoRespuesta temp = (GcmObjetoRespuesta) intent.getExtras().getSerializable("GcmObjeto");
            gcmObjetoRespuesta.add(temp);
            ubicaciones.add(new LatLng(temp.getLatitud(), temp.getLongitud()));
            Log.d("receiver", "Got message: " + temp);
            mapFragment.getMapAsync(Mapa_equipo.this);
            mapFragment.onStart();

        }
    };

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
        getMenuInflater().inflate(R.menu.mapa_equipo, menu);
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
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        //googleMap.clear();
        googleMap.getCameraPosition();
        googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        googleMap.setTrafficEnabled(true);
        googleMap.getUiSettings().setRotateGesturesEnabled(false);
        //googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(11.227439, -74.178289), 14));

        for (GcmObjetoRespuesta g : gcmObjetoRespuesta) {
            googleMap.addMarker(new MarkerOptions().position(new LatLng(g.getLatitud(), g.getLongitud())).draggable(false)
                    .title("" + g.getNombreUsuarioResponde()));
            gcmObjetoRespuesta.remove(g);
            Log.d("COEMOVIL", "for onMapReady " + g.getLatitud());
        }
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(centroZona(ubicaciones), ZoomZona(ubicaciones)));

    }

    /**
     * funcion encargada de calcular cual sera el centro del mapa dependiendo
     * de la distancia entre los marcadores que representan a los usuarios
     *
     * @param latLngs
     * @return LatLng
     */
    public LatLng centroZona(List<LatLng> latLngs) {
        try {
            if (latLngs.size() == 1) {
                return latLngs.get(0);
            } else if (latLngs.size() >= 2) {
                //latitud menor
                LatLng latmenor = latLngs.get(0);
                LatLng latMayor = latLngs.get(0);
                for (int i = 0; i < latLngs.size(); i++) {
                    if (latLngs.get(i).latitude < latmenor.latitude) {
                        latmenor = latLngs.get(i);
                    }
                    if (latLngs.get(i).latitude > latMayor.latitude) {
                        latMayor = latLngs.get(i);
                    }
                }

                //longitud menor
                LatLng lonmenor = latLngs.get(0);
                LatLng lonMayor = latLngs.get(0);
                for (int i = 0; i < latLngs.size(); i++) {
                    if (latLngs.get(i).longitude < lonmenor.longitude) {
                        lonmenor = latLngs.get(i);
                    }
                    if (latLngs.get(i).longitude > lonMayor.longitude) {
                        lonMayor = latLngs.get(i);
                    }
                }

                double dLat = latMayor.latitude - latmenor.latitude;
                double dLng = lonMayor.longitude - lonmenor.longitude;
                double sindLat = dLat / 2;
                double sindLng = dLng / 2;

                LatLng coord1 = new LatLng(sindLat + latmenor.latitude, sindLng + lonmenor.longitude);
                return coord1;

            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

    /**
     * funcion encargada de detectar el nivel de zoom del mapa dependiendo la distacia entre usuarios (marcadores del mapa)
     * a mas distancia entre puntos el zoom sera mayor para que el usuario pueda visualizar todos los marcadores desde el inicio.
     *
     * @param latLngs
     * @return float
     */
    public float ZoomZona(List<LatLng> latLngs) {
        try {
            if (latLngs.size() == 1) {
                return 18;
            } else if (latLngs.size() >= 2) {
                //latitud menor
                LatLng latmenor = latLngs.get(0);
                LatLng latMayor = latLngs.get(0);
                for (int i = 0; i < latLngs.size(); i++) {
                    if (latLngs.get(i).latitude < latmenor.latitude) {
                        latmenor = latLngs.get(i);
                    }
                    if (latLngs.get(i).latitude > latMayor.latitude) {
                        latMayor = latLngs.get(i);
                    }
                }

                //longitud menor
                LatLng lonmenor = latLngs.get(0);
                LatLng lonMayor = latLngs.get(0);
                for (int i = 0; i < latLngs.size(); i++) {
                    if (latLngs.get(i).longitude < lonmenor.longitude) {
                        lonmenor = latLngs.get(i);
                    }
                    if (latLngs.get(i).longitude > lonMayor.longitude) {
                        lonMayor = latLngs.get(i);
                    }
                }

                double dLat = latMayor.latitude - latmenor.latitude;
                double dLng = lonMayor.longitude - lonmenor.longitude;
                double sindLat = dLat / 2;
                double sindLng = dLng / 2;

                LatLng coord1 = new LatLng(sindLat + latmenor.latitude, sindLng + lonmenor.longitude);
                //return coord1;

            }
        } catch (Exception e) {
            e.printStackTrace();
            //return null;
        }
        return 10;
    }

    /**
     * metodo encargado de activar o desactivar ciertos elementos de la interfaz visual dependiendo del valor de la entrada del parametro.
     *
     * @param flag
     */
    private void cambiarEstadoVisual(final boolean flag) {
        mHandler.post(new Runnable() {
            public void run() {
                //nombreProyecto.setEnabled(flag);
                //zona.setEnabled(flag);
                if (flag) {
                    handler1.post(new Runnable() {
                        @Override
                        public void run() {
                            //mapFragment.getMapAsync(Mapa_equipo.this);
                        }
                    });

                } else {
                    //mapFragment.onStart();
                }

            }
        });
    }

    /**
     * metodo utilizado para mostrar un mensaje en pantalla
     * utilizando un elemento Toast
     *
     * @param mensaje
     * @param duracion
     */
    private void mostrarMensaje(final String mensaje, final int duracion) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), mensaje, duracion).show();
            }
        });
    }

    /**
     * clase Descagar
     * <br>
     * clase encarga de implementar el AsyncTask interfaz optimizada para comunicacion de datos desde servidor a el dispositivo.
     *
     * @author Wilmer
     * @see android.os.AsyncTask
     */
    public class DescargarEquipos extends AsyncTask<String, String, Boolean> {

        OkHttpClient client = new OkHttpClient();
        Response response = null;
        /**
         * The Gson.
         */
        Gson gson = new Gson();

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                String url = "http://" + Conexion.getLocalhost() + ":" + Conexion.getPuerto() + "/coe/webresources/EquipoTrabajo/";
                Log.d("COEMOVIL", "url: " + url);
                Request request = new Request.Builder()
                        .url(url)
                        .get()
                        .addHeader("cache-control", "no-cache")
                        .build();
                String res = null;
                client.setConnectTimeout(10, TimeUnit.SECONDS);
                client.setReadTimeout(10, TimeUnit.SECONDS);
                response = client.newCall(request).execute();
                if (response.code() == 200) {
                    res = response.body().string();
                    Type listType = new TypeToken<LinkedList<EquipoTrabajo>>() {
                    }.getType();
                    equipoTrabajo = new Gson().fromJson(res, listType);
                    return true;
                } else {
                    res = response.body().string();
                    Log.e("CoeMovil", "Error: " + res);
                    publishProgress("Error: " + res);
                    cambiarEstadoVisual(true);
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
        protected void onPostExecute(Boolean aVoid) {
            List<String> ejemplo = new LinkedList<>();
            ejemplo.add("--Selecione un Equipo");
            for (EquipoTrabajo trabajo : equipoTrabajo) {
                ejemplo.add(trabajo.getNombreEquipo());
            }
            ArrayAdapter<String> adaptador = new ArrayAdapter<>(getBaseContext(), R.layout.spinner_personalizado_item, ejemplo);
            spinnerListaEquipos.setAdapter(adaptador);
            super.onPostExecute(aVoid);
        }
    }

    /**
     * @author wilme
     * @see android.os.AsyncTask
     * <p>
     * clase encargada en segundo plano de recirbir una url y trasmitir la peticion al servidor,
     * a la espera de que los usuarios acepten dicha peticion.
     */
    public class PedirUbicacionCompañeros extends AsyncTask<String, String, Boolean> {

        OkHttpClient client = new OkHttpClient();
        Response response = null;
        /**
         * The Gson.
         */
        Gson gson = new Gson();
        /**
         * The Usuario.
         */
        Usuario usuario;

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
                    cambiarEstadoVisual(true);
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
        protected void onPostExecute(Boolean aVoid) {
            try {
                if (aVoid == null) {
                    cambiarEstadoVisual(true);
                } else if (!aVoid) {
                    cambiarEstadoVisual(true);
                } else {

                    cambiarEstadoVisual(true);
                }
            } catch (JsonSyntaxException e) {
                cambiarEstadoVisual(true);
                e.printStackTrace();
            }
            super.onPostExecute(aVoid);
        }
    }
}
