package co.edu.ucc.coemovil.clases;

import java.io.Serializable;

/**
 * Created by wilme on 14/10/2016.
 */

public class EquipoTrabajo implements Serializable {

    private Long id;
    private String nombreEquipo;
    private Usuario jefeEquipo;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombreEquipo() {
        return nombreEquipo;
    }

    public void setNombreEquipo(String nombreEquipo) {
        this.nombreEquipo = nombreEquipo;
    }

    public Usuario getJefeEquipo() {
        return jefeEquipo;
    }

    public void setJefeEquipo(Usuario jefeEquipo) {
        this.jefeEquipo = jefeEquipo;
    }
}
