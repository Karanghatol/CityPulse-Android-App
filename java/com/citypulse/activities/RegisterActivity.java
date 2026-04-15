package com.citypulse.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.bumptech.glide.Glide;
import com.citypulse.R;
import com.citypulse.local.AppDatabase;
import com.citypulse.local.entity.UserEntity;
import com.citypulse.utils.PasswordUtils;
import com.citypulse.utils.SessionManager;
import com.citypulse.utils.ValidationUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import de.hdodenhof.circleimageview.CircleImageView;
import java.util.concurrent.Executors;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout   tilName, tilPhone, tilEmail, tilPassword, tilConfirm;
    private TextInputEditText etName, etPhone, etEmail, etPassword, etConfirm;
    private Spinner           spinnerCity;
    private SwitchMaterial    switchJobs;
    private MaterialButton    btnRegister;
    private ProgressBar       progressBar;
    private TextView          tvError, tvLogin;
    private CircleImageView   ivPhoto;

    private String selectedCity = "";
    private String photoPath    = "";

    private final ActivityResultLauncher<String[]> photoPicker =
        registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
            if (uri != null) {
                getContentResolver().takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                photoPath = uri.toString();
                Glide.with(this).load(uri).circleCrop().into(ivPhoto);
            }
        });

    private final ActivityResultLauncher<String> permLauncher =
        registerForActivityResult(new ActivityResultContracts.RequestPermission(),
            granted -> { if (granted) photoPicker.launch(new String[]{"image/*"}); });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        bindViews();
        setupSpinner();

        ivPhoto.setOnClickListener(v -> {
            String perm = Build.VERSION.SDK_INT >= 33
                    ? Manifest.permission.READ_MEDIA_IMAGES
                    : Manifest.permission.READ_EXTERNAL_STORAGE;
            if (ContextCompat.checkSelfPermission(this, perm) == PackageManager.PERMISSION_GRANTED)
                photoPicker.launch(new String[]{"image/*"});
            else
                permLauncher.launch(perm);
        });

        btnRegister.setOnClickListener(v -> attemptRegister());
        tvLogin.setOnClickListener(v -> { finish();
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right); });
    }

    private void bindViews() {
        tilName    = findViewById(R.id.tilName);
        tilPhone   = findViewById(R.id.tilPhone);
        tilEmail   = findViewById(R.id.tilEmail);
        tilPassword= findViewById(R.id.tilPassword);
        tilConfirm = findViewById(R.id.tilConfirmPassword);
        etName     = findViewById(R.id.etName);
        etPhone    = findViewById(R.id.etPhone);
        etEmail    = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirm  = findViewById(R.id.etConfirmPassword);
        spinnerCity= findViewById(R.id.spinnerCity);
        switchJobs = findViewById(R.id.switchJobsGroups);
        btnRegister= findViewById(R.id.btnRegister);
        progressBar= findViewById(R.id.progressBar);
        tvError    = findViewById(R.id.tvError);
        tvLogin    = findViewById(R.id.tvLogin);
        ivPhoto    = findViewById(R.id.ivProfilePhoto);
    }

    private void setupSpinner() {
        String[] cities = getResources().getStringArray(R.array.indian_cities);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, cities);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCity.setAdapter(adapter);
        spinnerCity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                selectedCity = pos == 0 ? "" : cities[pos];
            }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        });
    }

    private void attemptRegister() {
        tvError.setVisibility(View.GONE);
        String name  = etName.getText()     != null ? etName.getText().toString().trim()    : "";
        String phone = etPhone.getText()    != null ? etPhone.getText().toString().trim()   : "";
        String email = etEmail.getText()    != null ? etEmail.getText().toString().trim()   : "";
        String pass  = etPassword.getText() != null ? etPassword.getText().toString()       : "";
        String conf  = etConfirm.getText()  != null ? etConfirm.getText().toString()        : "";

        if (name.isEmpty())                 { tilName.setError("Required");   return; }
        if (!ValidationUtils.isValidEmail(email)) { tilEmail.setError("Invalid email"); return; }
        if (pass.length() < 6)             { tilPassword.setError("Min 6 chars"); return; }
        if (!pass.equals(conf))            { tilConfirm.setError("Passwords don't match"); return; }
        if (selectedCity.isEmpty())        { showError("Please select your city"); return; }

        setLoading(true);

        Executors.newSingleThreadExecutor().execute(() -> {
            // Check duplicate email
            if (AppDatabase.get(this).userDao().emailExists(email) > 0) {
                runOnUiThread(() -> { setLoading(false); showError("Email already registered"); });
                return;
            }

            UserEntity u = new UserEntity();
            u.name             = name;
            u.email            = email.toLowerCase();
            u.passwordHash     = PasswordUtils.hash(pass);
            u.phone            = phone;
            u.city             = selectedCity;
            u.profilePicPath   = photoPath;
            u.interestedInJobs = switchJobs.isChecked();
            u.createdAt        = System.currentTimeMillis();

            long newId = AppDatabase.get(this).userDao().insert(u);

            runOnUiThread(() -> {
                setLoading(false);
                SessionManager.get().login((int) newId, name, selectedCity, photoPath);
                Intent i = new Intent(this, MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            });
        });
    }

    private void setLoading(boolean on) {
        btnRegister.setEnabled(!on);
        progressBar.setVisibility(on ? View.VISIBLE : View.GONE);
    }

    private void showError(String msg) {
        tvError.setText(msg);
        tvError.setVisibility(View.VISIBLE);
    }
}
