package com.devapp.smartrecord.ui.alarm;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;

import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.devapp.smartrecord.R;

import java.util.List;

public class ItemClassContentAdapter extends RecyclerView.Adapter<ItemClassContentAdapter.ItemHolder> {
    private Context context;

    private List<ItemClassContent> listData;
    private OnItemClickListener listener;

    public ItemClassContentAdapter(Context context, OnItemClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rcv_alarm, parent, false);

        return new ItemHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemHolder holder, int position) {
        ItemClassContent folder = listData.get(position);

        if (folder == null){
            return;
        }
        holder.itemTitle.setText(folder.getTitle());
        holder.itemTime.setText(folder.getTime() + "");
        if (folder.getChecked()) {
            holder.cbItem.setChecked(true);
        }

        holder.bind(listData.get(position), listener);
    }

    @Override
    public int getItemCount() {
        if (listData.size() != 0){
            return listData.size();
        }

        return 0;
    }

    public interface OnItemClickListener {
        void onItemClick(int position);

        void onItemClick(int position, boolean checked);
    }

    public void setData(List<ItemClassContent> listData){
        this.listData = listData;

        notifyDataSetChanged();
    }

    public class ItemHolder extends RecyclerView.ViewHolder {
        private TextView itemTitle, itemTime;
        private CheckBox cbItem;

        public ItemHolder(@NonNull View itemView) {
            super(itemView);
            itemTitle = itemView.findViewById(R.id.item_rcv_title);
            itemTime = itemView.findViewById(R.id.item_rcv_time);
            cbItem = itemView.findViewById(R.id.item_rcv_cbox);
        }

        public void bind(ItemClassContent item, final OnItemClickListener listener) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    listener.onItemClick(position, item.getChecked());
                }
            });
        }
    }

}
