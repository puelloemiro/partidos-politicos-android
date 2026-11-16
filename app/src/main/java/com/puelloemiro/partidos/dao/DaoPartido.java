package com.puelloemiro.partidos.dao;

import com.puelloemiro.partidos.Sesion;
import com.puelloemiro.partidos.entidades.Partido;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) para la gestión de Partidos Políticos.
 * En la arquitectura MVC propuesta por el docente, los objetos DAO encapsulan
 * las operaciones de acceso a datos (en este caso reemplazan a SQLite comunicándose
 * directamente con la API REST del backend).
 *
 * Se encarga de serializar y deserializar los objetos Partido utilizando org.json.
 *
 * @author Emiro Puello
 */
public class DaoPartido {

    private static final String RUTA_PARTIDOS = "/api/partidos-politicos";

    /**
     * Registra un nuevo partido político en el backend (POST /api/partidos-politicos).
     * El cuerpo JSON incluye el ID proporcionado por el usuario y los contadores como números.
     *
     * @param partido Objeto Partido a registrar.
     * @return El partido creado retornado por el servidor.
     * @throws Exception Si ocurre algún error de validación o red.
     */
    public Partido agregarPartido(Partido partido) throws Exception {
        if (partido == null) {
            throw new Exception("El partido no puede ser nulo.");
        }
        // Incluir el ID en el body para el POST
        JSONObject jsonBody = partido.toJSON(true);
        String respuesta = ConexionApi.ejecutarPeticion("POST", RUTA_PARTIDOS, jsonBody.toString(), Sesion.token);
        return Partido.fromJSON(new JSONObject(respuesta));
    }

    /**
     * Modifica un partido político existente (PUT /api/partidos-politicos/{id}).
     * IMPORTANTE: Según la especificación del backend, el ID va únicamente en la URL,
     * NO en el cuerpo del JSON.
     *
     * @param partido Objeto Partido con los datos actualizados.
     * @return El partido modificado retornado por el servidor.
     * @throws Exception Si el ID no existe o la petición falla.
     */
    public Partido modificarPartido(Partido partido) throws Exception {
        if (partido == null || partido.getId() == null || partido.getId().trim().isEmpty()) {
            throw new Exception("El ID del partido es obligatorio para modificar.");
        }
        String idCodificado = URLEncoder.encode(partido.getId().trim(), StandardCharsets.UTF_8.name());
        String ruta = RUTA_PARTIDOS + "/" + idCodificado;

        // NO incluir el ID en el body para el PUT
        JSONObject jsonBody = partido.toJSON(false);
        String respuesta = ConexionApi.ejecutarPeticion("PUT", ruta, jsonBody.toString(), Sesion.token);
        return Partido.fromJSON(new JSONObject(respuesta));
    }

    /**
     * Elimina un partido político por su ID (DELETE /api/partidos-politicos/{id}).
     *
     * @param id Identificador único del partido.
     * @throws Exception Si el partido no existe o no se puede eliminar.
     */
    public void borrarPartido(String id) throws Exception {
        if (id == null || id.trim().isEmpty()) {
            throw new Exception("Debe especificar el ID del partido a eliminar.");
        }
        String idCodificado = URLEncoder.encode(id.trim(), StandardCharsets.UTF_8.name());
        String ruta = RUTA_PARTIDOS + "/" + idCodificado;
        ConexionApi.ejecutarPeticion("DELETE", ruta, null, Sesion.token);
    }

    /**
     * Consulta un único partido político por su identificador (GET /api/partidos-politicos/{id}).
     *
     * @param id Identificador único del partido.
     * @return El objeto Partido encontrado.
     * @throws Exception Si no se encuentra el partido o ocurre un fallo de red.
     */
    public Partido consultarUnPartido(String id) throws Exception {
        if (id == null || id.trim().isEmpty()) {
            throw new Exception("Debe especificar el ID a buscar.");
        }
        String idCodificado = URLEncoder.encode(id.trim(), StandardCharsets.UTF_8.name());
        String ruta = RUTA_PARTIDOS + "/" + idCodificado;
        String respuesta = ConexionApi.ejecutarPeticion("GET", ruta, null, Sesion.token);
        return Partido.fromJSON(new JSONObject(respuesta));
    }

    /**
     * Lista todos los partidos políticos registrados en el sistema (GET /api/partidos-politicos).
     *
     * @return Lista de objetos Partido.
     * @throws Exception Si falla la consulta.
     */
    public List<Partido> listarTodosLosPartidos() throws Exception {
        String respuesta = ConexionApi.ejecutarPeticion("GET", RUTA_PARTIDOS, null, Sesion.token);
        return parsearListaPartidos(respuesta);
    }

    /**
     * Obtiene el reporte de partidos políticos filtrados por país (GET /api/partidos-politicos/reportes/por-pais?pais=).
     *
     * @param pais Nombre del país a consultar.
     * @return Lista de partidos del país indicado.
     * @throws Exception Si ocurre un fallo en la consulta.
     */
    public List<Partido> reportePorPais(String pais) throws Exception {
        if (pais == null || pais.trim().isEmpty()) {
            throw new Exception("Debe ingresar un país para consultar.");
        }
        String paisCodificado = URLEncoder.encode(pais.trim(), StandardCharsets.UTF_8.name());
        String ruta = RUTA_PARTIDOS + "/reportes/por-pais?pais=" + paisCodificado;
        String respuesta = ConexionApi.ejecutarPeticion("GET", ruta, null, Sesion.token);
        return parsearListaPartidos(respuesta);
    }

    /**
     * Obtiene el reporte de partidos políticos con un número mínimo de alcaldes
     * (GET /api/partidos-politicos/reportes/por-alcaldes?min=).
     *
     * @param min Cantidad mínima de alcaldes.
     * @return Lista de partidos que cumplen la condición.
     * @throws Exception Si ocurre un fallo en la consulta.
     */
    public List<Partido> reportePorAlcaldes(int min) throws Exception {
        String ruta = RUTA_PARTIDOS + "/reportes/por-alcaldes?min=" + min;
        String respuesta = ConexionApi.ejecutarPeticion("GET", ruta, null, Sesion.token);
        return parsearListaPartidos(respuesta);
    }

    /**
     * Método auxiliar para transformar una respuesta JSON con arreglo en una lista Java de Partidos.
     *
     * @param jsonString Cadena JSON que representa un arreglo JSONArray.
     * @return Lista poblada de instancias de Partido.
     * @throws Exception Si la cadena no es un arreglo JSON válido.
     */
    private List<Partido> parsearListaPartidos(String jsonString) throws Exception {
        List<Partido> lista = new ArrayList<>();
        if (jsonString == null || jsonString.trim().isEmpty()) {
            return lista;
        }
        JSONArray arreglo = new JSONArray(jsonString);
        for (int i = 0; i < arreglo.length(); i++) {
            JSONObject obj = arreglo.getJSONObject(i);
            lista.add(Partido.fromJSON(obj));
        }
        return lista;
    }
}
