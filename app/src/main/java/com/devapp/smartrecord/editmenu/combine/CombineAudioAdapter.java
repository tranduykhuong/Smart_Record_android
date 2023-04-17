package com.devapp.smartrecord.editmenu.combine;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.devapp.smartrecord.R;
import com.devapp.smartrecord.ui.home.Audio;

import java.util.List;

public class CombineAudioAdapter extends RecyclerView.Adapter<CombineAudioAdapter.CombineAudioHolder>{
    private final Context context;
    private List<Audio>audioList;

    public CombineAudioAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public CombineAudioHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_audio_combine, parent,false);
        return new CombineAudioHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull CombineAudioHolder holder, int position) {
        Audio audio = audioList.get(position);
        if(audio == null) {
            return;
        }

        holder.nameAudio.setText(audio.getName());
        holder.tagAudio.setText(audio.getName().substring(audio.getName().lastIndexOf(".") +1).toUpperCase() + " - " + audio.getSize());
        holder.timeAudio.setText("00:00 - " + audio.getTimeOfAudio());
    }

    @Override
    public int getItemCount() {
        if (audioList != null) {
            return audioList.size();
        }
        return 0;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setData(List<Audio> audioList){
        this.audioList = audioList;
        notifyDataSetChanged();
    }


    public static class CombineAudioHolder extends RecyclerView.ViewHolder {
        private final TextView nameAudio, tagAudio, timeAudio;

        public CombineAudioHolder(@NonNull View itemView) {
            super(itemView);
            nameAudio = itemView.findViewById(R.id.combine_txt_name_item);
            tagAudio = itemView.findViewById(R.id.combine_txt_tag);
            timeAudio = itemView.findViewById(R.id.combine_txt_time);
        }
    }
}
