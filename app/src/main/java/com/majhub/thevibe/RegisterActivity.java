package com.majhub.thevibe;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class RegisterActivity extends AppCompatActivity {
    private Button CreateAccountButton;
    private EditText UserEmail, UserPassword;
    private TextView AlreadyhaveaccountLink;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;
    private DatabaseReference RootRef;

    private ImageView cloud1,star;
    Animation animCloud,animStar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        cloud1      = findViewById(R.id.cloud1);
        star        = findViewById(R.id.star);
        animCloud   = AnimationUtils.loadAnimation(this,R.anim.animcloud);
        animStar    = AnimationUtils.loadAnimation(this,R.anim.animstar);
        cloud1.startAnimation(animCloud);
        star.startAnimation(animStar);

        mAuth = FirebaseAuth.getInstance();
        RootRef = FirebaseDatabase.getInstance().getReference();

        InitializeFields();

        AlreadyhaveaccountLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToLoginActivity();
            }
        });

        CreateAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateNewAccount();
            }
        });
    }



    private void CreateNewAccount() {
        String email = UserEmail.getText().toString();
        String password = UserPassword.getText().toString();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "please enter your email", Toast.LENGTH_SHORT);
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "please enter your password", Toast.LENGTH_SHORT);
        } else {
            loadingBar.setTitle("creating account");
            loadingBar.setMessage("we are setting up your  new account.Thanks for your patience.");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();

            mAuth.createUserWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){

                                String deviceToken = FirebaseInstanceId.getInstance().getToken();
                                String currentUserID = mAuth.getCurrentUser().getUid();
                                RootRef.child("Users").child(currentUserID).setValue("");
                                RootRef.child("Users").child(currentUserID).child("device_token")
                                        .setValue(deviceToken);

                                SendUserToMainActivity();
                                Toast.makeText(RegisterActivity.this,"account created successfully",Toast.LENGTH_SHORT);
                                    loadingBar.dismiss();
                            }else{
                                String message = task.getException().toString();
                                Toast.makeText(RegisterActivity.this, "Error : "+ message, Toast.LENGTH_SHORT).show();
                                    loadingBar.dismiss();
                            }
                        }
                    });

        }
    }
        private void InitializeFields() {
            CreateAccountButton = (Button)findViewById(R.id.register_button);
            UserEmail = (EditText) findViewById(R.id.register_email);
            UserPassword = (EditText) findViewById(R.id.register_password);
            AlreadyhaveaccountLink = (TextView) findViewById(R.id.already_have_an_account_link);

            loadingBar = new ProgressDialog(this);
        }

    private void SendUserToLoginActivity() {
        Intent loginIntent = new Intent(RegisterActivity.this,LoginActivity.class);
        startActivity(loginIntent);
    }
    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(RegisterActivity.this,MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}
