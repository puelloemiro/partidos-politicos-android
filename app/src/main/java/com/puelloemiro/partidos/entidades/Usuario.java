package com.puelloemiro.partidos.entidades;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Entidad que representa a un Usuario del sistema.
 * Utilizada para los procesos de inicio de sesión, registro y recuperación de clave.
 *
 * @author Emiro Puello
 */
public class Usuario implements Serializable {

    // Identificador único (el requerimiento indica que el cliente usa el email como id)
    private String id;

    // Nombre completo del usuario
    private String name;

    // Correo electrónico del usuario
    private String email;

    // Contraseña de acceso
    private String password;

    // Rol dentro del sistema (por defecto "MEMBER")
    private String role;

    // Estado de la cuenta ("PENDING", "ACTIVE", etc.)
    private String status;

    /**
     * Constructor vacío.
     */
    public Usuario() {
        this.role = "MEMBER";
    }

    /**
     * Constructor para registro con los campos básicos.
     *
     * @param name Nombre del usuario
     * @param email Correo electrónico
     * @param password Contraseña elegida
     */
    public Usuario(String name, String email, String password) {
        this.id = email;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = "MEMBER";
    }

    /**
     * Constructor completo.
     */
    public Usuario(String id, String name, String email, String password, String role, String status) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role != null ? role : "MEMBER";
        this.status = status;
    }

    // --- Getters y Setters ---

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Convierte el usuario a formato JSON para el registro en la API.
     *
     * @return JSONObject con id, name, email, password y role.
     * @throws JSONException Si ocurre error al armar el JSON.
     */
    public JSONObject toJSONRegistro() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", id != null ? id : email);
        json.put("name", name != null ? name : "");
        json.put("email", email != null ? email : "");
        json.put("password", password != null ? password : "");
        json.put("role", role != null ? role : "MEMBER");
        return json;
    }

    /**
     * Construye una instancia de Usuario a partir del JSON de respuesta de la API.
     *
     * @param json Objeto JSON con los datos del usuario.
     * @return Instancia poblada de Usuario.
     */
    public static Usuario fromJSON(JSONObject json) {
        Usuario u = new Usuario();
        u.setId(json.optString("id", ""));
        u.setName(json.optString("name", ""));
        u.setEmail(json.optString("email", ""));
        u.setRole(json.optString("role", "MEMBER"));
        u.setStatus(json.optString("status", ""));
        return u;
    }
}
