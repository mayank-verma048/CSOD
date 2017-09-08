package com.goodsamaritan;

import android.*;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.goodsamaritan.drawer.map.MapFragment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.util.Date;

public class ReportActivity extends AppCompatActivity {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    Button mcamera;
    ImageView mImageView;
    private Object Bitmap;
    private Button postButton;
    private TextView subjectText;
    private TextView descText;
    Button location;
    Button timeButton;

    private Uri mImageUri;
    private File thumb;

    private static final int APP_PERMS = 1097;


    private FirebaseDatabase ref;
    private StorageReference sRef;
    private FirebaseAuth fAuth;

    //Variables for image file
    private File file;
    private boolean fSaved=false;

    private MapFragment.OnFragmentInteractionListener mListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

/*        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }*/

        //Should be handled in MainScreenActivity and not a fragment
        FirebaseApp.initializeApp(ReportActivity.this);
        fAuth= FirebaseAuth.getInstance();
        if(fAuth.getCurrentUser()==null)signInAnonymously();
        sRef = FirebaseStorage.getInstance().getReference();


    }

    @Override
    public void onStart() {
        super.onStart();

        //Should be handled in MainScreenActivity and not a fragment
        //Request Permissions (for Marshmallow onwards)
        requestPermissions();

        final TextView time  = (TextView) findViewById(R.id.time);
        ref = FirebaseDatabase.getInstance();
        subjectText = (EditText) findViewById(R.id.subject);
        descText = (EditText) findViewById(R.id.description);
        postButton = (Button) findViewById(R.id.postButton);
        location = (Button) findViewById(R.id.ButtonLocation);
        timeButton = (Button) findViewById(R.id.ButtonTime);

        timeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
                time.setText(currentDateTimeString);
           }
        });

        location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ReportActivity.this,"Location Send",Toast.LENGTH_SHORT).show();
            }
        });


        /** time.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {


        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());

        // textView is the TextView view that should display it
        time.setText(currentDateTimeString);
        }


        });**/


        mcamera = (Button) findViewById(R.id.ButtonCamera);
        mImageView = (ImageView) findViewById(R.id.imageview);
        mcamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                File photo=null;
                try
                {
//                    // place where to store camera taken picture
//                    photo = SubmitReport.this.createTemporaryFile("picture_up", ".jpg");
                    thumb = ReportActivity.this.createTemporaryFile("pthumb_up", ".jpg");
//                    photo.delete();
                    thumb.delete();
                }
                catch(Exception e)
                {

                }

                startActivityForResult(intent,0);
            }
        });
        // Left for the Firebase implementation.
        //Sends data to Firebase
        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PostDetails details = new PostDetails();
                ULocation location = new ULocation();
                location.setLatitude(Double.toString(LocationService.getUserLocation().getLatitude()));
                location.setLongitude(Double.toString(LocationService.getUserLocation().getLongitude()));
                details.setDesc(descText.getText().toString());
                details.setSubject(subjectText.getText().toString());
                details.setLocation(location);
                DatabaseReference fRef= ref.getReference().getRoot().child("Posts").push();
                fRef.setValue(details);

                /*sRef.child("images/"+fRef.getKey()+"/picture.jpg").putFile(Uri.fromFile(thumb)).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                       @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(ReportActivity.this,"Image Uploaded",Toast.LENGTH_LONG).show();

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ReportActivity.this,"Error while uploading Image",Toast.LENGTH_LONG).show();
                    }
                });*/
                final ProgressDialog pd = new ProgressDialog(ReportActivity.this);
                pd.setMessage("UPLOADING");
                Toast.makeText(ReportActivity.this, "Report Submitted", Toast.LENGTH_SHORT).show();
                if(fSaved){
                    pd.show();
                    sRef.child("images/"+fRef.getKey()+"/picture.jpg").putFile(Uri.fromFile(file)).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            pd.dismiss();
                            Toast.makeText(ReportActivity.this,"Image Uploaded",Toast.LENGTH_LONG).show();

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(ReportActivity.this,"Error uploading Image",Toast.LENGTH_LONG).show();
                        }
                    });
                }

            }
        });
    }

    private File createTemporaryFile(String part, String ext) throws Exception
    {
        File tempDir= Environment.getExternalStorageDirectory();
        tempDir=new File(tempDir.getAbsolutePath()+"/.temp/");
        if(!tempDir.exists())
        {
            tempDir.mkdirs();
        }
        return File.createTempFile(part, ext, tempDir);
    }

/*    public void grabImage(ImageView imageView)
    {
        this.getActivity().getContentResolver().notifyChange(mImageUri, null);
        ContentResolver cr = this.getActivity().getContentResolver();
        android.graphics.Bitmap bitmap;
        try
        {
            bitmap = android.provider.MediaStore.Images.Media.getBitmap(cr, mImageUri);

            FileOutputStream out = null;
            try {
                thumb.delete();
                out = new FileOutputStream(thumb);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            Log.d("COMPRESSION",""+bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG,100,out));
            out.flush();
            Log.d("COMPRESSION",""+thumb.length()+" "+thumb.getPath());
            imageView.setImageBitmap(bitmap);
        }
        catch (Exception e)
        {
            Toast.makeText(getActivity(), "Failed to load", Toast.LENGTH_SHORT).show();
            //Log.d(TAG, "Failed to load", e);
        }
    }*/

    public void requestPermissions(){
        if ((ContextCompat.checkSelfPermission(ReportActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED)||
                (ContextCompat.checkSelfPermission(ReportActivity.this, android.Manifest.permission.READ_PHONE_STATE)!= PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(ReportActivity.this,new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.READ_PHONE_STATE},APP_PERMS);
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

    private void signInAnonymously() {
        fAuth.signInAnonymously().addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                Log.d("SIGNIN","Success");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("SIGNIN","Fail");
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        fSaved = false;

        android.graphics.Bitmap bp = (Bitmap) data.getExtras().get("data");
//        grabImage(mImageView);
/*        FileOutputStream out = null;
        try {
            out = new FileOutputStream(thumb);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        bp.compress(android.graphics.Bitmap.CompressFormat.PNG,100,out);
*/
        mImageView.setImageBitmap(bp);

        //Image compression code.
        final File f3 = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/gs_upload/");
        file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/gs_upload/"+"temp"+".png");

        if (!f3.exists())
            Log.d("FILE_CREATED",Boolean.toString(f3.mkdir()));
        OutputStream outStream;
        try {
            Log.d("FILE_CREATED",Boolean.toString(file.createNewFile()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            outStream = new FileOutputStream(file);
            bp.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, outStream);
            outStream.close();
            fSaved=true;
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    // TODO: Rename method, update argument and hook method into UI event
 /*   public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }*/

/*    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }*/

/*    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }*/

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
/*    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }*/
}

