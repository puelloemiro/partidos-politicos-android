package com.puelloemiro.partidos;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.puelloemiro.partidos.dao.DaoUsuario;
import com.puelloemiro.partidos.entidades.Usuario;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Actividad encargada del registro de nuevos usuarios en la plataforma.
 * Conecta con el endpoint POST /api/users del backend.
 *
 * @author Emiro Puello
 */
public class ActividadRegistro extends AppCompatActivity {

    private EditText etNombre;
    private EditText etEmail;
    private EditText etPassword;
    private Button btnRegistrar;
    private ProgressBar pbRegistro;

    private DaoUsuario daoUsuario;
    private ExecutorService executor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actividad_registro);

        etNombre = findViewById(R.id.etNombreRegistro);
        etEmail = findViewById(R.id.etEmailRegistro);
        etPassword = findViewById(R.id.etPasswordRegistro);
        btnRegistrar = findViewById(R.id.btnRegistrar);
        pbRegistro = findViewById(R.id.pbRegistro);

        daoUsuario = new DaoUsuario();
        executor = Executors.newSingleThreadExecutor();
    }

    /**
     * Evento onClick para procesar el registro del usuario (android:onClick="registrarUsuario").
     *
     * @param v Vista del botón.
     */
    public void registrarUsuario(View v) {
        String nombre = etNombre.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validaciones de entrada
        if (nombre.isEmpty()) {
            etNombre.setError("Ingrese su nombre completo");
            etNombre.requestFocus();
            return;
        }

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

        mostrarCargando(true);

        // Se construye la entidad Usuario (el ID toma el valor del email y rol MEMBER por defecto)
        Usuario nuevoUsuario = new Usuario(nombre, email, password);

        // Operación de red en hilo secundario
        executor.execute(() -> {
            try {
                daoUsuario.registrar(nuevoUsuario);

                runOnUiThread(() -> {
                    mostrarCargando(false);
                    Toast.makeText(ActividadRegistro.this,
                            "Usuario registrado con éxito. Estado: PENDIENTE de activación.",
                            Toast.LENGTH_LONG).show();
                    finish(); // Regresa a la pantalla de login
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    mostrarCargando(false);
                    String mensaje = e.getMessage() != null ? e.getMessage() : "Error al registrar el usuario.";
                    Toast.makeText(ActividadRegistro.this, mensaje, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    /**
     * Evento onClick para regresar al Login (android:onClick="volverALogin").
     *
     * @param v Vista del botón.
     */
    public void volverALogin(View v) {
        finish();
    }

    private void mostrarCargando(boolean cargando) {
        if (pbRegistro != null) {
            pbRegistro.setVisibility(cargando ? View.VISIBLE : View.GONE);
        }
        if (btnRegistrar != null) {
            btnRegistrar.setEnabled(!cargando);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }
}
