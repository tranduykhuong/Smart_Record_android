package com.devapp.smartrecord.ui.trash;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.chauthai.swipereveallayout.SwipeRevealLayout;
import com.chauthai.swipereveallayout.ViewBinderHelper;
import com.devapp.smartrecord.R;
import com.devapp.smartrecord.ui.folder.FolderCLassContent;

import java.io.File;
import java.util.List;

public class TrashAdapter extends RecyclerView.Adapter<TrashAdapter.TrashHolder>{
    private final Context context;
    private List<Item> itemList;
    private final OnItemClickListenerTrash listener;
    private final ViewBinderHelper viewBinderHelper = new ViewBinderHelper();

    public TrashAdapter(Context context, OnItemClickListenerTrash listener) {
        this.context = context;
        this.listener = listener;
    }

    public interface OnItemClickListenerTrash {
        void onItemClick(int position);
        void playSound(String name);
    }
    @NonNull
    @Override
    public TrashHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_trash, parent, false);
        return new TrashHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TrashHolder holder, int position) {
        Item item = itemList.get(position);
        if(item == null) {
            return;
        }
        viewBinderHelper.bind(holder.swipeRevealLayout, String.valueOf(position));
        viewBinderHelper.setOpenOnlyOne(true);

        holder.permanentlyDeleteBtn.setOnClickListener(view -> {

        });

        //Khôi phục lại
        holder.trashRestoreBtn.setOnClickListener(view -> {
            Item itemRestore = itemList.get(holder.getAbsoluteAdapterPosition());
            String fileName = itemRestore.getName();
            File sourceFile  = new File(Environment.getExternalStorageDirectory().toString() + "/Recordings/TrashAudio/" + fileName); // Lấy đường dẫn đầy đủ đến tệp
            File destinationFolder = new File(Environment.getExternalStorageDirectory().toString()+"/Recordings/");

            if (!destinationFolder.exists()) {
                destinationFolder.mkdir();
            }
            //Tạo ra dialog để xác nhận khôi phục hay không
            AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
            builder.setMessage(view.getContext().getString(R.string.question_restored));
            builder.setPositiveButton(view.getContext().getString(R.string.answer_yes), (dialogInterface, j) -> {
                try {
                    if (sourceFile.exists()) { //Kiểm tra tệp có tồn tại hay không
                        File destinationFile = new File(destinationFolder, fileName); // Tạo tệp đích mới
                        if (sourceFile.renameTo(destinationFile)) { // Di chuyển tệp đến thư mục đích và kiểm tra kết quả
                            itemList.remove(holder.getAbsoluteAdapterPosition());
                            notifyItemRemoved(holder.getAbsoluteAdapterPosition());
                            Toast.makeText(context, view.getContext().getString(R.string.announce_restored_successfully), Toast.LENGTH_SHORT).show();
                            listener.onItemClick(j);

                        }else {
                            Toast.makeText(context, view.getContext().getString(R.string.announce_restored_unsuccessfully), Toast.LENGTH_SHORT).show();
                        }
                    }
                    else {
                        Toast.makeText(context, view.getContext().getString(R.string.annouce_file_not_exist), Toast.LENGTH_SHORT).show();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(context, view.getContext().getString(R.string.announce_restored_unsuccessfully) + ": " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
            builder.setNegativeButton(view.getContext().getString(R.string.answer_no), (dialogInterface, i) -> {

            });
            builder.show();

        });
        //Xóa vĩnh viễn
        holder.permanentlyDeleteBtn.setOnClickListener(view -> {
            Item item1 = itemList.get(holder.getAbsoluteAdapterPosition());
            String fileName = item1.getName();
            File file = new File(Environment.getExternalStorageDirectory().toString()+ "/Recordings/TrashAudio/" + fileName);

            //Tạo ra dialog để xác nhận xóa file vĩnh viễn
            AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
            builder.setMessage(view.getContext().getString(R.string.question_delete_permanently));
            //Xử lí lựa chọn
            builder.setPositiveButton(view.getContext().getString(R.string.answer_yes), (dialogInterface, j) -> {
                try {
                    if (file.exists()) { // Xóa tệp và kiểm tra kết quả
                        Log.e("TAG", "onClick: " + file);
                        itemList.remove(holder.getAbsoluteAdapterPosition());
                        file.delete();
                        listener.onItemClick(j);
                        notifyItemRemoved(holder.getAbsoluteAdapterPosition());
                        Toast.makeText(context, view.getContext().getString(R.string.announce_deleted_successfully), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, view.getContext().getString(R.string.annouce_file_not_exist), Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(context, view.getContext().getString(R.string.announce_deleted_unsuccessfully) + ": " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

            builder.setNegativeButton(view.getContext().getString(R.string.answer_no), (dialogInterface, i) -> {

            });
            builder.show();
        });

        holder.icAudioTrash.setImageResource(item.getImage());
        holder.icAudioTrash.setOnClickListener(view -> listener.playSound(itemList.get(holder.getAbsoluteAdapterPosition()).getName()));
        holder.nameItem.setText(item.getName());
        holder.timeOfItem.setText(item.getTimeOfAudio());
        holder.sizeItem.setText(item.getSize());
        holder.createDateItem.setText(item.getCreateDate());
    }

    @Override
    public int getItemCount() {
        if (itemList != null) {
            return itemList.size();
        }
        return 0;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setDataItem(List<Item> itemList) {
        this.itemList = itemList;
        notifyDataSetChanged();
    }

    public static class TrashHolder extends RecyclerView.ViewHolder {
        private final TextView nameItem, timeOfItem, sizeItem, createDateItem;
        private final SwipeRevealLayout swipeRevealLayout;
        private final ImageView permanentlyDeleteBtn, trashRestoreBtn, icAudioTrash;

        public TrashHolder(@NonNull View itemView) {
            super(itemView);

            icAudioTrash = itemView.findViewById(R.id.trash_item_icon_audio);
            nameItem = itemView.findViewById(R.id.trash_item_name_audio);
            timeOfItem = itemView.findViewById(R.id.trash_item_time_of_audio);
            sizeItem = itemView.findViewById(R.id.trash_item_size);
            createDateItem = itemView.findViewById(R.id.trash_item_create_date);
            swipeRevealLayout = itemView.findViewById(R.id.swipe_reveal_trash_layout);
            RelativeLayout relativeLayout = itemView.findViewById(R.id.item_audio_trash);
            permanentlyDeleteBtn = itemView.findViewById(R.id.trash_btn_delete);
            trashRestoreBtn = itemView.findViewById(R.id.trash_btn_restore);
        }
    }

}
