package co.edu.ucc.coemovil;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.Gson;

import java.math.BigDecimal;

import co.edu.ucc.coemovil.clases.AlertaManualGcm;
import co.edu.ucc.coemovil.clases.GcmObjeto;


/**
 * Clase GcmIntentService
 * <br>
 * se encarga de activar cuando la clase GcmBroadcastReceiver se lo indica
 * y despues de esto envia una notificacion.
 *
 * @author Wilmer
 * @see IntentService
 */
public class GcmIntentService extends IntentService {


    String TAG = "coemovil";

    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {  // has effect of unparcelling Bundle

            //Log.d("SAT", "Extras: " + extras.toString());
            for (String s : extras.keySet()) {
                //Log.d("SAT", "Extras:describe " + s);
                Log.d("COEMOVIL", "Extras:contenido " + extras.getString(s));
            }

            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                sendNotification("Send error: " + extras.toString());
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
                sendNotification("Deleted messages on server: " +
                        extras.toString());
                // If it's a regular GCM message, do some work.
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                String recieved_message = intent.getStringExtra("message");
                sendNotification(recieved_message);

                //Log.d("SAT", "message recieved: " + recieved_message);
                Intent sendIntent = new Intent("message_recieved");
                sendIntent.putExtra("message", recieved_message);
                LocalBroadcastManager.getInstance(this).sendBroadcast(sendIntent);
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    /**
     * sendNotification
     * <br>
     * recibe un objeto y lo tramita para ser mostrado en la zona de notificacion
     * llama una nueva actividad
     *
     * @param msg
     */
    private void sendNotification(String msg) {
        Log.d("COEMOVIL", "CGM Recibido: " + msg);
        Gson gson = new Gson();
        GcmObjeto objeto = gson.fromJson(msg, GcmObjeto.class);

        if (objeto.getTipo().equals("CompartirUbicacion")) {
            /*Log.d("COEMOVIL", "entro");
            Intent intent1 = new Intent(this, GpsNotificacionService.class);
            intent1.putExtra("GcmObjeto", msg);
            this.startService(intent1);*/
            notification6(objeto.getPeticion().getId(), "Coe Movil", objeto.getPeticion().getNombreUsuario() + " desea Saber tu ubucacion, deseas Compartirla?");
        } else if (objeto.getTipo().equals("RespuestaPeticion")) {
            Intent intent = new Intent("actulizar-mapa");
            intent.putExtra("GcmObjeto", objeto.getRespuesta());
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
            Log.d("COEMOVIL", "Tipo RespuestaPeticion");
        } else if (objeto.getTipo().equals("alertaManual")) {
            Log.d("COEMOVIL", "recibio alertamanual");
            AlertaManualGcm manualGcm = objeto.getAlertaManualGcm();
            Log.d("COEMOVIL", "ManualGcm: " + manualGcm.getId());
            notificationAlerta(objeto, objeto.getAlertaManualGcm().getId(), "Alerta Manual Reportada", "el usuario " + objeto.getAlertaManualGcm().getNombre()
                    + " reporto una alerta " + objeto.getAlertaManualGcm().getNivelAlerta());
        }
    }

    /**
     * metodo encargado de crear la notificacion con dos opciones, "si" o  "no"
     *
     * @param id
     * @param titulo
     * @param contenido
     */
    public void notification6(Long id, String titulo, String contenido) {
        NotificationManager notifyMgr = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        int i = new BigDecimal(id).intValueExact();
        //si dice si
        Intent intent1 = new Intent(this, MyService.class);
        intent1.setAction(Intent.ACTION_VIEW);
        intent1.putExtra("id", i);
        PendingIntent Gps = PendingIntent.getService(this, 0, intent1, PendingIntent.FLAG_UPDATE_CURRENT);

        //si dice no
        Intent intent2 = new Intent(this, Cancel_notificacion.class);
        intent2.putExtra("id", i);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent2, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.icons_campana)
                        .setLargeIcon(BitmapFactory.decodeResource(
                                getResources(),
                                R.mipmap.icons_campana
                                )
                        )
                        .setContentTitle(titulo)
                        .setContentText(contenido)
                        .setColor(getResources().getColor(R.color.Dark))
                        .setStyle(
                                new NotificationCompat.BigTextStyle()
                                        .bigText(contenido))
                        .addAction(R.drawable.icono_notificacion_pedirubicacion,
                                "Si", Gps)
                        .addAction(R.drawable.ic_negar_algo,
                                "No", pendingIntent)
                        .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 500, 500})
                        .setSound(Uri.parse(Settings.System.ALARM_ALERT))
                        .setAutoCancel(true);

        Notification notification = mBuilder.build();
        try {
            notifyMgr.notify(i, notification);
        } catch (Exception e) {
            Log.d("COEMOVIL", "id: " + id + " Despues de convertir " + i);
        }
    }

    public void notificationAlerta(GcmObjeto gcmObjeto, Long id, String titulo, String contenido) {
        NotificationManager notifyMgr = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        int i = new BigDecimal(id).intValueExact();
        //si dice si

        Intent intent1 = new Intent(this, VerAlertas.class);
        intent1.setAction(Intent.ACTION_VIEW);
        intent1.putExtra("id", i);
        intent1.putExtra("objeto", gcmObjeto);
        PendingIntent mapa = PendingIntent.getActivity(this, 0, intent1, PendingIntent.FLAG_UPDATE_CURRENT);

        //si dice no
        Intent intent2 = new Intent(this, Cancel_notificacion.class);
        intent2.putExtra("id", i);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent2, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.icons_campana)
                        .setLargeIcon(BitmapFactory.decodeResource(
                                getResources(),
                                R.mipmap.icons_campana
                                )
                        )
                        .setContentTitle(titulo)
                        .setContentText(contenido)
                        //.setColor(getResources().getColor(R.color.Dark))
                        .setStyle(
                                new NotificationCompat.BigTextStyle()
                                        .bigText(contenido))
                        .addAction(R.drawable.ic_mapa,
                                "Ver Alerta", mapa)
                        .addAction(R.drawable.ic_negar_algo,
                                "Omitir", pendingIntent)
                        .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 500, 500})
                        .setSound(Uri.parse(Settings.System.ALARM_ALERT))
                        .setAutoCancel(true);

        Notification notification = mBuilder.build();
        try {
            notifyMgr.notify(i, notification);
        } catch (Exception e) {
            Log.d("COEMOVIL", "id: " + id + " Despues de convertir " + i);
        }
    }
}
