package com.puelloemiro.partidos;

/**
 * Clase que gestiona la sesión activa en la aplicación.
 * Siguiendo las directrices del docente, el token de autenticación
 * se mantiene únicamente en memoria (variable estática) durante
 * el ciclo de vida de la ejecución y NUNCA se persiste en disco ni SharedPreferences.
 *
 * @author Emiro Puello
 */
public class Sesion {

    // Variable estática que almacena el token JWT recibido tras el login exitoso
    public static String token = null;

    /**
     * Verifica si existe una sesión activa válida en memoria.
     * @return true si el token no es nulo ni vacío, false en caso contrario.
     */
    public static boolean tieneSesion() {
        return token != null && !token.trim().isEmpty();
    }

    /**
     * Cierra la sesión activa borrando el token de la memoria RAM.
     */
    public static void cerrarSesion() {
        token = null;
    }
}
