package com.devapp.smartrecord.editmenu.insertion;

import static android.content.Intent.getIntent;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.chauthai.swipereveallayout.SwipeRevealLayout;
import com.chauthai.swipereveallayout.ViewBinderHelper;
import com.devapp.smartrecord.R;
import com.devapp.smartrecord.ui.folder.FolderCLassContent;
import com.devapp.smartrecord.ui.folder.FolderClassContentAdapter;
import com.devapp.smartrecord.ui.home.Audio;

import java.util.List;


public class InsertionAudioAdapter extends RecyclerView.Adapter<InsertionAudioAdapter.InsertionAudioHolder>{
    private Context context;
    private List<Audio> audioList;
    private OnItemClickListener listener;
    private ViewBinderHelper viewBinderHelper = new ViewBinderHelper();

    public InsertionAudioAdapter(Context context, OnItemClickListener listener) {
        this.listener = listener;
        this.context = context;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }
    @NonNull
    @Override
    public InsertionAudioHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_audio_home, parent,false);
        return new InsertionAudioHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull InsertionAudioHolder holder, int position) {
        Audio audio = audioList.get(position);
        if(audio == null) {
            return;
        }
        viewBinderHelper.bind(holder.swipeRevealLayout, String.valueOf(position));
        viewBinderHelper.setOpenOnlyOne(true);

        holder.icAudioHome.setImageResource(audio.getImage());
        holder.nameAudio.setText(audio.getName());
        holder.timeOfAudio.setText(audio.getTimeOfAudio());
        holder.sizeAudio.setText(audio.getSize());
        holder.createDateAudio.setText(audio.getCreateDate());

        holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null)
                    listener.onItemClick(holder.getAbsoluteAdapterPosition());
            }
        });
    }
    @Override
    public int getItemCount() {
        if (audioList != null) {
            return audioList.size();
        }
        return 0;
    }

    public void setData(List<Audio> audioList) {
        this.audioList = audioList;
        notifyDataSetChanged();
    }
    public int getLength(){
        return this.audioList.size();
    }

    public void filterList(List<Audio> filteredList) {
        audioList = filteredList;
        notifyDataSetChanged();
    }
    public class InsertionAudioHolder extends RecyclerView.ViewHolder {
        private ImageView icAudioHome;
        private TextView nameAudio, timeOfAudio, sizeAudio, createDateAudio;
        private SwipeRevealLayout swipeRevealLayout;
        private RelativeLayout relativeLayout;
        private ImageView homeTrashBtn, homeMoreBtn, homeShareBtn;
        public InsertionAudioHolder(@NonNull View itemView) {
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