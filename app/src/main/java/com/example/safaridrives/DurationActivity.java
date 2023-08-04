package com.example.safaridrives;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class DurationActivity extends AppCompatActivity {
    private Button next, submit,hamburgerButton;
    private Intent sendIntent, sendIntentTwo;
    private Spinner brand, model;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String plateNumber, color, rentPrice, userId, rentalID;
    private Map<String, List<String>> brandModelMap;
    private Map<String, Object> data_sent_personal, customer_data, upload_data;
    private EditText dateFrom, dateTo;
    private ImageView carImage;
    private TextView price, totalPrice;
    private boolean check = true;
    private ProgressBar mProgressBar;
    private int progressStatus = 0;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_duration);
        setTitle(getString(R.string.app_name));

        auth = FirebaseAuth.getInstance();
        next = findViewById(R.id.next_personal_info);
        brand = findViewById(R.id.spinnerBrand);
        model = findViewById(R.id.spinnerModel);
        submit = findViewById(R.id.submit_Car_Date);
        db = FirebaseFirestore.getInstance();
        brandModelMap = new HashMap<>();
        data_sent_personal = new HashMap<>();
        customer_data = new HashMap<>();
        upload_data = new HashMap<>();
        sendIntent = new Intent(DurationActivity.this, PersonalInformationActivity.class);
        sendIntentTwo = new Intent(DurationActivity.this, FinalActivity.class);
        dateFrom = findViewById(R.id.edtTxtDateFrom);
        dateTo = findViewById(R.id.edtTxtDateTo);
        dateFrom.setFocusable(false);
        dateFrom.setClickable(false);
        carImage = findViewById(R.id.car_image);
        price = findViewById(R.id.viewPrice);
        totalPrice = findViewById(R.id.viewTotalPrice);
        mProgressBar = findViewById(R.id.progress_bar);
        hamburgerButton = findViewById(R.id.hamburgerButton);


        userId = auth.getCurrentUser().getUid();

        dateTo.setFocusable(false);
        dateTo.setClickable(false);

        List<String> brandNames = new ArrayList<>();
        // On click listener for the date From and To , this is to select the dates
        dateFrom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(dateFrom);
            }
        });

        dateTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(dateTo);
            }
        });
        //this is a query to gather all the current available cars that are recognized with yes.
        db.collection("Cars").whereEqualTo("available", "Yes").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot document : task.getResult()) {
                        String brandName = document.getString("brandName");
                        String modelName = document.getString("modelName");

                        if (!brandNames.contains(brandName)) {
                            brandNames.add(brandName);
                        }
                        List<String> carModels = brandModelMap.get(brandName);
                        if (carModels == null) {
                            carModels = new ArrayList<>();
                            brandModelMap.put(brandName, carModels);
                        }
                        if (!carModels.contains(modelName)) {
                            carModels.add(modelName);
                        }
                    }

                    ArrayAdapter<String> brandAdapter = new ArrayAdapter<>(DurationActivity.this, android.R.layout.simple_spinner_item, brandNames);
                    brandAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    brand.setAdapter(brandAdapter);
                }
            }
        });
        //selecting the car brand
        brand.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedBrand = parent.getItemAtPosition(position).toString();
                List<String> carModels = brandModelMap.get(selectedBrand);
                if (carModels != null) {
                    ArrayAdapter<String> modelAdapter = new ArrayAdapter<>(DurationActivity.this, android.R.layout.simple_spinner_item, carModels);
                    modelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    model.setAdapter(modelAdapter);

                    // Get the selected model from the model spinner
                    String selectedModel = model.getSelectedItem() != null ? model.getSelectedItem().toString() : "";

                    // Display car image after the user has finished selecting brand and model
                    displayCarImage(selectedBrand, selectedModel);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        //selecting the car model
        model.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedBrand = brand.getSelectedItem() != null ? brand.getSelectedItem().toString() : "";
                String selectedModel = parent.getItemAtPosition(position).toString();

                // Display car image after the user has finished selecting brand and model
                displayCarImage(selectedBrand, selectedModel);

                // collecting the model price.
                db.collection("Cars").whereEqualTo("modelName", selectedModel).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QuerySnapshot query = task.getResult();
                            if (query != null && !query.isEmpty()) {
                                DocumentSnapshot document = query.getDocuments().get(0);
                                String rentPrice = document.getString("rentPricePerDay");
                                if (rentPrice != null) {
                                    price.setText(rentPrice);
                                } else {
                                    Toast.makeText(DurationActivity.this, "Price not found!", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(DurationActivity.this, "Query failed!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                check = false;
                String selectedBrand = brand.getSelectedItem() != null ? brand.getSelectedItem().toString() : "";
                String selectedModel = model.getSelectedItem() != null ? model.getSelectedItem().toString() : "";


                if (!selectedBrand.isEmpty() && !selectedModel.isEmpty()) {
                    String dateFromStr = dateFrom.getText().toString();
                    String dateToStr = dateTo.getText().toString();

                    if (!dateFromStr.isEmpty() && !dateToStr.isEmpty()) {
                        // Check for booking conflicts before proceeding with the booking
                        doesBookingConflict(dateFromStr, dateToStr, selectedBrand, selectedModel);

                    } else {
                        Toast.makeText(DurationActivity.this, "Please select both date from and date to.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(DurationActivity.this, "Please select a brand and model", Toast.LENGTH_SHORT).show();
                }
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!check){
                    checkDetailsExist();
                }
                else{
                    Toast.makeText(DurationActivity.this, "Please press the submit button before continuing", Toast.LENGTH_SHORT).show();
                }

            }
        });

        hamburgerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show the menu when the button is clicked
                showPopupMenu();
            }
        });
    }

    //checkin whether there is a booking already for the current car in choice to avoid double booking

    private void doesBookingConflict(String dateFrom, String dateTo, String selectedBrand, String selectedModel) {
        db.collection("customerCarChoice")
                .whereEqualTo("brandName", selectedBrand)
                .whereEqualTo("modelName", selectedModel)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<String> conflictingBookings = new ArrayList<>();
                            for (DocumentSnapshot document : task.getResult()) {
                                String existingDateFrom = document.getString("dateFrom");
                                String existingDateTo = document.getString("dateTo");

                                if (existingDateFrom == null || existingDateTo == null) {
                                    continue; // Skip this document as it doesn't have valid date information
                                }

                                // Check if the chosen date range overlaps with any existing booking
                                if (isDateRangeOverlapping(dateFrom, dateTo, existingDateFrom, existingDateTo)) {
                                    conflictingBookings.add(existingDateFrom + " to " + existingDateTo);
                                }
                            }

                            if (!conflictingBookings.isEmpty()) {
                                // Conflict found, show a custom dialog with conflicting bookings
                                showConflictDialog(conflictingBookings);
                            } else {
                                // If no conflict, proceed with the booking process
                                startFillingAnimation();
                                addBookingToDatabase(selectedBrand, selectedModel);
                            }
                        }
                    }
                });
    }

    // displaying that the dialog that dictates when other bookings exist
    private void showConflictDialog(List<String> conflictingBookings) {
        // Inflate the custom dialog layout with the details
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.custom_dialog_layout, null);

        // Find and set the conflictingBookingsTextView content
        TextView conflictingBookingsTextView = dialogView.findViewById(R.id.conflictingBookingsTextView);
        StringBuilder message = new StringBuilder("Sorry, the car is already booked for the chosen dates. The car is not available on the following dates:\n");
        for (String conflictingBooking : conflictingBookings) {
            message.append("- ").append(conflictingBooking).append("\n");
        }
        conflictingBookingsTextView.setText(message.toString());

        // Create the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //this check is to ensure that the submit is clicked before the next button
                check = true;
            }
        });

        // Set the custom style for the positive button
        AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button button = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                button.setTextColor(Color.parseColor("#007BFF")); // Change the text color to #007BFF
                button.setBackgroundResource(R.drawable.custom_button_background); // Apply the custom button background
            }
        });

        alertDialog.show();
    }




    //the data that will be carried to the next activity for uploading.
    private void addBookingToDatabase(String selectedBrand, String selectedModel) {
        db.collection("Cars")
                .whereEqualTo("brandName", selectedBrand)
                .whereEqualTo("modelName", selectedModel)
                .whereEqualTo("available", "Yes")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<DocumentSnapshot> matchingDocuments = task.getResult().getDocuments();
                            if (!matchingDocuments.isEmpty()) {
                                DocumentSnapshot document = matchingDocuments.get(0);
                                plateNumber = document.getString("plateNumber");
                                color = document.getString("color");
                                rentPrice = document.getString("rentPricePerDay");

                                data_sent_personal.put("plateNumber", plateNumber);
                                int rentalId = generateRandomId();
                                rentalID = String.valueOf(rentalId);
                                data_sent_personal.put("rentalId", rentalId);
                                data_sent_personal.put("modelName", selectedModel);
                                upload_data.put("modelName", selectedModel);
                                data_sent_personal.put("brandName", selectedBrand);
                                upload_data.put("brandName", selectedBrand);
                                data_sent_personal.put("color", color);
                                upload_data.put("color", color);
                                data_sent_personal.put("rentPricePerDay", rentPrice);
                                upload_data.put("rentPricePerDay", rentPrice);
                                data_sent_personal.put("plateNumber", plateNumber);
                                upload_data.put("plateNumber", plateNumber);

                                String imageUrl = document.getString("imageURL");
                                if (imageUrl != null && !imageUrl.isEmpty()) {
                                    Picasso.get().load(imageUrl).into(carImage);
                                    carImage.setVisibility(View.VISIBLE);
                                }

                            }
                        }
                    }
                });
    }

    // this is to display the calendar / date picker
    private void showDatePickerDialog(final EditText editText) {
        // Check if the dateFrom is already selected
        if (editText == dateTo && data_sent_personal.get("dateFrom") == null) {
            Toast.makeText(DurationActivity.this, "Please select Date From first.", Toast.LENGTH_SHORT).show();
        } else {
            // Get current date
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            // Create DatePickerDialog
            DatePickerDialog datePickerDialog = new DatePickerDialog(DurationActivity.this, new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                    // Set selected date to the EditText
                    String selectedDate = String.format("%02d/%02d/%04d", dayOfMonth, monthOfYear + 1, year);
                    editText.setText(selectedDate);

                    String key = editText == dateTo ? "dateTo" : "dateFrom";

                    // Check if "Date to" is less than "Date From"
                    if (editText == dateTo && isDateToLessThanDateFrom(selectedDate)) {
                        Toast.makeText(DurationActivity.this, "Date To cannot be less than Date From", Toast.LENGTH_SHORT).show();
                        editText.setText(""); // Clear the date field
                        data_sent_personal.remove(key); // Remove the invalid date from data_sent_personal
                    } else if (isDateLessThanCurrentDate(selectedDate)) {
                        Toast.makeText(DurationActivity.this, "Selected date cannot be less than the current date", Toast.LENGTH_SHORT).show();
                        editText.setText(""); // Clear the date field
                        data_sent_personal.remove(key); // Remove the invalid date from data_sent_personal
                    } else {
                        data_sent_personal.put(key, selectedDate);
                        // Calculate total price only if both dateFrom and dateTo are selected
                        if (data_sent_personal.containsKey("dateFrom") && data_sent_personal.containsKey("dateTo")) {
                            String selectedBrand = brand.getSelectedItem() != null ? brand.getSelectedItem().toString() : "";
                            String selectedModel = model.getSelectedItem() != null ? model.getSelectedItem().toString() : "";
                            calculateTotalPrice(data_sent_personal.get("dateFrom").toString(), selectedDate, selectedBrand, selectedModel);
                        }
                    }
                }
            }, year, month, day);

            // Show the dialog
            datePickerDialog.show();
        }
    }

    //checking if the date to is less than date from
    private boolean isDateToLessThanDateFrom(String dateTo) {
        String dateFrom = (String) data_sent_personal.get("dateFrom");

        String[] dateToParts = dateTo.split("/");
        String[] dateFromParts = dateFrom.split("/");

        int yearTo = Integer.parseInt(dateToParts[2]);
        int monthTo = Integer.parseInt(dateToParts[1]);
        int dayTo = Integer.parseInt(dateToParts[0]);

        int yearFrom = Integer.parseInt(dateFromParts[2]);
        int monthFrom = Integer.parseInt(dateFromParts[1]);
        int dayFrom = Integer.parseInt(dateFromParts[0]);

        if (yearTo < yearFrom || (yearTo == yearFrom && monthTo < monthFrom) || (yearTo == yearFrom && monthTo == monthFrom && dayTo < dayFrom)) {
            return true;
        }
        return false;
    }

    //checking if the date is less than the current date

    private boolean isDateLessThanCurrentDate(String selectedDate) {
        // Get the current date
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int currentMonth = calendar.get(Calendar.MONTH) + 1; // Month value is zero-based, so add 1
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);

        // Extract year, month, and day from the selected date
        String[] dateParts = selectedDate.split("/");
        int year = Integer.parseInt(dateParts[2]);
        int month = Integer.parseInt(dateParts[1]);
        int day = Integer.parseInt(dateParts[0]);

        // Compare the selected date with the current date
        if (year < currentYear || (year == currentYear && month < currentMonth) || (year == currentYear && month == currentMonth && day < currentDay)) {
            return true;
        }
        return false;
    }

    //check if the date ranges are overlapping to ensure there is no double booking within the same duration
    private boolean isDateRangeOverlapping(String dateFrom1, String dateTo1, String dateFrom2, String dateTo2) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            // using the simple date format to convert the string as stored in the database to a date.
            Date fromDate1 = sdf.parse(dateFrom1);
            Date toDate1 = sdf.parse(dateTo1);
            Date fromDate2 = sdf.parse(dateFrom2);
            Date toDate2 = sdf.parse(dateTo2);

            return (fromDate1.compareTo(toDate2) <= 0 && toDate1.compareTo(fromDate2) >= 0);
        } catch (ParseException e) {
            //printing the stack trace of an exception
            e.printStackTrace();
            return false;
        }
    }

    //generating the rental id
    private int generateRandomId() {
        // Generate a 6-digit random ID
        Random random = new Random();
        return random.nextInt(900000) + 100000;
    }

    //displaying the image of the car using the picassio dependency.
    private void displayCarImage(String selectedBrand, String selectedModel) {
        db.collection("Cars")
                .whereEqualTo("brandName", selectedBrand)
                .whereEqualTo("modelName", selectedModel)
                .whereEqualTo("available", "Yes")
                .limit(1)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<DocumentSnapshot> matchingDocuments = task.getResult().getDocuments();
                            if (!matchingDocuments.isEmpty()) {
                                DocumentSnapshot document = matchingDocuments.get(0);
                                String imageUrl = document.getString("imageURL");
                                if (imageUrl != null && !imageUrl.isEmpty()) {
                                    Picasso.get().load(imageUrl).into(carImage);
                                    carImage.setVisibility(View.VISIBLE);
                                }
                            }
                        }
                    }
                });
    }

    //calculating the total price of the booking to show it to the user
    private void calculateTotalPrice(String dateFrom, String dateTo, String selectedBrand, String selectedModel) {
        // Calculate the total price based on the rentPrice and the number of days between dateFrom and dateTo
        db.collection("Cars").whereEqualTo("modelName", selectedModel).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    QuerySnapshot query = task.getResult();
                    if (query != null && !query.isEmpty()) {
                        DocumentSnapshot document = query.getDocuments().get(0);
                        String rentPriceStr = document.getString("rentPricePerDay");
                        if (rentPriceStr != null) {
                            double rentPrice = Double.parseDouble(rentPriceStr);
                            double totalPrices = calculateTotalPrice(rentPrice, dateFrom, dateTo);
                            totalPrices = Math.round(totalPrices * 100.0) / 100.0; // Round to 2 decimal places
                            totalPrice.setText(String.valueOf(totalPrices));
                        } else {
                            Toast.makeText(DurationActivity.this, "Price not found!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(DurationActivity.this, "Query failed!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private double calculateTotalPrice(double rentPrice, String dateFrom, String dateTo) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        try {
            Date fromDate = sdf.parse(dateFrom);
            Date toDate = sdf.parse(dateTo);

            //calculating the days
            long diffInMilliseconds = toDate.getTime() - fromDate.getTime();
            long diffInDays = diffInMilliseconds / (24 * 60 * 60 * 1000);

            if (diffInDays == 0) {
                diffInDays = 1;
            }

            return rentPrice * diffInDays;
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }

    private void sendToPersonalInformation(){
        HashMap<String, Object> serializableData = new HashMap<>();
        serializableData.putAll(data_sent_personal); // Create a deep copy of the map to send it to the next activity instead of querying everything.

        sendIntent.putExtra("car_picked_info", serializableData);
        startActivity(sendIntent);
    }

    private void sendToFinalActivity(){
        String documentName = rentalID;
        db.collection("RegisteredUsers").document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()){

                        List<String> rentalIds = (List<String>) document.get("rentalIds");

                        if (rentalIds != null && rentalIds.size() > 0) {
                            // Get the first element (assuming you want a Long value)
                            String firstRentalId = rentalIds.get(0);

                            db.collection("Customers").document(firstRentalId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()){
                                        DocumentSnapshot document = task.getResult();
                                        if(document.exists()){
                                            String surname, phonenumber, nationalId,licensenumber, datefrom, dateto;
                                            surname = document.getString("surname");
                                            customer_data.put("surname", surname);
                                            phonenumber = document.getString("phoneNumber");
                                            customer_data.put("phoneNumber", phonenumber);
                                            nationalId = document.getString("nationalId");
                                            customer_data.put("nationalId", nationalId);
                                            licensenumber = document.getString("licenseNumber");
                                            customer_data.put("licenseNumber", licensenumber);
                                            customer_data.put("rentalId", rentalID);
                                            upload_data.put("rentalId", rentalID);
                                            datefrom = data_sent_personal.get("dateFrom").toString();
                                            dateto = data_sent_personal.get("dateTo").toString();
                                            upload_data.put("dateFrom", datefrom);
                                            upload_data.put("dateTo", dateto);

                                            db.collection("Customers").document(documentName).set(customer_data).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful()){
                                                       /* Toast.makeText(DurationActivity.this, "Update complete", Toast.LENGTH_SHORT).show();*/
                                                    }
                                                }
                                            });
                                            db.collection("RegisteredUsers").document(userId).update("rentalIds", FieldValue.arrayUnion(documentName))
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {
                                                                // Rental ID added to the array, now proceed to the FinalActivity
                                                                // Create a deep copy of the map to send it to the next activity instead of querying everything.
                                                                /*Toast.makeText(DurationActivity.this, "Rental id updated", Toast.LENGTH_SHORT).show();*/
                                                            } else {
                                                                // Failed to add the rental ID to the array
                                                                Toast.makeText(DurationActivity.this, "Failed to add rental ID!", Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                    });
                                            db.collection("customerCarChoice").document(documentName).set(upload_data).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    Toast.makeText(DurationActivity.this, "Booking completed!", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                        else{
                                            Toast.makeText(DurationActivity.this, "", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }
                            });


                        } else {
                            Toast.makeText(DurationActivity.this, "RentalIds is empty!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });
        HashMap<String, Object> serializableData = new HashMap<>();
        serializableData.putAll(data_sent_personal); // Create a deep copy of the map to send it to the next activity instead of querying everything.

        sendIntentTwo.putExtra("car_data", serializableData);
        startActivity(sendIntentTwo);
    }

    private void checkDetailsExist(){

        db.collection("RegisteredUsers").document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    if(document.exists()){
                        String boolCheck;
                        boolCheck = document.getString("booked");
                        if ("Yes".equals(boolCheck)) {
                            sendToFinalActivity();
                        } else {
                            sendToPersonalInformation();
                        }

                    }
                }
            }
        });
    }

    private void startFillingAnimation() {
        new Thread(new Runnable() {
            public void run() {
                while (progressStatus < 100) {
                    progressStatus += 25;
                    // Update the progress bar on the UI thread.
                    handler.post(new Runnable() {
                        public void run() {
                            mProgressBar.setProgress(progressStatus);
                        }
                    });
                    try {
                        // Add a delay between each increment to control the speed of filling.
                        Thread.sleep(25); // You can adjust the delay time to change the animation speed.
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private void showPopupMenu() {
        PopupMenu popupMenu = new PopupMenu(this, hamburgerButton);
        popupMenu.getMenuInflater().inflate(R.menu.hamburger_menu, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_item2:
                        startActivity(new Intent(DurationActivity.this, InformationActivity.class));
                        return true;
                    case R.id.menu_item1:
                        auth.signOut();
                        startActivity(new Intent(DurationActivity.this, SignupActivity.class));
                        finish();
                        return true;
                }
                return false;
            }
        });

        popupMenu.show();
    }
}
