package com.example.soura.healingfactor;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

import static com.example.soura.healingfactor.R.layout.activity_settings;

public class SettingsActivity extends AppCompatActivity {


    private DatabaseReference mUserDatabase,mUserRef;
    private FirebaseUser mCurrentUser;

    private FirebaseAuth mAuth;

    private CircleImageView mDisplayImage;
    private TextView mDisplayName,mDisplayStatus;

    private StorageReference mImageStorage;

    private String current_uid;

    private ProgressDialog mProgressDialog;
    private static final int GALLERY_PICK = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        current_uid = mCurrentUser.getUid();
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);
        mUserDatabase.keepSynced(true);

        mImageStorage = FirebaseStorage.getInstance().getReference();

        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null)
        {
            mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
        }


        mProgressDialog = new ProgressDialog(SettingsActivity.this);
        mProgressDialog.setTitle("Uploading");
        mProgressDialog.setMessage("Please Wait While We Upload and Process.");
        mProgressDialog.setCanceledOnTouchOutside(false);

        mDisplayImage = (CircleImageView) findViewById(R.id.profile_image);
        mDisplayName = (TextView) findViewById(R.id.setting_name);
        mDisplayStatus=(TextView)findViewById(R.id.setting_status);

        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                final String image = dataSnapshot.child("image").getValue().toString();

                mDisplayName.setText("Hi! " + name);
                mDisplayStatus.setText(status);

                if (!image.equals("default")) {
                    Picasso.get().load(image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.profile).into(mDisplayImage, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError(Exception e) {
                            Picasso.get().load(image).placeholder(R.drawable.profile).into(mDisplayImage);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void Change(View view) {

        Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(galleryIntent, GALLERY_PICK);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();

            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mProgressDialog.show();
                Uri resultUri = result.getUri();

                final File thumb_filePath = new File(resultUri.getPath());

                String uid = mCurrentUser.getUid();


                Bitmap thumb_bitmap = null;

                try {
                    thumb_bitmap = new Compressor(this)
                            .setQuality(75)
                            .setMaxHeight(300)
                            .setMaxWidth(300)
                            .compressToBitmap(thumb_filePath);
                } catch (IOException e) {
                    e.printStackTrace();
                }


                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                final byte[] thumb_byte = baos.toByteArray();

                final StorageReference filepath = mImageStorage.child(uid).child("profile_images").child(uid + ".jpg");
                final StorageReference thumb_filepath = mImageStorage.child(uid).child("thumbs").child(uid + ".jpg");


                filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                final Uri fileUrl = uri;

                                final String downloadFileUrl = fileUrl.toString();

                                UploadTask uploadTask = thumb_filepath.putBytes(thumb_byte);

                                uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onComplete(final Task<UploadTask.TaskSnapshot> thumb_task) {
                                        thumb_filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {
                                                Uri thumbUrl = uri;

                                                final String downloadThumbUrl = thumbUrl.toString();

                                                if (thumb_task.isSuccessful()) {
                                                    Map update_hashMap = new HashMap();
                                                    update_hashMap.put("image", downloadFileUrl);
                                                    update_hashMap.put("thumb_image", downloadThumbUrl);

                                                    mUserDatabase.updateChildren(update_hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {
                                                                mProgressDialog.dismiss();
                                                            }
                                                        }
                                                    });
                                                } else {
                                                    Toast.makeText(SettingsActivity.this, "Failed to Upload thumbs", Toast.LENGTH_SHORT).show();
                                                    mProgressDialog.dismiss();
                                                }
                                            }
                                        });
                                    }
                                });
                            }
                        });
                    }

                });

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    public void ChangeStatus(View view)
    {
        String status_value= mDisplayStatus.getText().toString();
        Intent status = new Intent(SettingsActivity .this,StatusActivity.class);
        status.putExtra("status",status_value);
        startActivity(status);
    }
}