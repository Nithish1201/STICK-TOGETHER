package com.nithishkumar.sticktogether;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {

    private EditText usernameEditText;
    private EditText nameEditText;
    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText confirmPasswordEditText;
    private Button registeringButton;
    private TextView loginTextView;

    private DatabaseReference mRootRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        usernameEditText = findViewById(R.id.usernameEditText);
        nameEditText = findViewById(R.id.nameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = (EditText) findViewById(R.id.passwordEditText);
        confirmPasswordEditText = (EditText) findViewById(R.id.confirmPasswordEditText);
        registeringButton = findViewById(R.id.loggingButton);
        loginTextView = (TextView) findViewById(R.id.registerTextView);

        mRootRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        loginTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RegisterActivity.this,LoginActivity.class));
                finish();
            }
        });


        registeringButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String txtUsername = usernameEditText.getText().toString();
                String txtName = nameEditText.getText().toString();
                String txtEmail = emailEditText.getText().toString();
                String txtPassword = passwordEditText.getText().toString();
                if (TextUtils.isEmpty(txtUsername) || TextUtils.isEmpty(txtName) || TextUtils.isEmpty(txtEmail) || TextUtils.isEmpty(txtPassword)){
                    Toast.makeText(RegisterActivity.this, "Empty Credentials", Toast.LENGTH_SHORT).show();
                }else if (txtPassword.length() < 6){
                    Toast.makeText(RegisterActivity.this, "Password to be of atleast 6 Character", Toast.LENGTH_LONG).show();
                }else {
                    if (passwordEditText.getText().toString().equals(confirmPasswordEditText.getText().toString())){

                        registerUser(txtUsername , txtName , txtEmail , txtPassword);

                    }else {
                        Toast.makeText(RegisterActivity.this, "Both passwords don't match please enter the password correctly", Toast.LENGTH_LONG).show();
                        passwordEditText.getText().clear();
                        confirmPasswordEditText.getText().clear();
                    }

                }
            }
        });


    }

    private void registerUser(final String txtUsername, final String txtName, final String txtEmail, String txtPassword) {

        mAuth.createUserWithEmailAndPassword(txtEmail , txtPassword).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {

                HashMap<String , Object> map = new HashMap<>();
                map.put("bio" , "");
                map.put("imageurl" , "default");
                map.put("email" , txtEmail);
                map.put("name" , txtName);
                map.put("username" , txtUsername);
                map.put("id" , Objects.requireNonNull(mAuth.getCurrentUser()).getUid());

                mRootRef.child("users").child(mAuth.getCurrentUser().getUid()).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(RegisterActivity.this, "You are registered... Update the profile for better experience", Toast.LENGTH_LONG).show();
                            startActivity(new Intent(RegisterActivity.this,MainActivity.class));
                            finish();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(RegisterActivity.this, "Registration failed please try again !!", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(RegisterActivity.this,StartActivity.class));
        finish();
    }
}