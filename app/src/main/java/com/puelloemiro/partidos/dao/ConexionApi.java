package com.puelloemiro.partidos.dao;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Clase utilitaria para la comunicación en red con la API REST mediante HttpURLConnection.
 * Siguiendo las directrices del profesor, no se utilizan librerías externas de terceros
 * (como Retrofit, OkHttp o Volley), sino las herramientas nativas que provee el SDK de Android.
 *
 * Configura un tiempo de espera (timeout) de 60 segundos debido a que el servidor en Render
 * puede entrar en estado de suspensión en el plan gratuito.
 * Lee adecuadamente las respuestas de éxito y los flujos de error para extraer el campo "message".
 *
 * @author Emiro Puello
 */
public class ConexionApi {

    // URL base del backend desplegado en la nube
    public static final String BASE_URL = "https://partidos-api.onrender.com";

    // Tiempo de espera para la conexión y lectura en milisegundos (60 segundos)
    private static final int TIMEOUT_MS = 60000;

    /**
     * Ejecuta una petición HTTP genérica (GET, POST, PUT, DELETE) contra el backend.
     *
     * @param metodo Método HTTP a ejecutar ("GET", "POST", "PUT", "DELETE").
     * @param rutaEndpoint Ruta relativa del recurso (ej: "/api/auth/login" o "/api/partidos-politicos").
     * @param jsonBody Cadena en formato JSON para el cuerpo de la petición (puede ser null para GET o DELETE).
     * @param token Token JWT para la cabecera Authorization (opcional, puede ser null si es endpoint público).
     * @return Cadena de texto con la respuesta del servidor en caso de éxito (código 200..299).
     * @throws Exception Si la respuesta HTTP no es exitosa o si ocurre un fallo de conexión.
     *                   El mensaje de la excepción corresponde al campo "message" devuelto por la API.
     */
    public static String ejecutarPeticion(String metodo, String rutaEndpoint, String jsonBody, String token) throws Exception {
        HttpURLConnection conexion = null;
        try {
            // Construcción de la URL absoluta
            String urlCompleta = BASE_URL + rutaEndpoint;
            URL url = new URL(urlCompleta);

            // Apertura de la conexión HTTP nativa
            conexion = (HttpURLConnection) url.openConnection();
            conexion.setRequestMethod(metodo);
            conexion.setConnectTimeout(TIMEOUT_MS);
            conexion.setReadTimeout(TIMEOUT_MS);

            // Cabeceras HTTP estándar
            conexion.setRequestProperty("Accept", "application/json");

            // Si se proporciona un token de autenticación, se incluye en el encabezado Authorization
            if (token != null && !token.trim().isEmpty()) {
                conexion.setRequestProperty("Authorization", "Bearer " + token.trim());
            }

            // Si hay un cuerpo para enviar (POST o PUT)
            if (jsonBody != null && !jsonBody.trim().isEmpty()) {
                conexion.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conexion.setDoOutput(true);

                // Escritura de los datos en el flujo de salida
                try (OutputStream os = conexion.getOutputStream()) {
                    byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                    os.flush();
                }
            }

            // Obtención del código de estado HTTP retornado por el servidor
            int codigoEstado = conexion.getResponseCode();

            // Si el código está en el rango 200 - 299 se considera exitoso
            if (codigoEstado >= 200 && codigoEstado < 300) {
                // Manejo especial para respuestas 204 No Content (como en DELETE)
                if (codigoEstado == 204) {
                    return "";
                }
                return leerFlujo(conexion.getInputStream());
            } else {
                // Si el servidor devolvió un error (400, 401, 404, 409, 500, etc.)
                String cuerpoError = "";
                if (conexion.getErrorStream() != null) {
                    cuerpoError = leerFlujo(conexion.getErrorStream());
                }

                // El backend retorna errores con la estructura {"status":int,"message":"..."}
                String mensajeError = extraerMensajeDeError(cuerpoError, codigoEstado);
                throw new Exception(mensajeError);
            }

        } finally {
            if (conexion != null) {
                conexion.disconnect();
            }
        }
    }

    /**
     * Lee un InputStream de forma segura convirtiéndolo en un String con codificación UTF-8.
     *
     * @param stream Flujo de entrada de datos.
     * @return Contenido completo del flujo en formato String.
     * @throws Exception Si ocurre un error de lectura.
     */
    private static String leerFlujo(InputStream stream) throws Exception {
        if (stream == null) {
            return "";
        }
        StringBuilder respuesta = new StringBuilder();
        try (BufferedReader lector = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String linea;
            while ((linea = lector.readLine()) != null) {
                respuesta.append(linea);
            }
        }
        return respuesta.toString();
    }

    /**
     * Intenta extraer el campo "message" del JSON de error retornado por la API.
     * Si no es posible parsear el JSON, devuelve un mensaje genérico con el código HTTP.
     *
     * @param cuerpoError Cadena recibida del getErrorStream().
     * @param codigoEstado Código de respuesta HTTP.
     * @return Mensaje entendible para ser mostrado al usuario mediante Toast.
     */
    private static String extraerMensajeDeError(String cuerpoError, int codigoEstado) {
        if (cuerpoError != null && !cuerpoError.trim().isEmpty()) {
            try {
                JSONObject jsonError = new JSONObject(cuerpoError);
                if (jsonError.has("message")) {
                    return jsonError.getString("message");
                }
            } catch (Exception ignored) {
                // Si no tiene formato JSON válido, se usa el cuerpo crudo si es corto
                if (cuerpoError.length() < 150) {
                    return cuerpoError;
                }
            }
        }

        switch (codigoEstado) {
            case 401:
                return "No autorizado o sesión expirada.";
            case 404:
                return "Recurso no encontrado.";
            case 409:
                return "Conflicto con los datos existentes.";
            case 500:
                return "Error interno del servidor.";
            default:
                return "Error en la operación (HTTP " + codigoEstado + ").";
        }
    }
}
