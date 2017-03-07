package co.edu.ucc.coemovil.clases;

import java.io.Serializable;

/**
 * Created by wilme on 1/12/2016.
 */

public class GcmObjeto implements Serializable {

    String tipo;
    Peticion peticion;
    GcmObjetoRespuesta respuesta;
    AlertaManualGcm alertaManualGcm;

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Peticion getPeticion() {
        return peticion;
    }

    public void setPeticion(Peticion peticion) {
        this.peticion = peticion;
    }

    public GcmObjetoRespuesta getRespuesta() {
        return respuesta;
    }

    public void setRespuesta(GcmObjetoRespuesta respuesta) {
        this.respuesta = respuesta;
    }

    public AlertaManualGcm getAlertaManualGcm() {
        return alertaManualGcm;
    }

    public void setAlertaManualGcm(AlertaManualGcm alertaManualGcm) {
        this.alertaManualGcm = alertaManualGcm;
    }
}
