package com.devapp.smartrecord.editmenu.combine;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.devapp.smartrecord.R;
import com.devapp.smartrecord.ui.home.Audio;

import java.util.List;

public class CombineModalAdapter extends RecyclerView.Adapter<CombineModalAdapter.CombineModalHolder>{
    private final Context context;
    private List<Audio> audioList;
    private Boolean flagCheck;

    public CombineModalAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public CombineModalAdapter.CombineModalHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_add_combine, parent,false);
        return new CombineModalHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull CombineModalAdapter.CombineModalHolder holder, int position) {
        Audio audio = audioList.get(position);
        if(audio == null) {
            return;
        }

        holder.nameAudio.setText(audio.getName());
        holder.timeAudio.setText(audio.getTimeOfAudio());
        holder.linearLayout.setOnClickListener(view -> {
            flagCheck = holder.checkBox.isChecked();
            holder.checkBox.setChecked(!flagCheck);
        });
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


    public static class CombineModalHolder extends RecyclerView.ViewHolder {
        private final TextView nameAudio, timeAudio;
        private final LinearLayout linearLayout;
        private final CheckBox checkBox;

        public CombineModalHolder(@NonNull View itemView) {
            super(itemView);
            nameAudio = itemView.findViewById(R.id.combine_add_name_item);
            timeAudio = itemView.findViewById(R.id.combine_add_time);
            linearLayout = itemView.findViewById(R.id.combine_add_item);
            checkBox = itemView.findViewById(R.id.combine_checkbox);


        }
    }
}
