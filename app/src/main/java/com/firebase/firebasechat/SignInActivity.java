package com.firebase.firebasechat;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthCredential;
import com.google.firebase.auth.GoogleAuthProvider;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SignInActivity extends AppCompatActivity implements View.OnClickListener {

    @BindView(R.id.sign_in_button)
    SignInButton signInButton;
    @BindView(R.id.sign_in_layout)
    LinearLayout signInLayout;
    FirebaseAuth mFirebaseAuth;
    FirebaseUser mFirebaseUser;
    private String mUserName;
    private String mPhotoUrl;
    GoogleSignInOptions mGoogleSignInOptions;
    GoogleSignInClient mGoogleSignInClient;
    private int RC_SIGN_IN = 50001;
    private String TAG = this.getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        ButterKnife.bind(this);

        configureGoogleSignInOptions();

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        signInButton.setOnClickListener(this);

    }

    private void configureGoogleSignInOptions() {
        mGoogleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, mGoogleSignInOptions);
    }

    private void verifyUser() {
        if (mFirebaseUser == null) {
            startActivity(new Intent(this, SignInActivity.class));
            finish();
        } else {
            mUserName = mFirebaseUser.getDisplayName();
            if (mFirebaseUser.getPhotoUrl() != null)
                mPhotoUrl = mFirebaseUser.getPhotoUrl().toString();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                signIn();
                break;
        }
    }


    private void signIn() {
        Intent singInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(singInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInTask(task);
        }
    }

    private void handleSignInTask(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount mSignInAccount = completedTask.getResult(ApiException.class);
            if (mSignInAccount != null) {
                firebaseAuthWithGoogle(mSignInAccount);
            }

        } catch (ApiException e) {
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }


    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        Log.d(TAG, account.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mFirebaseAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "signInWithCredential:success");
                    FirebaseUser user = mFirebaseAuth.getCurrentUser();
                    startActivity(new Intent(SignInActivity.this, MainActivity.class));
                    finish();
//                    updateUI(user);
                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.getException());
                    Toast.makeText(SignInActivity.this, "Authentication failed", Toast.LENGTH_LONG).show();
                }
            }
        });


    }
}
