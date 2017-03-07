package co.edu.ucc.coemovil.Sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Wilmer on 27/02/2015.
 */


public class parametroBD {

    private funcionesBD funcionesBD;
    private SQLiteDatabase db;

    public parametroBD(Context context) {
        funcionesBD = new funcionesBD(context);
        db = funcionesBD.getWritableDatabase();
    }

    //insertar parametros

    //crear las tablas
    public static final String crear_tabla_dispositivo = "CREATE TABLE dispositivo (" +
            "id  NOT NULL ," +
            "key_google TEXT NOT NULL ," +
            "id_apk TEXT NOT NULL ," +
            "codigo_equipo INTEGER NOT NULL ," +
            "PRIMARY KEY(id)" +
            ");";


    //ContentValues
    private ContentValues generarContentValues_Dispositivo(String key_google, String id_apk, Long codigo_equipo) {
        ContentValues valores = new ContentValues();
        valores.put("key_google", key_google);
        valores.put("id_apk", id_apk);
        valores.put("codigo_equipo", codigo_equipo);
        return valores;
    }

    //insert dispositivos
    public long insertar_dispositivo(String key_google, String id_apk, Long codigo_equipo) {
        return db.insert("dispositivo", null, generarContentValues_Dispositivo(key_google, id_apk, codigo_equipo));
    }

    public Cursor consultarDispositivo(String id_apk) {
        return db.rawQuery("SELECT * FROM dispositivo where id_apk='" + id_apk + "'", null);
    }

}








