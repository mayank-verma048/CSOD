package com.goodsamaritan.drawer.home;

/**
 * Created by mayank on 12/11/16.
 */

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.goodsamaritan.HelperListUser;
import com.goodsamaritan.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;


import java.util.List;

public class HelperPopupAdapter extends RecyclerView.Adapter<HelperPopupAdapter.MyViewHolder> {


    private static final FirebaseAuth auth;

    private static final FirebaseDatabase database;

    static {
        auth = FirebaseAuth.getInstance();
        database= FirebaseDatabase.getInstance();
    }

    private List<HelperListUser> helperListUserList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title;

        public MyViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.user_title);
        }
    }


    public HelperPopupAdapter(List<HelperListUser> helperListUserList) {
        this.helperListUserList = helperListUserList;
    }

    @Override
    public HelperPopupAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.helper_user, parent, false);
        return new HelperPopupAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final HelperPopupAdapter.MyViewHolder holder, int position) {
        final HelperListUser helperListUser = helperListUserList.get(position);
        holder.title.setText(helperListUser.getUName()+":"+helperListUser.getUPhone());
        holder.title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                database.getReference().getRoot().child("Users").child(helperListUser.getUID()).child("credit_points").setValue("50").addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(holder.title.getContext(),"User "+helperListUser.getUName()+"awarded 50 credits",Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

    }

    @Override
    public int getItemCount() {
        return helperListUserList.size();
    }
}
