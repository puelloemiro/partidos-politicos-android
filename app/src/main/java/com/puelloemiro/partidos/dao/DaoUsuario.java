package com.puelloemiro.partidos.dao;

import com.puelloemiro.partidos.entidades.Usuario;

import org.json.JSONObject;

/**
 * Data Access Object (DAO) para la autenticación y gestión de usuarios.
 * Maneja las peticiones hacia los endpoints de seguridad (/api/auth y /api/users).
 *
 * @author Emiro Puello
 */
public class DaoUsuario {

    /**
     * Inicia sesión con correo y contraseña en la API (POST /api/auth/login).
     *
     * @param email Correo electrónico registrado.
     * @param password Contraseña del usuario.
     * @return El token de acceso (accessToken) generado por el servidor.
     * @throws Exception Si las credenciales son incorrectas o la cuenta no está activa (HTTP 401).
     */
    public String login(String email, String password) throws Exception {
        if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            throw new Exception("Debe ingresar correo y contraseña.");
        }

        JSONObject credenciales = new JSONObject();
        credenciales.put("email", email.trim());
        credenciales.put("password", password);

        String respuesta = ConexionApi.ejecutarPeticion("POST", "/api/auth/login", credenciales.toString(), null);
        JSONObject jsonResp = new JSONObject(respuesta);

        if (jsonResp.has("accessToken")) {
            return jsonResp.getString("accessToken");
        } else {
            throw new Exception("Respuesta inesperada del servidor al autenticar.");
        }
    }

    /**
     * Registra un nuevo usuario en la plataforma (POST /api/users).
     * Por requerimiento, el ID se asigna con el mismo valor del email y el rol por defecto es "MEMBER".
     *
     * @param usuario Instancia con los datos del nuevo usuario.
     * @return Objeto Usuario creado por el servidor.
     * @throws Exception Si el correo ya existe (HTTP 409) o fallan las validaciones.
     */
    public Usuario registrar(Usuario usuario) throws Exception {
        if (usuario == null) {
            throw new Exception("Los datos del usuario no pueden ser nulos.");
        }
        if (usuario.getEmail() == null || usuario.getEmail().trim().isEmpty()) {
            throw new Exception("El correo es requerido.");
        }
        if (usuario.getPassword() == null || usuario.getPassword().trim().isEmpty()) {
            throw new Exception("La contraseña es requerida.");
        }

        JSONObject body = usuario.toJSONRegistro();
        String respuesta = ConexionApi.ejecutarPeticion("POST", "/api/users", body.toString(), null);
        return Usuario.fromJSON(new JSONObject(respuesta));
    }

    /**
     * Solicita la recuperación de contraseña de una cuenta (POST /api/auth/forgot-password).
     *
     * @param email Correo electrónico registrado.
     * @return Mensaje descriptivo retornado por el backend.
     * @throws Exception Si el correo es inválido (HTTP 400).
     */
    public String recuperarClave(String email) throws Exception {
        if (email == null || email.trim().isEmpty()) {
            throw new Exception("Debe ingresar su correo electrónico.");
        }

        JSONObject body = new JSONObject();
        body.put("email", email.trim());

        String respuesta = ConexionApi.ejecutarPeticion("POST", "/api/auth/forgot-password", body.toString(), null);
        JSONObject jsonResp = new JSONObject(respuesta);

        if (jsonResp.has("message")) {
            return jsonResp.getString("message");
        }
        return "Solicitud procesada correctamente.";
    }
}
