package com.devapp.smartrecord.ui.folder;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
<<<<<<< HEAD
import android.widget.ImageButton;
=======
>>>>>>> 9f187e4 (Chính: update logic UI folder and new page UI Adjust)
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class FolderClassContentAdapter extends RecyclerView.Adapter<FolderClassContentAdapter.FolderHolder> {
    private Context context;

    private List<FolderCLassContent> listFolder;

    private ViewBinderHelper viewBinderHelper = new ViewBinderHelper();

    public FolderClassContentAdapter(Context context) {
        this.context = context;
    }

    private boolean mSortByNameAscending = true;

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

        viewBinderHelper.bind(holder.swipeRevealLayout, String.valueOf(position));
        viewBinderHelper.setOpenOnlyOne(true);

//        holder.bind(folder);
//        holder.filterName.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                sortByName();
//                notifyDataSetChanged();
//            }
//        });

        holder.folderShareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // inflate the layout of the popup window
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View popupView = inflater.inflate(R.layout.folder_context_share_menu, null);

                // create the popup window
                int width = LinearLayout.LayoutParams.MATCH_PARENT;
                int height = LinearLayout.LayoutParams.MATCH_PARENT ;
                boolean focusable = true; // lets taps outside the popup also dismiss it
                final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

                // show the popup window
                // which view you pass in doesn't matter, it is only used for the window token
                popupWindow.showAtLocation(view, Gravity.BOTTOM, 0, 0);

                // dismiss the popup window when touched
                popupView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        popupWindow.dismiss();
                        return true;
                    }
                });
            }
        });

        holder.folderMoreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // inflate the layout of the popup window
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View popupView = inflater.inflate(R.layout.activity_adjust, null);

                // create the popup window
                int width = LinearLayout.LayoutParams.MATCH_PARENT;
                int height = LinearLayout.LayoutParams.MATCH_PARENT ;
                boolean focusable = true; // lets taps outside the popup also dismiss it
                final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

                // show the popup window
                // which view you pass in doesn't matter, it is only used for the window token
                popupWindow.showAtLocation(view, Gravity.BOTTOM, 0, 0);

                // dismiss the popup window when touched
                popupView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        popupWindow.dismiss();
                        return true;
                    }
                });
            }
        });

        holder.folderDeleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listFolder.remove(holder.getBindingAdapterPosition());
                notifyItemRemoved(holder.getBindingAdapterPosition());
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

    public class FolderHolder extends RecyclerView.ViewHolder {
        private ImageView imageFolder;
        private TextView folderTitle, folderAmount, folderSize, folderHour;
        private SwipeRevealLayout swipeRevealLayout;

        private RelativeLayout relativeLayout;
        private ImageView folderDeleteBtn, folderMoreBtn, folderShareBtn;

        public FolderHolder(@NonNull View itemView) {
            super(itemView);
            imageFolder = itemView.findViewById(R.id.folder_item_image);
            folderTitle = itemView.findViewById(R.id.folder_item_title);
            folderAmount = itemView.findViewById(R.id.folder_item_amount);
            folderSize = itemView.findViewById(R.id.folder_item_size);
            folderHour = itemView.findViewById(R.id.folder_item_hours);
            swipeRevealLayout = itemView.findViewById(R.id.swipe_reveal_layout);
            relativeLayout = itemView.findViewById(R.id.folder_item_row);
            folderMoreBtn = itemView.findViewById(R.id.folder_btn_more);
            folderShareBtn = itemView.findViewById(R.id.folder_btn_share);
            folderDeleteBtn = itemView.findViewById(R.id.folder_btn_delete);
        }
    }
}
