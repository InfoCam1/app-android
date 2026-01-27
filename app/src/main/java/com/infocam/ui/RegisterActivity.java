package com.infocam.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.infocam.R;
import com.infocam.model.Usuario;
import com.infocam.network.ApiCallback;
import com.infocam.network.ServicioApi;

/**
 * RegisterActivity: Gestión de la creación de nuevos usuarios.
 */
public class RegisterActivity extends AppCompatActivity {

    private EditText etUser, etNombre, etApellido, etPhone, etEmail, etPass;
    private Button btnRegister;
    private ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etUser = findViewById(R.id.etUsernameReg);
        etNombre = findViewById(R.id.etNombreReg);
        etApellido = findViewById(R.id.etApellidoReg);
        etPhone = findViewById(R.id.etTelefonoReg);
        etEmail = findViewById(R.id.etEmailReg);
        etPass = findViewById(R.id.etPasswordReg);
        btnRegister = findViewById(R.id.btnRegister);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());
        btnRegister.setOnClickListener(v -> procesarRegistro());
    }

    private void procesarRegistro() {
        String username = etUser.getText().toString().trim();
        String nombre = etNombre.getText().toString().trim();
        String apellido = etApellido.getText().toString().trim();
        String telefonoStr = etPhone.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPass.getText().toString().trim();

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(nombre) || TextUtils.isEmpty(apellido) ||
                TextUtils.isEmpty(telefonoStr) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Por favor, rellena todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        long telefono;
        try {
            telefono = Long.parseLong(telefonoStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Número de teléfono no válido", Toast.LENGTH_SHORT).show();
            return;
        }

        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setUsername(username);
        nuevoUsuario.setNombre(nombre);
        nuevoUsuario.setApellido(apellido);
        nuevoUsuario.setTelefono(telefono);
        nuevoUsuario.setEmail(email);
        nuevoUsuario.setPassword(password);
        nuevoUsuario.setAdmin(false);

        btnRegister.setEnabled(false);

        ServicioApi.obtenerInstancia().registrarUsuario(nuevoUsuario, new ApiCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                Toast.makeText(RegisterActivity.this, "Usuario registrado con éxito", Toast.LENGTH_SHORT).show();
                finish(); // Volvemos al login
            }

            @Override
            public void onError(String error) {
                btnRegister.setEnabled(true);
                Toast.makeText(RegisterActivity.this, "Error en registro: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }
}
