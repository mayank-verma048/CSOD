package com.goodsamaritan;


import com.goodsamaritan.drawer.contacts.Contacts;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mayank on 19/10/16.
 */

public class User {
    public String uid;
    public String name;
    public String gender;
    public String phone;
    public List<Contacts.ContactItem> contactItemList=new ArrayList<Contacts.ContactItem>();
    public UserLocation location;
    public String credit_points;
    public String is_online;
    public String last_online;
    public String isAvailable;
    public Password password;
    //Need to add helpers list.

    public User(){} //Required by Firebase database
    public User(String uid,String name,String gender,String phone,List<Contacts.ContactItem> contactItemList,String credit_points,String password){
        this.uid=uid;
        this.name=name;
        this.gender=gender;
        this.phone=phone;
        this.contactItemList=contactItemList;
        this.credit_points=credit_points;
        this.location=new UserLocation();
        this.location.latitude="0";
        this.location.longitude="0";
        this.password= new Password();
        this.password.currentPassword = password;
        this.password.inputPassword = "";
    }

}

class Password{
    public String currentPassword;
    public String inputPassword;

    public Password(){} //Required by Firebase database

}
