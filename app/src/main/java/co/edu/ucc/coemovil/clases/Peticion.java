package co.edu.ucc.coemovil.clases;

import java.io.Serializable;

/**
 * Created by wilme on 1/12/2016.
 */

public class Peticion implements Serializable {

    private Long id;
    private String NombreUsuario;
    private String IDdispositivoRealizador;

    public String getNombreUsuario() {
        return NombreUsuario;
    }

    public void setNombreUsuario(String NombreUsuario) {
        this.NombreUsuario = NombreUsuario;
    }

    public String getIDdispositivoRealizador() {
        return IDdispositivoRealizador;
    }

    public void setIDdispositivoRealizador(String IDdispositivoRealizador) {
        this.IDdispositivoRealizador = IDdispositivoRealizador;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}