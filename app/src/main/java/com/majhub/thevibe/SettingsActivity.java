package com.majhub.thevibe;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;


import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private Button UpdateAccoutSettings;
    private EditText userName, userStatus;
    private CircleImageView userProfileImage;
    private String currentUserID;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;
    private static final int GalleryPick = 1;
    private StorageReference userProfileImageRef;
    private ProgressDialog loadingBar;
    Uri ImageUri;
    private static final int REQUEST_WRITE_PERMISSION = 786;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();
        userProfileImageRef= FirebaseStorage.getInstance().getReference().child("Profile Images");

        InitializeFields();




        UpdateAccoutSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateSettings();
            }
        });

        RetrieveUserInfo();

        userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_PERMISSION);
                } else {
                    CropImage.startPickImageActivity(SettingsActivity.this);
                }

            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_WRITE_PERMISSION && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            CropImage.startPickImageActivity(SettingsActivity.this);
        }
    }


    private void InitializeFields() {
        UpdateAccoutSettings=(Button)findViewById(R.id.update_settings_button);
        userName=(EditText) findViewById(R.id.set_user_name);
        userStatus=(EditText) findViewById(R.id.set_profile_status);
        loadingBar= new ProgressDialog(this);
        userProfileImage = (CircleImageView)findViewById(R.id.set_profile_image);



    }

    @Override
    @SuppressLint("NewApi")
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Uri imageUri = CropImage.getPickImageResultUri(this, data);
            Log.i("RESPONSE getPath", imageUri.getPath());
            Log.i("RESPONSE getScheme", imageUri.getScheme());
            Log.i("RESPONSE PathSegments", imageUri.getPathSegments().toString());

            //NOW CROP IMAGE URI
            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setMultiTouchEnabled(true)
                    //ASPECT RATIO, DELETE IF YOU NEED CROP ANY SIZE
                    .setAspectRatio(1,1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);


            if (resultCode==RESULT_OK){
                Log.i("RESPONSE getUri", result.getUri().toString());

                loadingBar.setTitle("Setting profile image");
                loadingBar.setMessage("please wait ,as we set process your image");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();





                Uri resultUri =(result.getUri());
                final StorageReference filePath = userProfileImageRef.child(currentUserID + ".jpg"+ resultUri.getLastPathSegment());

                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(SettingsActivity.this, "Profile image uploaded successfully", Toast.LENGTH_SHORT).show();

                            filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {


                                    RootRef.child("Users").child(currentUserID).child("image")
                                            .setValue(String.valueOf(uri))
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful()){
                                                        loadingBar.dismiss();
                                                        Toast.makeText(SettingsActivity.this, "Image Saved successfully", Toast.LENGTH_SHORT).show();
                                                    }else{
                                                        loadingBar.dismiss();
                                                        String message = task.getException().toString();
                                                        Toast.makeText(SettingsActivity.this, "Error: "+ message, Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });

                                }
                            });


                        }else {
                            loadingBar.dismiss();
                            String message = task.getException().toString();
                            Toast.makeText(SettingsActivity.this, "Error: "+ message, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }

    }

    private void UpdateSettings() {
        String setUserName = userName.getText().toString();
        String setStatus = userStatus.getText().toString();

        if(TextUtils.isEmpty(setUserName)){
            Toast.makeText(this, "please input user name", Toast.LENGTH_SHORT).show();
        }if (TextUtils.isEmpty(setStatus)){
            Toast.makeText(this, "please enter your status", Toast.LENGTH_SHORT).show();
        }else{
            HashMap<String, Object> profileMap =new HashMap<>();
                profileMap.put("uid", currentUserID);
                profileMap.put("name", setUserName);
                profileMap.put("status", setStatus);
                RootRef.child("Users").child(currentUserID).updateChildren(profileMap)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                               if (task.isSuccessful()){
                                   SendUserToMainActivity();
                                   Toast.makeText(SettingsActivity.this, "Profile updated successfully ", Toast.LENGTH_SHORT).show();

                               }else{


                                   String message = task.getException().toString();
                                   Toast.makeText(SettingsActivity.this, "Error " + message, Toast.LENGTH_SHORT).show();
                               }
                            }
                        });

        }
    }


    private void RetrieveUserInfo() {
        RootRef.child("Users").child(currentUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("name") && (dataSnapshot.hasChild("image")))) {
                            String retrieveUsername = dataSnapshot.child("name").getValue().toString();
                            String retrieveStatus = dataSnapshot.child("status").getValue().toString();
                            String retrieveProfileImage = dataSnapshot.child("image").getValue().toString();

                            userName.setText(retrieveUsername);
                            userStatus.setText(retrieveStatus);
                            Picasso.get().load(retrieveProfileImage).into(userProfileImage);

                        } else if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("name"))) {

                            String retrieveUsername = dataSnapshot.child("name").getValue().toString();
                            String retrieveStatus = dataSnapshot.child("status").getValue().toString();

                            userName.setText(retrieveUsername);
                            userStatus.setText(retrieveStatus);

                        } else {
                            Toast.makeText(SettingsActivity.this, "please set and update your profile information", Toast.LENGTH_SHORT).show();
                        }
                    }


                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(SettingsActivity.this,MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        SendUserToMainActivity();
    }
}
