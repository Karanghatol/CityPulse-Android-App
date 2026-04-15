package com.citypulse.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.citypulse.R;
import com.citypulse.local.AppDatabase;
import com.citypulse.local.entity.PostEntity;
import com.citypulse.utils.SessionManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CreatePostActivity extends AppCompatActivity {

    private EditText etDesc, etLocation;
    private Spinner spinnerIntensity, spinnerDuration;
    private Button btnPickMedia, btnCapture, btnPost;
    private ImageView ivPreview;
    private SessionManager sm;

    private String mediaType = "none";
    private String savedFilePath = "";
    private String currentPhotoPath;

    private final ActivityResultLauncher<String> requestCameraPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    captureMedia();
                } else {
                    Toast.makeText(this, "Camera permission is required to capture photos", Toast.LENGTH_SHORT).show();
                }
            }
    );

    private final ActivityResultLauncher<Intent> pickMediaLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri mediaUri = result.getData().getData();
                    if (mediaUri != null) {
                        processSelectedMedia(mediaUri);
                    }
                }
            }
    );

    private final ActivityResultLauncher<Intent> captureLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    mediaType = "image";
                    savedFilePath = currentPhotoPath;
                    ivPreview.setVisibility(View.VISIBLE);
                    ivPreview.setImageURI(Uri.fromFile(new File(savedFilePath)));
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        sm = SessionManager.get();

        etDesc = findViewById(R.id.etPostDesc);
        etLocation = findViewById(R.id.etPostLocation);
        spinnerIntensity = findViewById(R.id.spinnerIntensity);
        spinnerDuration = findViewById(R.id.spinnerDuration);
        btnPickMedia = findViewById(R.id.btnPickMedia);
        btnCapture = findViewById(R.id.btnCapture);
        btnPost = findViewById(R.id.btnPost);
        ivPreview = findViewById(R.id.ivPreview);

        btnPickMedia.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            String[] mimeTypes = {"image/*", "video/*"};
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
            pickMediaLauncher.launch(intent);
        });

        btnCapture.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                captureMedia();
            } else {
                requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA);
            }
        });

        btnPost.setOnClickListener(v -> {
            String desc = etDesc.getText().toString().trim();
            String location = etLocation.getText().toString().trim();
            Object selectedIntensity = spinnerIntensity.getSelectedItem();
            String intensity = selectedIntensity != null ? selectedIntensity.toString() : "general";
            
            String selectedDuration = spinnerDuration.getSelectedItem().toString();

            if (desc.isEmpty()) {
                Toast.makeText(this, "Please enter a description", Toast.LENGTH_SHORT).show();
                return;
            }

            PostEntity post = new PostEntity();
            post.description = desc;
            post.location = location;
            post.intensity = intensity.toLowerCase(Locale.getDefault());
            post.authorId = sm.getUserId();
            post.authorName = sm.getUserName();
            post.authorPicPath = sm.getProfilePic();
            post.city = sm.getUserCity();
            post.createdAt = System.currentTimeMillis();
            
            // Set expiry time
            long now = System.currentTimeMillis();
            if (selectedDuration.equalsIgnoreCase("1 Day")) {
                post.expiryTime = now + (24 * 60 * 60 * 1000L);
            } else if (selectedDuration.equalsIgnoreCase("1 Week")) {
                post.expiryTime = now + (7 * 24 * 60 * 60 * 1000L);
            } else if (selectedDuration.equalsIgnoreCase("1 Month")) {
                post.expiryTime = now + (30 * 24 * 60 * 60 * 1000L); // Approx 30 days
            } else {
                post.expiryTime = 0; // Permanent
            }
            
            if (!savedFilePath.isEmpty()) {
                post.mediaPath = savedFilePath;
                post.mediaType = mediaType;
            } else {
                post.mediaPath = "";
                post.mediaType = "none";
            }

            new Thread(() -> {
                AppDatabase.get(this).postDao().insert(post);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Post created!", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }).start();
        });
    }

    private void processSelectedMedia(Uri uri) {
        String type = getContentResolver().getType(uri);
        if (type == null) {
            String extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
            if (extension != null) {
                type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
            }
        }
        
        mediaType = (type != null && type.startsWith("video")) ? "video" : "image";
        
        try {
            File file = createMediaFile(mediaType.equals("video") ? ".mp4" : ".jpg");
            copyUriToFile(uri, file);
            savedFilePath = file.getAbsolutePath();
            
            ivPreview.setVisibility(View.VISIBLE);
            if (mediaType.equals("video")) {
                ivPreview.setImageResource(R.drawable.ic_play_circle);
            } else {
                ivPreview.setImageURI(Uri.fromFile(file));
            }
        } catch (IOException e) {
            Toast.makeText(this, "Failed to process media: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void captureMedia() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            File photoFile = createMediaFile(".jpg");
            currentPhotoPath = photoFile.getAbsolutePath();
            Uri photoURI = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", photoFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            captureLauncher.launch(takePictureIntent);
        } catch (IOException ex) {
            Toast.makeText(this, "Error creating file", Toast.LENGTH_SHORT).show();
        }
    }

    private File createMediaFile(String extension) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fileName = "CP_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(null);
        if (storageDir != null && !storageDir.exists()) {
            if (!storageDir.mkdirs()) {
                throw new IOException("Failed to create storage directory");
            }
        }
        return File.createTempFile(fileName, extension, storageDir);
    }

    private void copyUriToFile(Uri uri, File dest) throws IOException {
        try (InputStream in = getContentResolver().openInputStream(uri);
             OutputStream out = new FileOutputStream(dest)) {
            if (in == null) throw new IOException("Failed to open input stream");
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        }
    }
}
