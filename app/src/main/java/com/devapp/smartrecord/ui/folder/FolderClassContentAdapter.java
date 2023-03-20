package com.devapp.smartrecord.ui.folder;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.TimedText;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.devapp.smartrecord.R;
import com.devapp.smartrecord.databinding.FolderItemBackgroundRowBinding;
import com.devapp.smartrecord.databinding.FragmentFolderBinding;

import java.util.List;

public class FolderClassContentAdapter extends RecyclerView.Adapter<FolderClassContentAdapter.FolderHolder> {
    private Context context;

    private List<FolderCLassContent> listFolder;

    public FolderClassContentAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public FolderHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.folder_item_background_row, parent, false);

        return new FolderHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FolderHolder holder, int position) {
        FolderCLassContent folder = listFolder.get(position);

        if (folder == null){
            return;
        }
        holder.imageFolder.setImageResource(folder.getImage());
        holder.folderTitle.setText(folder.getTitle());
        holder.folderAmount.setText(folder.getAmount() + "");
        holder.folderSize.setText(folder.getSize() + "");
        holder.folderHour.setText(folder.getHour());
    }

    @Override
    public int getItemCount() {
        if (listFolder.size() != 0){
            return listFolder.size();
        }

        return 0;
    }

    public void setData(List<FolderCLassContent> listFolder){
        this.listFolder = listFolder;

        notifyDataSetChanged();
    }

    public class FolderHolder extends RecyclerView.ViewHolder {
        private ImageView imageFolder;
        private TextView folderTitle, folderAmount, folderSize, folderHour;

        public FolderHolder(@NonNull View itemView) {
            super(itemView);
            imageFolder = itemView.findViewById(R.id.folder_item_image);
            folderTitle = itemView.findViewById(R.id.folder_item_title);
            folderAmount = itemView.findViewById(R.id.folder_item_amount);
            folderSize = itemView.findViewById(R.id.folder_item_size);
            folderHour = itemView.findViewById(R.id.folder_item_hours);
        }
    }

}
