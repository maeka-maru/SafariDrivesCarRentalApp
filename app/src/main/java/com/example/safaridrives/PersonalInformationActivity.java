package com.example.safaridrives;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PersonalInformationActivity extends AppCompatActivity {
    private EditText surname, contactInfo, nationalID, licenseNumber;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private Button next, submit;
    private Map<String, Object> personalDetails;
    private Map<String, String> booking_details;
    private Intent getIntent;
    private String rentalId, userId, booking;
    private Object plateNumber, brandName, modelName;
    private String plate_number, brand_name, rental_id, model_name, co_lor, rent_price, date_to,date_from;
    private boolean check = true;
    private int submitCounter = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_information);

        surname = findViewById(R.id.surname);
        contactInfo = findViewById(R.id.contact_number);
        nationalID = findViewById(R.id.national_id);
        licenseNumber = findViewById(R.id.license_number);
        next = findViewById(R.id.next_report);
        submit = findViewById(R.id.submit_personal_info);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        personalDetails = new HashMap<>();
        booking_details = new HashMap<>();
        getIntent = getIntent();
        booking = "Yes";

        //getting details that have been passed from the previous activity(the booking details to be exact).
        // The booking details are passed on to the customerCarChoice collection in firebase.
        // The personal details are passed on to the Customers collection in firebase.
        if (getIntent != null && getIntent.hasExtra("car_picked_info")) {
            HashMap<String, Object> receivedData = (HashMap<String, Object>) getIntent.getSerializableExtra("car_picked_info");
            if (receivedData.containsKey("rentalId")) {
                rentalId = receivedData.get("rentalId").toString();
                rental_id = receivedData.get("rentalId").toString();
                personalDetails.put("rentalId", rentalId);
                booking_details.put("rentalId", rental_id);
            }
            if (receivedData.containsKey("plateNumber")) {
                plateNumber = receivedData.get("plateNumber");
                plate_number = receivedData.get("plateNumber").toString();
                booking_details.put("plateNumber", plate_number);
            }
            if (receivedData.containsKey("brandName")){
                brandName = receivedData.get("brandName");
                brand_name = receivedData.get("brandName").toString();
                booking_details.put("brandName", brand_name);
            }
            if (receivedData.containsKey("modelName")){
                modelName = receivedData.get("modelName");
                model_name = receivedData.get("modelName").toString();
                booking_details.put("modelName", model_name);
            }
            if (receivedData.containsKey("color")){
                co_lor = receivedData.get("color").toString();
                booking_details.put("color",co_lor);
            }
            if (receivedData.containsKey("rentPricePerDay")){
                rent_price = receivedData.get("rentPricePerDay").toString();
                booking_details.put("rentPricePerDay", rent_price);
            }
            if (receivedData.containsKey("dateFrom")){
                date_from = receivedData.get("dateFrom").toString();
                booking_details.put("dateFrom", date_from);
            }
            if (receivedData.containsKey("dateTo")){
                date_to = receivedData.get("dateTo").toString();
                booking_details.put("dateTo", date_to);
            }
        }

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (submitCounter == 0) {
                    // Only proceed with data submission if the counter is 0 (first click).
                    submitCounter++; // Increment the counter to prevent multiple submissions.
                    check = false;

                    personalDetails.put("surname", surname.getText().toString());
                    personalDetails.put("nationalId", nationalID.getText().toString());
                    personalDetails.put("phoneNumber", contactInfo.getText().toString());
                    personalDetails.put("licenseNumber", licenseNumber.getText().toString());

                    // Updating the booking details from the previous activity.
                    String documentName = String.valueOf(rentalId);
                    db.collection("customerCarChoice").document(documentName).set(booking_details).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                /*Toast.makeText(PersonalInformationActivity.this, "Data added", Toast.LENGTH_SHORT).show();*/
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(PersonalInformationActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });



                    db.collection("Customers").document(rentalId).set(personalDetails)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                       /* Toast.makeText(PersonalInformationActivity.this, "Booking completed", Toast.LENGTH_SHORT).show();*/

                                    } else {
                                        Toast.makeText(PersonalInformationActivity.this, "Data entry failed", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                    personalDetails.put("plateNumber", plateNumber);
                    personalDetails.put("rentalId", rentalId);
                    personalDetails.put("modelName", modelName);
                    personalDetails.put("brandName", brandName);

                    userId = auth.getCurrentUser().getUid();
                    db.collection("RegisteredUsers")
                            .whereEqualTo("uid", userId)
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        QuerySnapshot querySnapshot = task.getResult();
                                        if (!querySnapshot.isEmpty()) {
                                            DocumentSnapshot document = querySnapshot.getDocuments().get(0);
                                            List<String> rentalIds = (List<String>) document.get("rentalIds");
                                            if (rentalIds == null) {
                                                rentalIds = new ArrayList<>();
                                            }
                                            rentalIds.add(rentalId);

                                            Map<String, Object> updatedData = new HashMap<>();
                                            updatedData.put("booked", booking);
                                            updatedData.put("rentalIds", rentalIds);

                                            // Update the 'booked' field and 'rentalIds' array
                                            document.getReference().update(updatedData)
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> updateTask) {
                                                            if (updateTask.isSuccessful()) {
                                                                // Update successful
                                                                Toast.makeText(PersonalInformationActivity.this, "Booking status and RentalIds updated", Toast.LENGTH_SHORT).show();
                                                            } else {
                                                                // Error occurred while updating
                                                                Exception e = updateTask.getException();
                                                                Toast.makeText(PersonalInformationActivity.this, "Failed to update booking status and RentalIds: " + e, Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                    });
                                        } else {
                                            // User not found
                                            Toast.makeText(PersonalInformationActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        // Error occurred while querying
                                        Exception e = task.getException();
                                        Toast.makeText(PersonalInformationActivity.this, "Failed to query user", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });


                } else {
                    // Display the toast message if the counter is greater than 0 showing you've already hit the submit button
                    Toast.makeText(PersonalInformationActivity.this, "Submission has already occurred.", Toast.LENGTH_SHORT).show();
                }

            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!check){
                    navigateToWebViewActivity();
                }else{
                    Toast.makeText(PersonalInformationActivity.this, "Please press the submit button before continuing", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    //this navigates to the final activity where the rental id is displayed.
    private void navigateToWebViewActivity() {
        Intent intent = new Intent(PersonalInformationActivity.this, FinalActivity.class);
        HashMap<String, Object> carData = new HashMap<>();
        carData.putAll(personalDetails);
        intent.putExtra("car_data", carData);
        startActivity(intent); //starting the final activity
    }

}
