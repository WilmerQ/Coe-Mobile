package co.edu.ucc.coemovil.clases;

import java.io.Serializable;

/**
 * Created by wilme on 25/11/2016.
 */

public class SolicitarEquipo implements Serializable {

    private EquipoTrabajo equipoTrabajo;
    private Usuario usuarioSolicita;

    public EquipoTrabajo getEquipoTrabajo() {
        return equipoTrabajo;
    }

    public void setEquipoTrabajo(EquipoTrabajo equipoTrabajo) {
        this.equipoTrabajo = equipoTrabajo;
    }

    public Usuario getUsuarioSolicita() {
        return usuarioSolicita;
    }

    public void setUsuarioSolicita(Usuario usuarioSolicita) {
        this.usuarioSolicita = usuarioSolicita;
    }
}
