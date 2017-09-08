package com.goodsamaritan;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import java.util.List;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.widget.Toast;

import com.goodsamaritan.drawer.contacts.Contacts;
import com.goodsamaritan.drawer.home.HomeFragment;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static android.telephony.PhoneNumberUtils.TOA_International;

public class LocationService extends Service implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    static boolean isRunning=false;
    static FirebaseDatabase database;
    static FirebaseAuth auth;

    //For Firebase Database listener
    private static boolean listened=false;
    private static boolean flag;

    private Handler eventHandler= null;
    private HandlerThread eventThread= null;
    private static int notificationCount=0;
    private static String myphone;

    //Google API Client for getting last known location
    GoogleApiClient googleApiClient;

    private static HelperListMaintainer helperListMaintainer;


    public LocationService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }


    private LocationManager locationManager;
    private final Criteria criteria = new Criteria();
    private final static int minUpdateTime = 1000*60*3;
    private final static int minUpdateDistance = 100;
    private static UserLocation userLocation;
    private static Location oUserLocation;
    private static HelpListMaintainer maintainer;

    private static final String TAG = "LOCATION_SERVICE";

    @Override
    public void onCreate() {
        super.onCreate();

        // Get a reference to the Location Manager
        String svcName = Context.LOCATION_SERVICE;
        locationManager = (LocationManager)getSystemService(svcName);

        // Specify Location Provider criteria
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        criteria.setAltitudeRequired(true);
        criteria.setBearingRequired(true);
        criteria.setSpeedRequired(true);
        criteria.setCostAllowed(true);

        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

    }

    @Override
    public int onStartCommand(Intent intent,int flags, int startId){
        isRunning=true;
        database=FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        database.getReference().child(".info/connected").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue(Boolean.class))database.getReference().getRoot().child("Users").child(auth.getCurrentUser().getUid()).child("isOnline").setValue("true");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //Get last known location of user
        googleApiClient.connect();

        database.getReference().getRoot().child("Users").child(auth.getCurrentUser().getUid()).child("isOnline").onDisconnect().setValue("false");
        database.getReference().getRoot().child("Users").child(auth.getCurrentUser().getUid()).child("isAvailable").onDisconnect().setValue("false");

        eventThread = new HandlerThread("EventThread");
        eventThread.start();
        eventHandler= new Handler(eventThread.getLooper());

        //Start tracking user location
        try {
            registerListener();
        }catch (SecurityException e){
            Log.d("ERROR","/Not enough permission.");
        }

        //Obtain list of HelpUsers
        maintainer = new HelpListMaintainer(eventHandler,this);

        //Obtain list of Helpers
        Log.d(TAG,"Obtain helpers");
        helperListMaintainer = new HelperListMaintainer(eventHandler,this);


        myphone=intent.getStringExtra("com.goodsamaritan.myphone");

        return Service.START_REDELIVER_INTENT;
    }


    private void registerListener() throws SecurityException {
        unregisterAllListeners();
        String bestProvider =
                locationManager.getBestProvider(criteria, false);
        String bestAvailableProvider =
                locationManager.getBestProvider(criteria, true);

        Log.d(TAG, bestProvider + " / " + bestAvailableProvider);

        if (bestProvider == null)
            Log.d(TAG, "No Location Providers exist on device.");
        else if (bestProvider.equals(bestAvailableProvider))
            locationManager.requestLocationUpdates(bestAvailableProvider,
                    minUpdateTime, minUpdateDistance,
                    bestAvailableProviderListener);
        else {
            locationManager.requestLocationUpdates(bestProvider,
                    minUpdateTime, minUpdateDistance, bestProviderListener);

            if (bestAvailableProvider != null)
                locationManager.requestLocationUpdates(bestAvailableProvider,
                        minUpdateTime, minUpdateDistance,
                        bestAvailableProviderListener);
            else {
                List<String> allProviders = locationManager.getAllProviders();
                for (String provider : allProviders)
                    locationManager.requestLocationUpdates(provider, 0, 0,
                            bestProviderListener);
                Log.d(TAG, "No Location Providers currently available.");
            }
        }
    }

    private void unregisterAllListeners() throws SecurityException {
        locationManager.removeUpdates(bestProviderListener);
        locationManager.removeUpdates(bestAvailableProviderListener);
    }

    private void reactToLocationChange(Location location) {
        // TODO [ React to location change ]
        Log.d("LOCATIONSERVICE","My Location changed!");
        userLocation = new UserLocation();
        userLocation.latitude=Double.toString(location.getLatitude());
        userLocation.longitude=Double.toString(location.getLongitude());
        userLocation.provider=location.getProvider();
        database.getReference().getRoot().child("Users").child(auth.getCurrentUser().getUid()).child("location").setValue(userLocation);
        //oUserLocation=location;
        setUserLocation(location);
        //Track if you have come near to a person who needs help
        for(final HelpListUser user:maintainer.getUsers()){
            eventHandler.postAtTime(new Runnable() {
                @Override
                public void run() {
                    Log.d("LOCATIONSERVICE","Location changed! "+user.getUName()+" "+user.getUPhone());
                    LocationService.track(user.getUName(),user.getUPhone(),user.getULocation(),user.getUID(),LocationService.this);
                }
            }, SystemClock.uptimeMillis());
        }

    }

    private LocationListener bestProviderListener
            = new LocationListener() {

        public void onLocationChanged(Location location) {
            reactToLocationChange(location);
        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
            registerListener();
        }

        public void onStatusChanged(String provider,
                                    int status, Bundle extras) {}
    };

    private LocationListener bestAvailableProviderListener =
            new LocationListener() {
                public void onProviderEnabled(String provider) {
                }

                public void onProviderDisabled(String provider) {
                    registerListener();
                }

                public void onLocationChanged(Location location) {
                    reactToLocationChange(location);
                }

                public void onStatusChanged(String provider,
                                            int status, Bundle extras) {}
            };

    public static synchronized Location getUserLocation(){
        return oUserLocation;
    }
    public static synchronized void setUserLocation(Location location){oUserLocation=location;}

    public static synchronized void track(String name,String phone,Location location,String uid,Context context){

        if(name==null||phone==null||location==null||getUserLocation()==null){
            Log.e("TRACKER","Objects either not initialized or destroyed!");
            return;
        } else{
            if(((getUserLocation().distanceTo(location)<=500) && !(phone.equals(myphone)))||checkIfInMyContacts(phone)){

                Log.d("PHONE",phone+" "+myphone);

                Intent mainScreen = new Intent(context,MainScreenActivity.class);
                mainScreen.putExtra("com.goodsamaritan.name",name)
                        .putExtra("com.goodsamaritan.phone",phone)
                        .putExtra("com.goodsamaritan.location",location)
                        .putExtra("com.goodsamaritan.startMaps",true)
                        .putExtra("com.goodsamaritan.uid",uid);
                mainScreen.setData(Uri.parse(name+phone+location));
                PendingIntent intent = PendingIntent.getActivity(context, 1,mainScreen, PendingIntent.FLAG_UPDATE_CURRENT);


                NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
                Notification.Builder builder = new Notification.Builder(context);

                builder.setSmallIcon(R.drawable.ic_notification)
                        .setLargeIcon(((BitmapDrawable)context.getResources().getDrawable(R.drawable.ic_notification)).getBitmap())
                        .setWhen(System.currentTimeMillis())
                        .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE)
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                        .setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 })
                        .setLights(Color.RED, 0, 1)
                        .setContentTitle(name+" is nearby and needs your help!")
                        .setContentText("Contact the person:"+phone)
                        .setStyle(new Notification.BigTextStyle().bigText(name+" is nearby and needs your help!"+"\nContact the person:"+phone))
                        .setContentIntent(intent)
                        .setAutoCancel(true);

                Notification notification = builder.build();
                //notificationManager.notify(LocationService.notificationCount++,notification); //Separate Notification per trigger.
                notificationManager.notify(Integer.parseInt(phone.substring(4)),notification); //May only work in india, that too first integer is removed.

                Log.d("NOTIFICATION","It's built.");
                Log.d("TRACKER","Distance is:"+getUserLocation().distanceTo(location)+"Notification ID:"+phone.substring(3));
            } else {
                Log.d("TRACKER","Distance is:"+getUserLocation().distanceTo(location));
            }
        }

    }

    private static boolean checkIfInMyContacts(final String phone) {
        final String fPhone = PhoneNumberUtils.formatNumberToE164(phone,"IN");
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().getRoot().child("Users/"+auth.getCurrentUser().getUid()+"/contactItemList");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()){
                    Contacts.ContactItem contact= postSnapshot.getValue(Contacts.ContactItem.class);
                    Log.d("PHONE_NUMBER",PhoneNumberUtils.formatNumberToE164(contact.phone_number,"IN")+" "+fPhone);
                    if(PhoneNumberUtils.formatNumberToE164(contact.phone_number,"IN")!=null && PhoneNumberUtils.formatNumberToE164(contact.phone_number,"IN").contains(fPhone)){
                        flag=true;
                        listened=true;
                    }
                }

                listened=true;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //Note that it simply means failure of data retrieval and not failure in finding contact.
                listened=true;
                
            }
        });
        while(!listened){
            try {
                Thread.sleep(100);
                Log.d("THREAD",Thread.currentThread().toString());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        listened = false;
        return flag;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) throws SecurityException {
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        if (mLastLocation != null) {
            setUserLocation(mLastLocation);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public synchronized static HelperListMaintainer getMaintainer(){
        return helperListMaintainer;
    }

}
