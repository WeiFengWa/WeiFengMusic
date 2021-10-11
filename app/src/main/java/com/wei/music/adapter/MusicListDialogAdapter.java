package com.wei.music.adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.media.session.MediaSessionCompat;
import android.widget.Toast;
import android.support.v4.media.MediaBrowserCompat;
import java.util.ArrayList;
import android.media.MediaMetadata;
import android.support.v4.media.MediaMetadataCompat;
import com.wei.music.utils.GlideLoadUtils;
import com.wei.music.R;


public class MusicListDialogAdapter extends RecyclerView.Adapter<MusicListDialogAdapter.MusicViewHolder> {

    private Context mCont;
    private List<MediaBrowserCompat.MediaItem> mList;
    
    private OnItemClick mListener;

    public MusicListDialogAdapter(Context cont, List<MediaBrowserCompat.MediaItem> list) {
        this.mCont = cont;
        this.mList = list;  
    }

    public interface OnItemClick {
        void OnClick(MediaBrowserCompat.MediaItem data, int postion);
    }

    public void OnClickListener(OnItemClick listener) {
        this.mListener = listener;
    }

    @NonNull
    @Override
    public void onBindViewHolder(final MusicViewHolder holder, final int position) {
        holder.mName.setText(mList.get(position).getDescription().getTitle().toString());
        holder.mSinger.setText(mList.get(position).getDescription().getSubtitle().toString());
        GlideLoadUtils mGlideLoadUtils = GlideLoadUtils.getInstance();
        mGlideLoadUtils.setRound(mCont, mList.get(position).getDescription().getDescription().toString(), 8, holder.mImage);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.OnClick(mList.get(position), position);
                }
            });
    }


    @Override
    public MusicViewHolder onCreateViewHolder(ViewGroup viewgroup, int viewtype) {
        View view = LayoutInflater.from(mCont).inflate(R.layout.item_music_list, viewgroup, false);
        return new MusicViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    static class MusicViewHolder extends RecyclerView.ViewHolder {

        private TextView mName, mSinger ;
        private ImageView mImage;

        public MusicViewHolder(@NonNull View itemView) {
            super(itemView);
            mName = itemView.findViewById(R.id.music_title);
            mSinger = itemView.findViewById(R.id.music_msg);
            mImage = itemView.findViewById(R.id.music_imag);
        }

    }


}

