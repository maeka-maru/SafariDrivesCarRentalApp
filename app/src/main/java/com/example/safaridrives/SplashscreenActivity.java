package com.example.safaridrives;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class SplashscreenActivity extends AppCompatActivity {
    private Intent intent;
    private FirebaseAuth auth;
    private FirebaseFirestore store;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashscreen);
        store = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    store.collection("RegisteredUsers")
                            .whereEqualTo("uid", userId)
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        QuerySnapshot querySnapshot = task.getResult();
                                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                                            for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                                                if (document.contains("booked") && document.getString("booked").equals("Yes")) {
                                                    startActivity(new Intent(SplashscreenActivity.this, InformationActivity.class));
                                                    finish();
                                                    return; // Exit the method to avoid executing the remaining logic
                                                }
                                            }
                                        }
                                    }

                                    // Proceed with the existing logic
                                    startActivity(new Intent(SplashscreenActivity.this, DurationActivity.class));
                                    finish();
                                }
                            });
                }
            }, 3000); // Add a 3-second delay before executing the code inside the Handler
        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    startActivity(new Intent(SplashscreenActivity.this, SignupActivity.class));
                    finish();
                }
            }, 3000); // Add a 3-second delay before executing the code inside the Handler
        }
    }

    }