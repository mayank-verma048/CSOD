package com.goodsamaritan;

/**
 * Created by mayank on 11/11/16.
 */

import android.content.Context;
import android.location.Location;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;



public class HelperListUser{
    private static FirebaseAuth auth;
    private static FirebaseDatabase database;

    private String uid;
    private String name;
    private String phone;
    private ValueEventListener nameListener;
    private ValueEventListener phoneListener;

    static {
        auth = FirebaseAuth.getInstance();
        database= FirebaseDatabase.getInstance();
    }

    public HelperListUser(String uid){

        this.uid=uid;

        nameListener=new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                name=dataSnapshot.getValue(String.class);
                Log.e("LISTENERS","Happening");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("LISTENERS","Not happening.");
            }
        };
        database.getReference().getRoot().child("Users").child(uid).child("name").addValueEventListener(nameListener);

        phoneListener=new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                phone=dataSnapshot.getValue(String.class);
                Log.e("LISTENERS","Happening");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("LISTENERS","Not happening.");
            }
        };
        database.getReference().getRoot().child("Users").child(uid).child("phone").addValueEventListener(phoneListener);

        Log.d("CONSTRUCTOR","Reached end "+uid);
    }

    public void removeListeners(){
        database.getReference().getRoot().child("Users").child(uid).child("name").removeEventListener(nameListener);
        database.getReference().getRoot().child("Users").child(uid).child("phone").removeEventListener(phoneListener);

    }

    public synchronized String getUName(){
        return name;
    }

    public synchronized String getUPhone(){
        return phone;
    }

    public synchronized String getUID(){ return uid;}

    public synchronized boolean isReady(){
        if(name==null||phone==null)return false;
        return true;
    }


}
