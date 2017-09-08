package com.goodsamaritan;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
//Deprecated. Using ContactPicker by 1gravity
public class ContactsPickerActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts_picker);

        recyclerView = (RecyclerView) findViewById(R.id.contacts_picker_recycler_view);
        layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        adapter = new ContactsPickerAdapter(this);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        Log.d("CONTACTS_PICKER","Set");
    }
}
