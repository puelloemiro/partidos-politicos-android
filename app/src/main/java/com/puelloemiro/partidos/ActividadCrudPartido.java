package com.puelloemiro.partidos;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.puelloemiro.partidos.dao.DaoPartido;
import com.puelloemiro.partidos.entidades.Partido;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Actividad para la gestión completa (CRUD) de un Partido Político.
 * Permite Guardar, Buscar, Modificar, Eliminar y Limpiar el formulario.
 *
 * Incluye:
 * 1. Diálogo de búsqueda personalizado mediante LayoutInflater inflando el XML
 *    dialogo_buscar_partido.xml ("Digite el ID del partido:").
 * 2. Sensor Acelerómetro (Shake Detection): al agitar el teléfono ejecuta limpiarCampos
 *    y muestra el Toast "Formulario limpiado", con un anti-rebote de 1 segundo.
 * 3. Hilos secundarios para peticiones de red y ProgressBar para feedback visual.
 *
 * @author Emiro Puello
 */
public class ActividadCrudPartido extends AppCompatActivity implements SensorEventListener {

    // Controles visuales del formulario
    private EditText etId;
    private EditText etNombre;
    private EditText etEslogan;
    private EditText etPresidente;
    private EditText etSecretario;
    private EditText etTesorero;
    private EditText etPais;
    private EditText etNumPresidentes;
    private EditText etNumGobernadores;
    private EditText etNumAlcaldes;
    private EditText etNumConsejales;
    private EditText etNumCongresistas;

    private Button btnGuardar;
    private Button btnModificar;
    private Button btnEliminar;
    private Button btnBuscar;
    private Button btnLimpiar;
    private ProgressBar pbCrud;

    // Capa de persistencia (DAO) y ejecutor asíncrono
    private DaoPartido daoPartido;
    private ExecutorService executor;

    // Gestión del sensor acelerómetro para detección de agitación (Shake)
    private SensorManager sensorManager;
    private Sensor acelerometro;
    private long ultimoTiempoShake = 0;
    private static final long INTERVALO_ANTI_REBOTE_SHAKE = 1000; // 1 segundo
    private static final float UMBRAL_SHAKE = 13.0f; // Umbral de aceleración neta

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actividad_crud_partido);

        // Enlace de controles del layout
        vincularVistas();

        daoPartido = new DaoPartido();
        executor = Executors.newSingleThreadExecutor();

        // Configuración del sensor acelerómetro
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager != null) {
            acelerometro = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }

        // Si se abrió la actividad desde la lista de partidos pasando un ID por Intent
        if (getIntent() != null && getIntent().hasExtra("ID_PARTIDO")) {
            String idRecibido = getIntent().getStringExtra("ID_PARTIDO");
            if (idRecibido != null && !idRecibido.trim().isEmpty()) {
                etId.setText(idRecibido);
                consultarPorId(idRecibido);
            }
        }
    }

    private void vincularVistas() {
        etId = findViewById(R.id.etId);
        etNombre = findViewById(R.id.etNombre);
        etEslogan = findViewById(R.id.etEslogan);
        etPresidente = findViewById(R.id.etPresidente);
        etSecretario = findViewById(R.id.etSecretario);
        etTesorero = findViewById(R.id.etTesorero);
        etPais = findViewById(R.id.etPais);
        etNumPresidentes = findViewById(R.id.etNumPresidentes);
        etNumGobernadores = findViewById(R.id.etNumGobernadores);
        etNumAlcaldes = findViewById(R.id.etNumAlcaldes);
        etNumConsejales = findViewById(R.id.etNumConsejales);
        etNumCongresistas = findViewById(R.id.etNumCongresistas);

        btnGuardar = findViewById(R.id.btnGuardar);
        btnModificar = findViewById(R.id.btnModificar);
        btnEliminar = findViewById(R.id.btnEliminar);
        btnBuscar = findViewById(R.id.btnBuscar);
        btnLimpiar = findViewById(R.id.btnLimpiar);
        pbCrud = findViewById(R.id.pbCrud);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Registro del listener del acelerómetro
        if (sensorManager != null && acelerometro != null) {
            sensorManager.registerListener(this, acelerometro, SensorManager.SENSOR_DELAY_UI);
        } else {
            Toast.makeText(this, R.string.msg_no_sensor_acelerometro, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Desregistro del listener para evitar consumo innecesario de batería
        if (sensorManager != null && acelerometro != null) {
            sensorManager.unregisterListener(this);
        }
    }

    // --- Lógica del Acelerómetro (Shake Detection) ---

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            // Cálculo de la aceleración neta restando la fuerza gravitacional terrestre
            double aceleracionNeta = Math.sqrt(x * x + y * y + z * z) - SensorManager.GRAVITY_EARTH;

            if (aceleracionNeta > UMBRAL_SHAKE) {
                long tiempoActual = System.currentTimeMillis();
                // Verificación del anti-rebote de 1 segundo (1000 ms)
                if (tiempoActual - ultimoTiempoShake > INTERVALO_ANTI_REBOTE_SHAKE) {
                    ultimoTiempoShake = tiempoActual;

                    // Ejecutar limpiarCampos y mostrar el Toast indicado
                    limpiarCampos(null);
                    Toast.makeText(this, R.string.msg_formulario_limpiado, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // No requerido
    }

    // --- Métodos de Acción para los Botones (android:onClick) ---

    /**
     * Limpia todos los campos de texto del formulario (android:onClick="limpiarCampos").
     *
     * @param v Vista del botón (o null cuando se invoca desde el sensor acelerómetro).
     */
    public void limpiarCampos(View v) {
        etId.setText("");
        etNombre.setText("");
        etEslogan.setText("");
        etPresidente.setText("");
        etSecretario.setText("");
        etTesorero.setText("");
        etPais.setText("");
        etNumPresidentes.setText("");
        etNumGobernadores.setText("");
        etNumAlcaldes.setText("");
        etNumConsejales.setText("");
        etNumCongresistas.setText("");
        etId.requestFocus();
    }

    /**
     * Guarda un nuevo partido político en el backend (android:onClick="guardarPartido").
     *
     * @param v Vista del botón.
     */
    public void guardarPartido(View v) {
        Partido p = construirPartidoDesdeFormulario(true);
        if (p == null) {
            return; // Falló la validación
        }

        mostrarCargando(true);
        executor.execute(() -> {
            try {
                Partido creado = daoPartido.agregarPartido(p);
                runOnUiThread(() -> {
                    mostrarCargando(false);
                    Toast.makeText(ActividadCrudPartido.this,
                            "Partido '" + creado.getNombre() + "' registrado exitosamente.",
                            Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    mostrarCargando(false);
                    String msg = e.getMessage() != null ? e.getMessage() : "Error al guardar el partido.";
                    Toast.makeText(ActividadCrudPartido.this, msg, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    /**
     * Modifica un partido político existente (android:onClick="modificarPartido").
     * En el backend el ID va en la URL, NO en el body.
     *
     * @param v Vista del botón.
     */
    public void modificarPartido(View v) {
        Partido p = construirPartidoDesdeFormulario(true);
        if (p == null) {
            return;
        }

        mostrarCargando(true);
        executor.execute(() -> {
            try {
                Partido actualizado = daoPartido.modificarPartido(p);
                runOnUiThread(() -> {
                    mostrarCargando(false);
                    Toast.makeText(ActividadCrudPartido.this,
                            "Partido '" + actualizado.getNombre() + "' actualizado exitosamente.",
                            Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    mostrarCargando(false);
                    String msg = e.getMessage() != null ? e.getMessage() : "Error al modificar el partido.";
                    Toast.makeText(ActividadCrudPartido.this, msg, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    /**
     * Elimina un partido político por su ID (android:onClick="eliminarPartido").
     *
     * @param v Vista del botón.
     */
    public void eliminarPartido(View v) {
        String id = etId.getText().toString().trim();
        if (id.isEmpty()) {
            etId.setError("Debe digitar el ID para eliminar");
            etId.requestFocus();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Eliminar Partido")
                .setMessage("¿Está seguro de eliminar el partido con ID '" + id + "'?")
                .setPositiveButton("Sí, eliminar", (dialog, which) -> {
                    mostrarCargando(true);
                    executor.execute(() -> {
                        try {
                            daoPartido.borrarPartido(id);
                            runOnUiThread(() -> {
                                mostrarCargando(false);
                                limpiarCampos(null);
                                Toast.makeText(ActividadCrudPartido.this,
                                        "Partido eliminado correctamente.",
                                        Toast.LENGTH_SHORT).show();
                            });
                        } catch (Exception e) {
                            runOnUiThread(() -> {
                                mostrarCargando(false);
                                String msg = e.getMessage() != null ? e.getMessage() : "Error al eliminar el partido.";
                                Toast.makeText(ActividadCrudPartido.this, msg, Toast.LENGTH_LONG).show();
                            });
                        }
                    });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    /**
     * Muestra el diálogo personalizado para buscar un partido por ID
     * (android:onClick="mostrarDialogoBuscar").
     * Infla el layout dialogo_buscar_partido.xml.
     *
     * @param v Vista del botón.
     */
    public void mostrarDialogoBuscar(View v) {
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialogo_buscar_partido, null);
        EditText etIdBuscar = dialogView.findViewById(R.id.etIdBuscar);

        new AlertDialog.Builder(this)
                .setTitle(R.string.dialogo_buscar_titulo)
                .setView(dialogView)
                .setPositiveButton(R.string.btn_aceptar, (dialog, which) -> {
                    String idBuscado = etIdBuscar.getText().toString().trim();
                    if (!idBuscado.isEmpty()) {
                        consultarPorId(idBuscado);
                    } else {
                        Toast.makeText(ActividadCrudPartido.this,
                                "Debe digitar un ID válido.",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.btn_cancelar, null)
                .show();
    }

    /**
     * Realiza la consulta de un partido en la API por su identificador.
     *
     * @param id Identificador a consultar.
     */
    private void consultarPorId(String id) {
        mostrarCargando(true);
        executor.execute(() -> {
            try {
                Partido p = daoPartido.consultarUnPartido(id);
                runOnUiThread(() -> {
                    mostrarCargando(false);
                    cargarDatosEnFormulario(p);
                    Toast.makeText(ActividadCrudPartido.this,
                            "Partido cargado: " + p.getNombre(),
                            Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    mostrarCargando(false);
                    String msg = e.getMessage() != null ? e.getMessage() : "Partido no encontrado.";
                    Toast.makeText(ActividadCrudPartido.this, msg, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    /**
     * Rellena todos los campos del formulario con los datos de una entidad Partido.
     *
     * @param p Instancia de Partido.
     */
    private void cargarDatosEnFormulario(Partido p) {
        if (p == null) return;
        etId.setText(p.getId());
        etNombre.setText(p.getNombre());
        etEslogan.setText(p.getEslogan());
        etPresidente.setText(p.getPresidente());
        etSecretario.setText(p.getSecretario());
        etTesorero.setText(p.getTesorero());
        etPais.setText(p.getPais());
        etNumPresidentes.setText(String.valueOf(p.getNumPresidentes()));
        etNumGobernadores.setText(String.valueOf(p.getNumGobernadores()));
        etNumAlcaldes.setText(String.valueOf(p.getNumAlcaldes()));
        etNumConsejales.setText(String.valueOf(p.getNumConsejales()));
        etNumCongresistas.setText(String.valueOf(p.getNumCongresistas()));
    }

    /**
     * Valida el formulario y construye el objeto Partido.
     * Verifica que no existan campos vacíos y que los contadores sean números válidos.
     *
     * @param requiereId true si el ID es obligatorio.
     * @return Objeto Partido o null si falla alguna validación.
     */
    private Partido construirPartidoDesdeFormulario(boolean requiereId) {
        String id = etId.getText().toString().trim();
        String nombre = etNombre.getText().toString().trim();
        String eslogan = etEslogan.getText().toString().trim();
        String presidente = etPresidente.getText().toString().trim();
        String secretario = etSecretario.getText().toString().trim();
        String tesorero = etTesorero.getText().toString().trim();
        String pais = etPais.getText().toString().trim();

        String strPres = etNumPresidentes.getText().toString().trim();
        String strGob = etNumGobernadores.getText().toString().trim();
        String strAlc = etNumAlcaldes.getText().toString().trim();
        String strCons = etNumConsejales.getText().toString().trim();
        String strCong = etNumCongresistas.getText().toString().trim();

        if (requiereId && id.isEmpty()) {
            etId.setError("El ID es obligatorio");
            etId.requestFocus();
            return null;
        }

        if (nombre.isEmpty()) {
            etNombre.setError("El nombre es obligatorio");
            etNombre.requestFocus();
            return null;
        }

        if (pais.isEmpty()) {
            etPais.setError("El país es obligatorio");
            etPais.requestFocus();
            return null;
        }

        // Conversión y validación de campos numéricos (enteros JSON)
        int numPres, numGob, numAlc, numCons, numCong;
        try {
            numPres = strPres.isEmpty() ? 0 : Integer.parseInt(strPres);
            numGob = strGob.isEmpty() ? 0 : Integer.parseInt(strGob);
            numAlc = strAlc.isEmpty() ? 0 : Integer.parseInt(strAlc);
            numCons = strCons.isEmpty() ? 0 : Integer.parseInt(strCons);
            numCong = strCong.isEmpty() ? 0 : Integer.parseInt(strCong);

            if (numPres < 0 || numGob < 0 || numAlc < 0 || numCons < 0 || numCong < 0) {
                Toast.makeText(this, R.string.error_numeros_invalidos, Toast.LENGTH_SHORT).show();
                return null;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, R.string.error_numeros_invalidos, Toast.LENGTH_SHORT).show();
            return null;
        }

        return new Partido(id, nombre, eslogan, presidente, secretario, tesorero, pais,
                numPres, numGob, numAlc, numCons, numCong);
    }

    private void mostrarCargando(boolean cargando) {
        if (pbCrud != null) {
            pbCrud.setVisibility(cargando ? View.VISIBLE : View.GONE);
        }
        if (btnGuardar != null) btnGuardar.setEnabled(!cargando);
        if (btnModificar != null) btnModificar.setEnabled(!cargando);
        if (btnEliminar != null) btnEliminar.setEnabled(!cargando);
        if (btnBuscar != null) btnBuscar.setEnabled(!cargando);
        if (btnLimpiar != null) btnLimpiar.setEnabled(!cargando);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }
}
