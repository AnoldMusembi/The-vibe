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
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.concurrent.TimeUnit;

public class PhoneLoginActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private ImageView cloud1,star;
    Animation animCloud,animStar;
    private Button SendVerificationCodeButton, VerifyButton;
    private EditText InputPhoneNumber, InputVerificationCode;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    private String mVerificationID;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private ProgressDialog loadingBar;
    private DatabaseReference UsersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);
        cloud1      = findViewById(R.id.cloud1);
        star        = findViewById(R.id.star);
        animCloud   = AnimationUtils.loadAnimation(this,R.anim.animcloud);
        animStar    = AnimationUtils.loadAnimation(this,R.anim.animstar);
        cloud1.startAnimation(animCloud);
        star.startAnimation(animStar);

        mAuth = FirebaseAuth.getInstance();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        SendVerificationCodeButton= (Button) findViewById(R.id.send_ver_code_button);
        VerifyButton= (Button) findViewById(R.id.ver_code_button);
        InputPhoneNumber= (EditText) findViewById(R.id.phone_number_input);
        InputVerificationCode= (EditText) findViewById(R.id.verification_code_input);
        loadingBar = new ProgressDialog(this);

        SendVerificationCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String phoneNumber = InputPhoneNumber.getText().toString();

                if (TextUtils.isEmpty(phoneNumber)){
                    Toast.makeText(PhoneLoginActivity.this, "Please enter verification...", Toast.LENGTH_SHORT).show();
                }else {

                    loadingBar.setTitle("Phone Verification");
                    loadingBar.setMessage("Please wait, while we are authenticating using your phone...");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();


                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            phoneNumber,
                            60,
                            TimeUnit.SECONDS,
                            PhoneLoginActivity.this,
                            callbacks);
                }
            }
        });


        VerifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                InputPhoneNumber.setVisibility(View.INVISIBLE);
                SendVerificationCodeButton.setVisibility(View.INVISIBLE);


                String verificationCode = InputVerificationCode.getText().toString();

                if (TextUtils.isEmpty(verificationCode))
                {
                    Toast.makeText(PhoneLoginActivity.this, "Please enter verification code ...", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    loadingBar.setTitle("Verification Code");
                    loadingBar.setMessage("Please wait, while we are verifying verification code...");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();

                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationID, verificationCode);
                    signInWithPhoneAuthCredential(credential);
                }
            }
        });

            callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                @Override
                public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                            signInWithPhoneAuthCredential(phoneAuthCredential);
                }

                @Override
                public void onVerificationFailed(FirebaseException e) {
                    Toast.makeText(PhoneLoginActivity.this, "Invalid number, please enter your phone with your country-code ", Toast.LENGTH_SHORT).show();

                    InputPhoneNumber.setVisibility(View.VISIBLE);
                    SendVerificationCodeButton.setVisibility(View.VISIBLE);

                    InputVerificationCode.setVisibility(View.INVISIBLE);
                    VerifyButton.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {

                        mVerificationID = verificationId;
                        mResendToken = token;

                    Toast.makeText(PhoneLoginActivity.this, "Code has been sent, please check and verify...", Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();

                    InputPhoneNumber.setVisibility(View.INVISIBLE);
                    SendVerificationCodeButton.setVisibility(View.INVISIBLE);

                    InputVerificationCode.setVisibility(View.VISIBLE);
                    VerifyButton.setVisibility(View.VISIBLE);

                }
            } ;
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential)
    {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful())
                        {
                            String currentUserId = mAuth.getCurrentUser().getUid();
                            String deviceToken = FirebaseInstanceId.getInstance().getToken();

                            UsersRef.child(currentUserId).child("device_token")
                                    .setValue(deviceToken).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task)
                                {
                                    if (task.isSuccessful())
                                    {
                                        loadingBar.dismiss();
                                        Toast.makeText(PhoneLoginActivity.this, "Congratulations, you're logged in Successfully.", Toast.LENGTH_SHORT).show();
                                        SendUserToMainActivity();
                                    }
                                }
                            });
                        }
                        else
                        {
                            String message = task.getException().toString();
                            Toast.makeText(PhoneLoginActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                        }
                    }
                });
    }




    private void SendUserToMainActivity()
    {
        Intent mainIntent = new Intent(PhoneLoginActivity.this, MainActivity.class);
        startActivity(mainIntent);
        finish();
    }


}
