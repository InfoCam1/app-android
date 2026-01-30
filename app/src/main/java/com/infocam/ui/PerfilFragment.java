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

/**
 * PerfilFragment: Información del usuario actual y cierre de sesión.
 * 
 * Conceptos clave para DAM:
 * 1. SharedPreferences: Usamos el gestor de sesión para recuperar los datos
 * persistidos.
 * 2. Intents con Flags: Al cerrar sesión, limpiamos el historial para que no se
 * pueda volver atrás.
 */
public class PerfilFragment extends Fragment {

    private TextView txtNombreUsuario;
    private EditText etNombre, etEmail, etTelefono, etPassword;
    private Button btnGuardar, btnLogout;
    private SessionManager preferenciaSesion;
    private DataRepository databaseLocal;

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

        if (user != null) {
            txtNombreUsuario.setText(user.getUsername());
            etNombre.setText(user.getNombre());
            etEmail.setText(user.getEmail());
            etTelefono.setText(String.valueOf(user.getTelefono()));
        }

        btnGuardar.setOnClickListener(v -> ejecutarAccionActualizar());

        btnLogout.setOnClickListener(v -> {
            // Limpieza antes de salir
            if (user != null) {
                databaseLocal.vaciarFavoritosDeUsuario(user.getId());
            }
            preferenciaSesion.cerrarSesion();

            // Navegación segura al Login
            Intent i = new Intent(getActivity(), LoginActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            if (getActivity() != null)
                getActivity().finish();
        });

        return vistaRaiz;
    }

    private void ejecutarAccionActualizar() {
        String n = etNombre.getText().toString().trim();
        String em = etEmail.getText().toString().trim();
        String telStr = etTelefono.getText().toString().trim();
        String pass = etPassword.getText().toString().trim();

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

        Usuario actual = preferenciaSesion.obtenerUsuario();
        String tk = preferenciaSesion.getToken();

        btnGuardar.setEnabled(false);

        InfocamServiceClient.obtenerInstancia().actualizarUsuario(tk, actual.getId(), n, em, t, pass,
                new ApiCallback<Usuario>() {
                    @Override
                    public void onSuccess(Usuario nuevo) {
                        btnGuardar.setEnabled(true);
                        etPassword.setText(""); // Limpiar campo de contraseña
                        // Mantenemos el token si el API no lo devuelve en este endpoint
                        if (nuevo.getToken() == null)
                            nuevo.setToken(tk);

                        preferenciaSesion.guardarSesion(nuevo);
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
