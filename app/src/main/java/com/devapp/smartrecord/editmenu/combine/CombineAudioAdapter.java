package com.devapp.smartrecord.editmenu.combine;

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
    private Context context;
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

    @Override
    public void onBindViewHolder(@NonNull CombineAudioHolder holder, int position) {
        Audio audio = audioList.get(position);
        if(audio == null) {
            return;
        }

        holder.nameAudio.setText(audio.getName());
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


    public class CombineAudioHolder extends RecyclerView.ViewHolder {
        private TextView nameAudio;
        private LinearLayout linearLayout;

        public CombineAudioHolder(@NonNull View itemView) {
            super(itemView);
            nameAudio = itemView.findViewById(R.id.combine_txt_name_item);
            linearLayout = itemView.findViewById(R.id.item_audio_home);
        }
    }
}
