package com.prod.artchain.ui.login;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.prod.artchain.R;
import com.prod.artchain.data.model.Ward;
import com.prod.artchain.data.service.AuthApiService;
import com.prod.artchain.data.service.WardApiService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SignUpActivity extends AppCompatActivity {
    private EditText usernameEditText;
    private EditText passwordEditText;
    private EditText confirmPasswordEditText;
    private EditText fullNameEditText;
    private EditText emailEditText;
    private EditText schoolNameEditText;
    private Spinner wardSpinner;
    private Spinner gradeSpinner;
    private TextView birthdayTextView;
    private Button signUpButton;
    private ProgressBar loadingProgressBar;
    private TextView loginLink;
    private ImageButton togglePasswordVisibility;
    private ImageButton toggleConfirmPasswordVisibility;

    private Date selectedBirthday = null;
    private String selectedRole = "COMPETITOR"; // Default role
    private String selectedWard = "";
    private String selectedGrade = "";
    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;
    private List<Ward> wardList = new ArrayList<>();
    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        initializeViews();
        setupWardSpinner();
        setupGradeSpinner();
        setupDatePicker();
        setupPasswordToggle();
        setupTextWatchers();
        setupButtons();
        updateSignUpButtonState();
    }

    private void initializeViews() {
        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        confirmPasswordEditText = findViewById(R.id.confirmPassword);
        fullNameEditText = findViewById(R.id.fullName);
        emailEditText = findViewById(R.id.email);
        schoolNameEditText = findViewById(R.id.schoolName);
        wardSpinner = findViewById(R.id.wardSpinner);
        gradeSpinner = findViewById(R.id.grade);
        birthdayTextView = findViewById(R.id.birthday);
        signUpButton = findViewById(R.id.signUpButton);
        loadingProgressBar = findViewById(R.id.loading);
        loginLink = findViewById(R.id.loginLink);
        togglePasswordVisibility = findViewById(R.id.togglePasswordVisibility);
        toggleConfirmPasswordVisibility = findViewById(R.id.toggleConfirmPasswordVisibility);
    }

    private void setupWardSpinner() {
        // Show loading message
        List<String> loadingList = new ArrayList<>();
        loadingList.add("Loading ward list");
        ArrayAdapter<String> loadingAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                loadingList);
        loadingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        wardSpinner.setAdapter(loadingAdapter);
        wardSpinner.setEnabled(false);

        // Fetch ward data from API
        WardApiService.fetchWards(new WardApiService.WardCallback() {
            @Override
            public void onSuccess(List<Ward> wards) {
                wardList = wards;
                List<String> wardNames = new ArrayList<>();
                wardNames.add("Choose ward");
                for (Ward ward : wards) {
                    wardNames.add(ward.getName());
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        SignUpActivity.this,
                        android.R.layout.simple_spinner_item,
                        wardNames);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                wardSpinner.setAdapter(adapter);
                wardSpinner.setEnabled(true);

                wardSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if (position > 0) {
                            selectedWard = wardList.get(position - 1).getName();
                        } else {
                            selectedWard = "";
                        }
                        updateSignUpButtonState();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        selectedWard = "";
                        updateSignUpButtonState();
                    }
                });
            }

            @Override
            public void onError(String error) {
                Toast.makeText(SignUpActivity.this,
                        "Không thể tải danh sách phường/xã: " + error,
                        Toast.LENGTH_SHORT).show();

                // Show error state
                List<String> errorList = new ArrayList<>();
                errorList.add("Lỗi tải dữ liệu");
                ArrayAdapter<String> errorAdapter = new ArrayAdapter<>(
                        SignUpActivity.this,
                        android.R.layout.simple_spinner_item,
                        errorList);
                errorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                wardSpinner.setAdapter(errorAdapter);
                wardSpinner.setEnabled(false);
            }
        });
    }

    private void setupGradeSpinner() {
        List<String> grades = new ArrayList<>();
        grades.add("Choose grade");
        for (int i = 1; i <= 9; i++) {
            grades.add(String.valueOf(i));
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                grades);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        gradeSpinner.setAdapter(adapter);

        gradeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    selectedGrade = grades.get(position);
                } else {
                    selectedGrade = "";
                }
                updateSignUpButtonState();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedGrade = "";
                updateSignUpButtonState();
            }
        });
    }

    private void setupDatePicker() {
        birthdayTextView.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    SignUpActivity.this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        Calendar selectedDate = Calendar.getInstance();
                        selectedDate.set(selectedYear, selectedMonth, selectedDay);
                        selectedBirthday = selectedDate.getTime();

                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                        birthdayTextView.setText(sdf.format(selectedBirthday));
                        updateSignUpButtonState();
                    },
                    year, month, day);
            datePickerDialog.show();
        });
    }

    private void setupPasswordToggle() {
        togglePasswordVisibility.setOnClickListener(v -> {
            isPasswordVisible = !isPasswordVisible;
            if (isPasswordVisible) {
                passwordEditText
                        .setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                togglePasswordVisibility.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
            } else {
                passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                togglePasswordVisibility.setImageResource(android.R.drawable.ic_menu_view);
            }
            passwordEditText.setSelection(passwordEditText.getText().length());
        });

        toggleConfirmPasswordVisibility.setOnClickListener(v -> {
            isConfirmPasswordVisible = !isConfirmPasswordVisible;
            if (isConfirmPasswordVisible) {
                confirmPasswordEditText
                        .setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                toggleConfirmPasswordVisibility.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
            } else {
                confirmPasswordEditText
                        .setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                toggleConfirmPasswordVisibility.setImageResource(android.R.drawable.ic_menu_view);
            }
            confirmPasswordEditText.setSelection(confirmPasswordEditText.getText().length());
        });
    }

    private void setupTextWatchers() {
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                updateSignUpButtonState();
            }
        };

        usernameEditText.addTextChangedListener(textWatcher);
        passwordEditText.addTextChangedListener(textWatcher);
        confirmPasswordEditText.addTextChangedListener(textWatcher);
        fullNameEditText.addTextChangedListener(textWatcher);
        emailEditText.addTextChangedListener(textWatcher);
        schoolNameEditText.addTextChangedListener(textWatcher);

        // Add real-time password match validation
        confirmPasswordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String password = passwordEditText.getText().toString().trim();
                String confirmPassword = confirmPasswordEditText.getText().toString().trim();

                if (!confirmPassword.isEmpty() && !password.equals(confirmPassword)) {
                    confirmPasswordEditText.setError("Passwords do not match");
                } else {
                    confirmPasswordEditText.setError(null);
                }
            }
        });
    }

    private void setupButtons() {
        signUpButton.setOnClickListener(v -> handleSignUp());

        loginLink.setOnClickListener(v -> {
            startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void updateSignUpButtonState() {
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();
        String fullName = fullNameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String schoolName = schoolNameEditText.getText().toString().trim();

        boolean isValid = !username.isEmpty()
                && !password.isEmpty()
                && !confirmPassword.isEmpty()
                && !fullName.isEmpty()
                && !email.isEmpty()
                && !selectedRole.isEmpty()
                && selectedBirthday != null
                && !schoolName.isEmpty()
                && !selectedWard.isEmpty()
                && !selectedGrade.isEmpty()
                && !isLoading;

        signUpButton.setEnabled(isValid);

        // Change button background tint based on enabled state
        if (isValid) {
            signUpButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E22B2B")));
        } else {
            signUpButton.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
        }
    }

    private boolean validateInputs() {
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();

        // Validate username
        if (username.length() < 3) {
            usernameEditText.setError("Username must be at least 3 characters");
            usernameEditText.requestFocus();
            return false;
        }

        // Validate password
        if (password.length() < 6) {
            passwordEditText.setError("Password must be at least 6 characters");
            passwordEditText.requestFocus();
            return false;
        }

        // Validate confirm password
        if (!password.equals(confirmPassword)) {
            confirmPasswordEditText.setError("Passwords do not match");
            confirmPasswordEditText.requestFocus();
            return false;
        }

        // Validate email
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Please enter a valid email address");
            emailEditText.requestFocus();
            return false;
        }

        return true;
    }

    private void handleSignUp() {
        if (!validateInputs()) {
            return;
        }

        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String fullName = fullNameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String schoolName = schoolNameEditText.getText().toString().trim();
        String grade = selectedGrade;

        setLoading(true);

        AuthApiService.getInstance().registerAsync(
                username,
                password,
                fullName,
                email,
                selectedRole,
                selectedBirthday,
                schoolName,
                selectedWard,
                grade,
                new AuthApiService.RegisterCallback() {
                    @Override
                    public void onSuccess(String message) {
                        runOnUiThread(() -> {
                            setLoading(false);
                            Toast.makeText(SignUpActivity.this,
                                    "Registration successful! Please login.",
                                    Toast.LENGTH_LONG).show();
                            startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
                            finish();
                        });
                    }

                    @Override
                    public void onError(Exception e) {
                        runOnUiThread(() -> {
                            setLoading(false);
                            Toast.makeText(SignUpActivity.this,
                                    "Registration failed: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        });
                    }
                });
    }

    private void setLoading(boolean loading) {
        isLoading = loading;
        updateSignUpButtonState();
        loadingProgressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        usernameEditText.setEnabled(!loading);
        passwordEditText.setEnabled(!loading);
        confirmPasswordEditText.setEnabled(!loading);
        fullNameEditText.setEnabled(!loading);
        emailEditText.setEnabled(!loading);
        schoolNameEditText.setEnabled(!loading);
        wardSpinner.setEnabled(!loading);
        gradeSpinner.setEnabled(!loading);
        birthdayTextView.setEnabled(!loading);
    }
}
