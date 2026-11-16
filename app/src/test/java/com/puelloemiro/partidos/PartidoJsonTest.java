package com.puelloemiro.partidos;

import com.puelloemiro.partidos.entidades.Partido;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Pruebas unitarias con JUnit para verificar el armado y parseo de JSON
 * en la entidad Partido, garantizando que:
 * 1. Los contadores se envíen como números (no como cadenas de texto).
 * 2. En las peticiones PUT, el campo 'id' NO se incluya en el cuerpo JSON.
 * 3. En las peticiones POST, el campo 'id' sí se incluya en el JSON.
 * 4. El método fromJSON reconstruya fielmente la entidad a partir del JSON del backend.
 *
 * @author Emiro Puello
 */
public class PartidoJsonTest {

    private Partido partidoPrueba;

    @Before
    public void setUp() {
        partidoPrueba = new Partido(
                "P101",
                "Partido Liberal Democrata",
                "Libertad y Progreso",
                "Carlos Sanchez",
                "Elena Gomez",
                "Mauricio Lopez",
                "Colombia",
                2,
                7,
                15,
                32,
                11
        );
    }

    @Test
    public void testPostJsonIncluyeIdYContadoresComoNumeros() throws Exception {
        // En POST, incluirId = true
        JSONObject json = partidoPrueba.toJSON(true);

        assertNotNull("El JSON generado no debe ser nulo", json);
        assertTrue("El JSON para POST debe contener el campo 'id'", json.has("id"));
        assertEquals("P101", json.getString("id"));
        assertEquals("Partido Liberal Democrata", json.getString("nombre"));

        // Verificamos que los contadores son números (se leen con getInt y sus tipos en JSONObject son Integer)
        assertEquals(2, json.getInt("numPresidentes"));
        assertEquals(7, json.getInt("numGobernadores"));
        assertEquals(15, json.getInt("numAlcaldes"));
        assertEquals(32, json.getInt("numConsejales"));
        assertEquals(11, json.getInt("numCongresistas"));

        // Verificamos que el valor almacenado es de tipo numérico (Number)
        assertTrue(json.get("numPresidentes") instanceof Number);
        assertTrue(json.get("numGobernadores") instanceof Number);
        assertTrue(json.get("numAlcaldes") instanceof Number);
        assertTrue(json.get("numConsejales") instanceof Number);
        assertTrue(json.get("numCongresistas") instanceof Number);
    }

    @Test
    public void testPutJsonNoContieneIdEnElBody() throws Exception {
        // En PUT, incluirId = false (el ID va únicamente en la URL)
        JSONObject json = partidoPrueba.toJSON(false);

        assertNotNull("El JSON generado no debe ser nulo", json);
        assertFalse("El JSON para PUT NO debe contener el campo 'id'", json.has("id"));

        // Se deben mantener los demás campos y contadores numéricos
        assertEquals("Partido Liberal Democrata", json.getString("nombre"));
        assertEquals("Colombia", json.getString("pais"));
        assertEquals(15, json.getInt("numAlcaldes"));
        assertTrue(json.get("numAlcaldes") instanceof Number);
    }

    @Test
    public void testFromJSONReconstruyePartidoCorrectamente() throws Exception {
        String jsonTexto = "{"
                + "\"id\":\"PB100\","
                + "\"nombre\":\"Partido Verde\","
                + "\"eslogan\":\"Por el planeta\","
                + "\"presidente\":\"Ana Beltran\","
                + "\"secretario\":\"Juan Rivas\","
                + "\"tesorero\":\"Laura Cano\","
                + "\"pais\":\"Ecuador\","
                + "\"numPresidentes\":1,"
                + "\"numGobernadores\":3,"
                + "\"numAlcaldes\":8,"
                + "\"numConsejales\":14,"
                + "\"numCongresistas\":5"
                + "}";

        JSONObject json = new JSONObject(jsonTexto);
        Partido partido = Partido.fromJSON(json);

        assertNotNull("El objeto partido no debe ser nulo", partido);
        assertEquals("PB100", partido.getId());
        assertEquals("Partido Verde", partido.getNombre());
        assertEquals("Por el planeta", partido.getEslogan());
        assertEquals("Ana Beltran", partido.getPresidente());
        assertEquals("Juan Rivas", partido.getSecretario());
        assertEquals("Laura Cano", partido.getTesorero());
        assertEquals("Ecuador", partido.getPais());
        assertEquals(1, partido.getNumPresidentes());
        assertEquals(3, partido.getNumGobernadores());
        assertEquals(8, partido.getNumAlcaldes());
        assertEquals(14, partido.getNumConsejales());
        assertEquals(5, partido.getNumCongresistas());
    }
}
