package com.goodsamaritan;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mayank on 11/11/16.
 */

public class HelperListMaintainer {
    List<HelperListUser> users = new ArrayList<>();
    Handler eventHandler;

    FirebaseAuth auth;
    FirebaseDatabase database;


    public HelperListMaintainer(final Handler eventHandler, final Context context){
        this.eventHandler=eventHandler;
        auth= FirebaseAuth.getInstance();
        database= FirebaseDatabase.getInstance();

        database.getReference().getRoot().child("Users").child(auth.getCurrentUser().getUid()).child("helpers").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Log.d("HELPERS","List updated.");

                //Clear previous list safely (without memory leak).
                if(getUsers()!=null){
                    for (HelperListUser user: getUsers()) {
                        user.removeListeners();
                    }
                    getUsers().clear();
                }

                //Create a new list. Current implementation is expensive as the whole list is refreshed. Needs optimization.
                for (final DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    //HelperListUser user = new HelperListUser(snapshot.getKey());
                    Log.d("HELPERLISTUSER",snapshot.getKey());
                    //getUsers().add(user);

                    Thread t =new Thread(new Runnable() {
                        @Override
                        public void run() {
                            HelperListUser user = new HelperListUser(snapshot.getKey());
                            getUsers().add(user);
                            Log.d("HELPERLISTUSER",snapshot.getKey());
                            while (!user.isReady()){
                                try{
                                    Thread.sleep(500);
                                    Log.d("HELPERUSER","User data not initialized!");
                                }catch (InterruptedException e){

                                }
                            }
                            fireNotification(user,context);
                        }
                    });

                    t.start();


                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public synchronized List<HelperListUser> getUsers(){
        return users;
    }

    private synchronized void fireNotification(HelperListUser user,Context context){
        String name = user.getUName();
        String phone = user.getUPhone();
        Intent mainScreen = new Intent(context,MainScreenActivity.class);
        mainScreen.putExtra("com.goodsamaritan.name",name)
                .putExtra("com.goodsamaritan.phone",phone)
                .putExtra("com.goodsamaritan.startHelper",true)
                .putExtra("com.goodsamaritan.uid",auth.getCurrentUser().getUid());
        mainScreen.setData(Uri.parse(name+phone));
        PendingIntent intent = PendingIntent.getActivity(context, 1,mainScreen, PendingIntent.FLAG_UPDATE_CURRENT);


        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification.Builder builder = new Notification.Builder(context);

        builder.setSmallIcon(R.drawable.ic_notification)
                .setLargeIcon(((BitmapDrawable)context.getResources().getDrawable(R.drawable.ic_notification)).getBitmap())
                .setWhen(System.currentTimeMillis())
                .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 })
                .setLights(Color.RED, 0, 1)
                .setContentTitle(name+" coming for your help.")
                .setContentText("Contact the person:"+phone)
                .setStyle(new Notification.BigTextStyle().bigText(name+" is coming for your help."+"\nContact the person:"+phone))
                .setContentIntent(intent)
                .setAutoCancel(true);

        Notification notification = builder.build();
        //notificationManager.notify(LocationService.notificationCount++,notification); //Separate Notification per trigger.
        notificationManager.notify(Integer.parseInt(phone.substring(4)),notification); //May only work in india, that too first integer is removed.

        Log.d("NOTIFICATION FOR HELPER","It's built.");
    }
}
