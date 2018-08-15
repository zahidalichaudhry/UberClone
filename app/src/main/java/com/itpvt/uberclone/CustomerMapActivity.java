package com.itpvt.uberclone;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomerMapActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private GoogleMap mMap;

    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    LocationRequest mLocationRequest;
    private RadioGroup mRadioGroup;
    private Button mLogout,mRequest,mSettings;

    private Marker pickupMarker;
    private Boolean requestbol=false;
    private LatLng pickupLocation;
    private String destination,requestServics;

    private ImageView mdriverProfileImage;

    private LinearLayout mDriverInfo;
    private TextView mdriverName, mdriverPhone, mdriverCar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mDriverInfo = (LinearLayout) findViewById(R.id.driverInfo);
        mdriverProfileImage = (ImageView) findViewById(R.id.driverProfileImage);
        mdriverName = (TextView) findViewById(R.id.driverName);
        mdriverPhone = (TextView) findViewById(R.id.driverPhone);
        mdriverCar = (TextView) findViewById(R.id.driverCar);

        mLogout = (Button) findViewById(R.id.logout);
        mRequest = (Button) findViewById(R.id.request);
        mSettings = (Button) findViewById(R.id.settings);

        mRadioGroup=(RadioGroup)findViewById(R.id.radioGroup);
        mRadioGroup.check(R.id.UberX);

        mSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(CustomerMapActivity.this,CutomerSettingActivity.class);
                startActivity(intent);
                return;
            }
        });
        mRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                if (requestbol)
                {
                    requestbol=false;
                    geoQuery.removeAllListeners();
                    driverLoactionRef.removeEventListener(driverLoactionRefListener);
                    if (driverFoundID!=null)
                    {
                        DatabaseReference driverRef=FirebaseDatabase.getInstance().getReference().child("Users")
                                .child("Drivers").child(driverFoundID).child("customerRequest");
                        driverRef.removeValue();
                        driverFoundID=null;
                    }
                    driverFound=false;
                    radius=1;
                    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("customerRequest");
                    GeoFire geoFire = new GeoFire(ref);
                    geoFire.removeLocation(userId);
                    if (pickupMarker!=null)
                    {
                        pickupMarker.remove();
                    }
                    mRequest.setText("Call Uber");
                    mDriverInfo.setVisibility(View.GONE);
                    mdriverName.setText("");
                    mdriverPhone.setText("");
                    mdriverProfileImage.setImageResource(R.drawable.user);
                    mdriverCar.setText("");
                }else
                    {
                        int selectId=mRadioGroup.getCheckedRadioButtonId();
                        final RadioButton radioButton=(RadioButton)findViewById(selectId);
                        if (radioButton.getText()==null)
                        {
                            return;
                        }
                        requestServics=radioButton.getText().toString();


                        requestbol=true;
                        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("customerRequest");
                        GeoFire geoFire = new GeoFire(ref);
                        geoFire.setLocation(userId, new GeoLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
                        pickupLocation = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                        pickupMarker = mMap.addMarker(new MarkerOptions().position(pickupLocation).title("Pickup Here").icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_pickup)));
                        mRequest.setText("Getting your Driver....");



                        getClosestDriver();
                    }


            }
        });
        mLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(CustomerMapActivity   .this, MainActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });
        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                destination=place.getName().toString();
//                Log.i(TAG, "Place: " + place.getName());
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
//                Log.i(TAG, "An error occurred: " + status);
            }
        });
    }
    private int radius = 1;
    private Boolean driverFound = false;
    private String driverFoundID;
    GeoQuery geoQuery;
    private void getClosestDriver()
    {

        DatabaseReference driverLocation = FirebaseDatabase.getInstance().getReference().child("driversAvailable");
        GeoFire geoFire = new GeoFire(driverLocation);
        geoQuery = geoFire.queryAtLocation(new GeoLocation(pickupLocation.latitude, pickupLocation.longitude), radius);
        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location)
            {
                if (!driverFound&&requestbol) {
                    DatabaseReference mCustomerDatabse=FirebaseDatabase.getInstance().getReference().child("Users")
                            .child("Drivers").child(key);
                    mCustomerDatabse.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot)
                        {
                            if (dataSnapshot.exists()&&dataSnapshot.getChildrenCount()>0)
                            {
                                Map<String ,Object> drivermap=(Map<String,Object>)
                                        dataSnapshot.getValue();
                                if (driverFound)
                                {
                                    return;
                                }
                                if (drivermap.get("Service").equals(requestServics))
                                {
                                    driverFound = true;
                                    driverFoundID=dataSnapshot.getKey();
                                    DatabaseReference driverRef=FirebaseDatabase.getInstance().getReference().child("Users")
                                            .child("Drivers").child(driverFoundID).child("customerRequest");
                                    String customerId=FirebaseAuth.getInstance().getCurrentUser().getUid();
                                    HashMap map=new HashMap();
                                    map.put("customerRideId",customerId);
                                    map.put("destination",destination);
                                    driverRef.updateChildren(map);
                                    mRequest.setText("Loading For Driver Location..........");
                                    getDriverLocation();
                                    getDriverInfo();

                                }
                            }


                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });



                }

            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady()
            {
                if (!driverFound)
                {
                    radius++;
                    getClosestDriver();
                }

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    private void getDriverInfo()
    {
        mDriverInfo.setVisibility(View.VISIBLE);
        DatabaseReference mCustomerDatabase=FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers")
                .child(driverFoundID);
        mCustomerDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()&&dataSnapshot.getChildrenCount()>0)
                {
                    Map<String ,Object> map=(Map<String,Object>)
                            dataSnapshot.getValue();
                    if (map.get("name")!=null)
                    {
                        mdriverName.setText(map.get("name").toString());
                    }
                    if (map.get("phone")!=null)
                    {
                        mdriverPhone.setText(map.get("phone").toString());
                    }
                    if (map.get("profileImageUrl")!=null)
                    {
                        Glide.with(getApplication()).load(map.get("profileImageUrl").toString()).into(mdriverProfileImage);
                    }


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private Marker mDriverMarker;
    private DatabaseReference driverLoactionRef;
    private ValueEventListener driverLoactionRefListener;
    private void getDriverLocation()
    {
         driverLoactionRef=FirebaseDatabase.getInstance().getReference().child("driversWorking")
                .child(driverFoundID).child("l");
        driverLoactionRefListener=   driverLoactionRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists())
                {
                    List<Object> map=(List<Object>)dataSnapshot.getValue();
                    double locationlat=0;
                    double locationlng=0;
                    mRequest.setText("Driver Found");
                    if (map.get(0)!=null)
                    {
                        locationlat=Double.parseDouble(map.get(0).toString());
                    }
                    if (map.get(1)!=null)
                    {
                        locationlng=Double.parseDouble(map.get(1).toString());
                    }
                    LatLng driverLatLng=new LatLng(locationlat,locationlng);
                    if (mDriverMarker!=null)
                    {
                        mDriverMarker.remove();
                    }
                    Location loc1=new Location("");
                    loc1.setLatitude(pickupLocation.latitude);
                    loc1.setLongitude(pickupLocation.longitude);

                    Location loc2=new Location("");
                    loc2.setLatitude(driverLatLng.latitude);
                    loc2.setLongitude(driverLatLng.longitude);

                    float distance = loc1.distanceTo(loc2);

                    if (distance<100)
                    {
                        mRequest.setText("Driver is Here");
                    }else
                        {
                            mRequest.setText("Driver Found"+String.valueOf(distance));
                        }

                    mDriverMarker=mMap.addMarker(new MarkerOptions().position(driverLatLng).title("Your Driver")
                            .icon(BitmapDescriptorFactory.fromResource(R.mipmap.caricon)));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

//        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        buildGoogleApiClient();
        mMap.setMyLocationEnabled(true);
    }
    protected synchronized void buildGoogleApiClient(){
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        ;
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Override
    public void onLocationChanged(Location location)
    {
        mLastLocation = location;
        LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(11));
//
//        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
//        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("driversAvailable");
//        GeoFire geoFire=new GeoFire(ref);
//        geoFire.setLocation(userId,new GeoLocation(location.getLatitude(),location.getLongitude()));

    }

    @Override
    protected void onStop() {
        super.onStop();
//        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
//        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("driversAvailable");
//        GeoFire geoFire=new GeoFire(ref);
//        geoFire.removeLocation(userId);

    }
}
