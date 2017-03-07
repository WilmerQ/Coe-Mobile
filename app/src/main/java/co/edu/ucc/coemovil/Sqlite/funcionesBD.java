package co.edu.ucc.coemovil.Sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Wilmer on 27/02/2015.
 */
public class funcionesBD extends SQLiteOpenHelper {


    private static final String DB_NAME = "CoeMovil.sqlite";
    private static final int DB_SCHEME_VERSION = 1;


    public funcionesBD(Context context) {
        super(context, DB_NAME, null, DB_SCHEME_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(parametroBD.crear_tabla_dispositivo);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // db.execSQL(parametroBD.actualizar_gps);
    }
}
