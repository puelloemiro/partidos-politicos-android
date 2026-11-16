package com.puelloemiro.partidos;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.puelloemiro.partidos.dao.DaoUsuario;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Actividad inicial de autenticación (Login).
 * En la arquitectura MVC, esta clase actúa como Controlador de la vista actividad_login.xml.
 *
 * Captura los eventos del usuario mediante métodos públicos asociados con android:onClick,
 * gestiona la validación de formularios, ejecuta las peticiones asíncronas en hilos secundarios
 * utilizando ExecutorService y actualiza la interfaz de usuario con runOnUiThread.
 *
 * @author Emiro Puello
 */
public class ActividadLogin extends AppCompatActivity {

    // Componentes visuales de la vista XML
    private EditText etEmail;
    private EditText etPassword;
    private Button btnIniciarSesion;
    private ProgressBar pbLogin;

    // Objeto DAO para el acceso a las funciones de autenticación
    private DaoUsuario daoUsuario;

    // Pool de hilos para ejecutar peticiones HTTP en segundo plano y no bloquear el hilo principal (UI Thread)
    private ExecutorService executor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Asociación de la vista XML con el controlador Java
        setContentView(R.layout.actividad_login);

        // Enlace de componentes por su ID definido en el layout
        etEmail = findViewById(R.id.etEmailLogin);
        etPassword = findViewById(R.id.etPasswordLogin);
        btnIniciarSesion = findViewById(R.id.btnIniciarSesion);
        pbLogin = findViewById(R.id.pbLogin);

        // Inicialización de la capa DAO y el ejecutor de hilos
        daoUsuario = new DaoUsuario();
        executor = Executors.newSingleThreadExecutor();
    }

    /**
     * Manejador del evento onClick para iniciar sesión (android:onClick="iniciarSesion").
     *
     * @param v Vista que disparó el evento (el botón de login).
     */
    public void iniciarSesion(View v) {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validación de campos vacíos en el formulario
        if (email.isEmpty()) {
            etEmail.setError("Ingrese su correo electrónico");
            etEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("Ingrese su contraseña");
            etPassword.requestFocus();
            return;
        }

        // Indicador visual de carga: se deshabilita el botón y se muestra la barra de progreso
        mostrarCargando(true);

        // Ejecución en segundo plano para cumplir la regla de Android: "No Network on Main Thread"
        executor.execute(() -> {
            try {
                // Llamada a la API a través del DAO
                String token = daoUsuario.login(email, password);

                // El token se guarda en memoria en la clase Sesion (nunca en disco)
                Sesion.token = token;

                // El resultado y la actualización visual deben realizarse en el hilo principal
                runOnUiThread(() -> {
                    mostrarCargando(false);
                    Toast.makeText(ActividadLogin.this, "Inicio de sesión exitoso.", Toast.LENGTH_SHORT).show();

                    // Transición hacia el Menú Principal
                    Intent intent = new Intent(ActividadLogin.this, ActividadPrincipal.class);
                    startActivity(intent);
                    finish(); // Se finaliza el Login para no regresar con el botón atrás
                });

            } catch (Exception e) {
                // Captura y despliegue del mensaje exacto retornado por el backend
                runOnUiThread(() -> {
                    mostrarCargando(false);
                    String mensaje = e.getMessage() != null ? e.getMessage() : "Error al conectar con el servidor.";
                    Toast.makeText(ActividadLogin.this, mensaje, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    /**
     * Manejador del evento onClick para abrir la pantalla de Registro.
     *
     * @param v Vista que disparó el evento.
     */
    public void irARegistro(View v) {
        Intent intent = new Intent(this, ActividadRegistro.class);
        startActivity(intent);
    }

    /**
     * Manejador del evento onClick para abrir la pantalla de Recuperación de Clave.
     *
     * @param v Vista que disparó el evento.
     */
    public void irARecuperarClave(View v) {
        Intent intent = new Intent(this, ActividadRecuperarClave.class);
        startActivity(intent);
    }

    /**
     * Controla el estado visual de los controles durante operaciones asíncronas de red.
     *
     * @param cargando true para mostrar barra de progreso y deshabilitar botón, false al terminar.
     */
    private void mostrarCargando(boolean cargando) {
        if (pbLogin != null) {
            pbLogin.setVisibility(cargando ? View.VISIBLE : View.GONE);
        }
        if (btnIniciarSesion != null) {
            btnIniciarSesion.setEnabled(!cargando);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Liberación segura del ejecutor de hilos
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }
}
