package com.goodsamaritan.drawer.feed;

import android.graphics.Bitmap;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.goodsamaritan.PostDetails;
import com.goodsamaritan.R;

import java.util.List;

/**
 * Created by mayank on 9/9/17.
 */

public class FeedFragmentAdapter extends RecyclerView.Adapter<FeedFragmentAdapter.MyOwnHolder> {
    private List<PostDetails> postDetailsList;
    private  List<Bitmap> bitmaps;


    @Override
    public MyOwnHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_feed_item,parent,false);

        return new MyOwnHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyOwnHolder holder, int position) {
        PostDetails p = postDetailsList.get(position);
        lStats();
        try{
            Bitmap b = bitmaps.get(position);
            holder.imageView.setImageBitmap(b);
        }catch (Exception e){
            Log.d("IMAGE","Didn't/Couldn't download.");
        }

        holder.subject.setText(p.getSubject());
        holder.desc.setText(p.getDesc());

    }

    public FeedFragmentAdapter(List<PostDetails> listPostDetails){
        postDetailsList  = listPostDetails;
        if(listPostDetails == null) Log.d("CARDVIEW","NULL");
    }

    @Override
    public int getItemCount() {
        return postDetailsList.size();
    }

    public void setImageViewList(List<Bitmap> bitmaps){
        this.bitmaps = bitmaps;
    }

    public void lStats(){
        Log.d("IMAGELIST",""+(bitmaps==null));
        Log.d("IMAGELIST",""+(bitmaps.size()));
    }

    public class MyOwnHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView subject;
        ImageView imageView;
        TextView desc;

        public MyOwnHolder(View itemView) {
            super(itemView);
            cardView = (CardView)itemView.findViewById(R.id.card_view);
            subject = (TextView) cardView.findViewById(R.id.subject);
            imageView = (ImageView) cardView.findViewById(R.id.cardImage);
            desc = (TextView) cardView.findViewById(R.id.description);
        }
    }
}
