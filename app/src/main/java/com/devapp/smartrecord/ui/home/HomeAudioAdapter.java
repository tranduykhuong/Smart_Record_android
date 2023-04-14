package com.devapp.smartrecord.ui.home;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.arthenica.mobileffmpeg.FFmpeg;
import com.chauthai.swipereveallayout.SwipeRevealLayout;
import com.chauthai.swipereveallayout.ViewBinderHelper;
import com.devapp.smartrecord.R;
import com.devapp.smartrecord.ui.alarm.HandleDataAlarm;

import java.io.File;
import java.util.List;

import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_CANCEL;
import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

public class HomeAudioAdapter extends RecyclerView.Adapter<HomeAudioAdapter.HomeAudioHolder>{
    private final Context context;
    private List<Audio> audioList;
    private final OnItemClickListener listener;
    private final ViewBinderHelper viewBinderHelper = new ViewBinderHelper();
    public HomeAudioAdapter(Context context, OnItemClickListener listener) {
        this.listener = listener;
        this.context = context;
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
        void playSound(String name);
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

        holder.homeShareBtn.setOnClickListener(view -> {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            @SuppressLint("InflateParams") View popupView = inflater.inflate(R.layout.file_context_share_menu, null);

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
            TextView txtNameSound = popupView.findViewById(R.id.folder_share_item_title);
            TextView txtDuration = popupView.findViewById(R.id.folder_share_item_amount);

            txtNameSound.setText(audioList.get(position).getName());
            txtDuration.setText(audioList.get(position).getTimeOfAudio());

            LinearLayout messShare = popupView.findViewById(R.id.share_messenger);
            LinearLayout zaloShare = popupView.findViewById(R.id.share_zalo);
            LinearLayout outlookShare = popupView.findViewById(R.id.share_outlook);
            LinearLayout gmailShare = popupView.findViewById(R.id.share_gmail);
            LinearLayout smsShare = popupView.findViewById(R.id.share_sms);

            messShare.setOnClickListener(view12 -> {
                String filePath = Environment.getExternalStorageDirectory().toString() + "/Recordings/" + audioList.get(position).getName();

                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("*/*");
                shareIntent.setPackage("com.facebook.orca");

                File file = new File(filePath);
                Uri fileUri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".fileprovider", file);

                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);

                view12.getContext().startActivity(Intent.createChooser(shareIntent, "Share File"));
            });

            zaloShare.setOnClickListener(view12 -> {
                String filePath = Environment.getExternalStorageDirectory().toString() + "/Recordings/" + audioList.get(position).getName();

                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("*/*");
                shareIntent.setPackage("com.zing.zalo");

                File file = new File(filePath);
                Uri fileUri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".fileprovider", file);

                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);

                view12.getContext().startActivity(Intent.createChooser(shareIntent, "Share File"));
            });

            outlookShare.setOnClickListener(view12 -> {
                String filePath = Environment.getExternalStorageDirectory().toString() + "/Recordings/" + audioList.get(position).getName();

                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("*/*");
                shareIntent.setPackage("com.microsoft.office.outlook");

                File file = new File(filePath);
                Uri fileUri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".fileprovider", file);

                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);

                view12.getContext().startActivity(Intent.createChooser(shareIntent, "Share File"));
            });

            gmailShare.setOnClickListener(view12 -> {
                String filePath = Environment.getExternalStorageDirectory().toString() + "/Recordings/" + audioList.get(position).getName();

                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("*/*");
                shareIntent.setPackage("com.google.android.gm");

                File file = new File(filePath);
                Uri fileUri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".fileprovider", file);

                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);

                view12.getContext().startActivity(Intent.createChooser(shareIntent, "Share File"));
            });

            smsShare.setOnClickListener(view12 -> {
                String filePath = Environment.getExternalStorageDirectory().toString() + "/Recordings/" + audioList.get(position).getName();

                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("*/*");
                shareIntent.setPackage("com.android.mms");

                File file = new File(filePath);
                Uri fileUri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".fileprovider", file);

                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);

                view12.getContext().startActivity(Intent.createChooser(shareIntent, "Share File"));
            });


        });
        holder.homeMoreBtn.setOnClickListener(view -> {
            // inflate the layout of the popup window
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            @SuppressLint("InflateParams") View popupView = inflater.inflate(R.layout.model_more_of_item_audio, null);

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

            //Lời nhắc
            reminderItem.setOnClickListener(view13 -> {
                Audio item = audioList.get(holder.getAbsoluteAdapterPosition());
                String fileName = item.getName();
                String fileExtension = fileName.substring(fileName.lastIndexOf("."));
                File inputFilePath = new File(Environment.getExternalStorageDirectory().toString()+ "/Recordings/" + fileName);
                //CÁI PATH LÀ inputFilePath.getAbsolutePath() nha Duy Khương
                HandleDataAlarm handleDataAlarm = new HandleDataAlarm(context);
                handleDataAlarm.addReminder(inputFilePath.getAbsolutePath());
//                Toast.makeText(context.getApplicationContext(), String.valueOf(inputFilePath.getAbsolutePath()), Toast.LENGTH_LONG).show();
            });

            //Đổi tên
            renameItem.setOnClickListener(view1 -> {
                popupWindow.dismiss();

                // Lấy thông tin tệp cần đổi tên
                Audio item = audioList.get(holder.getAbsoluteAdapterPosition());
                String fileName = item.getName();
                String fileExtension = fileName.substring(fileName.lastIndexOf("."));
                File inputFilePath = new File(Environment.getExternalStorageDirectory().toString()+ "/Recordings/" + fileName);

                // Tạo Dialog để nhập tên mới
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Đổi tên tệp");

                // Thiết kế giao diện Dialog
                final EditText input = new EditText(context);
                input.setText(FilenameUtils.removeExtension(fileName)); // Hiển thị tên tệp hiện tại trong input
                input.setSelection(input.getText().length()); // Di chuyển con trỏ đến cuối input
                builder.setView(input);

                // Xử lý khi người dùng chọn OK
                builder.setPositiveButton("OK", (dialog, which) -> {
                    // Lấy tên mới và kiểm tra tính hợp lệ
                    String newFileName = input.getText().toString() + fileExtension;
                    File newFilePath = new File(Environment.getExternalStorageDirectory().toString() + "/Recordings/" + newFileName);
                    //Kiểm tra tên mới có hợp lệ không
                    if (newFileName.matches("^[a-zA-Z0-9_\\-.,()'\\s]+$\n")) {
                        Toast.makeText(context, "Tên tệp không hợp lệ", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (newFileName.isEmpty()) {
                        Toast.makeText(context, "Tên tệp không được để trống", Toast.LENGTH_SHORT).show();
                    } else if (newFileName.equals(fileName)) {
                        Toast.makeText(context, "Tên tệp mới phải khác tên tệp cũ", Toast.LENGTH_SHORT).show();
                    } else if (newFilePath.exists()) {
                        Toast.makeText(context, "Tệp đã tồn tại", Toast.LENGTH_SHORT).show();
                    } else {
                        //Đổi tên tệp
                        if (inputFilePath.renameTo(newFilePath)) {
                            Toast.makeText(context, "Đổi tên tệp thành công", Toast.LENGTH_SHORT).show();
                            //Cập nhật lại dữ liệu danh sách tệp
                            item.setName(newFileName);
                            audioList.set(holder.getAbsoluteAdapterPosition(), item);
                            notifyItemChanged(holder.getAbsoluteAdapterPosition());
                        } else {
                            Toast.makeText(context, "Đổi tên tệp thất bại", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                // Xử lý khi người dùng chọn Cancel
                builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

                // Hiển thị Dialog
                builder.show();
            });

            //Di chuyển đến thư mục...

            //Chuyển đổi định dạng file
            convertItem.setOnClickListener(view1 -> {
                popupWindow.dismiss();

                LayoutInflater inflater1 = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                @SuppressLint("InflateParams") View popupView1 = inflater1.inflate(R.layout.model_convert_audio_file, null);

                int width1 = LinearLayout.LayoutParams.MATCH_PARENT;
                int height1 = LinearLayout.LayoutParams.MATCH_PARENT ;
                boolean focusable1 = true;
                final PopupWindow popupWindow1 = new PopupWindow(popupView1, width1, height1, focusable1);
                popupWindow1.showAtLocation(view1, Gravity.BOTTOM, 0, 0);

                popupView1.setOnTouchListener((v, event) -> {
                    popupWindow1.dismiss();
                    return true;
                });

                TextView toM4A = popupView1.findViewById(R.id.model_convert_item_m4a);
                TextView toAAC = popupView1.findViewById(R.id.model_convert_item_aac);
                TextView toMP3 = popupView1.findViewById(R.id.model_convert_item_mp3);

                toM4A.setOnClickListener(view11 -> {
                    Audio item = audioList.get(holder.getAbsoluteAdapterPosition());
                    String fileName = item.getName();
                    File inputFilePath   = new File(Environment.getExternalStorageDirectory().toString()+"/Recordings/" + fileName); // Lấy đường dẫn đầy đủ đến tệp
                    File outputFilePath = new File(Environment.getExternalStorageDirectory().toString() + "/Recordings/" + FilenameUtils.removeExtension(fileName) + ".m4a");

                    String[] command = {"-i", String.valueOf(inputFilePath), "-c:a", "aac", "-b:a", "256k", String.valueOf(outputFilePath.getAbsolutePath())};
                    int rc = FFmpeg.execute(command);
                    if (rc == RETURN_CODE_SUCCESS) {
                        listener.onItemClickConvert(0);
                        Toast.makeText(context, "Convert successful", Toast.LENGTH_SHORT).show();
                        popupWindow1.dismiss();
                    } else if (rc == RETURN_CODE_CANCEL) {
                        Toast.makeText(context, "Convert cancelled", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Convert failed", Toast.LENGTH_SHORT).show();
                    }
                });

                toAAC.setOnClickListener(view112 -> {
                    Audio item = audioList.get(holder.getAbsoluteAdapterPosition());
                    String fileName = item.getName();
                    File inputFilePath   = new File(Environment.getExternalStorageDirectory().toString()+"/Recordings/" + fileName); // Lấy đường dẫn đầy đủ đến tệp
                    File outputFilePath = new File(Environment.getExternalStorageDirectory().toString() + "/Recordings/" + FilenameUtils.removeExtension(fileName) + ".aac");

                    String[] command = {"-i", String.valueOf(inputFilePath), "-c:a", "aac", "-b:a", "256k", outputFilePath.getAbsolutePath()};
                    int rc = FFmpeg.execute(command);
                    if (rc == RETURN_CODE_SUCCESS) {
                        // conversion success
                        listener.onItemClickConvert(0);
                        Toast.makeText(context, "Convert successful", Toast.LENGTH_SHORT).show();
                        popupWindow1.dismiss();

                    } else if (rc == RETURN_CODE_CANCEL) {
                        Toast.makeText(context, "Convert cancelled", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Convert failed", Toast.LENGTH_SHORT).show();
                    }
                });

                toMP3.setOnClickListener(view113 -> {
                    Audio item = audioList.get(holder.getAbsoluteAdapterPosition());
                    String fileName = item.getName();
                    File inputFilePath   = new File(Environment.getExternalStorageDirectory().toString()+"/Recordings/" + fileName); // Lấy đường dẫn đầy đủ đến tệp
                    File outputFilePath = new File(Environment.getExternalStorageDirectory().toString() + "/Recordings/" + FilenameUtils.removeExtension(fileName) + ".mp3");
                    String[] command = {"-i", String.valueOf(inputFilePath), "-acodec", "libmp3lame", "-b:a", "192k", "-f", "mp3", String.valueOf(outputFilePath)};
                    int rc = FFmpeg.execute(command);
                    if (rc == RETURN_CODE_SUCCESS) {
                        listener.onItemClickConvert(0);
                        popupWindow1.dismiss();
                        Toast.makeText(context, "Convert successful", Toast.LENGTH_SHORT).show();
                    } else if (rc == RETURN_CODE_CANCEL) {
                        Toast.makeText(context, "Convert cancelled", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Convert failed", Toast.LENGTH_SHORT).show();
                    }
                });
            });

            //Di chuyển đến thư mục...


        });
        holder.homeTrashBtn.setOnClickListener(view -> {

            Audio audio1 = audioList.get(holder.getAbsoluteAdapterPosition());
            String fileNameTrash = audio1.getName();
            Log.e("TAG", "onClick: " + fileNameTrash);
            File sourceFile  = new File(Environment.getExternalStorageDirectory() + "/Recordings/" + fileNameTrash); // Lấy đường dẫn đầy đủ đến tệp
            File destinationFolder = new File(Environment.getExternalStorageDirectory() + "/Recordings/TrashAudio/");

            // Tạo thư mục thùng rác nếu chưa tồn tại
            if (!destinationFolder.exists()) {
                destinationFolder.mkdir();
                Toast.makeText(context, "CC", Toast.LENGTH_SHORT).show();
            }
            else {

            }

            //Tạo ra dialog để xác nhận xóa hay không
            AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
            builder.setMessage(view.getContext().getString(R.string.question_delete));
            builder.setPositiveButton(view.getContext().getString(R.string.answer_yes), (dialogInterface, j) -> {
                try {
                    if (sourceFile.exists()) { //Kiểm tra tệp có tồn tại hay không
                        File destinationFile = new File(destinationFolder, fileNameTrash); // Tạo tệp đích mới
                        if (sourceFile.renameTo(destinationFile)) {// Di chuyển tệp đến thư mục đích và kiểm tra kết quả
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
            });
            builder.setNegativeButton(view.getContext().getString(R.string.answer_no), (dialogInterface, i) -> {

            });
            builder.show();
        });

        holder.icAudioHome.setImageResource(audio.getImage());
        holder.icAudioHome.setOnClickListener(view -> listener.playSound(audioList.get(holder.getAbsoluteAdapterPosition()).getName()));
        holder.nameAudio.setText(audio.getName());
        holder.timeOfAudio.setText(audio.getTimeOfAudio());
        holder.sizeAudio.setText(audio.getSize());
        holder.createDateAudio.setText(audio.getCreateDate());
        holder.relativeLayout.setOnClickListener(view -> listener.playSound(audioList.get(holder.getAbsoluteAdapterPosition()).getName()));

    }
    @Override
    public int getItemCount() {
        if (audioList != null) {
            return audioList.size();
        }
        return 0;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setData(List<Audio> audioList) {
        this.audioList = audioList;
        notifyDataSetChanged();
    }

    public static class HomeAudioHolder extends RecyclerView.ViewHolder {
        private final ImageView icAudioHome;
        private final TextView nameAudio, timeOfAudio, sizeAudio, createDateAudio;
        private final SwipeRevealLayout swipeRevealLayout;
        private final ImageView homeMoreBtn, homeShareBtn,  homeTrashBtn;
        private final RelativeLayout relativeLayout;


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