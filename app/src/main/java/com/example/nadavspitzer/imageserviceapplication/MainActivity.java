package com.example.nadavspitzer.imageserviceapplication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    /*******
     * the function creates the app
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Intent intent = new Intent(this, ImageService.class);
        stopService(intent);
    }

    /******
     * the function starts the service
     * @param view the service view
     */
    public void startService(View view) {
        Intent intent = new Intent(this, ImageService.class);
        startService(intent);
    }

    /*******
     * the function stops the service
     * @param view the service view
     */
    public void stopService(View view) {
        Intent intent = new Intent(this, ImageService.class);
        stopService(intent);
    }
}
