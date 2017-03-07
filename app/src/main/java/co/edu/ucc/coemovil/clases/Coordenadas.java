package co.edu.ucc.coemovil.clases;

import java.io.Serializable;

/**
 * Created by wilme on 25/11/2016.
 */

public class Coordenadas implements Serializable {

    private String IdDispostitivo;
    private String androidID;
    private double latitud;
    private double longitud;
    private Long IdPeticion;

    public String getIdDispostitivo() {
        return IdDispostitivo;
    }

    public void setIdDispostitivo(String idDispostitivo) {
        IdDispostitivo = idDispostitivo;
    }

    public String getAndroidID() {
        return androidID;
    }

    public void setAndroidID(String androidID) {
        this.androidID = androidID;
    }

    public double getLatitud() {
        return latitud;
    }

    public void setLatitud(double latitud) {
        this.latitud = latitud;
    }

    public double getLongitud() {
        return longitud;
    }

    public void setLongitud(double longitud) {
        this.longitud = longitud;
    }

    public Long getIdPeticion() {
        return IdPeticion;
    }

    public void setIdPeticion(Long idPeticion) {
        IdPeticion = idPeticion;
    }
}