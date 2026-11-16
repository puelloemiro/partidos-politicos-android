package com.puelloemiro.partidos;

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
 * Actividad encargada de solicitar el restablecimiento de contraseña.
 * Conecta con el endpoint POST /api/auth/forgot-password del backend.
 *
 * @author Emiro Puello
 */
public class ActividadRecuperarClave extends AppCompatActivity {

    private EditText etEmail;
    private Button btnRecuperar;
    private ProgressBar pbRecuperar;

    private DaoUsuario daoUsuario;
    private ExecutorService executor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actividad_recuperar_clave);

        etEmail = findViewById(R.id.etEmailRecuperar);
        btnRecuperar = findViewById(R.id.btnRecuperar);
        pbRecuperar = findViewById(R.id.pbRecuperar);

        daoUsuario = new DaoUsuario();
        executor = Executors.newSingleThreadExecutor();
    }

    /**
     * Evento onClick para enviar la solicitud de recuperación (android:onClick="enviarRecuperacion").
     *
     * @param v Vista del botón.
     */
    public void enviarRecuperacion(View v) {
        String email = etEmail.getText().toString().trim();

        if (email.isEmpty()) {
            etEmail.setError("Ingrese su correo electrónico");
            etEmail.requestFocus();
            return;
        }

        mostrarCargando(true);

        executor.execute(() -> {
            try {
                String mensaje = daoUsuario.recuperarClave(email);

                runOnUiThread(() -> {
                    mostrarCargando(false);
                    Toast.makeText(ActividadRecuperarClave.this, mensaje, Toast.LENGTH_LONG).show();
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    mostrarCargando(false);
                    String mensaje = e.getMessage() != null ? e.getMessage() : "Error al procesar la solicitud.";
                    Toast.makeText(ActividadRecuperarClave.this, mensaje, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    /**
     * Evento onClick para volver a la pantalla de login.
     *
     * @param v Vista del botón.
     */
    public void volverALogin(View v) {
        finish();
    }

    private void mostrarCargando(boolean cargando) {
        if (pbRecuperar != null) {
            pbRecuperar.setVisibility(cargando ? View.VISIBLE : View.GONE);
        }
        if (btnRecuperar != null) {
            btnRecuperar.setEnabled(!cargando);
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
