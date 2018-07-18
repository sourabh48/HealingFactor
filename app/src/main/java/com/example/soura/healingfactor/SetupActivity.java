package com.example.soura.healingfactor;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private EditText NameField;
    private DatabaseReference mDatabaseUsers;
    private CircleImageView ProfilePicture;
    String profilePicUrl=null;
    String type,name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        mAuth = FirebaseAuth.getInstance();
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseUsers.keepSynced(true);

        ProfilePicture = (CircleImageView)findViewById(R.id.profile_image);
        NameField = (EditText) findViewById(R.id.name);

        facebookimage();
    }


    private void facebookimage()
    {
        Bundle params = new Bundle();
        params.putString("fields", "id,email,gender,cover,picture.type(normal)");
        new GraphRequest(AccessToken.getCurrentAccessToken(), "me", params, HttpMethod.GET,
                new GraphRequest.Callback() {
                    @Override
                    public void onCompleted(GraphResponse response) {
                        if (response != null) {
                            try {
                                JSONObject data = response.getJSONObject();
                                if (data.has("picture")) {
                                    profilePicUrl = data.getJSONObject("picture").getJSONObject("data").getString("url");
                                    Picasso.get().load(profilePicUrl).placeholder(R.drawable.profile).into(ProfilePicture);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }).executeAsync();
    }


    private void logout() {
        mAuth.signOut();
        LoginManager.getInstance().logOut();
        updateUI();
    }

    private void updateUI() {
        Intent account = new Intent(SetupActivity.this, LoginActivity.class);
        startActivity(account);
        finish();
    }

    public void later(View view) {
        logout();
    }

    public void setup(View view) {
        name = NameField.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            NameField.setError("Required");
        } else {
            String device_Token = FirebaseInstanceId.getInstance().getToken();
            String name = NameField.getText().toString().trim();
            String userid = mAuth.getCurrentUser().getUid();

            mDatabaseUsers.child(userid).child("device_token").setValue(device_Token);
            mDatabaseUsers.child(userid).child("name").setValue(name);
            mDatabaseUsers.child(userid).child("status").setValue("Hi there! I am new in Healing Factor..");
            mDatabaseUsers.child(userid).child("image").setValue(profilePicUrl);
            mDatabaseUsers.child(userid).child("thumb_image").setValue(profilePicUrl);

            Intent account = new Intent(SetupActivity.this, AccountActivity.class);
            startActivity(account);
            finish();
        }
    }


    @Override
    public void onBackPressed()
    {
        logout();
        super.onBackPressed();
    }
}