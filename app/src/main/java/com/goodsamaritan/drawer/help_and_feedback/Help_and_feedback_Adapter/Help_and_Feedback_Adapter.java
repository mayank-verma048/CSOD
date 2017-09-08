package com.goodsamaritan.drawer.help_and_feedback.Help_and_feedback_Adapter;

/**
 * Created by gayathri_2 on 08-11-2016.
 */

//public class Help_and_Feedback_Adapter {
//}


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.goodsamaritan.R;
import com.goodsamaritan.drawer.help_and_feedback.Help_and_feedback;

import java.util.List;

public class Help_and_Feedback_Adapter extends RecyclerView.Adapter<Help_and_Feedback_Adapter.MyViewHolder> {

    private List<Help_and_feedback> help_and_feedbacks_List;

    class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title;

        MyViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.title);
        }
    }


    public Help_and_Feedback_Adapter(List<Help_and_feedback> help_and_feedbacks_List) {
        this.help_and_feedbacks_List = help_and_feedbacks_List;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.help_and_feedback_list_row, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Help_and_feedback help_and_feedback = help_and_feedbacks_List.get(position);
        holder.title.setText(help_and_feedback.getTitle());
    }

    @Override
    public int getItemCount() {
        return help_and_feedbacks_List.size();
    }
}
