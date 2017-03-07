package co.edu.ucc.coemovil;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import co.edu.ucc.coemovil.Sqlite.parametroBD;
import co.edu.ucc.coemovil.clases.Conexion;
import co.edu.ucc.coemovil.clases.CredencialesLoguin;
import co.edu.ucc.coemovil.clases.Usuario;

/**
 * Class {@link Loguin}
 * clase encargada de controlar la pantalla de logueo de la aplicacion
 * donde se ingresa el nombre de usuario y la contraseña y se valida en el servidor.
 *
 * @author wilme
 * @see AppCompatActivity
 * @see android.support.design.widget.NavigationView.OnNavigationItemSelectedListener
 */
public class Loguin extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    EditText nombreUsuario;
    EditText contrasena;
    Button entrar;
    private Handler mHandler = new Handler();
    private Handler mHandler1 = new Handler();
    /**
     * {@link ProgressBar} encargado de mostrar el avance en pantalla
     */
    ProgressBar progreso;
    /**
     * Intent encargado de lanzar una nueva actividad en dicho momento.
     */
    Intent i;
    Intent enviarAlerta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loguin);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        nombreUsuario = (EditText) findViewById(R.id.nombreUsuario);
        contrasena = (EditText) findViewById(R.id.contrasena);
        entrar = (Button) findViewById(R.id.entrarLoguin);
        progreso = (ProgressBar) findViewById(R.id.progressBar);
        progreso.setVisibility(View.INVISIBLE);
        i = new Intent(Loguin.this, Principal.class);
        enviarAlerta = new Intent(Loguin.this, EnviarAlertaManual.class);

        entrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (nombreUsuario.getText().length() == 0) {
                    mostrarMensaje("ERROR: ingrese nombre de usuario", Toast.LENGTH_SHORT);
                }

                if (contrasena.getText().length() == 0) {
                    mostrarMensaje("ERROR: ingrese la contraseña", Toast.LENGTH_SHORT);
                }

                if ((nombreUsuario.getText().length() != 0) && (contrasena.getText().length() != 0)) {
                    Gson g = new GsonBuilder().create();
                    CredencialesLoguin credenciales = new CredencialesLoguin();
                    credenciales.setNombre(nombreUsuario.getText().toString().trim());
                    credenciales.setContrasena(contrasena.getText().toString().trim());
                    Log.d("COEMOVIL", "deserializar gson: " + g.toJson(credenciales));
                    String url = "http://" + Conexion.getLocalhost() + ":" + Conexion.getPuerto() + "/coe/webresources/loguin/" + g.toJson(credenciales);
                    Log.d("COEMOVIL", "URL: " + url);
                    cambiarEstadoVisual(false);
                    new Descargar().execute(url);
                }
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(enviarAlerta);
                Log.d("COEMOVIL","-----------");
            }
        });
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
        getMenuInflater().inflate(R.menu.loguin, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

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

    /**
     * metodo que recibe un mensaje y la duracion, de un mensaje que aparecera en pantalla
     *
     * @param mensaje
     * @param duracion
     */
    private void mostrarMensaje(final String mensaje, final int duracion) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getBaseContext(), mensaje, duracion).show();
            }
        });
    }

    /**
     * metodo encargado de activar o desactivar ciertos elementos de la interfaz visual dependiendo del valor de la entrada del parametro.
     *
     * @param flag
     */
    private void cambiarEstadoVisual(final boolean flag) {
        mHandler.post(new Runnable() {
            public void run() {
                nombreUsuario.setEnabled(flag);
                contrasena.setEnabled(flag);
                entrar.setEnabled(flag);
            }
        });
    }

    /**
     * metodo encargado de actualizar la barra de progreso en la actividad.
     *
     * @param progress
     */
    private void actualizarVista(final int progress) {
        mHandler1.post(new Runnable() {
            public void run() {
                if (progreso != null) {
                    progreso.setProgress(progress);
                    Log.d("SAT", "Progreso " + progress);
                }
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
    public class Descargar extends AsyncTask<String, String, Boolean> {

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
        protected void onPreExecute() {
            super.onPreExecute();
            progreso.setMax(100);
            progreso.setVisibility(View.VISIBLE);
            actualizarVista(0);
        }

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
                    usuario = gson.fromJson(res, Usuario.class);
                    publishProgress("Bienvenido " + usuario.getNombreUsuario());
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
                actualizarVista(progreso.getProgress() + 10);
            }
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Boolean aVoid) {
            try {
                if (aVoid == null) {
                    cambiarEstadoVisual(true);
                    progreso.setVisibility(View.INVISIBLE);
                } else if (!aVoid) {
                    contrasena.setText("");
                    cambiarEstadoVisual(true);
                    progreso.setVisibility(View.INVISIBLE);

                } else {
                    nombreUsuario.setText("");
                    contrasena.setText("");
                    cambiarEstadoVisual(true);
                    i.putExtra("usuarioDatos", usuario);
                    progreso.setProgress(100);
                    startActivity(i);
                    finish();
                }
            } catch (JsonSyntaxException e) {
                cambiarEstadoVisual(true);
                progreso.setVisibility(View.INVISIBLE);
                e.printStackTrace();
            }
            super.onPostExecute(aVoid);
        }
    }

}
