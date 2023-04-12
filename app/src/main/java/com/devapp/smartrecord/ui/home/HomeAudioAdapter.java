package com.devapp.smartrecord.ui.home;

import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_CANCEL;
import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import androidx.recyclerview.widget.RecyclerView;
import com.arthenica.mobileffmpeg.FFmpeg;
import com.chauthai.swipereveallayout.SwipeRevealLayout;
import com.chauthai.swipereveallayout.ViewBinderHelper;
import com.devapp.smartrecord.R;
import org.apache.commons.io.FilenameUtils;
import java.io.File;
import java.util.List;
import android.widget.EditText;


public class HomeAudioAdapter extends RecyclerView.Adapter<HomeAudioAdapter.HomeAudioHolder>{
    private final Context context;
    private List<Audio>audioList;
    private final OnItemClickListener listener;
    private final ViewBinderHelper viewBinderHelper = new ViewBinderHelper();
    public HomeAudioAdapter(Context context, OnItemClickListener listener) {
        this.listener = listener;
        this.context = context;
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
        void onItemClickConvert(int position);
    }
    @NonNull
    @Override
    public HomeAudioHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_audio_home, parent,false);
        return new HomeAudioHolder(view);
    }
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull HomeAudioHolder holder, int position) {
        Audio audio = audioList.get(position);
        if(audio == null) {
            return;
        }
        viewBinderHelper.bind(holder.swipeRevealLayout, String.valueOf(position));
        viewBinderHelper.setOpenOnlyOne(true);

        holder.homeMoreBtn.setOnClickListener(view -> {
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
            popupView.setOnTouchListener((v, event) -> {
                popupWindow.dismiss();
                return true;
            });

            TextView reminderItem = popupView.findViewById(R.id.model_more_item_reminder);
            TextView renameItem = popupView.findViewById(R.id.model_more_item_rename);
            TextView convertItem = popupView.findViewById(R.id.model_more_item_convert);
            TextView moveItem = popupView.findViewById(R.id.model_more_item_move);


            //Xử lí các lựa chọn
            //Thêm lời nhắc (báo thức)

            //Đổi tên
//            renameItem.setOnClickListener(view1 -> {
//                popupWindow.dismiss();
//
//                // Lấy thông tin tệp cần đổi tên
//                Audio item = audioList.get(holder.getAbsoluteAdapterPosition());
//                String fileName = item.getName();
//                String fileExtension = fileName.substring(fileName.lastIndexOf("."));
//                File inputFilePath = new File(Environment.getExternalStorageDirectory().toString()+"/Recordings/" + fileName);
//
//                // Tạo Dialog để nhập tên mới
//                AlertDialog.Builder builder = new AlertDialog.Builder(context);
//                builder.setTitle("Đổi tên tệp");
//
//                // Thiết kế giao diện Dialog
//                final EditText input = new EditText(context);
//                input.setText(FilenameUtils.removeExtension(fileName)); // Hiển thị tên tệp hiện tại trong input
//                input.setSelection(input.getText().length()); // Di chuyển con trỏ đến cuối input
//                builder.setView(input);
//
//                // Xử lý khi người dùng chọn OK
//                builder.setPositiveButton("OK", (dialog, which) -> {
//                    // Lấy tên mới và kiểm tra tính hợp lệ
//                    String newFileName = input.getText().toString() + fileExtension;
//                    File newFilePath = new File(Environment.getExternalStorageDirectory().toString() + "/Recordings/" + newFileName);
//                    //Kiểm tra tên mới có hợp lệ không
//                    if (newFileName.matches("^[a-zA-Z0-9_\\-.,()'\\s]+$\n")) {
//                        Toast.makeText(context, "Tên tệp không hợp lệ", Toast.LENGTH_SHORT).show();
//                        return;
//                    }
//                    if (newFileName.isEmpty()) {
//                        Toast.makeText(context, "Tên tệp không được để trống", Toast.LENGTH_SHORT).show();
//                    } else if (newFileName.equals(fileName)) {
//                        Toast.makeText(context, "Tên tệp mới phải khác tên tệp cũ", Toast.LENGTH_SHORT).show();
//                    } else if (newFilePath.exists()) {
//                        Toast.makeText(context, "Tệp đã tồn tại", Toast.LENGTH_SHORT).show();
//                    } else {
//                        //Đổi tên tệp
//                        if (inputFilePath.renameTo(newFilePath)) {
//                            Toast.makeText(context, "Đổi tên tệp thành công", Toast.LENGTH_SHORT).show();
//                            //Cập nhật lại dữ liệu danh sách tệp
//                            item.setName(newFileName);
//                            audioList.set(holder.getAbsoluteAdapterPosition(), item);
//                            notifyItemChanged(holder.getAbsoluteAdapterPosition());
//                        } else {
//                            Toast.makeText(context, "Đổi tên tệp thất bại", Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                });

                // Xử lý khi người dùng chọn Cancel
                builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

                // Hiển thị Dialog
                builder.show();
            });


        //
            //Chuyển đổi định dạng file
//            convertItem.setOnClickListener(view1 -> {
//                popupWindow.dismiss();
//
//                // inflate the layout of the popup window
//                LayoutInflater inflater1 = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//                View popupView1 = inflater1.inflate(R.layout.model_convert_audio_file, null);
//
//                // create the popup window
//                int width1 = LinearLayout.LayoutParams.MATCH_PARENT;
//                int height1 = LinearLayout.LayoutParams.MATCH_PARENT ;
//                boolean focusable1 = true; // lets taps outside the popup also dismiss it
//                final PopupWindow popupWindow1 = new PopupWindow(popupView1, width1, height1, focusable1);
//                // show the popup window
//                popupWindow1.showAtLocation(view1, Gravity.BOTTOM, 0, 0);
//
//                popupView1.setOnTouchListener((v, event) -> {
//                    popupWindow1.dismiss();
//                    return true;
//                });
//
//                TextView toM4A = popupView1.findViewById(R.id.model_convert_item_m4a);
//                TextView toAAC = popupView1.findViewById(R.id.model_convert_item_aac);
//                TextView toMP3 = popupView1.findViewById(R.id.model_convert_item_mp3);
//
//                //Chuyển đồi file mp3 hoặc aac sang m4a
//                toM4A.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view1) {
//                        Audio item = audioList.get(holder.getAbsoluteAdapterPosition());
//                        String fileName = item.getName();
//                        File inputFilePath   = new File(Environment.getExternalStorageDirectory().toString()+"/Recordings/" + fileName); // Lấy đường dẫn đầy đủ đến tệp
//                        File outputFilePath = new File(Environment.getExternalStorageDirectory().toString() + "/Recordings/" + FilenameUtils.removeExtension(fileName) + ".m4a");
//
//                        // build FFmpeg command
//                        String[] command = {"-i", String.valueOf(inputFilePath), "-c:a", "aac", "-b:a", "256k", String.valueOf(outputFilePath.getAbsolutePath())};
//                        // run FFmpeg command
//                        int rc = FFmpeg.execute(command);
//                        // handle FFmpeg execution result
//                        if (rc == RETURN_CODE_SUCCESS) {
//                            // conversion success
//                            listener.onItemClickConvert(0);
//                            Toast.makeText(context, "Convert successful", Toast.LENGTH_SHORT).show();
//                            popupWindow1.dismiss();
//                        } else if (rc == RETURN_CODE_CANCEL) {
//                            // conversion cancelled by user
//                            Toast.makeText(context, "Convert cancelled", Toast.LENGTH_SHORT).show();
//                        } else {
//                            // conversion failed
//                            Toast.makeText(context, "Convert failed", Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                });
//                toAAC.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view1) {
//                        Audio item = audioList.get(holder.getAbsoluteAdapterPosition());
//                        String fileName = item.getName();
//                        File inputFilePath   = new File(Environment.getExternalStorageDirectory().toString()+"/Recordings/" + fileName); // Lấy đường dẫn đầy đủ đến tệp
//                        File outputFilePath = new File(Environment.getExternalStorageDirectory().toString() + "/Recordings/" + FilenameUtils.removeExtension(fileName) + ".aac");
//
//                        // build FFmpeg command
//                        String[] command = {"-i", String.valueOf(inputFilePath), "-c:a", "aac", "-b:a", "256k", String.valueOf(outputFilePath.getAbsolutePath())};
//                        // run FFmpeg command
//                        int rc = FFmpeg.execute(command);
//                        // handle FFmpeg execution result
//                        if (rc == RETURN_CODE_SUCCESS) {
//                            // conversion success
//                            listener.onItemClickConvert(0);
//                            Toast.makeText(context, "Convert successful", Toast.LENGTH_SHORT).show();
//                            popupWindow1.dismiss();
//
//                        } else if (rc == RETURN_CODE_CANCEL) {
//                            // conversion cancelled by user
//                            Toast.makeText(context, "Convert cancelled", Toast.LENGTH_SHORT).show();
//                        } else {
//                            // conversion failed
//                            Toast.makeText(context, "Convert failed", Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                });
//                toMP3.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view1) {
//                        Audio item = audioList.get(holder.getAbsoluteAdapterPosition());
//                        String fileName = item.getName();
//                        File inputFilePath   = new File(Environment.getExternalStorageDirectory().toString()+"/Recordings/" + fileName); // Lấy đường dẫn đầy đủ đến tệp
//                        File outputFilePath = new File(Environment.getExternalStorageDirectory().toString() + "/Recordings/" + FilenameUtils.removeExtension(fileName) + ".mp3");
//                        Log.e("fhjsfh", String.valueOf(inputFilePath));
//                        Log.e("sdnjfsn", String.valueOf(outputFilePath));
//                        // build FFmpeg command
//                        String[] command = {"-i", String.valueOf(inputFilePath), "-acodec", "libmp3lame", "-b:a", "192k", "-f", "mp3", String.valueOf(outputFilePath)};
//                        // run FFmpeg command
//                        int rc = FFmpeg.execute(command);
//                        // handle FFmpeg execution result
//                        if (rc == RETURN_CODE_SUCCESS) {
//                            // conversion success
//                            listener.onItemClickConvert(0);
//                            popupWindow1.dismiss();
//                            Toast.makeText(context, "Convert successful", Toast.LENGTH_SHORT).show();
//                        } else if (rc == RETURN_CODE_CANCEL) {
//                            // conversion cancelled by user
//                            Toast.makeText(context, "Convert cancelled", Toast.LENGTH_SHORT).show();
//                        } else {
//                            // conversion failed
//                            Toast.makeText(context, "Convert failed", Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                });
//
//            });

            //Di chuyển đến thư mục...

        });
        holder.homeTrashBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Audio audio = audioList.get(holder.getAbsoluteAdapterPosition());
                String fileName = audio.getName();
                Log.e("TAG", "onClick: " + fileName);
                File sourceFile  = new File(Environment.getExternalStorageDirectory().toString()+"/Recordings/" + fileName); // Lấy đường dẫn đầy đủ đến tệp
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
        private final ImageView icAudioHome;
        private final TextView nameAudio;
        private final TextView timeOfAudio;
        private final TextView sizeAudio;
        private final TextView createDateAudio;
        private final SwipeRevealLayout swipeRevealLayout;
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