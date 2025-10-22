package com.prod.artchain.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;

import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.prod.artchain.MainActivity;
import com.prod.artchain.databinding.ActivityLoginBinding;
import com.prod.artchain.data.service.AuthApiService;
import com.prod.artchain.data.local.TokenManager;
import com.prod.artchain.data.model.LoggedInUser;
import com.prod.artchain.data.remote.HttpClient;

public class LoginActivity extends AppCompatActivity {
    private TokenManager tokenManager;
    private boolean isLoading = false;
    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityLoginBinding binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        tokenManager = TokenManager.getInstance(this);

        // Check if user is already logged in
        if (tokenManager.isLoggedIn()) {
            // Set the token for HttpClient
            HttpClient.getInstance().setAuthToken(tokenManager.getToken());
            // User is logged in, go to MainActivity
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        usernameEditText = binding.username;
        passwordEditText = binding.password;
        loginButton = binding.login;
        final ProgressBar loadingProgressBar = binding.loading;

        // TextWatcher to enable/disable login button based on input
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                updateLoginButtonState();
            }
        };
        usernameEditText.addTextChangedListener(textWatcher);
        passwordEditText.addTextChangedListener(textWatcher);

        // Initial state
        updateLoginButtonState();

        loginButton.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString();
            String password = passwordEditText.getText().toString();
            loadingProgressBar.setVisibility(ProgressBar.VISIBLE);
            loginButton.setEnabled(false);
            isLoading = true;
            AuthApiService.getInstance().loginAsync(username, password, new AuthApiService.LoginCallback() {
                @Override
                public void onSuccess(LoggedInUser user) {
                    runOnUiThread(() -> {
                        loadingProgressBar.setVisibility(ProgressBar.GONE);
                        loginButton.setEnabled(true);
                        isLoading = false;
                        // Save access token
                        tokenManager.saveToken(user.getAccessToken());
                        String roleStr = user.getRole() != null ? user.getRole().toString() : "No Role";
                        Toast.makeText(LoginActivity.this, "Logged in as " + user.getFullName() + " (" + roleStr + ")", Toast.LENGTH_LONG).show();
                        // Proceed to MainActivity
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    });
                }
                @Override
                public void onError(Exception e) {
                    runOnUiThread(() -> {
                        loadingProgressBar.setVisibility(ProgressBar.GONE);
                        loginButton.setEnabled(true);
                        isLoading = false;
                        Toast.makeText(LoginActivity.this, "Login failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
                }
            });
        });
    }

    private void updateLoginButtonState() {
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        loginButton.setEnabled(!username.isEmpty() && !password.isEmpty() && !isLoading);
    }
}