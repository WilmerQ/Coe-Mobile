package co.edu.ucc.coemovil.clases;

import java.io.Serializable;

/**
 * Created by wilme on 14/10/2016.
 */

public class Usuario implements Serializable {
    private Long id;

    /**
     * nombre del usuario
     */
    private String nombreUsuario;
    /**
     * clave
     */
    private String contrasena;
    /**
     * email
     */
    private String email;
    /**
     * telefono
     */
    private String telefono;

    /**
     * informe de error: usuado para la verificacion de operacion con el servidor.
     */
    private Integer informeDeError;

    private EquipoTrabajo equipoTrabajo;


    public EquipoTrabajo getEquipoTrabajo() {
        return equipoTrabajo;
    }

    public void setEquipoTrabajo(EquipoTrabajo equipoTrabajo) {
        this.equipoTrabajo = equipoTrabajo;
    }

    /**
     * Gets nombre usuario.
     *
     * @return the nombre usuario
     */
    public String getNombreUsuario() {
        return nombreUsuario;
    }

    /**
     * Sets nombre usuario.
     *
     * @param nombreUsuario the nombre usuario
     */
    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    /**
     * Gets clave.
     *
     * @return la clave
     */
    public String getContrasena() {
        return contrasena;
    }

    /**
     * Sets clave.
     *
     * @param contrasena the clave
     */
    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    /**
     * Gets email.
     *
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets email.
     *
     * @param email the email
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Gets telefono.
     *
     * @return the telefono
     */
    public String getTelefono() {
        return telefono;
    }

    /**
     * Sets telefono.
     *
     * @param telefono the telefono
     */
    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    /**
     * Gets informe de error.
     *
     * @return el informe de error
     */
    public Integer getInformeDeError() {
        return informeDeError;
    }

    /**
     * Sets informe de error.
     *
     * @param informeDeError el informe de error
     */
    public void setInformeDeError(Integer informeDeError) {
        this.informeDeError = informeDeError;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

}
