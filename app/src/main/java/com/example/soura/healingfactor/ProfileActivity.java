package com.example.soura.healingfactor;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity
{

    private FusedLocationProviderClient mFusedLocationClient;
    String longitude,latitude;

    private ImageView mProfileImage;
    private TextView mDisplayName;
    private TextView mDisplayStatus;
    private TextView mDisplayFriendsCount;

    private FirebaseAuth mAuth;
    private DatabaseReference mUserRef,locationDatabase;

    private Button mRequestSendButton;
    private Button mRequestDecineButton;

    private DatabaseReference mUserDatabase,mKeyDatabase;
    private DatabaseReference mFriendRequestDatabase;
    private DatabaseReference mFriendDatabase;
    private DatabaseReference mNotificationDatabase;
    private DatabaseReference mRoot;

    private FirebaseUser mCurrent_User;
    private ProgressDialog mProgress;

    private String mCurrent_State ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mProgress =new ProgressDialog(this);
        mProgress.setTitle("Loading User");
        mProgress.setMessage("Please Wait");
        mProgress.setCanceledOnTouchOutside(false);
        mProgress.show();

       final String user_id = getIntent().getStringExtra("user_id");

        mProfileImage = (ImageView)findViewById(R.id.profile_image);
        mDisplayName=(TextView)findViewById(R.id.profile_name);
        mDisplayStatus=(TextView)findViewById(R.id.profile_status);
        mDisplayFriendsCount=(TextView)findViewById(R.id.profile_friends_count);
        mRequestDecineButton=(Button)findViewById(R.id.profile_request_decline);
        mRequestSendButton=(Button)findViewById(R.id.profile_request_send);

        mCurrent_State = "not_friends";

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        mFriendRequestDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_requests");
        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        mNotificationDatabase = FirebaseDatabase.getInstance().getReference().child("Notification");
        mKeyDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mFriendRequestDatabase.keepSynced(true);
        mUserDatabase.keepSynced(true);
        mFriendDatabase.keepSynced(true);
        mKeyDatabase.keepSynced(true);

        mAuth = FirebaseAuth.getInstance();
        mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());

        mRequestDecineButton.setVisibility(View.INVISIBLE);
        mRequestDecineButton.setEnabled(false);

        mCurrent_User = FirebaseAuth.getInstance().getCurrentUser();

        mRoot = FirebaseDatabase.getInstance().getReference();

        mUserDatabase.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {

                String display_name = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                final String image = dataSnapshot.child("image").getValue().toString();

                mDisplayName.setText(display_name);
                mDisplayStatus.setText(status);

                Picasso.get().load(image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.profile).into(mProfileImage, new Callback() {
                    @Override
                    public void onSuccess()
                    {

                    }

                    @Override
                    public void onError(Exception e)
                    {
                        Picasso.get().load(image).placeholder(R.drawable.profile).into(mProfileImage);
                    }
                });
                if(mCurrent_User.getUid().equals(user_id))
                {
                    mRequestDecineButton.setEnabled(false);
                    mRequestDecineButton.setVisibility(View.INVISIBLE);

                    mRequestSendButton.setEnabled(false);
                    mRequestSendButton.setVisibility(View.INVISIBLE);
                }

                //--------------- FRIENDS LIST / REQUEST FEATURE -----

                mFriendRequestDatabase.child(mCurrent_User.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if(dataSnapshot.hasChild(user_id)){

                            String req_type = dataSnapshot.child(user_id).child("request_type").getValue().toString();

                            if(req_type.equals("received"))
                            {
                                mCurrent_State = "request_received";
                                mRequestSendButton.setText(R.string.accept_friend_request);

                                mRequestDecineButton.setVisibility(View.VISIBLE);
                                mRequestDecineButton.setEnabled(true);
                            }
                            else if(req_type.equals("sent"))
                            {
                                mCurrent_State = "request_sent";
                                mRequestSendButton.setText(R.string.cancel_friend_request);

                                mRequestDecineButton.setVisibility(View.INVISIBLE);
                                mRequestDecineButton.setEnabled(false);
                            }
                            mProgress.dismiss();
                        }
                        else
                        {
                            mFriendDatabase.child(mCurrent_User.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    if(dataSnapshot.hasChild(user_id)){

                                        mCurrent_State = "friends";
                                        mRequestSendButton.setText(R.string.unfriend);

                                        mRequestDecineButton.setVisibility(View.INVISIBLE);
                                        mRequestDecineButton.setEnabled(false);
                                    }
                                    mProgress.dismiss();
                                }
                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                    mProgress.dismiss();
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

       //--------DECLINE--------//
        mRequestDecineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mCurrent_State.equals("request_received")){

                    Map declineMap = new HashMap();
                    declineMap.put("Friend_requests/" + mCurrent_User.getUid() + "/" + user_id, null);
                    declineMap.put("Friend_requests/" + user_id + "/" + mCurrent_User.getUid(), null);

                    mRoot.updateChildren(declineMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {


                            if(databaseError == null)
                            {
                                mRequestSendButton.setEnabled(true);
                                mCurrent_State = "not_friends";
                                mRequestSendButton.setText(R.string.send_friend_request);

                                mRequestDecineButton.setVisibility(View.INVISIBLE);
                                mRequestDecineButton.setEnabled(false);
                            }
                            else
                            {
                                String error = databaseError.getMessage();
                                Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT).show();
                            }

                            mRequestSendButton.setEnabled(true);

                        }
                    });

                }
            }
        });

        mRequestSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mRequestSendButton.setEnabled(false);

                // --------------- NOT FRIENDS STATE ------------

                if(mCurrent_State.equals("not_friends")){


                    DatabaseReference newNotificationref = mRoot.child("Notification").child(user_id).push();
                    String newNotificationId = newNotificationref.getKey();

                    HashMap<String, String> notificationData = new HashMap<>();
                    notificationData.put("from", mCurrent_User.getUid());
                    notificationData.put("type", "request");

                    Map requestMap = new HashMap();

                    final String currentDate = DateFormat.getDateTimeInstance().format(new Date());

                    requestMap.put("Friend_requests/" + mCurrent_User.getUid() + "/" + user_id + "/request_type", "sent");
                    requestMap.put("Friend_requests/" + mCurrent_User.getUid() + "/" + user_id + "/time", currentDate);

                    requestMap.put("Friend_requests/" + user_id + "/" + mCurrent_User.getUid() + "/request_type", "received");
                    requestMap.put("Friend_requests/" + mCurrent_User.getUid() + "/" + user_id + "/time", currentDate);

                    requestMap.put("Notification/" + user_id + "/" + newNotificationId, notificationData);

                    mRoot.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if(databaseError != null){

                                Toast.makeText(ProfileActivity.this, "There was some error in sending request", Toast.LENGTH_SHORT).show();

                            } else {

                                mCurrent_State = "request_sent";
                                mRequestSendButton.setText(R.string.cancel_friend_request);

                            }

                            mRequestSendButton.setEnabled(true);
                        }
                    });

                }


                // - -------------- CANCEL REQUEST STATE ------------

                if(mCurrent_State.equals("request_sent")){

                    Map cancelMap = new HashMap();
                    cancelMap.put("Friend_requests/" + mCurrent_User.getUid() + "/" + user_id, null);
                    cancelMap.put("Friend_requests/" + user_id + "/" + mCurrent_User.getUid(), null);

                    mRoot.updateChildren(cancelMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {


                            if(databaseError == null)
                            {
                                mRequestSendButton.setEnabled(true);
                                mCurrent_State = "not_friends";
                                mRequestSendButton.setText(R.string.send_friend_request);

                                mRequestDecineButton.setVisibility(View.INVISIBLE);
                                mRequestDecineButton.setEnabled(false);
                            }

                            mRequestSendButton.setEnabled(true);

                        }
                    });

                }

                // ------------ REQ RECEIVED STATE ----------

                if(mCurrent_State.equals("request_received")) {

                    mKeyDatabase.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            String username = dataSnapshot.child(mCurrent_User.getUid()).child("name").getValue().toString();
                            String friendname = dataSnapshot.child(user_id).child("name").getValue().toString();

                            final String currentDate = DateFormat.getDateTimeInstance().format(new Date());

                            Map friendsMap = new HashMap();

                            friendsMap.put("Friends/" + mCurrent_User.getUid() + "/" + user_id + "/date", currentDate);
                            friendsMap.put("Friends/" + mCurrent_User.getUid() + "/" + user_id + "/name", friendname);

                            friendsMap.put("Friends/" + user_id + "/" + mCurrent_User.getUid() + "/date", currentDate);
                            friendsMap.put("Friends/" + user_id + "/" + mCurrent_User.getUid() + "/name", username);


                            friendsMap.put("Friend_requests/" + mCurrent_User.getUid() + "/" + user_id, null);
                            friendsMap.put("Friend_requests/" + user_id + "/" + mCurrent_User.getUid(), null);


                            mRoot.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {


                                    if (databaseError == null) {

                                        mRequestSendButton.setEnabled(true);
                                        mCurrent_State = "friends";
                                        mRequestSendButton.setText(R.string.unfriend);

                                        mRequestDecineButton.setVisibility(View.INVISIBLE);
                                        mRequestDecineButton.setEnabled(false);

                                    } else {
                                        String error = databaseError.getMessage();

                                        Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }

                // ------------ UNFRIENDS ---------

                if(mCurrent_State.equals("friends")){

                    Map unfriendMap = new HashMap();
                    unfriendMap.put("Friends/" + mCurrent_User.getUid() + "/" + user_id, null);
                    unfriendMap.put("Friends/" + user_id + "/" + mCurrent_User.getUid(), null);

                    unfriendMap.put("Notification/" + mCurrent_User.getUid() , null);
                    unfriendMap.put("Notification/" + user_id , null);

                    mRoot.updateChildren(unfriendMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {


                            if(databaseError == null)
                            {
                                mRequestSendButton.setEnabled(true);
                                mCurrent_State = "not_friends";
                                mRequestSendButton.setText(R.string.send_friend_request);

                                mRequestDecineButton.setVisibility(View.INVISIBLE);
                                mRequestDecineButton.setEnabled(false);
                            }
                            else
                            {
                                String error = databaseError.getMessage();
                                Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT).show();
                            }

                            mRequestSendButton.setEnabled(true);
                        }
                    });
                }
            }
        });


        locationDatabase= FirebaseDatabase.getInstance().getReference().child("Location").child(mAuth.getCurrentUser().getUid());
        locationDatabase.keepSynced(true);

        LocationRequest request = new LocationRequest();
        request.setInterval(10000);
        request.setFastestInterval(5000);
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(ProfileActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(ProfileActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED)
        {
            mFusedLocationClient.requestLocationUpdates(request, new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    Location location = locationResult.getLastLocation();
                    if (location != null) {
                        latitude = Double.toString(location.getLatitude());
                        longitude = Double.toString(location.getLongitude());

                        Map locationMap = new HashMap();

                        locationMap.put("Latitude",latitude);
                        locationMap.put("Longitude",longitude);

                        locationDatabase.updateChildren(locationMap);

                    }
                }
            }, null);
        }else{
            ActivityCompat.requestPermissions(ProfileActivity.this, new String[]
                    {android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION }, 1);
        }
    }
}
