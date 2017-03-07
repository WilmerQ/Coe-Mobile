package co.edu.ucc.coemovil.clases;

import java.io.Serializable;

/**
 * Created by wilme on 12/12/2016.
 */

public class GcmObjetoRespuesta implements Serializable {

    private String NombreUsuarioResponde;
    private String NombreGrupoTrabajo;
    private double latitud;
    private double Longitud;
    private Long IdPeticion;

    public String getNombreUsuarioResponde() {
        return NombreUsuarioResponde;
    }

    public void setNombreUsuarioResponde(String NombreUsuarioResponde) {
        this.NombreUsuarioResponde = NombreUsuarioResponde;
    }

    public String getNombreGrupoTrabajo() {
        return NombreGrupoTrabajo;
    }

    public void setNombreGrupoTrabajo(String NombreGrupoTrabajo) {
        this.NombreGrupoTrabajo = NombreGrupoTrabajo;
    }

    public double getLatitud() {
        return latitud;
    }

    public void setLatitud(double latitud) {
        this.latitud = latitud;
    }

    public double getLongitud() {
        return Longitud;
    }

    public void setLongitud(double Longitud) {
        this.Longitud = Longitud;
    }

    public Long getIdPeticion() {
        return IdPeticion;
    }

    public void setIdPeticion(Long IdPeticion) {
        this.IdPeticion = IdPeticion;
    }
}
