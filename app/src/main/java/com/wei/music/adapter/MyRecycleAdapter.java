package com.wei.music.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.wei.music.R;
import com.wei.music.bean.SongListBean;
import com.wei.music.utils.GlideLoadUtils;
import java.util.List;

public class MyRecycleAdapter extends RecyclerView.Adapter<MyRecycleAdapter.ViewHolder> {

    private Context mCont;
    private List<SongListBean> mList;
    
    private OnItemClick mListener;

    public MyRecycleAdapter(List<SongListBean> mList) {
        this.mList = mList;
    }

    public MyRecycleAdapter(Context mCont, List<SongListBean> mList) {
        this.mCont = mCont;
        this.mList = mList;
    }
    
    public interface OnItemClick {
        void OnClick(SongListBean data, View image, View title, View msg);
    }
    
    public void OnClickListener(OnItemClick listener) {
        this.mListener = listener;
    }


    @NonNull
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
       holder.mTitle.setText(mList.get(position).getTitle());
       holder.mNumber.setText(mList.get(position).getNumber() + " 首");
       GlideLoadUtils mGlideLoadUtils = GlideLoadUtils.getInstance();
       mGlideLoadUtils.setRound(mCont, mList.get(position).getImage(), 8, holder.mImage);
       holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.OnClick(mList.get(position), holder.mImage, holder.mTitle, holder.mNumber);
                }
            });
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewgroup, int viewtype) {
        View view = LayoutInflater.from(mCont).inflate(R.layout.item_song_list, viewgroup, false);
        return new ViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        
        private TextView mTitle, mNumber;
        private ImageView mImage;
        
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mTitle = itemView.findViewById(R.id.song_title);
            mNumber = itemView.findViewById(R.id.song_msg);
            mImage = itemView.findViewById(R.id.song_imag);
        }
        
    }
    
    
}
