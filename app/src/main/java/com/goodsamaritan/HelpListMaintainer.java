package com.goodsamaritan;

import android.content.Context;
import android.os.Handler;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mayank on 1/11/16.
 */

public class HelpListMaintainer {
    List<HelpListUser> users = new ArrayList<>();
    Handler eventHandler;

    FirebaseAuth auth;
    FirebaseDatabase database;


    public HelpListMaintainer(final Handler eventHandler, final Context context){
        this.eventHandler=eventHandler;
        auth= FirebaseAuth.getInstance();
        database= FirebaseDatabase.getInstance();

        database.getReference().getRoot().child("HelpUID").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //Clear previous list safely (without memory leak).
                if(getUsers()!=null){
                    for (HelpListUser user: getUsers()) {
                        user.removeListeners();
                    }
                    getUsers().clear();
                }

                //Create a new list. Current implementation is expensive as the whole list is refreshed. Needs optimization.
                for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    HelpListUser user = new HelpListUser(snapshot.getKey(),eventHandler,context);
                    getUsers().add(user);
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public synchronized List<HelpListUser> getUsers(){
        return users;
    }
}
