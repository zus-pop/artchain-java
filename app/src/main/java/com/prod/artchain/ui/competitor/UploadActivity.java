package com.prod.artchain.ui.competitor;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.material.textfield.TextInputEditText;
import com.prod.artchain.R;
import com.prod.artchain.data.local.TokenManager;
import com.prod.artchain.data.model.LoggedInUser;
import com.prod.artchain.data.model.Submission;
import com.prod.artchain.data.service.SubmissionApiService;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;

public class UploadActivity extends AppCompatActivity {

    private TextView userNameTextView;
    private TextView userSchoolTextView;
    private TextView userGradeTextView;
    private TextInputEditText titleEditText;
    private TextInputEditText descriptionEditText;
    private Button selectImageButton;
    private ImageView selectedImageView;
    private Button submitButton;

    private Uri selectedImageUri;
    private String contestId;
    private String roundId;

    private ActivityResultLauncher<String> imagePickerLauncher;
    private ActivityResultLauncher<Uri> cameraLauncher;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private Uri photoUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        // Set title
        setTitle("Upload Painting");

        // Enable back button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Get extras
        contestId = getIntent().getStringExtra("contestId");
        roundId = getIntent().getStringExtra("roundId");

        if (contestId == null || roundId == null) {
            Toast.makeText(this, "Invalid contest data", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize views
        userNameTextView = findViewById(R.id.userName);
        userSchoolTextView = findViewById(R.id.userSchool);
        userGradeTextView = findViewById(R.id.userGrade);
        titleEditText = findViewById(R.id.titleEditText);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        selectImageButton = findViewById(R.id.selectImageButton);
        selectedImageView = findViewById(R.id.selectedImageView);
        submitButton = findViewById(R.id.submitButton);

        // Load user info
        LoggedInUser user = TokenManager.getInstance(this).getUser();
        if (user != null) {
            userNameTextView.setText("Full Name: " + user.getFullName());
            userSchoolTextView.setText("School: " + (user.getWard() != null ? user.getWard() : "N/A"));
            userGradeTextView.setText("Grade: " + (user.getGrade() != null ? user.getGrade() : "N/A"));
        }

        // Register image picker launcher
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedImageUri = uri;
                        selectedImageView.setVisibility(ImageView.VISIBLE);
                        Picasso.get().load(selectedImageUri).into(selectedImageView);
                    }
                }
        );

        // Register camera launcher
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                success -> {
                    if (success && photoUri != null) {
                        selectedImageUri = photoUri;
                        selectedImageView.setVisibility(ImageView.VISIBLE);
                        Picasso.get().load(selectedImageUri).into(selectedImageView);
                    }
                }
        );

        // Register permission launcher
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        openCamera();
                    } else {
                        Toast.makeText(this, "Camera permission is required to take photos", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // Set listeners
        selectImageButton.setOnClickListener(v -> showImageSourceOptions());
        submitButton.setOnClickListener(v -> submitPainting());
    }

    private void openImagePicker() {
        imagePickerLauncher.launch("image/*");
    }

    private void submitPainting() {
        String title = Objects.requireNonNull(titleEditText.getText()).toString().trim();
        String description = Objects.requireNonNull(descriptionEditText.getText()).toString().trim();

        if (title.isEmpty()) {
            Toast.makeText(this, "Title is required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedImageUri == null) {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
            return;
        }

        LoggedInUser user = TokenManager.getInstance(this).getUser();
        if (user == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        // Convert Uri to File
        File imageFile = uriToFile(selectedImageUri);
        if (imageFile == null) {
            Toast.makeText(this, "Failed to process image", Toast.LENGTH_SHORT).show();
            return;
        }

        // Upload
        SubmissionApiService.getInstance().uploadAsync(
                user.getUserId(),
                title,
                description,
                contestId,
                roundId,
                imageFile,
                new SubmissionApiService.SubmissionCallback() {
                    @Override
                    public void onSuccess(List<Submission> submissions) {
                        runOnUiThread(() -> {
                            Toast.makeText(UploadActivity.this, "Painting uploaded successfully", Toast.LENGTH_SHORT).show();
                            finish();
                        });
                    }

                    @Override
                    public void onError(Exception e) {
                        runOnUiThread(() -> {
                            Toast.makeText(UploadActivity.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                    }
                }
        );
    }

    private File uriToFile(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            File tempFile = File.createTempFile("upload", ".jpg", getCacheDir());
            FileOutputStream outputStream = new FileOutputStream(tempFile);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.close();
            inputStream.close();
            return tempFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void showImageSourceOptions() {
        String[] options = {"Camera", "Gallery"};
        new AlertDialog.Builder(this)
                .setTitle("Select Image Source")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        // Camera
                        openCamera();
                    } else {
                        // Gallery
                        openImagePicker();
                    }
                })
                .show();
    }

    private void openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        } else {
            try {
                File photoFile = File.createTempFile("photo", ".jpg", getExternalFilesDir(null));
                photoUri = FileProvider.getUriForFile(this, "com.prod.artchain.fileprovider", photoFile);
                cameraLauncher.launch(photoUri);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to open camera", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
