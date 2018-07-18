package com.example.soura.healingfactor;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import de.hdodenhof.circleimageview.CircleImageView;


public class AccountActivity extends AppCompatActivity
{
    private FusedLocationProviderClient mFusedLocationClient;
    Double longitude,latitude;

    private FirebaseAuth mAuth;
    private DatabaseReference databaseUser,locationDatabase,mUserRef;
    private String mCurrentUserId;

    private CircleImageView ProfileImage;
    private TextView nName;

    private Toolbar toolbar;
    private ViewPager mViewPager;
    private SectionPagerAdapter mSectionPager;

    private TabLayout mTabLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        mAuth = FirebaseAuth.getInstance();
        toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        mViewPager=(ViewPager)findViewById(R.id.tab_Pager);
        mSectionPager=new SectionPagerAdapter(getSupportFragmentManager());

        mTabLayout=(TabLayout)findViewById(R.id.main_tabs);
        mTabLayout.setupWithViewPager(mViewPager);
        mViewPager.setAdapter(mSectionPager);
        setSupportActionBar(toolbar);

        if (mAuth.getCurrentUser() != null)
        {
            mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
            mUserRef.child("online").setValue("true");
        }

        databaseUser= FirebaseDatabase.getInstance().getReference().child("Users");
        databaseUser.keepSynced(true);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();

        locationDatabase= FirebaseDatabase.getInstance().getReference().child("Location").child(mCurrentUserId);
        locationDatabase.keepSynced(true);

        ProfileImage=(CircleImageView)findViewById(R.id.custom_bar_image);
        nName=(TextView)findViewById(R.id.custom_bar_title);

        LocationRequest request = new LocationRequest();
        request.setInterval(10000);
        request.setFastestInterval(5000);
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(AccountActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(AccountActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED)
        {
            mFusedLocationClient.requestLocationUpdates(request, new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    Location location = locationResult.getLastLocation();
                    if (location != null) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();

                        Map locationMap = new HashMap();

                        locationMap.put("Latitude",latitude);
                        locationMap.put("Longitude",longitude);

                        locationDatabase.updateChildren(locationMap);

                    }
                }
            }, null);
        }else{
            ActivityCompat.requestPermissions(AccountActivity.this, new String[]
                    {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION }, 1);
        }


        databaseUser.child(mCurrentUserId).addValueEventListener(new ValueEventListener() {

            @Override

            public void onDataChange(DataSnapshot dataSnapshot)

            {
                String name = dataSnapshot.child("name").getValue().toString();

                nName.setText(name);

                final String image = dataSnapshot.child("image").getValue().toString();

                Picasso.get().load(image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.profile).into(ProfileImage, new Callback() {

                    @Override
                    public void onSuccess()
                    {}
                    @Override
                    public void onError(Exception e)
                    {
                        Picasso.get().load(image).placeholder(R.drawable.profile).into(ProfileImage);
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    private void updateUI()
    {
        Intent account = new Intent(AccountActivity .this,LoginActivity.class);
        startActivity(account);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout)
        {
            logout();
            Toast.makeText(getApplicationContext(), "Logged out!", Toast.LENGTH_LONG).show();
        }
        if (id == R.id.account_Settings)
        {
            Intent settings=new Intent(AccountActivity.this,SettingsActivity.class);
            startActivity(settings);
        }
        if (id == R.id.all_users)
        {
            Intent users=new Intent(AccountActivity.this,UserActivity.class);
            startActivity(users);
        }
        return super.onOptionsItemSelected(item);
    }

    private void logout()
    {
        mUserRef.child("online").setValue(ServerValue.TIMESTAMP);
        mAuth.signOut();
        disconnectFromFacebook();
        updateUI();
    }


    public void disconnectFromFacebook() {

        if (AccessToken.getCurrentAccessToken() == null) {
            return; // already logged out
        }

        new GraphRequest(AccessToken.getCurrentAccessToken(), "/me/permissions/", null, HttpMethod.DELETE, new GraphRequest
                .Callback() {
            @Override
            public void onCompleted(GraphResponse graphResponse) {

                LoginManager.getInstance().logOut();

            }
        }).executeAsync();
    }

    private static final int TIME_INTERVAL = 1000;
    private long mBackPressed;

    @Override
    public void onBackPressed()
    {
        if (mBackPressed + TIME_INTERVAL > System.currentTimeMillis())
        {
            super.onBackPressed();
            return;
        }
        else { Toast.makeText(getBaseContext(), "Tap back button in order to exit", Toast.LENGTH_SHORT).show(); }

        mBackPressed = System.currentTimeMillis();
    }

    @Override

    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser == null)
        {
            updateUI();
        }
        else
        {
            mUserRef.child("online").setValue("true");
        }
    }
}
