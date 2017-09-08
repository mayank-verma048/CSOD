package com.goodsamaritan;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.provider.ContactsContract;

import java.util.ArrayList;
import java.util.List;

//Deprecated. Using ContactPicker by 1gravity.
/**
 * Created by mayank on 26/6/17.
 */

public class ContactsPickerAdapter extends RecyclerView.Adapter<ContactsPickerAdapter.MyViewHolder> {
    class MyViewHolder extends RecyclerView.ViewHolder{
        TextView name;
        TextView phone;
        ImageButton dp;
        MyViewHolder(View view){
            super(view);
            name = (TextView) view.findViewById(R.id.contact_name);
            phone = (TextView) view.findViewById(R.id.contact_phone);
            dp = (ImageButton) view.findViewById(R.id.contact_image);
        }
    }
    Cursor cursor;

    class Contacts {
        String name;
        String phoneNumber;
    }

    List<Contacts> contactsList;

    ContactsPickerAdapter(Context context){
        cursor = context.getApplicationContext().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,null,null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME+" ASC");
        Log.d("CONTACTS_PICKER","Query Successful count="+Integer.toString(cursor.getCount()));
        contactsList = new ArrayList<>();

        //Initialize and set all contacts
        /*
        Note: In future, only display contacts available on server.
         */
        while (cursor.moveToNext()) {
            Contacts contact = new Contacts();
            contact.name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            contact.phoneNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            contactsList.add(contact);

        }

    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.contacts_picker_list_row,parent,false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.name.setText(contactsList.get(position).name);
        holder.phone.setText( contactsList.get(position).phoneNumber);

    }

    @Override
    public int getItemCount() {
        return cursor.getCount();
    }
}
