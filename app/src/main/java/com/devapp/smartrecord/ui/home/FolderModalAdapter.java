package com.devapp.smartrecord.ui.home;

import android.annotation.SuppressLint;
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
import com.devapp.smartrecord.R;
import com.devapp.smartrecord.editmenu.combine.CombineModalAdapter;
import com.devapp.smartrecord.ui.folder.FolderCLassContent;

import java.util.List;

public class FolderModalAdapter extends RecyclerView.Adapter<FolderModalAdapter.FolderModalHolder>{
    private final Context context;
    private List<FolderCLassContent> folderList;
    private OnItemClickListener listener;
    public FolderModalAdapter(Context context, OnItemClickListener listener) {
        this.listener = listener;
        this.context = context;
    }
    public interface OnItemClickListener {
        void onItemClickModal(int position);
    }

    @NonNull
    @Override
    public FolderModalAdapter.FolderModalHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.folder_item_background_row, parent,false);
        return new FolderModalAdapter.FolderModalHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull FolderModalAdapter.FolderModalHolder holder, int position) {
        FolderCLassContent folder = folderList.get(position);
        if(folder == null) {
            return;
        }

        holder.imageFolder.setImageResource(folder.getImage());
        holder.folderTitle.setText(folder.getTitle());
        holder.folderAmount.setText(folder.getAmount() + "");
        holder.folderSize.setText(folder.getSize() + "");
        holder.folderHour.setText(folder.getHour());
        holder.relativeLayout.setOnClickListener(view -> {
            if (listener != null)
                listener.onItemClickModal(holder.getAbsoluteAdapterPosition());
        });
    }

    @Override
    public int getItemCount() {
        if (folderList != null) {
            return folderList.size();
        }
        return 0;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setData(List<FolderCLassContent> audioList){
        this.folderList = audioList;
        notifyDataSetChanged();
    }


    public static class FolderModalHolder extends RecyclerView.ViewHolder {
        private ImageView imageFolder;
        private TextView folderTitle, folderAmount, folderSize, folderHour;
        private SwipeRevealLayout swipeRevealLayout;

        private RelativeLayout relativeLayout;
        private ImageView folderDeleteBtn, folderMoreBtn;

        public FolderModalHolder(@NonNull View itemView) {
            super(itemView);
            imageFolder = itemView.findViewById(R.id.folder_item_image);
            folderTitle = itemView.findViewById(R.id.folder_item_title);
            folderAmount = itemView.findViewById(R.id.folder_item_amount);
            folderSize = itemView.findViewById(R.id.folder_item_size);
            folderHour = itemView.findViewById(R.id.folder_item_hours);
            swipeRevealLayout = itemView.findViewById(R.id.swipe_reveal_layout);
            relativeLayout = itemView.findViewById(R.id.folder_item_view);
            folderDeleteBtn = itemView.findViewById(R.id.folder_btn_delete);
            folderMoreBtn = itemView.findViewById(R.id.folder_btn_more);
        }
    }
}
