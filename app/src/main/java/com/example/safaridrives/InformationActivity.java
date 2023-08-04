package com.example.safaridrives;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.auth.User;
import com.example.safaridrives.UserInformation;

import java.util.ArrayList;
import java.util.List;

public class InformationActivity extends AppCompatActivity {
    private Button book, cancelBooking, hamburgerButton;
    private TextView licenseNumber, plateNumber, brandName, modelName, rental_Id, price;
    private FirebaseAuth auth;
    private FirebaseFirestore store;
    private String userId, plate_number, model_name, brand_name, rent_price, license_number;
    private Spinner carNumber;
    private List<String> borrowedCarsList;
    private String selectedPlateNumber, currentRentalId;
    private int completedQueries;
    private ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information);

        book = findViewById(R.id.buttonBookAnotherCar);
        licenseNumber = findViewById(R.id.textViewtheLicenseNumber);
        plateNumber = findViewById(R.id.textViewthePlateNumber);
        brandName = findViewById(R.id.textViewtheBrandName);
        modelName = findViewById(R.id.textViewtheModelName);
        rental_Id = findViewById(R.id.textViewtheRentalId);
        price = findViewById(R.id.textViewthePrice);
        cancelBooking = findViewById(R.id.buttonCancelBooking);
        carNumber = findViewById(R.id.spinnerCarCounter);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        hamburgerButton = findViewById(R.id.hamburgerButtonTwo);


        auth = FirebaseAuth.getInstance();
        store = FirebaseFirestore.getInstance();

        userId = auth.getCurrentUser().getUid();

        // querying the RegisteredUsers collection to gain the rental id's which are specific to each booking
        // inorder to display the plate numbers associated with the booking so that the users can
        store.collection("RegisteredUsers").document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot userDocument = task.getResult();
                    if (userDocument.contains("rentalIds")) {
                        List<String> rentalIds = (List<String>) userDocument.get("rentalIds");
                        populateSpinner(rentalIds);

                    }
                } else {
                    Toast.makeText(InformationActivity.this, "Failed to fetch user data", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //the button for booking another car.
        book.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(InformationActivity.this, DurationActivity.class));
            }
        });

        //the button for cancelling bookings
        cancelBooking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create a confirmation dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(InformationActivity.this);
                builder.setTitle("Cancel Booking");
                builder.setMessage("Are you sure you want to cancel this booking?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Perform cancellation of the booking
                        cancelBooking();
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing, cancel the dialog
                        dialog.dismiss();
                    }
                });

                // Show the confirmation dialog
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
        hamburgerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenu();
            }
        });
    }

    private void populateSpinner(List<String> rentalIds) {
        borrowedCarsList = new ArrayList<>();

        // Directly fetch the documents by their rental IDs
        for (String rentalId : rentalIds) {
            store.collection("customerCarChoice").document(rentalId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            // Assuming that each rentalId corresponds to one car
                            if (document.contains("plateNumber")) {
                                String plateNumber = document.getString("plateNumber");
                                borrowedCarsList.add(plateNumber);

                            }
                        }

                        // Populate the Spinner when all queries are completed
                        if (borrowedCarsList.size() == rentalIds.size()) {
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(InformationActivity.this,
                                    android.R.layout.simple_spinner_item, borrowedCarsList);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            carNumber.setAdapter(adapter);

                            // Set up the Spinner (carNumber) item selection listener
                            carNumber.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                    selectedPlateNumber = parent.getItemAtPosition(position).toString();
                                    currentRentalId = rentalIds.get(position);
                                    // Fetch and display car details based on the selected plate number
                                    fetchAndDisplayCarDetails(selectedPlateNumber);

                                    store.collection("Customers").document(currentRentalId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            DocumentSnapshot document = task.getResult();
                                            if (document.exists()){
                                                license_number = document.getString("licenseNumber");
                                                licenseNumber.setText(license_number);
                                                rental_Id.setText(currentRentalId);
                                            }
                                            else{
                                                Toast.makeText(InformationActivity.this, "Could not find the licenseNumber!", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }



                                @Override
                                public void onNothingSelected(AdapterView<?> parent) {
                                    // Do nothing if nothing is selected
                                }
                            });

                            // Fetch and display car details for the first plate number
                            if (!borrowedCarsList.isEmpty()) {
                                selectedPlateNumber = borrowedCarsList.get(0);
                                fetchAndDisplayCarDetails(selectedPlateNumber);
                            }
                        }
                    } else {
                        Toast.makeText(InformationActivity.this, "Failed to fetch user data", Toast.LENGTH_SHORT).show();
                    }


                }
            });
        }
    }

    private void fetchAndDisplayCarDetails(String selectedPlateNumber) {
        // Query "Cars" collection based on the selected plate number
        store.collection("Cars").document(selectedPlateNumber).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot carDocument = task.getResult();
                            if (carDocument.exists()) {
                                plate_number = carDocument.getString("plateNumber");
                                brand_name = carDocument.getString("brandName");
                                model_name = carDocument.getString("modelName");
                                rent_price = carDocument.getString("rentPricePerDay");

                                plateNumber.setText(plate_number);
                                brandName.setText(brand_name);
                                modelName.setText(model_name);
                                price.setText(rent_price);

                            } else {
                                Toast.makeText(InformationActivity.this, "No car details found for the selected plate number", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(InformationActivity.this, "Failed to fetch car details", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    private void cancelBooking() {
        if (currentRentalId != null && !currentRentalId.isEmpty()) {
            // Show the progress bar to indicate loading
            progressBar.setVisibility(View.VISIBLE);

            // Delete the document from the customerCarChoice collection
            store.collection("customerCarChoice").document(currentRentalId)
                    .delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // Document deleted successfully, now delete the document from Customers collection
                            store.collection("Customers").document(currentRentalId)
                                    .delete()
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            // Both documents deleted successfully, now update the rentalIds array
                                            // in the RegisteredUsers collection
                                            DocumentReference userDocumentRef = store.collection("RegisteredUsers").document(userId);
                                            userDocumentRef.update("rentalIds", FieldValue.arrayRemove(currentRentalId))
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            // Check if the rentalIds array is empty
                                                            userDocumentRef.get()
                                                                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                                        @Override
                                                                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                                            List<String> rentalIds = (List<String>) documentSnapshot.get("rentalIds");
                                                                            if (rentalIds == null || rentalIds.isEmpty()) {
                                                                                // If rentalIds array is empty, update the booked status
                                                                                userDocumentRef.update("booked", "No")
                                                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                            @Override
                                                                                            public void onSuccess(Void aVoid) {
                                                                                                // Booking canceled and status updated
                                                                                                Toast.makeText(InformationActivity.this, "Booking Canceled!", Toast.LENGTH_SHORT).show();
                                                                                                // Clear the car details fields since there are no more bookings
                                                                                                clearCarDetailsFields();
                                                                                                // Update the spinner adapter with an empty list
                                                                                                ArrayAdapter<String> adapter = new ArrayAdapter<>(InformationActivity.this,
                                                                                                        android.R.layout.simple_spinner_item, new ArrayList<>());
                                                                                                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                                                                                carNumber.setAdapter(adapter);
                                                                                            }
                                                                                        })
                                                                                        .addOnFailureListener(new OnFailureListener() {
                                                                                            @Override
                                                                                            public void onFailure(@NonNull Exception e) {
                                                                                                Toast.makeText(InformationActivity.this, "Failed to update booking status", Toast.LENGTH_SHORT).show();
                                                                                            }
                                                                                        });
                                                                            } else {
                                                                                // Booking canceled but there are other bookings
                                                                                Toast.makeText(InformationActivity.this, "Booking Canceled!", Toast.LENGTH_SHORT).show();
                                                                                // After removing the item, re-populate the spinner with the updated list
                                                                                populateSpinner(rentalIds);
                                                                            }

                                                                            // Hide the progress bar after the task is complete
                                                                            progressBar.setVisibility(View.GONE);
                                                                        }
                                                                    })
                                                                    .addOnFailureListener(new OnFailureListener() {
                                                                        @Override
                                                                        public void onFailure(@NonNull Exception e) {
                                                                            Toast.makeText(InformationActivity.this, "Failed to fetch user data", Toast.LENGTH_SHORT).show();
                                                                            // Hide the progress bar after the task is complete
                                                                            progressBar.setVisibility(View.GONE);
                                                                        }
                                                                    });
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Toast.makeText(InformationActivity.this, "Failed to remove rental id", Toast.LENGTH_SHORT).show();
                                                            // Hide the progress bar after the task is complete
                                                            progressBar.setVisibility(View.GONE);
                                                        }
                                                    });
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(InformationActivity.this, "Failed to delete booking details", Toast.LENGTH_SHORT).show();
                                            // Hide the progress bar after the task is complete
                                            progressBar.setVisibility(View.GONE);
                                        }
                                    });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(InformationActivity.this, "Failed to cancel booking", Toast.LENGTH_SHORT).show();
                            // Hide the progress bar after the task is complete
                            progressBar.setVisibility(View.GONE);
                        }
                    });
        }
    }

    // Helper method to clear the car details fields
    private void clearCarDetailsFields() {
        plateNumber.setText("");
        brandName.setText("");
        modelName.setText("");
        price.setText("");
        rental_Id.setText("");
        licenseNumber.setText("");
    }
    private void showPopupMenu() {
        PopupMenu popupMenu = new PopupMenu(this, hamburgerButton);
        popupMenu.getMenuInflater().inflate(R.menu.hamburger_menu_two, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_item1:
                        auth.signOut();
                        startActivity(new Intent(InformationActivity.this, SignupActivity.class));
                        finish();
                        return true;
                }
                return false;
            }
        });

        popupMenu.show();
    }

}
