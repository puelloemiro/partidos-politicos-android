package com.puelloemiro.partidos;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.puelloemiro.partidos.dao.DaoPartido;
import com.puelloemiro.partidos.entidades.Partido;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Actividad encargada de la generación y visualización de Reportes Estadísticos.
 * Permite ejecutar dos consultas especializadas:
 * 1. Reporte de partidos por País (GET /api/partidos-politicos/reportes/por-pais?pais=).
 * 2. Reporte de partidos con un Mínimo de Alcaldes (GET /api/partidos-politicos/reportes/por-alcaldes?min=).
 *
 * Muestra los resultados en un ListView utilizando el adaptador de partidos.
 * Al tocar un resultado, también permite abrir la vista de detalle en ActividadCrudPartido.
 *
 * @author Emiro Puello
 */
public class ActividadReportes extends AppCompatActivity {

    private EditText etPais;
    private EditText etAlcaldes;
    private Button btnConsultarPais;
    private Button btnConsultarAlcaldes;
    private ProgressBar pbReportes;
    private TextView tvReporteVacio;
    private ListView lvResultados;

    private DaoPartido daoPartido;
    private ExecutorService executor;

    private List<Partido> listaResultados;
    private ActividadListaPartidos.AdaptadorPartido adaptador;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actividad_reportes);

        etPais = findViewById(R.id.etReportePais);
        etAlcaldes = findViewById(R.id.etReporteAlcaldes);
        btnConsultarPais = findViewById(R.id.btnConsultarPais);
        btnConsultarAlcaldes = findViewById(R.id.btnConsultarAlcaldes);
        pbReportes = findViewById(R.id.pbReportes);
        tvReporteVacio = findViewById(R.id.tvReporteVacio);
        lvResultados = findViewById(R.id.lvResultadosReporte);

        daoPartido = new DaoPartido();
        executor = Executors.newSingleThreadExecutor();

        listaResultados = new ArrayList<>();
        adaptador = new ActividadListaPartidos.AdaptadorPartido(this, listaResultados);
        lvResultados.setAdapter(adaptador);

        // Al tocar un ítem del reporte se abre el CRUD con ese partido
        lvResultados.setOnItemClickListener((parent, view, position, id) -> {
            Partido p = listaResultados.get(position);
            if (p != null) {
                Intent intent = new Intent(ActividadReportes.this, ActividadCrudPartido.class);
                intent.putExtra("ID_PARTIDO", p.getId());
                startActivity(intent);
            }
        });
    }

    /**
     * Consulta el reporte de partidos políticos por país (android:onClick="consultarPorPais").
     *
     * @param v Vista del botón.
     */
    public void consultarPorPais(View v) {
        String pais = etPais.getText().toString().trim();
        if (pais.isEmpty()) {
            etPais.setError("Ingrese el país a consultar");
            etPais.requestFocus();
            return;
        }

        mostrarCargando(true);
        executor.execute(() -> {
            try {
                List<Partido> partidos = daoPartido.reportePorPais(pais);
                actualizarListaEnUi(partidos);
            } catch (Exception e) {
                mostrarErrorEnUi(e.getMessage());
            }
        });
    }

    /**
     * Consulta el reporte de partidos con un número mínimo de alcaldes (android:onClick="consultarPorAlcaldes").
     *
     * @param v Vista del botón.
     */
    public void consultarPorAlcaldes(View v) {
        String strMin = etAlcaldes.getText().toString().trim();
        if (strMin.isEmpty()) {
            etAlcaldes.setError("Ingrese la cantidad mínima de alcaldes");
            etAlcaldes.requestFocus();
            return;
        }

        int min;
        try {
            min = Integer.parseInt(strMin);
            if (min < 0) {
                etAlcaldes.setError("El número no puede ser negativo");
                etAlcaldes.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            etAlcaldes.setError("Ingrese un número entero válido");
            etAlcaldes.requestFocus();
            return;
        }

        mostrarCargando(true);
        executor.execute(() -> {
            try {
                List<Partido> partidos = daoPartido.reportePorAlcaldes(min);
                actualizarListaEnUi(partidos);
            } catch (Exception e) {
                mostrarErrorEnUi(e.getMessage());
            }
        });
    }

    private void actualizarListaEnUi(List<Partido> partidos) {
        runOnUiThread(() -> {
            mostrarCargando(false);
            listaResultados.clear();
            if (partidos != null && !partidos.isEmpty()) {
                listaResultados.addAll(partidos);
                tvReporteVacio.setVisibility(View.GONE);
            } else {
                tvReporteVacio.setVisibility(View.VISIBLE);
            }
            adaptador.notifyDataSetChanged();
        });
    }

    private void mostrarErrorEnUi(String mensaje) {
        runOnUiThread(() -> {
            mostrarCargando(false);
            String msg = mensaje != null ? mensaje : "Error al consultar el reporte.";
            Toast.makeText(ActividadReportes.this, msg, Toast.LENGTH_LONG).show();
        });
    }

    private void mostrarCargando(boolean cargando) {
        if (pbReportes != null) {
            pbReportes.setVisibility(cargando ? View.VISIBLE : View.GONE);
        }
        if (btnConsultarPais != null) btnConsultarPais.setEnabled(!cargando);
        if (btnConsultarAlcaldes != null) btnConsultarAlcaldes.setEnabled(!cargando);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }
}
