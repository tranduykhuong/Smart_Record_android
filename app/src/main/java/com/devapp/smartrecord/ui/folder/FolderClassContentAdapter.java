package com.devapp.smartrecord.ui.folder;

import static androidx.core.content.ContextCompat.startActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
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
import com.devapp.smartrecord.editmenu.adjust.AdjustActivity;
import com.devapp.smartrecord.ui.home.Audio;
import com.devapp.smartrecord.ui.home.HomeAudioAdapter;
import com.devapp.smartrecord.ui.home.HomeFragment;

import java.io.File;
import java.util.List;

public class FolderClassContentAdapter extends RecyclerView.Adapter<FolderClassContentAdapter.FolderHolder> {
    private Context context;

    private List<FolderCLassContent> listFolder;

    private OnItemClickListener listener;

    private ViewBinderHelper viewBinderHelper = new ViewBinderHelper();

    public FolderClassContentAdapter(Context context, OnItemClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
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

        holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null)
                    listener.onItemClick(holder.getAbsoluteAdapterPosition());
            }
        });
        viewBinderHelper.bind(holder.swipeRevealLayout, String.valueOf(position));
        viewBinderHelper.setOpenOnlyOne(true);

        holder.folderDeleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FolderCLassContent folder = listFolder.get(holder.getAbsoluteAdapterPosition());
                String folderName = folder.getTitle();
                File sourceFile  = new File(Environment.getExternalStorageDirectory().toString() + "/Recordings/" + folderName); // Lấy đường dẫn đầy đủ đến tệp
                File destinationFolder = new File(Environment.getExternalStorageDirectory().toString() + "/Recordings/", "TrashAudio");

                // Tạo thư mục thùng rác nếu chưa tồn tại
                if (!destinationFolder.exists()) {
                    destinationFolder.mkdirs();
                }

                //Tạo ra dialog để xác nhận xóa hay không
                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setMessage(view.getContext().getString(R.string.question_delete));
                builder.setPositiveButton(view.getContext().getString(R.string.answer_yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int j) {
                        try {
                            File destinationFile = new File(destinationFolder, folderName); // Tạo tệp đích mới
                            boolean success = sourceFile.renameTo(destinationFile);
                            if (success) { // Di chuyển tệp đến thư mục đích và kiểm tra kết quả
                                listFolder.remove(holder.getAbsoluteAdapterPosition());
                                notifyItemRemoved(holder.getAbsoluteAdapterPosition());
                                Toast.makeText(context, view.getContext().getString(R.string.announce_moved_successfully), Toast.LENGTH_SHORT).show();

                                listener.onItemClick(j);
                            } else {
                                Toast.makeText(context, view.getContext().getString(R.string.announce_moved_unsuccessfully), Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(context, view.getContext().getString(R.string.announce_moved_unsuccessfully) + ": " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                builder.setNegativeButton(view.getContext().getString(R.string.answer_no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                builder.show();
            }
        });

        holder.folderMoreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // inflate the layout of the popup window
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View popupView = inflater.inflate(R.layout.folder_context_more_menu, null);

                // create the popup window
                int width = LinearLayout.LayoutParams.MATCH_PARENT;
                int height = LinearLayout.LayoutParams.MATCH_PARENT ;
                boolean focusable = true; // lets taps outside the popup also dismiss it
                final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

                // show the popup window
                // which view you pass in doesn't matter, it is only used for the window token
                popupWindow.showAtLocation(v, Gravity.BOTTOM, 0, 0);

                popupView.setOnTouchListener((v1, event) -> {
                    popupWindow.dismiss();
                    return true;
                });

                TextView header = popupView.findViewById(R.id.folder_more_item_header);
                TextView btnRename = popupView.findViewById(R.id.folder_more_item_rename);
                TextView btnDestrouy = popupView.findViewById(R.id.folder_more_item_destroy);

                header.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        popupWindow.update();
                    }
                });

                btnRename.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        popupWindow.dismiss();
                        FolderCLassContent folder = listFolder.get(holder.getAbsoluteAdapterPosition());

                        // inflate the layout of the popup window
                        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        View popupView = inflater.inflate(R.layout.folder_modal_rename, null);

                        // create the popup window
                        int width = LinearLayout.LayoutParams.MATCH_PARENT;
                        int height = LinearLayout.LayoutParams.MATCH_PARENT ;
                        boolean focusable = true; // lets taps outside the popup also dismiss it
                        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

                        // show the popup window
                        // which view you pass in doesn't matter, it is only used for the window token
                        popupWindow.showAtLocation(v, Gravity.BOTTOM, 0, 0);

                        TextView btnDestroyMenuRename = popupView.findViewById(R.id.folder_modal_destroy_rename);
                        TextView btnOkMenuRename = popupView.findViewById(R.id.folder_modal_ok_rename);
                        EditText edtRename = popupView.findViewById(R.id.folder_rename_edt);

                        btnDestroyMenuRename.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                popupWindow.dismiss();
                            }
                        });

                        btnOkMenuRename.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                popupWindow.dismiss();

                                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                                builder.setMessage(v.getContext().getText(R.string.rename_folder_question) +edtRename.getText().toString() + "\"?");

                                builder.setPositiveButton(v.getContext().getString(R.string.answer_yes), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int j) {
                                        // Lấy đường dẫn tuyệt đối của thư mục cần đổi tên
                                        File oldDirectory = new File(Environment.getExternalStorageDirectory() + "/Recordings/" + folder.getTitle());
                                        File newDirectory = new File(Environment.getExternalStorageDirectory() + "/Recordings/" + edtRename.getText().toString());
                                        viewBinderHelper.closeLayout(String.valueOf(holder.getLayoutPosition()));
                                        folder.setTitle(edtRename.getText().toString());

                                        if (oldDirectory.renameTo(newDirectory)) {
                                            Toast.makeText(context, v.getContext().getText(R.string.rename_success), Toast.LENGTH_LONG).show();
                                        } else {
                                            Toast.makeText(context, v.getContext().getText(R.string.rename_fail), Toast.LENGTH_LONG).show();
                                        }

                                        notifyDataSetChanged();
                                    }
                                });
                                builder.setNegativeButton(v.getContext().getString(R.string.answer_no), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                    }
                                });
                                builder.show();
                            }
                        });
                    }
                });

                btnDestrouy.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        popupWindow.dismiss();
                    }
                });
            }
        });

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

    public void updateList(List<FolderCLassContent> newList) {
        listFolder = newList;
        notifyDataSetChanged();
    }

    public void filterList(List<FolderCLassContent> filteredList) {
        listFolder = filteredList;
        notifyDataSetChanged();
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public class FolderHolder extends RecyclerView.ViewHolder {
        private ImageView imageFolder;
        private TextView folderTitle, folderAmount, folderSize, folderHour;
        private SwipeRevealLayout swipeRevealLayout;

        private RelativeLayout relativeLayout;
        private ImageView folderDeleteBtn, folderMoreBtn;

        public FolderHolder(@NonNull View itemView) {
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