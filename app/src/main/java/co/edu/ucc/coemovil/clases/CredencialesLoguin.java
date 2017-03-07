package co.edu.ucc.coemovil.clases;

/**
 * Created by wilme on 13/10/2016.
 */

public class CredencialesLoguin {

    private String nombre;
    private String contrasena;

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = Md5.getEncoddedString(contrasena);
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}
