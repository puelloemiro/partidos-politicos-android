package com.puelloemiro.partidos.entidades;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Entidad que representa un Partido Político dentro del modelo del sistema (MVC).
 * Contiene todas las propiedades requeridas por el backend, encapsuladas con
 * modificadores de acceso privados y sus respectivos métodos getters y setters.
 *
 * @author Emiro Puello
 */
public class Partido implements Serializable {

    // Identificador único del partido (ejemplo: "PB100")
    private String id;

    // Nombre oficial del partido político
    private String nombre;

    // Eslogan o lema representativo
    private String eslogan;

    // Nombre del presidente del partido
    private String presidente;

    // Nombre del secretario general
    private String secretario;

    // Nombre del tesorero del partido
    private String tesorero;

    // País de origen o jurisdicción del partido
    private String pais;

    // Número de presidentes que ha tenido el partido
    private int numPresidentes;

    // Número de gobernadores
    private int numGobernadores;

    // Número de alcaldes
    private int numAlcaldes;

    // Número de consejales (se respeta la nomenclatura del backend: 'numConsejales')
    private int numConsejales;

    // Número de congresistas o parlamentarios
    private int numCongresistas;

    /**
     * Constructor vacío requerido por los estándares de JavaBeans y frameworks de serialización.
     */
    public Partido() {
    }

    /**
     * Constructor con todos los parámetros para inicializar una instancia completa de Partido.
     *
     * @param id Identificador único
     * @param nombre Nombre del partido
     * @param eslogan Eslogan o lema
     * @param presidente Nombre del presidente
     * @param secretario Nombre del secretario
     * @param tesorero Nombre del tesorero
     * @param pais País del partido
     * @param numPresidentes Cantidad de presidentes
     * @param numGobernadores Cantidad de gobernadores
     * @param numAlcaldes Cantidad de alcaldes
     * @param numConsejales Cantidad de consejales
     * @param numCongresistas Cantidad de congresistas
     */
    public Partido(String id, String nombre, String eslogan, String presidente,
                   String secretario, String tesorero, String pais,
                   int numPresidentes, int numGobernadores, int numAlcaldes,
                   int numConsejales, int numCongresistas) {
        this.id = id;
        this.nombre = nombre;
        this.eslogan = eslogan;
        this.presidente = presidente;
        this.secretario = secretario;
        this.tesorero = tesorero;
        this.pais = pais;
        this.numPresidentes = numPresidentes;
        this.numGobernadores = numGobernadores;
        this.numAlcaldes = numAlcaldes;
        this.numConsejales = numConsejales;
        this.numCongresistas = numCongresistas;
    }

    // --- Métodos Getters y Setters ---

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEslogan() {
        return eslogan;
    }

    public void setEslogan(String eslogan) {
        this.eslogan = eslogan;
    }

    public String getPresidente() {
        return presidente;
    }

    public void setPresidente(String presidente) {
        this.presidente = presidente;
    }

    public String getSecretario() {
        return secretario;
    }

    public void setSecretario(String secretario) {
        this.secretario = secretario;
    }

    public String getTesorero() {
        return tesorero;
    }

    public void setTesorero(String tesorero) {
        this.tesorero = tesorero;
    }

    public String getPais() {
        return pais;
    }

    public void setPais(String pais) {
        this.pais = pais;
    }

    public int getNumPresidentes() {
        return numPresidentes;
    }

    public void setNumPresidentes(int numPresidentes) {
        this.numPresidentes = numPresidentes;
    }

    public int getNumGobernadores() {
        return numGobernadores;
    }

    public void setNumGobernadores(int numGobernadores) {
        this.numGobernadores = numGobernadores;
    }

    public int getNumAlcaldes() {
        return numAlcaldes;
    }

    public void setNumAlcaldes(int numAlcaldes) {
        this.numAlcaldes = numAlcaldes;
    }

    public int getNumConsejales() {
        return numConsejales;
    }

    public void setNumConsejales(int numConsejales) {
        this.numConsejales = numConsejales;
    }

    public int getNumCongresistas() {
        return numCongresistas;
    }

    public void setNumCongresistas(int numCongresistas) {
        this.numCongresistas = numCongresistas;
    }

    /**
     * Convierte la instancia de Partido a un objeto JSON (org.json.JSONObject).
     * Los contadores numéricos se almacenan explícitamente como enteros (números JSON),
     * nunca como texto.
     *
     * @param incluirId Determina si se incluye la llave "id". En el método POST debe ir el ID,
     *                  pero en el método PUT el backend exige que el ID no vaya en el body.
     * @return JSONObject listo para ser enviado en el cuerpo de la petición HTTP.
     * @throws JSONException Si ocurre algún error en la construcción del objeto JSON.
     */
    public JSONObject toJSON(boolean incluirId) throws JSONException {
        JSONObject json = new JSONObject();

        if (incluirId && id != null) {
            json.put("id", id);
        }

        json.put("nombre", nombre != null ? nombre : "");
        json.put("eslogan", eslogan != null ? eslogan : "");
        json.put("presidente", presidente != null ? presidente : "");
        json.put("secretario", secretario != null ? secretario : "");
        json.put("tesorero", tesorero != null ? tesorero : "");
        json.put("pais", pais != null ? pais : "");

        // Los campos contadores se colocan como tipos numéricos (int) en el JSON
        json.put("numPresidentes", numPresidentes);
        json.put("numGobernadores", numGobernadores);
        json.put("numAlcaldes", numAlcaldes);
        json.put("numConsejales", numConsejales);
        json.put("numCongresistas", numCongresistas);

        return json;
    }

    /**
     * Construye una instancia de Partido a partir de un JSONObject retornado por la API.
     *
     * @param json Objeto JSON con los atributos del partido.
     * @return Instancia poblada de Partido.
     * @throws JSONException Si falla la lectura de algún campo.
     */
    public static Partido fromJSON(JSONObject json) throws JSONException {
        Partido p = new Partido();
        p.setId(json.optString("id", ""));
        p.setNombre(json.optString("nombre", ""));
        p.setEslogan(json.optString("eslogan", ""));
        p.setPresidente(json.optString("presidente", ""));
        p.setSecretario(json.optString("secretario", ""));
        p.setTesorero(json.optString("tesorero", ""));
        p.setPais(json.optString("pais", ""));

        // Se extraen los enteros utilizando optInt para evitar excepciones si vienen nulos
        p.setNumPresidentes(json.optInt("numPresidentes", 0));
        p.setNumGobernadores(json.optInt("numGobernadores", 0));
        p.setNumAlcaldes(json.optInt("numAlcaldes", 0));
        p.setNumConsejales(json.optInt("numConsejales", 0));
        p.setNumCongresistas(json.optInt("numCongresistas", 0));

        return p;
    }

    /**
     * Representación textual para visualización en adaptadores simples (ListView).
     */
    @Override
    public String toString() {
        return id + " - " + nombre + " (" + pais + ")\n" +
                "Alcaldes: " + numAlcaldes + " | Congresistas: " + numCongresistas;
    }
}
