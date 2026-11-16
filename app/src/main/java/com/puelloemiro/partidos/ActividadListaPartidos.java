package com.puelloemiro.partidos;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.puelloemiro.partidos.dao.DaoPartido;
import com.puelloemiro.partidos.entidades.Partido;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Actividad encargada de listar todos los partidos políticos registrados.
 * Utiliza un ListView con un ArrayAdapter personalizado para vincular la lista
 * de entidades Partido con el layout item_partido.xml.
 *
 * Al pulsar sobre un elemento de la lista, abre ActividadCrudPartido cargando
 * automáticamente la información del partido seleccionado para su edición o consulta.
 *
 * @author Emiro Puello
 */
public class ActividadListaPartidos extends AppCompatActivity {

    private ListView lvPartidos;
    private ProgressBar pbLista;
    private TextView tvListaVacia;
    private Button btnRecargar;

    private DaoPartido daoPartido;
    private ExecutorService executor;

    private List<Partido> listaPartidos;
    private AdaptadorPartido adaptador;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actividad_lista_partidos);

        lvPartidos = findViewById(R.id.lvPartidos);
        pbLista = findViewById(R.id.pbLista);
        tvListaVacia = findViewById(R.id.tvListaVacia);
        btnRecargar = findViewById(R.id.btnRecargarLista);

        daoPartido = new DaoPartido();
        executor = Executors.newSingleThreadExecutor();

        listaPartidos = new ArrayList<>();
        adaptador = new AdaptadorPartido(this, listaPartidos);
        lvPartidos.setAdapter(adaptador);

        // Al tocar un ítem de la lista se abre el formulario CRUD con ese partido
        lvPartidos.setOnItemClickListener((parent, view, position, id) -> {
            Partido seleccionado = listaPartidos.get(position);
            if (seleccionado != null) {
                Intent intent = new Intent(ActividadListaPartidos.this, ActividadCrudPartido.class);
                intent.putExtra("ID_PARTIDO", seleccionado.getId());
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recargar la lista al volver de la pantalla de CRUD para reflejar cambios
        cargarPartidos(null);
    }

    /**
     * Consulta la lista de partidos en la API mediante un hilo secundario
     * (android:onClick="cargarPartidos").
     *
     * @param v Vista del botón (o null en onResume).
     */
    public void cargarPartidos(View v) {
        mostrarCargando(true);

        executor.execute(() -> {
            try {
                List<Partido> partidos = daoPartido.listarTodosLosPartidos();

                runOnUiThread(() -> {
                    mostrarCargando(false);
                    listaPartidos.clear();
                    if (partidos != null && !partidos.isEmpty()) {
                        listaPartidos.addAll(partidos);
                        tvListaVacia.setVisibility(View.GONE);
                    } else {
                        tvListaVacia.setVisibility(View.VISIBLE);
                    }
                    adaptador.notifyDataSetChanged();
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    mostrarCargando(false);
                    String msg = e.getMessage() != null ? e.getMessage() : "Error al obtener la lista de partidos.";
                    Toast.makeText(ActividadListaPartidos.this, msg, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void mostrarCargando(boolean cargando) {
        if (pbLista != null) {
            pbLista.setVisibility(cargando ? View.VISIBLE : View.GONE);
        }
        if (btnRecargar != null) {
            btnRecargar.setEnabled(!cargando);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }

    /**
     * Adaptador personalizado basado en ArrayAdapter para vincular la entidad Partido
     * con el diseño de celda item_partido.xml.
     */
    public static class AdaptadorPartido extends ArrayAdapter<Partido> {

        public AdaptadorPartido(AppCompatActivity context, List<Partido> datos) {
            super(context, 0, datos);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_partido, parent, false);
            }

            Partido p = getItem(position);
            if (p != null) {
                TextView tvId = convertView.findViewById(R.id.tvItemId);
                TextView tvNombre = convertView.findViewById(R.id.tvItemNombre);
                TextView tvPais = convertView.findViewById(R.id.tvItemPais);
                TextView tvEslogan = convertView.findViewById(R.id.tvItemEslogan);
                TextView tvLideres = convertView.findViewById(R.id.tvItemDetalleLideres);
                TextView tvCifras = convertView.findViewById(R.id.tvItemCifras);

                tvId.setText(p.getId());
                tvNombre.setText(p.getNombre());
                tvPais.setText(p.getPais());
                tvEslogan.setText(p.getEslogan() != null && !p.getEslogan().isEmpty() ? "\"" + p.getEslogan() + "\"" : "");

                String lideres = "Pres: " + p.getPresidente() + " | Sec: " + p.getSecretario() + " | Tes: " + p.getTesorero();
                tvLideres.setText(lideres);

                String cifras = "Alcaldes: " + p.getNumAlcaldes() + " | Congresistas: " + p.getNumCongresistas()
                        + " | Gob: " + p.getNumGobernadores() + " | Pres: " + p.getNumPresidentes();
                tvCifras.setText(cifras);
            }

            return convertView;
        }
    }
}
