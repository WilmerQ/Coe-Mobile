package co.edu.ucc.coemovil.clases;

import java.io.Serializable;

/**
 * Created by wilme on 25/11/2016.
 */

public class Dispositivo implements Serializable {
    private String tokenGoogle;
    private String androidID;
    private Usuario usuario;

    public String getTokenGoogle() {
        return tokenGoogle;
    }

    public void setTokenGoogle(String tokenGoogle) {
        this.tokenGoogle = tokenGoogle;
    }

    public String getAndroidID() {
        return androidID;
    }

    public void setAndroidID(String androidID) {
        this.androidID = androidID;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
}
