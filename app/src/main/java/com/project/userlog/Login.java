package com.project.userlog;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;


import java.util.Arrays;
import java.util.List;

public class Login extends AppCompatActivity {

    static final String TAG = "Login";
    public static final int AUTH_CODE = 200;

    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        firebaseAuth = FirebaseAuth.getInstance();

        if(firebaseAuth.getCurrentUser() != null && isConnected()){
           updateUI();
        }
    }



    /* On Login Button Click*/

    public void Login(View view) {
        List<AuthUI.IdpConfig> providers = Arrays.asList(
            new AuthUI.IdpConfig.GoogleBuilder().build()
        );


        Intent intent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setTheme(R.style.AppTheme_color)
                .setAlwaysShowSignInMethodScreen(true)
                .build();

        if(isConnected()){
            startActivityForResult(intent, AUTH_CODE);
        }else{
            Toast.makeText(this, ""+getString(R.string.network_error), Toast.LENGTH_SHORT).show();
        }
    }


    /* onActivityResult for AuthUI */

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == AUTH_CODE){
            if(resultCode == RESULT_OK){
                Log.d(TAG, "onActivityResult");
                Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show();
                updateUI();
            }
            else{
                IdpResponse response = IdpResponse.fromResultIntent(data);
                if(response == null){
                    Log.d(TAG, "onActivityResult Cancelled");
                    Toast.makeText(this, "Cancelled Login", Toast.LENGTH_SHORT).show();
                }
                else{
                    Log.d(TAG, "onActivityResult Error");
                    Toast.makeText(this, ""+response.getError(), Toast.LENGTH_SHORT).show();

                }
            }
        }
    }



    private void updateUI() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        this.finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(FirebaseAuth.getInstance().getCurrentUser() != null){
            updateUI();
        }
    }

    /*Check for Internet*/
    private boolean isConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

}