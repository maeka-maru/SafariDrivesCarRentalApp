package com.example.safaridrives;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {
    private EditText username, password;
    private Button login;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private ProgressBar progressBarSignin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        login = findViewById(R.id.buttonLogin);
        username = findViewById(R.id.edtTxtUsernameLogin);
        password = findViewById(R.id.edtTxtPasswordLogin);
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        progressBarSignin  = findViewById(R.id.progressBarSignin);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String txt_username = username.getText().toString().trim();
                String txt_password = password.getText().toString().trim();

                if (txt_password.isEmpty()) {
                    password.setError("Password is required");
                    password.requestFocus();
                    return;
                }

                if (txt_password.length() < 6) {
                    password.setError("Password must be at least 6 characters long");
                    password.requestFocus();
                    return;
                }
                progressBarSignin.setVisibility(View.VISIBLE);

                // Query Firestore to find the user document with the entered username
                firestore.collection("RegisteredUsers")
                        .whereEqualTo("username", txt_username)
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                if (task.getResult() != null && !task.getResult().isEmpty()) {
                                    // User document found
                                    DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                                    String email = documentSnapshot.getString("email");
                                    String booking = documentSnapshot.getString("booked");
                                    progressBarSignin.setVisibility(View.INVISIBLE);

                                    auth.signInWithEmailAndPassword(email, txt_password)
                                            .addOnCompleteListener(loginTask -> {
                                                if (loginTask.isSuccessful()) {
                                                    if (booking != null && booking.equals("Yes")) {
                                                        Toast.makeText(LoginActivity.this, "Sign in successful!", Toast.LENGTH_SHORT).show();
                                                        startActivity(new Intent(LoginActivity.this, InformationActivity.class));
                                                    } else {
                                                        Toast.makeText(LoginActivity.this, "Sign in successful!", Toast.LENGTH_SHORT).show();
                                                        startActivity(new Intent(LoginActivity.this, DurationActivity.class));
                                                    }
                                                } else {
                                                    Toast.makeText(LoginActivity.this, "Sign in failed. Please check your credentials.", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                } else {
                                    // User document not found
                                    Toast.makeText(LoginActivity.this, "Username not found.", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                // Failed to retrieve user document
                                Toast.makeText(LoginActivity.this, "Failed to retrieve user data. Please try again.", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

    }
}
