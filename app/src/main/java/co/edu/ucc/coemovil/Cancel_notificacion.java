package co.edu.ucc.coemovil;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * @author wilme
 * clase Tipo Service utilizado para en el caso el usuario decida NO compartir la ubicacion.
 *         <p>
 *         {@link Cancel_notificacion}
 * @see android.app.Service
 */
public class Cancel_notificacion extends Service {
    public Cancel_notificacion() {
    }

    NotificationManager notifyMgr;

    @Override
    public void onCreate() {
        notifyMgr = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int i = intent.getExtras().getInt("id");
        Log.d("COEMOVIL", "Cancel_notificacion: id " + i);
        notifyMgr.cancel(i);
        Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        getApplicationContext().sendBroadcast(it);
        return super.onStartCommand(intent, flags, startId);
    }
}
