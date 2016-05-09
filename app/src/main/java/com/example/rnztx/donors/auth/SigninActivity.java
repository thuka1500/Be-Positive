package com.example.rnztx.donors.auth;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;

import com.example.rnztx.donors.MainActivity;
import com.example.rnztx.donors.R;
import com.example.rnztx.donors.models.UserInfo;
import com.example.rnztx.donors.utils.Constants;
import com.example.rnztx.donors.utils.Utilities;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class SigninActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener{
    private GoogleApiClient mGoogleApiClient;
    private static final String LOG_TAG = SigninActivity.class.getSimpleName();
    private static final int RC_SIGN_IN = 9001;
    ImageView imgGoogleAvatar;
    @Bind(R.id.sign_in_button) Button btnSignIn;
    @Bind(R.id.btn_sign_out) Button btnSignOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);
        ButterKnife.bind(this);
        Firebase.setAndroidContext(this);
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestProfile()
                .build();

        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();


        try {
            mGoogleApiClient.connect();
            if (Utilities.isUserLogged(this))
                signIn();

        }
        catch (Exception e){
            Log.e(LOG_TAG,e.getMessage());
        }
        btnSignIn.setEnabled(true);
    }


    @OnClick(R.id.sign_in_button)
    public void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @OnClick(R.id.btn_sign_out)
    public void signOut(){
        Utilities.signOut(this,mGoogleApiClient);
        if (imgGoogleAvatar!=null)
            imgGoogleAvatar.setImageBitmap(null);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }


    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(LOG_TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount userAccount = result.getSignInAccount();

//            only for debugging
            imgGoogleAvatar = (ImageView)findViewById(R.id.img_google_plus_avatar);
            Picasso.with(this).load(userAccount.getPhotoUrl()).into(imgGoogleAvatar);

            // store User data
            Utilities.storeUserCredential(userAccount,this);

            // update user information on Firebase
            updateUserInfo();

            // start Main Activity
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        } else {
            // Signed out, show unauthenticated UI.
            Log.e(LOG_TAG,"Failed Signin");
        }
    }
    private void updateUserInfo(){
        Firebase.goOnline();
        Firebase fRoot = new Firebase(Constants.FIREBASE_URL);
        Firebase fUsersLocation = fRoot.child(Constants.FIREBASE_LOCATION_USERS);
        UserInfo currentUser = new UserInfo("70381254",Utilities.getUserEmail(),
                Utilities.getUserDisplayName(),Utilities.getUserPhotoUrl());

        fUsersLocation.child(Utilities.getUserId())
                .setValue(currentUser, new Firebase.CompletionListener() {
                    @Override
                    public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                        if (firebaseError!=null)
                            updateUserInfo();
                        // recursion Be Careful !!!
                        else
                            Firebase.goOffline();
                    }
                });

    }




    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(LOG_TAG,connectionResult.getErrorMessage());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mGoogleApiClient.disconnect();
    }
}
