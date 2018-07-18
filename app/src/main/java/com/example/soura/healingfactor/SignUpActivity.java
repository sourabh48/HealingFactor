package com.example.soura.healingfactor;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class SignUpActivity extends AppCompatActivity {

    private EditText Name;
    private EditText Password;
    private EditText Email;

    private DatabaseReference mDatabase;

    String name,password,email;

    private Button ResisterButton;

    private FirebaseAuth mAuth;
    private ProgressDialog mProgress;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        Email=(EditText)findViewById(R.id.username);
        Password=(EditText)findViewById(R.id.password);
        Name=(EditText)findViewById(R.id.name);

        mAuth=FirebaseAuth.getInstance();
        mDatabase= FirebaseDatabase.getInstance().getReference().child("Users");

        mProgress =new ProgressDialog(this);

        ResisterButton=(Button)findViewById(R.id.sign_up);

        ResisterButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                startResister();
            }
        });
    }

    public void gologin(View view)
    {
        Intent account = new Intent(SignUpActivity.this,LoginActivity.class);
        startActivity(account);
        finish();
    }

    public void startResister()
    {
        name=Name.getText().toString().trim();
        email=Email.getText().toString().trim();
        password=Password.getText().toString().trim();

        if(TextUtils.isEmpty(name) && TextUtils.isEmpty(email)&& TextUtils.isEmpty(password))
        {
            Name.setError("Required");
            Email.setError("Required");
            Password.setError("Required");
        }
        else if(TextUtils.isEmpty(email))
        {
            Email.setError("Required");
        }
        else if(TextUtils.isEmpty(password))
        {
            Password.setError("Required");
        }
        else
        {
            String titleId="Signing in...";
            mProgress.setTitle(titleId);
            mProgress.setMessage("Please Wait...");
            mProgress.show();
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>()
            {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task)
                {
                    if (task.isSuccessful())
                    {
                        String device_Token = FirebaseInstanceId.getInstance().getToken();
                        String user_id=mAuth.getCurrentUser().getUid();
                        DatabaseReference current_user= mDatabase.child(user_id);
                        current_user.child("device_token").setValue(device_Token);
                        current_user.child("name").setValue(name);
                        current_user.child("status").setValue("Hi there! I am new in Healing Factor..");
                        current_user.child("image").setValue("default");
                        current_user.child("thumb_image").setValue("default");



                        mProgress.dismiss();
                        Intent account = new Intent(SignUpActivity.this,AccountActivity.class);
                        account.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(account);
                        finish();

                    }
                    else
                    {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(SignUpActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}
