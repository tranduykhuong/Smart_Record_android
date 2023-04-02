package com.devapp.smartrecord.ui.home;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;
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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.chauthai.swipereveallayout.SwipeRevealLayout;
import com.chauthai.swipereveallayout.ViewBinderHelper;
import com.devapp.smartrecord.HomeActivity;
import com.devapp.smartrecord.R;

import java.io.File;
import java.util.List;

public class HomeAudioAdapter extends RecyclerView.Adapter<HomeAudioAdapter.HomeAudioHolder>{
    private Context context;
    private List<Audio>audioList;
    private OnItemClickListener listener;
    private ViewBinderHelper viewBinderHelper = new ViewBinderHelper();
    public HomeAudioAdapter(Context context, OnItemClickListener listener) {
        this.listener = listener;
        this.context = context;
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }
    @NonNull
    @Override
    public HomeAudioHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_audio_home, parent,false);
        return new HomeAudioHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull HomeAudioHolder holder, int position) {
        Audio audio = audioList.get(position);
        if(audio == null) {
            return;
        }
        viewBinderHelper.bind(holder.swipeRevealLayout, String.valueOf(position));
        viewBinderHelper.setOpenOnlyOne(true);

        holder.homeMoreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // inflate the layout of the popup window
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View popupView = inflater.inflate(R.layout.model_more_of_item_audio, null);

                // create the popup window
                int width = LinearLayout.LayoutParams.MATCH_PARENT;
                int height = LinearLayout.LayoutParams.MATCH_PARENT ;
                boolean focusable = true; // lets taps outside the popup also dismiss it
                final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
                // show the popup window
                popupWindow.showAtLocation(view, Gravity.BOTTOM, 0, 0);

                // dismiss the popup window when touched
                popupView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        popupWindow.dismiss();
                        return true;
                    }
                });

                TextView reminderItem = popupView.findViewById(R.id.model_more_item_reminder);
                TextView renameItem = popupView.findViewById(R.id.model_more_item_rename);
                TextView convertItem = popupView.findViewById(R.id.model_more_item_convert);
                TextView moveItem = popupView.findViewById(R.id.model_more_item_move);


                //Xử lí các lựa chọn
                //Thêm lời nhắc (báo thức)

                //Đổi tên

                //Chuyển đổi định dạng file
                convertItem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        popupWindow.dismiss();

                        // inflate the layout of the popup window
                        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        View popupView1 = inflater.inflate(R.layout.model_convert_audio_file, null);

                        // create the popup window
                        int width = LinearLayout.LayoutParams.MATCH_PARENT;
                        int height = LinearLayout.LayoutParams.MATCH_PARENT ;
                        boolean focusable = true; // lets taps outside the popup also dismiss it
                        final PopupWindow popupWindow1 = new PopupWindow(popupView1, width, height, focusable);
                        // show the popup window
                        popupWindow1.showAtLocation(view, Gravity.BOTTOM, 0, 0);

                        popupView1.setOnTouchListener(new View.OnTouchListener() {
                            @Override
                            public boolean onTouch(View v, MotionEvent event) {
                                popupWindow1.dismiss();
                                return true;
                            }
                        });

                        TextView toM4A = popupView1.findViewById(R.id.model_convert_item_m4a);
                        TextView toAAC = popupView1.findViewById(R.id.model_convert_item_m4a);
                        TextView toMP3 = popupView1.findViewById(R.id.model_convert_item_m4a);



                    }
                });

                //Di chuyển đến thư mục...


            }
        });
        holder.homeTrashBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Audio audio = audioList.get(holder.getAbsoluteAdapterPosition());
                String fileName = audio.getName();
                Log.e("TAG", "onClick: " + fileName);
                File sourceFile  = new File(Environment.getExternalStorageDirectory().toString()+"/Recorder/" + fileName); // Lấy đường dẫn đầy đủ đến tệp
                File destinationFolder = new File(Environment.getExternalStorageDirectory().toString()+"/TrashAudio/");

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
                            if (sourceFile.exists()) { //Kiểm tra tệp có tồn tại hay không
                                File destinationFile = new File(destinationFolder, fileName); // Tạo tệp đích mới
                                if (sourceFile.renameTo(destinationFile)) { // Di chuyển tệp đến thư mục đích và kiểm tra kết quả
                                    audioList.remove(holder.getAbsoluteAdapterPosition());
                                    notifyItemRemoved(holder.getAbsoluteAdapterPosition());
                                    Toast.makeText(context, view.getContext().getString(R.string.announce_moved_successfully), Toast.LENGTH_SHORT).show();

                                    listener.onItemClick(j);
                                }else {
                                    Toast.makeText(context, view.getContext().getString(R.string.announce_moved_unsuccessfully), Toast.LENGTH_SHORT).show();
                                }
                            }
                            else {
                                Toast.makeText(context, view.getContext().getString(R.string.annouce_file_not_exist), Toast.LENGTH_SHORT).show();
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

        holder.icAudioHome.setImageResource(audio.getImage());
        holder.nameAudio.setText(audio.getName());
        holder.timeOfAudio.setText(audio.getTimeOfAudio());
        holder.sizeAudio.setText(audio.getSize());
        holder.createDateAudio.setText(audio.getCreateDate());
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
    public class HomeAudioHolder extends RecyclerView.ViewHolder {
        private ImageView icAudioHome;
        private TextView nameAudio, timeOfAudio, sizeAudio, createDateAudio;
        private SwipeRevealLayout swipeRevealLayout;
        private RelativeLayout relativeLayout;
        private ImageView homeTrashBtn, homeMoreBtn, homeShareBtn;
        public HomeAudioHolder(@NonNull View itemView) {
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