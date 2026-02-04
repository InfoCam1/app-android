package com.infocam.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.infocam.R;
import com.infocam.data.DataRepository;
import com.infocam.data.SessionManager;
import com.infocam.model.Usuario;
import com.infocam.network.ApiCallback;
import com.infocam.network.InfocamServiceClient;

// Esta clase permite al usuario editar sus datos personales y gestionar la sesión. Extiende de "Fragment" por ser una pantalla secundaria.
public class PerfilFragment extends Fragment {

    private TextView txtNombreUsuario;
    private EditText etNombre, etEmail, etTelefono, etPassword;
    private Button btnGuardar, btnLogout;
    private SessionManager preferenciaSesion;
    private DataRepository databaseLocal;

    // Inflamos el layout del fragmento y configuramos los componentes.
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflador, @Nullable ViewGroup contenedor,
            @Nullable Bundle estadoAnterior) {
        View vistaRaiz = inflador.inflate(R.layout.fragment_perfil, contenedor, false);

        preferenciaSesion = new SessionManager(getContext());
        databaseLocal = new DataRepository(getContext());

        // Recuperamos los datos del usuario de la sesión persistente
        Usuario user = preferenciaSesion.obtenerUsuario();

        txtNombreUsuario = vistaRaiz.findViewById(R.id.tvUsernamePerfil);
        etNombre = vistaRaiz.findViewById(R.id.etNombrePerfil);
        etEmail = vistaRaiz.findViewById(R.id.etEmailPerfil);
        etTelefono = vistaRaiz.findViewById(R.id.etTelefonoPerfil);
        etPassword = vistaRaiz.findViewById(R.id.etPasswordPerfil);
        btnGuardar = vistaRaiz.findViewById(R.id.btnGuardarPerfil);
        btnLogout = vistaRaiz.findViewById(R.id.btnLogout);

        // Si el usuario está autenticado, se rellenan los campos con sus datos.
        if (user != null) {
            txtNombreUsuario.setText(user.getNombreUsuario());
            etNombre.setText(user.getNombre());
            etEmail.setText(user.getEmail());
            etTelefono.setText(String.valueOf(user.getTelefono()));
        }

        // Configuramos el botón de guardar cambios.
        btnGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ejecutarAccionActualizar();
            }
        });

        // Configuramos el botón de cierre de sesión.
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (user != null) {
                    databaseLocal.vaciarFavoritosDeUsuario(user.getId()); // Limpieza de datos temporales.
                }
                preferenciaSesion.cerrarSesion(); // Cierre de sesión.

                // Navegación segura al Login.
                Intent i = new Intent(getActivity(), LoginActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);

                if (getActivity() != null) {
                    getActivity().finish();
                }
            }
        });

        return vistaRaiz;
    }

    // Método para actualizar el perfil del usuario.
    private void ejecutarAccionActualizar() {
        String n = etNombre.getText().toString().trim();
        String em = etEmail.getText().toString().trim();
        String telStr = etTelefono.getText().toString().trim();
        String pass = etPassword.getText().toString().trim();
        // Validación de campos obligatorios.
        if (TextUtils.isEmpty(n) || TextUtils.isEmpty(em) || TextUtils.isEmpty(telStr)) {
            Toast.makeText(getContext(), "Campos obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        long t;
        try {
            t = Long.parseLong(telStr);
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Teléfono inválido", Toast.LENGTH_SHORT).show();
            return;
        }

        Usuario actual = preferenciaSesion.obtenerUsuario(); // Obtenemos el usuario de la sesión.
        String tk = preferenciaSesion.getToken(); // Obtenemos el token de la sesión.

        btnGuardar.setEnabled(false); // Deshabilitamos el botón para evitar múltiples clics.

        InfocamServiceClient.obtenerInstancia().actualizarUsuario(tk, actual.getId(), n, em, t, pass,
                new ApiCallback<Usuario>() {
                    // Método para manejar la respuesta positiva de la API.
                    @Override
                    public void onSuccess(Usuario nuevo) {
                        btnGuardar.setEnabled(true); // Habilitamos el botón.
                        etPassword.setText(""); // Limpiamos el campo de contraseña
                        // Mantenemos el token si el API no lo devuelve en este endpoint.
                        if (nuevo.getToken() == null)
                            nuevo.setToken(tk);

                        preferenciaSesion.guardarSesion(nuevo); // Guardamos el usuario en la sesión.
                        Toast.makeText(getContext(), "Perfil actualizado", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(String error) {
                        btnGuardar.setEnabled(true);
                        Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
