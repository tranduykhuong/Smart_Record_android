package com.devapp.smartrecord.ui.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.chauthai.swipereveallayout.SwipeRevealLayout;
import com.chauthai.swipereveallayout.ViewBinderHelper;
import com.devapp.smartrecord.R;

import java.util.List;

public class HomeAudioAdapter extends RecyclerView.Adapter<HomeAudioAdapter.HomeAudioHolder>{
    private Context context;
    private List<Audio>audioList;
    private ViewBinderHelper viewBinderHelper = new ViewBinderHelper();

    public HomeAudioAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public HomeAudioHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_audio_home, parent,false);
        return new HomeAudioHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HomeAudioHolder holder, int position) {
        Audio audio = audioList.get(position);
        if(audio == null) {
            return;
        }

        viewBinderHelper.bind(holder.swipeRevealLayout, String.valueOf(position));
        viewBinderHelper.setOpenOnlyOne(true);
        holder.homeTrashBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                audioList.remove(holder.getBindingAdapterPosition());
                notifyItemRemoved(holder.getBindingAdapterPosition());
            }
        });

        holder.icAudioHome.setImageResource(audio.getImage());
        holder.nameAudio.setText(audio.getName());
        holder.timeOfAudio.setText(audio.getTimeOfAudio());
        holder.sizeAudio.setText(audio.getSize());
        holder.createDateAudio.setText(audio.getCreateDate());
    }

    @Override
    public int getItemCount() {
        if (audioList != null) {
            return audioList.size();
        }
        return 0;
    }

    public void setData(List<Audio> audioList){
        this.audioList = audioList;
        notifyDataSetChanged();
    }


    public class HomeAudioHolder extends RecyclerView.ViewHolder {
        private ImageView icAudioHome;
        private TextView nameAudio, timeOfAudio, sizeAudio, createDateAudio;

        private SwipeRevealLayout swipeRevealLayout;
        private RelativeLayout relativeLayout;
        private ImageView homeTrashBtn, homeMoreBtn, homeShareBtn;

        public HomeAudioHolder(@NonNull View itemView) {
            super(itemView);
            icAudioHome = itemView.findViewById(R.id.home_item_icon_audio);
            nameAudio = itemView.findViewById(R.id.home_item_name_audio);
            timeOfAudio = itemView.findViewById(R.id.home_item_time_of_audio);
            sizeAudio = itemView.findViewById(R.id.home_item_size);
            createDateAudio = itemView.findViewById(R.id.home_item_create_date);
            swipeRevealLayout = itemView.findViewById(R.id.swipe_reveal_layout);
            relativeLayout = itemView.findViewById(R.id.item_audio_home);
            homeTrashBtn = itemView.findViewById(R.id.home_btn_delete);
            homeMoreBtn = itemView.findViewById(R.id.home_btn_more);
            homeShareBtn = itemView.findViewById(R.id.home_btn_share);
        }
    }
}