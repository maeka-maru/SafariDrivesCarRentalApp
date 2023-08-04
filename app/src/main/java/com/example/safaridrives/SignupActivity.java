package com.example.safaridrives;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class SignupActivity extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    private EditText usernameEditText, emailEditText, passwordEditText, confirmPasswordEditText;
    private Button signupButton;
    private ProgressBar progressBarSignup;
    private TextView login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        usernameEditText = findViewById(R.id.edtTxtUsernameSignup);
        emailEditText = findViewById(R.id.edtTxtEmailSignup);
        passwordEditText = findViewById(R.id.edtTxtPasswordSignup);
        confirmPasswordEditText = findViewById(R.id.edtTxtConfirmPasswordSignup);
        signupButton = findViewById(R.id.buttonSignup);
        progressBarSignup = findViewById(R.id.progressBarSignup);
        login = findViewById(R.id.txtViewAlreadyHaveAccount);

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String username = usernameEditText.getText().toString().trim();
                final String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();
                String confirmPassword = confirmPasswordEditText.getText().toString().trim();

                if (username.isEmpty()) {
                    usernameEditText.setError("Username is required");
                    usernameEditText.requestFocus();
                    return;
                }

                if (email.isEmpty()) {
                    emailEditText.setError("Email is required");
                    emailEditText.requestFocus();
                    return;
                }

                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    emailEditText.setError("Enter a valid email address");
                    emailEditText.requestFocus();
                    return;
                }

                if (password.isEmpty()) {
                    passwordEditText.setError("Password is required");
                    passwordEditText.requestFocus();
                    return;
                }

                if (password.length() < 6) {
                    passwordEditText.setError("Password must be at least 6 characters long");
                    passwordEditText.requestFocus();
                    return;
                }

                if (!password.equals(confirmPassword)) {
                    confirmPasswordEditText.setError("Passwords do not match");
                    confirmPasswordEditText.requestFocus();
                    return;
                }

                progressBarSignup.setVisibility(View.VISIBLE);

                // Check if the username already exists in Firestore
                firestore.collection("RegisteredUsers")
                        .whereEqualTo("username", username)
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                if (task.getResult() != null && !task.getResult().isEmpty()) {
                                    // Username already exists
                                    progressBarSignup.setVisibility(View.INVISIBLE);
                                    Toast.makeText(SignupActivity.this, "Username is already taken", Toast.LENGTH_SHORT).show();
                                } else {
                                    // Username is available, create the user
                                    firebaseAuth.createUserWithEmailAndPassword(email, password)
                                            .addOnCompleteListener(createUserTask -> {
                                                progressBarSignup.setVisibility(View.INVISIBLE);

                                                if (createUserTask.isSuccessful()) {
                                                    FirebaseUser user = firebaseAuth.getCurrentUser();
                                                    String uid = user.getUid();

                                                    // Store the username, email, and UID in Firestore
                                                    Users newUser = new Users(username, email, uid);
                                                    firestore.collection("RegisteredUsers")
                                                            .document(uid)
                                                            .set(newUser)
                                                            .addOnSuccessListener(documentReference -> {
                                                                Toast.makeText(SignupActivity.this, "Sign up successful!", Toast.LENGTH_SHORT).show();
                                                                startActivity(new Intent(SignupActivity.this, DurationActivity.class));
                                                            })
                                                            .addOnFailureListener(e -> {
                                                                Toast.makeText(SignupActivity.this, "Failed to store user data. Please try again.", Toast.LENGTH_SHORT).show();
                                                            });
                                                } else {
                                                    Toast.makeText(SignupActivity.this, "Sign up failed. Please try again.", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                }
                            } else {
                                Toast.makeText(SignupActivity.this, "Failed to check username availability. Please try again.", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignupActivity.this, LoginActivity.class));
            }
        });
    }
}
