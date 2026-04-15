package com.citypulse.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.citypulse.R;
import com.citypulse.local.AppDatabase;
import com.citypulse.local.entity.UserEntity;
import com.citypulse.utils.PasswordUtils;
import com.citypulse.utils.SessionManager;
import com.citypulse.utils.ValidationUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout   tilEmail, tilPassword;
    private TextInputEditText etEmail, etPassword;
    private MaterialButton    btnLogin;
    private ProgressBar       progressBar;
    private TextView          tvError, tvRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        bindViews();

        btnLogin.setOnClickListener(v -> attemptLogin());
        tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });
    }

    private void bindViews() {
        tilEmail    = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        etEmail     = findViewById(R.id.etEmail);
        etPassword  = findViewById(R.id.etPassword);
        btnLogin    = findViewById(R.id.btnLogin);
        progressBar = findViewById(R.id.progressBar);
        tvError     = findViewById(R.id.tvError);
        tvRegister  = findViewById(R.id.tvRegister);
    }

    private void attemptLogin() {
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String pass  = etPassword.getText() != null ? etPassword.getText().toString() : "";

        tilEmail.setError(null);
        tilPassword.setError(null);
        tvError.setVisibility(View.GONE);

        if (!ValidationUtils.isValidEmail(email)) {
            tilEmail.setError("Enter a valid email"); return;
        }
        if (pass.length() < 6) {
            tilPassword.setError("Password too short"); return;
        }

        setLoading(true);

        // Room query runs on background thread
        Executors.newSingleThreadExecutor().execute(() -> {
            UserEntity user = AppDatabase.get(this).userDao().findByEmail(email);

            runOnUiThread(() -> {
                setLoading(false);
                if (user == null || !PasswordUtils.verify(pass, user.passwordHash)) {
                    tvError.setText("Incorrect email or password");
                    tvError.setVisibility(View.VISIBLE);
                } else {
                    SessionManager.get().login(
                            user.id, user.name, user.city, user.profilePicPath);
                    goToMain();
                }
            });
        });
    }

    private void goToMain() {
        Intent i = new Intent(this, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void setLoading(boolean on) {
        btnLogin.setEnabled(!on);
        progressBar.setVisibility(on ? View.VISIBLE : View.GONE);
    }
}
