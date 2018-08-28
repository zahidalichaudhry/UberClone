package com.itpvt.uberclone;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    private Button mDriver,mCustomer;
    private static final int LOCATION_PERMISION_REQUEST_CODE = 1234;
    private static final String FINE_LOCATION = android.Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final String STORAGE_READ= Manifest.permission.READ_EXTERNAL_STORAGE;
    private static final String STORAGE_WRITE= Manifest.permission.WRITE_EXTERNAL_STORAGE;
    private Boolean mLoactionPermissionGranted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mDriver=(Button)findViewById(R.id.driver);
        mCustomer=(Button)findViewById(R.id.customer);
        startService(new Intent(MainActivity.this,onAppKilled.class));
        getLocationPermission();
        mDriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MainActivity.this,DriverLoginActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });
        mCustomer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MainActivity.this,CustomerLoginActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });
    }
    private void getLocationPermission()
    {
//        Log.d(TAG,"getting location permissions");
        String[] permission={Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION)== PackageManager.PERMISSION_GRANTED)
        {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COURSE_LOCATION)==PackageManager.PERMISSION_GRANTED)
            {
                mLoactionPermissionGranted=true;
//                initmap();

            }else
            {
                ActivityCompat.requestPermissions(this,permission,
                        LOCATION_PERMISION_REQUEST_CODE);
            }
        }else
        {
            ActivityCompat.requestPermissions(this,permission,
                    LOCATION_PERMISION_REQUEST_CODE);
        }
    }

    private void initmap()
    {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        Log.d(TAG,"permission called");
        mLoactionPermissionGranted=false;
        switch (requestCode)
        {
            case LOCATION_PERMISION_REQUEST_CODE:
            {
                if (grantResults.length>0)

                {
                    for (int i=0;i<grantResults.length;i++)
                    {
                        if (grantResults[i]!=PackageManager.PERMISSION_GRANTED)
                        {
//                            Log.d(TAG,"permission failed");
                            mLoactionPermissionGranted=false;
                        }
                    }
//                    Log.d(TAG,"permission granted");
                    mLoactionPermissionGranted=true;
                    initmap();
                }
            }
        }
    }
}
