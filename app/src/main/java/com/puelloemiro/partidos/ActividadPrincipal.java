package com.puelloemiro.partidos;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Actividad Principal (Menú de la aplicación).
 * Contiene el menú con los accesos hacia:
 * 1. Formulario CRUD para Guardar/Gestionar partidos.
 * 2. Listado de partidos en un ListView.
 * 3. Consultas de reportes.
 * 4. Cierre de sesión.
 *
 * Integra el Sensor de Proximidad mediante SensorEventListener:
 * - Se registra en onResume y se desregistra en onPause.
 * - Si no existe el sensor, notifica con un Toast sin generar fallos ni cierres forzados.
 * - Al detectar proximidad (mano cerca de la pantalla), muestra el mensaje solicitado:
 *   "Sensor de proximidad detectado".
 *
 * @author Emiro Puello
 */
public class ActividadPrincipal extends AppCompatActivity implements SensorEventListener {

    // Administrador y sensor de proximidad
    private SensorManager sensorManager;
    private Sensor sensorProximidad;

    // Control para evitar la repetición excesiva de alertas mientras la mano permanece cerca
    private long ultimoTiempoDeteccion = 0;
    private static final long INTERVALO_ANTI_REBOTE = 2000; // 2 segundos

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actividad_principal);

        // Inicialización del servicio de sensores de Android
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager != null) {
            sensorProximidad = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Verificación de sesión activa: si no hay token en memoria, redirige al Login
        if (!Sesion.tieneSesion()) {
            Toast.makeText(this, "Sesión no válida o expirada.", Toast.LENGTH_SHORT).show();
            irALogin();
            return;
        }

        // Registro del listener del sensor de proximidad en el ciclo de vida onResume
        if (sensorManager != null && sensorProximidad != null) {
            sensorManager.registerListener(this, sensorProximidad, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            // Si el dispositivo o emulador no cuenta con el sensor
            Toast.makeText(this, R.string.msg_no_sensor_proximidad, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Buena práctica Android: desregistrar listeners en onPause para ahorrar batería
        if (sensorManager != null && sensorProximidad != null) {
            sensorManager.unregisterListener(this);
        }
    }

    // --- Métodos de la interfaz SensorEventListener ---

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
            float distancia = event.values[0];
            // Se considera proximidad si la distancia es menor al rango máximo del sensor
            if (distancia < sensorProximidad.getMaximumRange()) {
                long ahora = System.currentTimeMillis();
                if (ahora - ultimoTiempoDeteccion > INTERVALO_ANTI_REBOTE) {
                    ultimoTiempoDeteccion = ahora;

                    // Despliegue de la notificación requerida
                    Toast.makeText(this, R.string.msg_sensor_proximidad, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // No se requiere acción ante cambios de precisión
    }

    // --- Manejadores de Eventos onClick para la navegación (MVC) ---

    /**
     * Navega a la pantalla del formulario CRUD de Partidos (android:onClick="irAGuardarPartido").
     *
     * @param v Vista del botón.
     */
    public void irAGuardarPartido(View v) {
        Intent intent = new Intent(this, ActividadCrudPartido.class);
        startActivity(intent);
    }

    /**
     * Navega a la pantalla con el ListView de todos los partidos (android:onClick="irAListarPartidos").
     *
     * @param v Vista del botón.
     */
    public void irAListarPartidos(View v) {
        Intent intent = new Intent(this, ActividadListaPartidos.class);
        startActivity(intent);
    }

    /**
     * Navega a la pantalla de Reportes (android:onClick="irAReportes").
     *
     * @param v Vista del botón.
     */
    public void irAReportes(View v) {
        Intent intent = new Intent(this, ActividadReportes.class);
        startActivity(intent);
    }

    /**
     * Cierra la sesión activa en memoria y devuelve al usuario a la pantalla de Login (android:onClick="cerrarSesion").
     *
     * @param v Vista del botón.
     */
    public void cerrarSesion(View v) {
        new AlertDialog.Builder(this)
                .setTitle("Cerrar Sesión")
                .setMessage("¿Está seguro de que desea salir del sistema?")
                .setPositiveButton("Sí, salir", (dialog, which) -> {
                    Sesion.cerrarSesion();
                    Toast.makeText(ActividadPrincipal.this, R.string.sesion_cerrada, Toast.LENGTH_SHORT).show();
                    irALogin();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void irALogin() {
        Intent intent = new Intent(this, ActividadLogin.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
