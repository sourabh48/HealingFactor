package com.example.soura.healingfactor;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.support.annotation.NonNull;
import android.Manifest;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.SphericalUtil;
import com.google.maps.android.ui.IconGenerator;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,RoutingListener{

    private GoogleMap mMap;
    DatabaseReference userMarkerdata,UserDatabase;
    String mChatUser,name,image;
    Double latitude, longitude;
    FusedLocationProviderClient mFusedLocationClient;
    Double endlongitude,endlatitude;
    Marker mCurrLocationMarker;
    private List<Polyline> polylines;
    private static final int[] COLORS = new int[]{R.color.colorPrimaryDark,R.color.colorPrimary,R.color.activated_text,R.color.colorAccent,R.color.primary_dark_material_light};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        mChatUser = getIntent().getStringExtra("user_id");

        polylines = new ArrayList<>();

        userMarkerdata = FirebaseDatabase.getInstance().getReference().child("Location").child(mChatUser);
        UserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(mChatUser);
        UserDatabase.keepSynced(true);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        userMarkerdata.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                mMap.clear();

                latitude = (Double) dataSnapshot.child("Latitude").getValue();
                longitude = (Double) dataSnapshot.child("Longitude").getValue();

                final LatLng userLocation = new LatLng(latitude, longitude);

                UserDatabase.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot1)
                    {
                        name = dataSnapshot1.child("name").getValue().toString();
                        image = dataSnapshot1.child("thumb_image").getValue().toString();

                        View marker = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.view_custom_marker, null);
                        final ImageView ProfileImage = (ImageView) marker.findViewById(R.id.pic);

                        Picasso.get().load(image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.profile).into(ProfileImage);

                    final IconGenerator mIconGenerator = new IconGenerator(getApplicationContext());
                    mIconGenerator.setContentView(marker);
                    Bitmap icon = mIconGenerator.makeIcon();

                    mCurrLocationMarker = mMap.addMarker(new MarkerOptions()
                    .position(userLocation)
                    .title(name)
                    .icon(BitmapDescriptorFactory.fromBitmap(icon)));


                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(userLocation, 19);
                    mMap.animateCamera(cameraUpdate);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                addtravelroutes(userLocation);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError){}
        });

        if (ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            mMap.setMyLocationEnabled(true);
        }
    }

    private void addtravelroutes(final LatLng userLocation)
    {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(MapsActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapsActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED)
        {
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null)
                            {
                                endlatitude = location.getLatitude();
                                endlongitude = location.getLongitude();

                                final LatLng nowLocation = new LatLng(endlatitude, endlongitude);

                                Routing routing = new Routing.Builder()
                                        .travelMode(AbstractRouting.TravelMode.DRIVING)
                                        .withListener((RoutingListener) MapsActivity.this)
                                        .alternativeRoutes(false)
                                        .waypoints(nowLocation, userLocation)
                                        .build();
                                routing.execute();

                            }
                        }
                    });
        }else{
            ActivityCompat.requestPermissions(MapsActivity.this, new String[]
                    {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION }, 1);
        }
    }

    @Override
    public void onRoutingFailure(RouteException e) {

    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> arrayList, int shortestRouteIndex)
    {
        if(polylines.size()>0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }

        polylines = new ArrayList<>();


        for (int i = 0; i <arrayList.size(); i++) {
            int colorIndex = i % COLORS.length;

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(COLORS[colorIndex]));
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(arrayList.get(i).getPoints());

            arrayList.get(i).getDistanceValue();
            arrayList.get(i).getDurationValue();

            Polyline polyline = mMap.addPolyline(polyOptions);
            polylines.add(polyline);
        }
    }

    @Override
    public void onRoutingCancelled() {}
}
