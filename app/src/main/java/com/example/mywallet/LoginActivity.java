package com.example.mywallet;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    TextInputEditText edtName, edtEmail;
    MaterialButton btnLogin;

    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        edtName = findViewById(R.id.edtName);
        edtEmail = findViewById(R.id.edtEmail);
        btnLogin = findViewById(R.id.btnLogin);

        sharedPreferences = getSharedPreferences("MyWalletPrefs", Context.MODE_PRIVATE);

        btnLogin.setOnClickListener(v -> login());
    }

    private void login() {
        String name = edtName.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();

        if (name.isEmpty()) {
            edtName.setError("Nhập tên của bạn");
            return;
        }

        if (email.isEmpty()) {
            edtEmail.setError("Nhập email");
            return;
        }

        sharedPreferences.edit()
                .putBoolean("is_logged_in", true)
                .putString("user_name", name)
                .putString("user_email", email)
                .apply();

        Toast.makeText(this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}