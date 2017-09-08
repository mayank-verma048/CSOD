package com.goodsamaritan;


import android.content.Context;
import android.location.Location;
import android.os.Handler;
import android.os.SystemClock;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HelpListUser{
    private static FirebaseAuth auth;
    private static FirebaseDatabase database;

    private String uid;
    private String name;
    private String phone;
    private Location location;
    private ValueEventListener nameListener;
    private ValueEventListener phoneListener;
    private ValueEventListener locationListener;
    private ValueEventListener uidListener;

    static {
        auth = FirebaseAuth.getInstance();
        database= FirebaseDatabase.getInstance();
    }

    public HelpListUser(String uid, final Handler eventHandler, final Context context){

        this.uid=uid;

        nameListener=new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                name=dataSnapshot.getValue(String.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        database.getReference().getRoot().child("Users").child(uid).child("name").addValueEventListener(nameListener);

        phoneListener=new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                phone=dataSnapshot.getValue(String.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        database.getReference().getRoot().child("Users").child(uid).child("phone").addValueEventListener(phoneListener);

        locationListener=new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                UserLocation userLocation = dataSnapshot.getValue(UserLocation.class);
                Location location = new Location(userLocation.provider);
                location.setLatitude(Double.parseDouble(userLocation.latitude));
                location.setLongitude(Double.parseDouble(userLocation.longitude));
                HelpListUser.this.location=location;

                //Fire Event Handler here
                eventHandler.postAtTime(new Runnable() {
                    @Override
                    public void run() {
                        LocationService.track(getUName(),getUPhone(),getULocation(),getUID(),context);
                    }
                }, SystemClock.uptimeMillis());

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        database.getReference().getRoot().child("Users").child(uid).child("location").addValueEventListener(locationListener);


    }

    public void removeListeners(){
        database.getReference().getRoot().child("Users").child(uid).child("name").removeEventListener(nameListener);
        database.getReference().getRoot().child("Users").child(uid).child("phone").removeEventListener(phoneListener);
        database.getReference().getRoot().child("Users").child(uid).child("location").removeEventListener(locationListener);

    }

    public synchronized String getUName(){
        return name;
    }

    public synchronized String getUPhone(){
        return phone;
    }

    public synchronized Location getULocation(){
        return location;
    }

    public synchronized String getUID(){ return uid; }
}