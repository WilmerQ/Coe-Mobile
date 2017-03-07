package co.edu.ucc.coemovil;

import android.*;
import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import co.edu.ucc.coemovil.clases.AlertaManual;
import co.edu.ucc.coemovil.clases.Conexion;
import co.edu.ucc.coemovil.clases.Usuario;

import co.edu.ucc.coemovil.ui.ViewProxy;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class EnviarAlertaManual extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {

    public final static int RESP_TOMAR_FOTO = 1000;
    public final static int RESP_TOMAR_Video = 1000;
    private final String ruta_fotos = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/misfotos/";
    File file = new File(ruta_fotos);
    File audio = new File(ruta_fotos);
    private static final int MY_PERMISSIONS_REQUEST_GLOBAL = 1;
    Spinner spinnerNivelesAlerta;
    public LocationManager mLocationManager;
    public LocationUpdaterListener mLocationListener;
    AlertaManual alertaManual;
    AlertDialog alert = null;
    MapFragment mapFragment;
    LatLng temp;
    double radio;
    ImageButton buttonFoto;
    Button enviarAlerta;
    Handler mHandler = new Handler();
    Bitmap original;
    EditText nombre;
    EditText nota;
    ProgressDialog dialog;
    ImageView imagen;
    ImageButton grabarVideo;
    MediaRecorder recorder = new MediaRecorder();

    private float distCanMove = dp(80);
    private View slideText;
    private float startedDraggingX = -1;
    private View recordPanel;
    private long startTime = 0L;
    long timeInMilliseconds = 0L;
    private Timer timer;
    long updatedTime = 0L;
    long timeSwapBuff = 0L;
    private TextView recordTimeText;
    private ImageButton audioSendButton;
    ImageView rec;
    ImageView imagen_arrow;
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enviar_alerta_manual);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.fragmentVerMapa1);
        buttonFoto = (ImageButton) findViewById(R.id.buttonFoto);
        enviarAlerta = (Button) findViewById(R.id.buttonEnviarAlerta);
        grabarVideo = (ImageButton) findViewById(R.id.buttonVideo);

        nombre = (EditText) findViewById(R.id.editTextNombre);
        nota = (EditText) findViewById(R.id.editTextNota);
        imagen = (ImageView) findViewById(R.id.imageView2);
        slideText = findViewById(R.id.slideText);
        recordPanel = findViewById(R.id.record_panel);
        recordTimeText = (TextView) findViewById(R.id.recording_time_text);
        textView = (TextView) findViewById(R.id.slideToCancelTextView);
        imagen_arrow = (ImageView) findViewById(R.id.imagen_arrow);

        audioSendButton = (ImageButton) findViewById(R.id.chat_audio_send_button);
        rec = (ImageView) findViewById(R.id.rec);
        dialog = new ProgressDialog(this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage("Espere un momento...");
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);
        enviarAlerta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertaManual.setNombrePersona(nombre.getText().toString());
                alertaManual.setNota(nota.getText().toString());
                if (VerificarFormulario()) {
                    Log.d("COEMOVIL", "------paso verificacion");
                    dialog.show();
                    new SubirDatos().execute();

                }
            }
        });

        List<String> permisos = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == -1)
            permisos.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == -1)
            permisos.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == -1)
            permisos.add(android.Manifest.permission.CAMERA);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == -1)
            permisos.add(android.Manifest.permission.RECORD_AUDIO);

        Log.d("COEMOVIL", "tamaño Permisos: " + permisos.size());
        if ((ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == -1) ||
                (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == -1) ||
                (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == -1) ||
                (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == -1)) {
            String[] strings = new String[permisos.size()];
            for (int i = 0; i < permisos.size(); i++) {
                Log.d("COEMOVIL", "Permisos Restastes: " + permisos.get(i));
                strings[i] = permisos.get(i);
            }
            Log.d("COEMOVIL", "Permiso: primero " + strings.length);
            ActivityCompat.requestPermissions(EnviarAlertaManual.this, strings, MY_PERMISSIONS_REQUEST_GLOBAL);
        }
        Log.d("COEMOVIL", "creando directorio ----- " + file.mkdir());
        Log.d("COEMOVIL", "creando directorio ----- " + audio.mkdir());

        imagen_arrow.setVisibility(View.INVISIBLE);
        audioSendButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    Log.d("COEMOVIL", "-----------Action DOWM");
                    imagen_arrow.setVisibility(View.VISIBLE);
                    textView.setText("Cancelar");
                    mostrarMensaje("Presione para grabar, suelte para guardar", Toast.LENGTH_SHORT);
                    FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) slideText.getLayoutParams();
                    params.leftMargin = dp(60);
                    slideText.setLayoutParams(params);
                    ViewProxy.setAlpha(slideText, 1);
                    startedDraggingX = -1;
                    rec.setImageResource(R.drawable.rec);
                    startRecording(true);
                    startrecord();
                    audioSendButton.getParent()
                            .requestDisallowInterceptTouchEvent(true);
                    recordPanel.setVisibility(View.VISIBLE);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    Log.d("COEMOVIL", "-----------Action UP");
                    FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) slideText.getLayoutParams();
                    float alpha = 0;
                    ViewProxy.setAlpha(slideText, alpha);
                    startedDraggingX = -1;
                    startRecording(false);
                    stoprecord();
                } else if (event.getAction() == MotionEvent.ACTION_CANCEL) {
                    Log.d("COEMOVIL", "-----------Action Cancel");
                    startedDraggingX = -1;
                    startRecording(false);
                    stoprecord();
                    recordTimeText.setText("00:00");
                } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    Log.d("COEMOVIL", "-----------Action Move");
                    float x = event.getX();
                    if (x < -distCanMove) {
                        if (!recordTimeText.getText().equals("00:00")) {
                            Log.d("COEMOVIL", "-----------Action Move-------");
                            stoprecord();
                            recordTimeText.setText("00:00");
                        }
                    }
                    x = x + ViewProxy.getX(audioSendButton);
                    FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) slideText
                            .getLayoutParams();
                    if (startedDraggingX != -1) {
                        float dist = (x - startedDraggingX);
                        params.leftMargin = dp(60) + (int) dist;
                        slideText.setLayoutParams(params);
                        float alpha = 1.0f + dist / distCanMove;
                        if (alpha > 1) {
                            alpha = 1;
                        } else if (alpha < 0) {
                            alpha = 0;
                        }
                        ViewProxy.setAlpha(slideText, alpha);
                    }
                    if (x <= ViewProxy.getX(slideText) + slideText.getWidth()
                            + dp(30)) {
                        if (startedDraggingX == -1) {
                            startedDraggingX = x;
                            distCanMove = (recordPanel.getMeasuredWidth()
                                    - slideText.getMeasuredWidth() - dp(48)) / 2.0f;
                            if (distCanMove <= 0) {
                                distCanMove = dp(80);
                            } else if (distCanMove > dp(80)) {
                                distCanMove = dp(80);
                            }
                        }
                    }
                    if (params.leftMargin > dp(60)) {
                        params.leftMargin = dp(60);
                        slideText.setLayoutParams(params);
                        ViewProxy.setAlpha(slideText, 1);
                        startedDraggingX = -1;
                    }
                }
                v.onTouchEvent(event);
                return true;

            }
        });
        grabarVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                String filePath = ruta_fotos + "/video.mp4";
                Log.d("COEMOVIL", "filePath---------- " + filePath);
                Uri output = Uri.fromFile(new File(filePath));
                intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 120);
                intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, output);
                Log.d("COEMOVIL", "Uri---------- " + output);
                startActivityForResult(intent, RESP_TOMAR_Video);
            }
        });

        buttonFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                String filePath = ruta_fotos + "/foto.jpg";
                Log.d("COEMOVIL", "filePath---------- " + filePath);
                Uri output = Uri.fromFile(new File(filePath));
                intent.putExtra(MediaStore.EXTRA_OUTPUT, output);
                Log.d("COEMOVIL", "Uri---------- " + output);
                startActivityForResult(intent, RESP_TOMAR_FOTO);
            }
        });

        spinnerNivelesAlerta = (Spinner) findViewById(R.id.spinner2);
        alertaManual = new AlertaManual();
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mLocationListener = new LocationUpdaterListener();
        if (mLocationManager.getProviders(true).contains(LocationManager.GPS_PROVIDER)) {
            Log.d("COEMOVIL", "ubicacion activada");
            if (mLocationManager.getAllProviders().contains(LocationManager.GPS_PROVIDER)) {
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
            }
        } else {
            Log.d("COEMOVIL", "gps desactivado");
            AlertNoGps();
        }
        List<String> ejemplo = new LinkedList<>();
        ejemplo.add("-Selecione un Nivel");
        ejemplo.add("Nivel Amarrillo");
        ejemplo.add("Nivel Naranja");
        ejemplo.add("Nivel Rojo");
        ArrayAdapter<String> adaptador = new ArrayAdapter<>(getBaseContext(), R.layout.spinner_personalizado_item, ejemplo);
        spinnerNivelesAlerta.setAdapter(adaptador);
        spinnerNivelesAlerta.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!parent.getSelectedItem().toString().equals("--Selecione un Equipo")) {
                    alertaManual.setNivelAlerta(parent.getSelectedItem().toString());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    private void startRecording(Boolean rec) {
        if (rec) {
            Boolean deleted = Boolean.FALSE;
            File file1 = new File(ruta_fotos + "nota.mp3");
            if (file1.exists()) {
                File file = new File(ruta_fotos + "nota.mp3");
                deleted = file.delete();
                Log.d("COEMOVIL", "eliminar archivo " + deleted);
            }
            try {
                if (deleted) {
                    recorder = new MediaRecorder();
                }
                recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                recorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
                recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
                recorder.setOutputFile(ruta_fotos + "nota.mp3");
                recorder.prepare();
                recorder.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            recorder.stop();
            recorder.reset();
            recorder.release();
            recorder = new MediaRecorder();
        }
    }

    @Override
    public void onBackPressed() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLocationManager.removeUpdates(mLocationListener);
        this.finish();
        super.onBackPressed();
        /*DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.enviar_alerta_manual, menu);
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

    public boolean isSdReadable() {
        boolean mExternalStorageAvailable = false;
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // We can read and write the media
            mExternalStorageAvailable = true;
            Log.i("isSdReadable", "External storage card is readable.");
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            // We can only read the media
            Log.i("isSdReadable", "External storage card is readable.");
            mExternalStorageAvailable = true;
        } else {
            // Something else is wrong. It may be one of many other
            // states, but all we need to know is we can neither read nor write
            mExternalStorageAvailable = false;
        }
        return mExternalStorageAvailable;
    }

    public Bitmap getThumbnail(String filename) {
        String fullPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/misfotos/";
        Bitmap thumbnail = null;
        // Look for the file on the external storage
        try {
            if (isSdReadable()) {
                thumbnail = BitmapFactory.decodeFile(fullPath + "/" + filename);
            }
        } catch (Exception e) {
            Log.e("COEMOVIL", "getThumbnail() on external storage" + e.getMessage());
        }
        // If no file on external storage, look in internal storage
        if (thumbnail == null) {
            try {
                File filePath = getApplicationContext().getFileStreamPath(filename);
                FileInputStream fi = new FileInputStream(filePath);
                thumbnail = BitmapFactory.decodeStream(fi);
            } catch (Exception ex) {
                Log.d("COEMOVIL", "NO SE ENCONTRO " + ex.getMessage());
            }
        }
        original = thumbnail;
        if ((thumbnail.getHeight() > 4096) || (thumbnail.getWidth() > 4096)) {
            Log.d("COEMOVIL", "Tamaño muy grande");
            Bitmap temp = Bitmap.createScaledBitmap(thumbnail, (int) thumbnail.getWidth() / 2, (int) thumbnail.getHeight() / 2, true);
            thumbnail = temp;
        }
        return thumbnail;
    }

    public boolean VerificarFormulario() {
        boolean b = true;
        if (alertaManual.getNombrePersona().length() < 1) {
            mostrarMensaje("Ingrese Su Nombre", Toast.LENGTH_SHORT);
            b = false;
        }

        if (alertaManual.getNivelAlerta().equals("-Selecione un Nivel")) {
            mostrarMensaje("Selecione un nivel de alerta", Toast.LENGTH_SHORT);
            b = false;
        }

        if (alertaManual.getNota().length() < 1) {
            mostrarMensaje("Ingrese algun comentario", Toast.LENGTH_SHORT);
            b = false;
        }

        if ((alertaManual.getLatitud() == null) || (alertaManual.getLongitud() == null)) {
            mostrarMensaje("Espere, se esta determinando su ubicacion", Toast.LENGTH_SHORT);
            b = false;
        }

        if (original == null) {
            mostrarMensaje("Tome una fotografia para comprobar su alerta", Toast.LENGTH_SHORT);
            b = false;
        }
        return b;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RESP_TOMAR_FOTO && resultCode == RESULT_OK) {
            Log.d("COEMOVIL", "--------tomo la fot y volvio");

            imagen.setImageBitmap(getThumbnail("foto.jpg"));
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.clear();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        googleMap.getUiSettings().setRotateGesturesEnabled(false);
        CircleOptions circleOptions = new CircleOptions();
        circleOptions.center(temp);
        circleOptions.radius(radio);
        circleOptions.fillColor(0x5500ff00);
        circleOptions.strokeWidth(1);
        googleMap.addCircle(circleOptions);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(temp, 16));

        if (radio < 15) {
            googleMap.clear();
            MarkerOptions options = new MarkerOptions();
            options.position(temp);
            options.draggable(false);
            options.title("Tu Ubicacion");
            googleMap.addMarker(options);
        }
    }

    public class LocationUpdaterListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            radio = location.getAccuracy();
            temp = new LatLng(location.getLatitude(), location.getLongitude());
            mapFragment.getMapAsync(EnviarAlertaManual.this);
            mapFragment.onStart();
            Log.d("COEMOVIL", "onLocationChanged " + location.getLatitude() + " : " + location.getLongitude() + " : " + location.getAccuracy());
            if (location.getAccuracy() < 15) {
                Log.d("COEMOVIL", "Ubicacion con 10 metros de precision");
                alertaManual.setLatitud(location.getLatitude());
                alertaManual.setLongitud(location.getLongitude());
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    mLocationManager.removeUpdates(mLocationListener);
                }
                mLocationManager.removeUpdates(mLocationListener);
                mapFragment.getMapAsync(EnviarAlertaManual.this);
                mapFragment.onStart();
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }

        @Override
        public void onProviderEnabled(String provider) {
        }

    }

    @Override
    protected void onRestart() {
        Log.d("COEMOVIL", "onRestart");
        if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
        }
        super.onRestart();
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
    public class SubirDatos extends AsyncTask<String, String, Boolean> {

        OkHttpClient client = new OkHttpClient();
        Response response = null;
        /**
         * The Gson.
         */
        Gson gson = new Gson();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                original.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] bytes = stream.toByteArray();
                String s = Base64.encodeToString(bytes, Base64.DEFAULT);
                Log.d("COEMOVIL", "s ------------" + s.length());

                String url = "http://" + Conexion.getLocalhost() + ":" + Conexion.getPuerto() + "/coe/webresources/auxiliar/";
                Log.d("COEMOVIL", "URL: " + url);
                alertaManual.setFoto(s);
                final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                RequestBody requestBody = RequestBody.create(JSON, gson.toJson(alertaManual));

                Request request = new Request.Builder()
                        .url(url)
                        .put(requestBody)
                        .addHeader("cache-control", "no-cache")
                        .build();

                String res = null;
                response = client.newCall(request).execute();

                if (response.code() == 200) {
                    res = response.body().string();

                    publishProgress("Respuesta " + res);
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
            Log.d("COEMOVIL", "publicprogress" + values[0]);
            if (!(values[0] == null)) {
                if ((values[0].contains("Respuesta")) || (values[0].contains("Error"))) {
                    mostrarMensaje(" " + values[0], Toast.LENGTH_LONG);
                } else {
                    dialog.setProgress(Integer.parseInt(values[0]));
                }
            }
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Boolean aVoid) {
            try {
                if (aVoid == null) {
                    dialog.dismiss();
                } else if (!aVoid) {
                    dialog.cancel();
                } else {
                    nombre.setText("");
                    nota.setText("");
                    imagen.setImageBitmap(null);
                    dialog.cancel();
                }
            } catch (JsonSyntaxException e) {
                dialog.cancel();
                e.printStackTrace();
            }
            super.onPostExecute(aVoid);
        }
    }

    public static int dp(float value) {
        return (int) Math.ceil(1 * value);
    }

    class MyTimerTask extends TimerTask {

        @Override
        public void run() {
            timeInMilliseconds = SystemClock.uptimeMillis() - startTime;
            updatedTime = timeSwapBuff + timeInMilliseconds;
            final String hms = String.format(
                    "%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(updatedTime)
                            - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS
                            .toHours(updatedTime)),
                    TimeUnit.MILLISECONDS.toSeconds(updatedTime)
                            - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS
                            .toMinutes(updatedTime)));
            long lastsec = TimeUnit.MILLISECONDS.toSeconds(updatedTime)
                    - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS
                    .toMinutes(updatedTime));
            System.out.println(lastsec + " hms " + hms);
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    try {
                        if (recordTimeText != null)
                            recordTimeText.setText(hms);
                    } catch (Exception e) {
                        // TODO: handle exception
                    }

                }
            });
        }
    }

    private void startrecord() {
        // TODO Auto-generated method stub
        startTime = SystemClock.uptimeMillis();
        timer = new Timer();
        MyTimerTask myTimerTask = new MyTimerTask();
        timer.schedule(myTimerTask, 1000, 1000);
        vibrate();
    }

    private void stoprecord() {
        // TODO Auto-generated method stub
        imagen_arrow.setVisibility(View.INVISIBLE);
        rec.setImageResource(R.drawable.ic_brightness_1_black_24dp);

        if (timer != null) {
            timer.cancel();
        }
        if (recordTimeText.getText().toString().equals("00:00")) {
            return;
        }
        vibrate();
    }

    private void vibrate() {
        // TODO Auto-generated method stub
        try {
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(200);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
