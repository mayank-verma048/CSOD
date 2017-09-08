package com.goodsamaritan;
import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Animatable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import com.digits.sdk.android.AuthCallback;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.LoggingBehavior;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.goodsamaritan.drawer.contacts.Contacts;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.FirebaseDatabase;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class MainActivity extends AppCompatActivity implements Serializable {

    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private static final String TAG ="TAG:";
    private static final int APP_PERMS = 1097;

    //Strong Reference to authCallback
    AuthCallback authCallback;

    //Facebook Variables
    CallbackManager callbackManager;
    List<String> permissionNeeds;
    AccessToken accessToken=null;

    //Firebase Variables
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks phoneVerificationCallback;
    private PhoneAuthCredential phoneAuthCredential;
    private String phoneVerificationId;
    private boolean isPhoneVerified = false;

    //Progress Dialog
    ProgressDialog pd;

    //Verification Status Flag
    boolean isComplete=false;

    //Is Verify clicked
    boolean isVerifyClicked=false;

    //Offset for password image.
    int offset=144;

    private boolean isSignUpClicked=false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        pd=new ProgressDialog(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Sign Up");

        //Request Permissions (for Marshmallow onwards)
        requestPermissions();


        //Start with Phone Number verification, then Facebook and then Firebase
        //In future, will check Firebase database to verify if new or old account.
        final EditText phone = (EditText) findViewById(R.id.phoneid);
        Button otpButton = (Button) findViewById(R.id.send_otp);
        mAuth = FirebaseAuth.getInstance();
        System.out.println("BEFORE PHONE");
        //((Animatable)((ImageView) findViewById(R.id.checkid)).getDrawable()).start();

        phoneVerificationCallback = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                MainActivity.this.phoneAuthCredential = phoneAuthCredential;
                Toast.makeText(MainActivity.this,"Phone number verified",Toast.LENGTH_SHORT).show();
                Log.d("PHONE VERIFICATION","Verified without any otp.");
                signInWithPhone();
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.w(TAG, "onVerificationFailed", e);

                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    // ...
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    // ...
                }

                // Show a message and update the UI
                // ...
            }

            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d(TAG, "onCodeSent:" + verificationId);


                EditText verifyOTP = (EditText) findViewById(R.id.verify);
                verifyOTP.setVisibility(View.VISIBLE);
                findViewById(R.id.img_password).animate().translationYBy(offset);
                offset=0;
                findViewById(R.id.send_otp).setEnabled(false);
                findViewById(R.id.verify_btn).setVisibility(View.VISIBLE);
                MainActivity.this.phoneVerificationId = verificationId;
                findViewById(R.id.verify_btn).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        findViewById(R.id.verify_btn).setVisibility(View.GONE);
                        isVerifyClicked=true;
                        findViewById(R.id.progress_bar).setVisibility(View.VISIBLE);
                        MainActivity.this.phoneAuthCredential = PhoneAuthProvider.getCredential(MainActivity.this.phoneVerificationId, ((EditText) findViewById(R.id.verify)).getText().toString());
                        signInWithPhone();
                    }
                });

                // ...
            }

            @Override
            public void onCodeAutoRetrievalTimeOut(String verificationId){

            }
        };

        otpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText phoneText = (EditText) findViewById(R.id.phoneid);
                if(phoneText.getText().length()==0)phoneText.setError("Required!");
                else {
                    PhoneAuthProvider.getInstance().verifyPhoneNumber(phoneText.getText().toString(),60,TimeUnit.SECONDS,MainActivity.this,phoneVerificationCallback);
                }
            }
        });

        //startPhoneNumberVerification(phone.getText());


        Button sign_up_btn= (Button) findViewById(R.id.sign_up_btn);
        sign_up_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isSignUpClicked = true;
                EditText nameText = (EditText) findViewById(R.id.nameid);
                EditText emailText = (EditText) findViewById(R.id.email);
                RadioButton radioFemale = (RadioButton) findViewById(R.id.radioFemale);
                EditText phoneText = (EditText) findViewById(R.id.phoneid);
                EditText passwordText = (EditText) findViewById(R.id.passwordid);
                RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radioSex);

                if(nameText.getText().length()==0)nameText.setError("Required!");
                else if(emailText.getText().length()==0)emailText.setError("Required!");
                else if(phoneText.getText().length()==0)phoneText.setError("Required!");
                else if(passwordText.getText().length()==0)passwordText.setError("Required!");
                else if(radioGroup.getCheckedRadioButtonId()==-1)radioFemale.setError("Required!");
                else if(!isPhoneVerified)phoneText.setError("Please verify your phone number first.");
                else {

                    //Start with Facebook and then Firebase

                    facebookLogin();


                }



            }
        });

        //Auto Login starts here:
        Log.d("AUTO LOGIN","Started");
        pd.setTitle("Authenticating");
        pd.setMessage("Verifying Phone Number");
        pd.setCancelable(false);
        pd.show();
        if(FirebaseAuth.getInstance().getCurrentUser()!=null)facebookLogin();/*authenticateFirebase();*/
        else pd.hide();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("FACEBOOK","Came back from Login Screen.");
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    void signInWithPhone(){
        mAuth.signInWithCredential(phoneAuthCredential).addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    //When Verify Button is clicked only
                    if(isVerifyClicked){
                        findViewById(R.id.progress_bar).setVisibility(View.GONE);
                        findViewById(R.id.checkid).setVisibility(View.VISIBLE);
                        Log.d("CHECKID","before");
                        ((Animatable)((ImageView) findViewById(R.id.checkid)).getDrawable()).start();
                        Log.d("CHECKID","after");
                    }

                    //For all cases
                    isPhoneVerified = true;
                } else {
                    if(isVerifyClicked){
                        findViewById(R.id.checkid).setVisibility(View.GONE);
                        ((EditText)findViewById(R.id.verify)).setError("Incorrect code.");
                        findViewById(R.id.verify_btn).setVisibility(View.VISIBLE);

                    }
                    Toast.makeText(MainActivity.this,"Phone verification failed.\n Please try again.",Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void handleFacebookAccessToken(AccessToken token) {

        if(accessToken==null)return;
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.getCurrentUser().linkWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "linkWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        authenticateFirebase();
                        if (!task.isSuccessful() && !task.getException().getMessage().contains("already been linked to the given provider")) {
                            Log.w(TAG, "linkWithCredential", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                });

    }

    void authenticateFirebase(){

        pd.dismiss();
        pd.setMessage("Going Online! :)");
        pd.show();

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    Toast.makeText(getApplicationContext(),user.getUid(),Toast.LENGTH_SHORT).show();

                    isComplete=true;

                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                    if(phoneAuthCredential==null)Toast.makeText(MainActivity.this,"Please verify your phone.",Toast.LENGTH_LONG).show();
                    else {
                        signInWithPhone();
                        facebookLogin();
                    }
                }
                // ...
            }
        };

        mAuth.addAuthStateListener(mAuthListener);
    }

    void facebookLogin(){
        Log.d("FACEBOOK","Started Facebook verification.");

        pd.dismiss();
        pd.setMessage("Verifying Facebook Credentials");
        pd.show();

        permissionNeeds= Arrays.asList("email","user_friends");
        FacebookSdk.setApplicationId("193849254386118");
        FacebookSdk.sdkInitialize(MainActivity.this.getApplicationContext()); //Facebook SDK auto initializes on start. Still, it's called just in case...

        FacebookSdk.addLoggingBehavior(LoggingBehavior.GRAPH_API_DEBUG_INFO);
        FacebookSdk.addLoggingBehavior(LoggingBehavior.DEVELOPER_ERRORS);
        FacebookSdk.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);
        FacebookSdk.addLoggingBehavior(LoggingBehavior.INCLUDE_RAW_RESPONSES);
        FacebookSdk.setIsDebugEnabled(true);

        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        // App code
                        //Toast.makeText(getApplicationContext(),"Success",Toast.LENGTH_LONG).show();
                        //System.out.println("1");
                        //Link Facebook with Phone
                        accessToken=loginResult.getAccessToken();
                        handleFacebookAccessToken(accessToken);

                        //Now Firebase Login
                        System.out.println("BEFORE FIREBASE");
                        authenticateFirebase();

                    }

                    @Override
                    public void onCancel() {
                        // App code
                        Toast.makeText(getApplicationContext(),"You cancelled some permissions required.",Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        // App code
                        LoginManager.getInstance().logInWithReadPermissions(MainActivity.this,MainActivity.this.permissionNeeds);
                    }
                }
        );
        LoginManager.getInstance().logInWithReadPermissions(MainActivity.this,permissionNeeds);
    }

    @Override
    protected void onStart() {
        super.onStart();

        Log.d("GOOD SAMARITAN","Started.");

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                while (!isComplete) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }



                Intent i = new Intent(MainActivity.this, MainScreenActivity.class);
                EditText name = (EditText) findViewById(R.id.nameid);
                EditText email = (EditText) findViewById(R.id.email);
                EditText phone = (EditText) findViewById(R.id.phoneid);
                RadioGroup radioGroup= (RadioGroup) findViewById(R.id.radioSex);
                String gender;
                switch (radioGroup.getCheckedRadioButtonId()){
                    case R.id.radioMale:
                        gender="male";
                        break;
                    case R.id.radioFemale:
                        gender="female";
                        break;
                    default:
                        gender="null";
                        break;
                }

                pd.dismiss();

                //Diagnosis
                for(String provider:FirebaseAuth.getInstance().getCurrentUser().getProviders())
                    Log.d("FIREBASE USER:",provider);

                if(isSignUpClicked){
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    EditText passwordText = (EditText) findViewById(R.id.passwordid);
                    User user =new User(mAuth.getCurrentUser().getUid(),name.getText().toString(),gender,FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber(),null,"0",passwordText.getText().toString());
                    System.out.println("FIREBASE SET_VALUE\n\n\n"+user.uid);
                    database.getReference().getRoot().child("Users/"+user.uid+"/").setValue(user);
                }

                i.putExtra("com.goodsamaritan.myphone",FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber());
                startActivity(i);
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            Log.d("GOOD SAMARITAN","Stopped");
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        EditText phone = (EditText) findViewById(R.id.phoneid);
        Log.d("GOOD SAMARITAN","Resummed");
        //startPhoneNumberVerification(phone.getText());

    }

    public void requestPermissions(){
        if ((ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED)||
                (ContextCompat.checkSelfPermission(this,Manifest.permission.READ_PHONE_STATE)!= PackageManager.PERMISSION_GRANTED)|| (ContextCompat.checkSelfPermission(this,Manifest.permission.READ_CONTACTS)!= PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.READ_PHONE_STATE,Manifest.permission.READ_CONTACTS},APP_PERMS);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case APP_PERMS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
}
