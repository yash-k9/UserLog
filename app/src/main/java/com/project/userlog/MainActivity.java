package com.project.userlog;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;


import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;



public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    public static final int LOCATION_REQUEST = 901;

    public String email, location, time;


    FusedLocationProviderClient fusedLocationProviderClient;
    RequestQueue queue;
    TextView status;
    Button submit;
    View parentLayout;
    Activity activity;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        status = findViewById(R.id.status);
        submit = findViewById(R.id.submit);
        activity = this;
        progressBar = findViewById(R.id.progress_bar);
        parentLayout = findViewById(android.R.id.content);
        fusedLocationProviderClient = new FusedLocationProviderClient(this);
    }


    /*On Button Click - logData()
    *
    * Check for Permission
    * Get the location, email and current time - getLocation()
    * Send the Request using Volley - sendRequest()
    * If Permission Denied - showDialog();
    *
    * */

    public void logData(View view)  {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            progressBar.setVisibility(View.VISIBLE);
            submit.setClickable(false);
            getLocation();

        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST);
        }
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    ExecutorService executors = Executors.newFixedThreadPool(2);
                    executors.execute(new Runnable() {
                        @Override
                        public void run() {
                            Location currLocation = task.getResult();
                            if (task.isSuccessful()) {
                                Calendar calendar = Calendar.getInstance();
                                int hours = calendar.get(Calendar.HOUR_OF_DAY);
                                int min = calendar.get(Calendar.MINUTE);
                                time = hours+":"+min;
                                email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
                                if(currLocation != null){
                                    location = (int)currLocation.getLatitude() + "'" +(int)currLocation.getLongitude()+"''";
                                }else{
                                    location = "NA";
                                }
                                sendRequest();
                            }
                        }
                    });
                }
            });
        }else{
            showDialog();
        }
    }

    private void sendRequest() {
        queue = Volley.newRequestQueue(this);
        String url = "https://script.google.com/macros/s/AKfycbzPzXFrmp44r8PL9zEdrJeSW0LeNOuf7uWYb2txJESBOnUrtoo/exec?email="+email+"&location="+location+"&time="+time;


        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, response -> {
            Log.d(TAG, response);
            progressBar.setVisibility(View.GONE);
            Snackbar.make(parentLayout, "Data Logged Successfully", Snackbar.LENGTH_LONG).show();
            submit.setClickable(true);
        }, error -> {
            progressBar.setVisibility(View.GONE);
            Log.d(TAG, ""+error.getMessage());
            Snackbar.make(parentLayout, "Network Error !", Snackbar.LENGTH_LONG).show();
            submit.setClickable(true);
        });

        stringRequest.setTag(TAG);
        queue.add(stringRequest);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0] != PackageManager.PERMISSION_GRANTED){
            showDialog();
        }
    }

    private void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setMessage("This Application needs GPS to function")
                .setPositiveButton("Enable", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }



    /*Options Menu*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.signout, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.logout:
                AuthUI.getInstance().signOut(this)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    updateToLoginUI();
                                }
                                else{
                                    Log.d(TAG, ""+task.getException());
                                }
                            }
                        });
                updateToLoginUI();
                break;
        }
        return true;
    }

    private void updateToLoginUI() {
        Intent intent = new Intent(this, Login.class);
        startActivity(intent);
        this.finish();
    }

    @Override
    protected void onStop () {
        super.onStop();
        if (queue != null) {
            queue.cancelAll(TAG);
        }
    }
}