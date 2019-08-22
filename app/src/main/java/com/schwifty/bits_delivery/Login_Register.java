package com.schwifty.bits_delivery;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;

public class Login_Register extends AppCompatActivity {

    //a constant for detecting the login intent result
    private static final int RC_SIGN_IN = 234;

    //Tag for the logs optional
    private static final String TAG = "simplifiedcoding";

    //creating a GoogleSignInClient object
    GoogleSignInClient mGoogleSignInClient;

    //And also a Firebase Auth object
    FirebaseAuth mAuth;
    EditText e;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login__register);

        //first we intialized the FirebaseAuth object
        mAuth = FirebaseAuth.getInstance();
        e = findViewById(R.id.BITSID);
        //Then we need a GoogleSignInOptions object
        //And we need to build it as below
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("314020875141-kqar5almojf07t4jkeqd2p6oohehqdjr.apps.googleusercontent.com")
                .setHostedDomain("goa.bits-pilani.ac.in")
                .requestEmail()
                .build();

        //Then we will get the GoogleSignInClient object from GoogleSignIn class
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        mGoogleSignInClient.signOut().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                    }
                });

        //Now we will attach a click listener to the sign_in_button
        //and inside onClick() method we are calling the signIn() method that will open
        //google sign in intent

        findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!TextUtils.isEmpty(e.getText().toString())
                    && e.getText().toString().trim().length()>10
                )
                {
                    signIn();
                }
                else
                {
                    Toast.makeText(Login_Register.this, "Please enter your BITS Id", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        //if the user is already signed in
        //we will close this activity
        //and take the user to profile activity
        if (mAuth.getCurrentUser() != null) {
            finish();
            startActivity(new Intent(this, ProfileActivity.class));
            overridePendingTransition(R.anim.fadein,R.anim.fadeout);
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //if the requestCode is the Google Sign In code that we defined at starting
        if (requestCode == RC_SIGN_IN) {
            //Getting the GoogleSignIn Task
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                //Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                overridePendingTransition(R.anim.fadein,R.anim.fadeout);;

                //authenticating with firebase
                firebaseAuthWithGoogle(account);


            } catch (ApiException e) {
                Toast.makeText(Login_Register.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId() + acct.getEmail());

        FirebaseDatabase.getInstance().getReference().child("Users")
                .child(acct.getEmail().replace('.','_'))
                .child("Id")
                .setValue(e.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {

            }
        });

        //getting the auth credential
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);

        //Now using firebase we are signing in the user here
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();



                            Toast.makeText(Login_Register.this, "User Signed In", Toast.LENGTH_SHORT).show();

                            if (mAuth.getCurrentUser() != null) {
                                finish();
                                startActivity(new Intent(Login_Register.this, ProfileActivity.class));
                                overridePendingTransition(R.anim.fadein,R.anim.fadeout);
                            }


                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(Login_Register.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

                        }

                        // ...
                    }
                });


    }


    //this method is called on click
    private void signIn() {
        //getting the google signin intent
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();

        //starting the activity for result
        startActivityForResult(signInIntent, RC_SIGN_IN);
        overridePendingTransition(R.anim.fadein,R.anim.fadeout);
    }
}