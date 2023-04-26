package com.devapp.smartrecord.ui.folder;

import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_CANCEL;
import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.icu.text.DecimalFormat;
import android.icu.text.SimpleDateFormat;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.arthenica.mobileffmpeg.FFmpeg;
import com.chauthai.swipereveallayout.SwipeRevealLayout;
import com.chauthai.swipereveallayout.ViewBinderHelper;
import com.devapp.smartrecord.R;
import com.devapp.smartrecord.editmenu.insertion.InsertionAudioAdapter;
import com.devapp.smartrecord.ui.alarm.HandleDataAlarm;
import com.devapp.smartrecord.ui.home.Audio;
import com.devapp.smartrecord.ui.home.FolderModalAdapter;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FileInFolderAdapter extends RecyclerView.Adapter<FileInFolderAdapter.FileInFolderHolder> implements FolderModalAdapter.OnItemClickListener{
    private Context context;
    private List<Audio> audioList;
    private OnItemClickListener listener;
    private ViewBinderHelper viewBinderHelper = new ViewBinderHelper();
    private String folderName;
    private ArrayList<FolderCLassContent> listFolder;
    private RecyclerView rcvFolder;
    private FolderModalAdapter adapterFolder;
    private Double sizeFolder = 0.0;
    private String fileNameCurrent;
    private PopupWindow popupWindowMove;

    public FileInFolderAdapter(Context context, OnItemClickListener listener) {
        this.listener = listener;
        this.context = context;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void onItemClickFile(String name, int position);
        void onItemClickConvert(int position);
        void onItemClick(int position);
    }
    @NonNull
    @Override
    public FileInFolderHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_audio_home, parent,false);
        return new FileInFolderHolder(view);
    }

    @Override
    public void onItemClickModal(int position){
        FolderCLassContent folder = listFolder.get(position);
        String folderName = folder.getTitle();
        File sourceFile  = new File(Environment.getExternalStorageDirectory() + "/Recordings/" + folderName + "/" + fileNameCurrent); // Lấy đường dẫn đầy đủ đến tệp
        File destinationFolder = new File(Environment.getExternalStorageDirectory() + "/Recordings/" + folderName);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(context.getString(R.string.question_move_folder));
        builder.setPositiveButton(context.getString(R.string.answer_yes), (dialogInterface, j) -> {
            try {
                if (sourceFile.exists()) { //Kiểm tra tệp có tồn tại hay không
                    File destinationFile = new File(destinationFolder, fileNameCurrent); // Tạo tệp đích mới
                    if (sourceFile.renameTo(destinationFile)) {// Di chuyển tệp đến thư mục đích và kiểm tra kết quả
                        audioList.remove(position);
                        notifyItemRemoved(position);
                        Toast.makeText(context, context.getString(R.string.announce_moved_folder_successfully), Toast.LENGTH_SHORT).show();

                        popupWindowMove.dismiss();
                    }else {
                        Toast.makeText(context, context.getString(R.string.announce_moved_folder_fail), Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    Toast.makeText(context, context.getString(R.string.annouce_file_not_exist), Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(context, context.getString(R.string.announce_moved_folder_fail) + ": " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton(context.getString(R.string.answer_no), (dialogInterface, i) -> {

        });
        builder.show();
    }

    @Override
    public void onBindViewHolder(@NonNull FileInFolderHolder holder, int position) {
        Audio audio = audioList.get(position);
        if(audio == null) {
            return;
        }
        viewBinderHelper.bind(holder.swipeRevealLayout, String.valueOf(position));
        viewBinderHelper.setOpenOnlyOne(true);

        holder.icAudioHome.setImageResource(audio.getImage());
        holder.nameAudio.setText(audio.getName());
        holder.timeOfAudio.setText(audio.getTimeOfAudio());
        holder.sizeAudio.setText(audio.getSize());
        holder.createDateAudio.setText(audio.getCreateDate());

        holder.relativeLayout.setOnClickListener(view -> {
            Toast.makeText(context, audioList.get(position).getName(), Toast.LENGTH_SHORT).show();
            if (listener != null)
                listener.onItemClickFile(audioList.get(position).getName(), holder.getAbsoluteAdapterPosition());
        });

        listFolder = new ArrayList<>();
        listFolder = (ArrayList<FolderCLassContent>) getListFolder();
        adapterFolder = new FolderModalAdapter(context, this);

//        totalSizeFolder.setText(decimalFormat.format(sizeFolder));
        adapterFolder.setData(listFolder);

//        holder.homeShareBtn.setOnClickListener(view -> {
//            LayoutInflater inflater1 = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//            View popupView1 = inflater1.inflate(R.layout.file_context_share_menu, null);
//
//            // create the popup window
//            int width = LinearLayout.LayoutParams.MATCH_PARENT;
//            int height = LinearLayout.LayoutParams.MATCH_PARENT ;
//            boolean focusable = true; // lets taps outside the popup also dismiss it
//            final PopupWindow popupWindow = new PopupWindow(popupView1, width, height, focusable);
//            // show the popup window
//            popupWindow.showAtLocation(view, Gravity.BOTTOM, 0, 0);
//
//            // dismiss the popup window when touched
//            popupView1.setOnTouchListener((v, event) -> {
//                popupWindow.dismiss();
//                return true;
//            });
//            TextView txtNameSound = popupView1.findViewById(R.id.folder_share_item_title);
//            TextView txtDuration = popupView1.findViewById(R.id.folder_share_item_amount);
//
//            txtNameSound.setText(audioList.get(position).getName());
//            txtDuration.setText(audioList.get(position).getTimeOfAudio());
//
//            LinearLayout messShare = popupView1.findViewById(R.id.share_messenger);
//            LinearLayout zaloShare = popupView1.findViewById(R.id.share_zalo);
//            LinearLayout outlookShare = popupView1.findViewById(R.id.share_outlook);
//            LinearLayout gmailShare = popupView1.findViewById(R.id.share_gmail);
//            LinearLayout smsShare = popupView1.findViewById(R.id.share_sms);
//
//            messShare.setOnClickListener(view12 -> {
//                String filePath = Environment.getExternalStorageDirectory().toString() + "/Recordings/"+ folderName + "/" + audioList.get(position).getName();
//
//                Intent shareIntent = new Intent(Intent.ACTION_SEND);
//                shareIntent.setType("*/*");
//                shareIntent.setPackage("com.facebook.orca");
//
//                File file = new File(filePath);
//                Uri fileUri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".fileprovider", file);
//
//                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
//
//                view12.getContext().startActivity(Intent.createChooser(shareIntent, "Share File"));
//            });
//
//            zaloShare.setOnClickListener(view12 -> {
//                String filePath = Environment.getExternalStorageDirectory().toString() + "/Recordings/"+ folderName + "/" + audioList.get(position).getName();
//
//                Intent shareIntent = new Intent(Intent.ACTION_SEND);
//                shareIntent.setType("*/*");
//                shareIntent.setPackage("com.zing.zalo");
//
//                File file = new File(filePath);
//                Uri fileUri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".fileprovider", file);
//
//                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
//
//                view12.getContext().startActivity(Intent.createChooser(shareIntent, "Share File"));
//            });
//
//            outlookShare.setOnClickListener(view12 -> {
//                String filePath = Environment.getExternalStorageDirectory().toString() + "/Recordings/"+ folderName + "/" + audioList.get(position).getName();
//
//                Intent shareIntent = new Intent(Intent.ACTION_SEND);
//                shareIntent.setType("*/*");
//                shareIntent.setPackage("com.microsoft.office.outlook");
//
//                File file = new File(filePath);
//                Uri fileUri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".fileprovider", file);
//
//                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
//
//                view12.getContext().startActivity(Intent.createChooser(shareIntent, "Share File"));
//            });
//
//            gmailShare.setOnClickListener(view12 -> {
//                String filePath = Environment.getExternalStorageDirectory().toString() + "/Recordings/"+ folderName + "/" + audioList.get(position).getName();
//
//                Intent shareIntent = new Intent(Intent.ACTION_SEND);
//                shareIntent.setType("*/*");
//                shareIntent.setPackage("com.google.android.gm");
//
//                File file = new File(filePath);
//                Uri fileUri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".fileprovider", file);
//
//                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
//
//                view12.getContext().startActivity(Intent.createChooser(shareIntent, "Share File"));
//            });
//
//            smsShare.setOnClickListener(view12 -> {
//                String filePath = Environment.getExternalStorageDirectory().toString() + "/Recordings/"+ folderName + "/" + audioList.get(position).getName();
//
//                Intent shareIntent = new Intent(Intent.ACTION_SEND);
//                shareIntent.setType("*/*");
//                shareIntent.setPackage("com.android.mms");
//
//                File file = new File(filePath);
//                Uri fileUri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".fileprovider", file);
//
//                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
//
//                view12.getContext().startActivity(Intent.createChooser(shareIntent, "Share File"));
//            });
//
//
//        });
//        holder.homeMoreBtn.setOnClickListener(view -> {
//            fileNameCurrent = audioList.get(holder.getAbsoluteAdapterPosition()).getName();
//
//            // inflate the layout of the popup window
//            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//            @SuppressLint("InflateParams") View popupView = inflater.inflate(R.layout.model_more_of_item_audio, null);
//
//            // create the popup window
//            int width = LinearLayout.LayoutParams.MATCH_PARENT;
//            int height = LinearLayout.LayoutParams.MATCH_PARENT ;
//            boolean focusable = true; // lets taps outside the popup also dismiss it
//            final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
//            // show the popup window
//            popupWindow.showAtLocation(view, Gravity.BOTTOM, 0, 0);
//
//            // dismiss the popup window when touched
//            popupView.setOnTouchListener((v, event) -> {
//                popupWindow.dismiss();
//                return true;
//            });
//
//            TextView reminderItem = popupView.findViewById(R.id.model_more_item_reminder);
//            TextView renameItem = popupView.findViewById(R.id.model_more_item_rename);
//            TextView convertItem = popupView.findViewById(R.id.model_more_item_convert);
//            TextView moveItem = popupView.findViewById(R.id.model_more_item_move);
//
//            //Lời nhắc
//            reminderItem.setOnClickListener(view13 -> {
//                Audio item = audioList.get(holder.getAbsoluteAdapterPosition());
//                String fileName = item.getName();
//                String fileExtension = fileName.substring(fileName.lastIndexOf("."));
//                File inputFilePath = new File(Environment.getExternalStorageDirectory().toString()+ "/Recordings/"+ folderName + "/" + fileName);
//                //CÁI PATH LÀ inputFilePath.getAbsolutePath() nha Duy Khương
//                HandleDataAlarm handleDataAlarm = HandleDataAlarm.getInstance(context);
//                handleDataAlarm.addReminder(inputFilePath.getAbsolutePath());
////                Toast.makeText(context.getApplicationContext(), String.valueOf(inputFilePath.getAbsolutePath()), Toast.LENGTH_LONG).show();
//            });
//
//            //Đổi tên
//            renameItem.setOnClickListener(view1 -> {
//                popupWindow.dismiss();
//
//                // Lấy thông tin tệp cần đổi tên
//                Audio item = audioList.get(holder.getAbsoluteAdapterPosition());
//                String fileNameRe = item.getName();
//                String fileExtension = fileNameRe.substring(fileNameRe.lastIndexOf("."));
//                File inputFilePath = new File(Environment.getExternalStorageDirectory().toString()+ "/Recordings/"+ folderName + "/" + fileNameRe);
//
//                // Tạo Dialog để nhập tên mới
//                AlertDialog.Builder builder = new AlertDialog.Builder(context);
//                builder.setTitle("Đổi tên tệp");
//
//                // Thiết kế giao diện Dialog
//                final EditText input = new EditText(context);
//                input.setText(FilenameUtils.removeExtension(fileNameRe)); // Hiển thị tên tệp hiện tại trong input
//                input.setSelection(input.getText().length()); // Di chuyển con trỏ đến cuối input
//                builder.setView(input);
//
//                // Xử lý khi người dùng chọn OK
//                builder.setPositiveButton("OK", (dialog, which) -> {
//                    // Lấy tên mới và kiểm tra tính hợp lệ
//                    String newFileName = input.getText().toString() + fileExtension;
//                    File newFilePath = new File(Environment.getExternalStorageDirectory().toString() + "/Recordings/"+ folderName + "/" + newFileName);
//                    //Kiểm tra tên mới có hợp lệ không
//                    if (newFileName.matches("^[a-zA-Z0-9_\\-.,()'\\s]+$\n")) {
//                        Toast.makeText(context, "Tên tệp không hợp lệ", Toast.LENGTH_SHORT).show();
//                        return;
//                    }
//                    if (newFileName.isEmpty()) {
//                        Toast.makeText(context, "Tên tệp không được để trống", Toast.LENGTH_SHORT).show();
//                    } else if (newFileName.equals(fileNameRe)) {
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
//
//                // Xử lý khi người dùng chọn Cancel
//                builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
//
//                // Hiển thị Dialog
//                builder.show();
//            });
//
//            //Di chuyển đến thư mục...
//            moveItem.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    popupWindow.dismiss();
//
//                    // inflate the layout of the popup window
//                    LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//                    @SuppressLint("InflateParams") View popupView = inflater.inflate(R.layout.modal_move_file_to_folder, null);
//
//                    // create the popup window
//                    int width = LinearLayout.LayoutParams.MATCH_PARENT;
//                    int height = LinearLayout.LayoutParams.MATCH_PARENT ;
//                    boolean focusable = true; // lets taps outside the popup also dismiss it
//                    popupWindowMove = new PopupWindow(popupView, width, height, focusable);
//                    // show the popup window
//                    popupWindowMove.showAtLocation(popupView, Gravity.BOTTOM, 0, 0);
//
//                    TextView btnMoveFileDestroy = popupView.findViewById(R.id.list_folder_move_modal_destroy);
//                    btnMoveFileDestroy.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            popupWindowMove.dismiss();
//                        }
//                    });
//
//                    rcvFolder = popupView.findViewById(R.id.list_folder_move);
//                    rcvFolder.setLayoutManager(new LinearLayoutManager(context));
//                    rcvFolder.setAdapter(adapterFolder);
//                }
//            });
//
//            //Chuyển đổi định dạng file
//            convertItem.setOnClickListener(view1 -> {
//                popupWindow.dismiss();
//
//                LayoutInflater inflater1 = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//                @SuppressLint("InflateParams") View popupView1 = inflater1.inflate(R.layout.model_convert_audio_file, null);
//
//                int width1 = LinearLayout.LayoutParams.MATCH_PARENT;
//                int height1 = LinearLayout.LayoutParams.MATCH_PARENT ;
//                boolean focusable1 = true;
//                final PopupWindow popupWindow1 = new PopupWindow(popupView1, width1, height1, focusable1);
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
//                toM4A.setOnClickListener(view11 -> {
//                    Audio item = audioList.get(holder.getAbsoluteAdapterPosition());
//                    String fileName = item.getName();
//                    File inputFilePath   = new File(Environment.getExternalStorageDirectory().toString()+"/Recordings/"+ folderName + "/" + fileName); // Lấy đường dẫn đầy đủ đến tệp
//                    File outputFilePath = new File(Environment.getExternalStorageDirectory().toString() + "/Recordings/"+ folderName + "/" + FilenameUtils.removeExtension(fileName) + ".m4a");
//
//                    String[] command = {"-i", String.valueOf(inputFilePath), "-c:a", "aac", "-b:a", "256k", String.valueOf(outputFilePath.getAbsolutePath())};
//                    int rc = FFmpeg.execute(command);
//                    if (rc == RETURN_CODE_SUCCESS) {
//                        listener.onItemClickConvert(0);
//                        Toast.makeText(context, "Convert successful", Toast.LENGTH_SHORT).show();
//                        popupWindow1.dismiss();
//                    } else if (rc == RETURN_CODE_CANCEL) {
//                        Toast.makeText(context, "Convert cancelled", Toast.LENGTH_SHORT).show();
//                    } else {
//                        Toast.makeText(context, "Convert failed", Toast.LENGTH_SHORT).show();
//                    }
//                });
//
//                toAAC.setOnClickListener(view112 -> {
//                    Audio item = audioList.get(holder.getAbsoluteAdapterPosition());
//                    String fileName = item.getName();
//                    File inputFilePath   = new File(Environment.getExternalStorageDirectory().toString()+"/Recordings/"+ folderName + "/" + fileName); // Lấy đường dẫn đầy đủ đến tệp
//                    File outputFilePath = new File(Environment.getExternalStorageDirectory().toString() + "/Recordings/"+ folderName + "/" + FilenameUtils.removeExtension(fileName) + ".aac");
//
//                    String[] command = {"-i", String.valueOf(inputFilePath), "-c:a", "aac", "-b:a", "256k", outputFilePath.getAbsolutePath()};
//                    int rc = FFmpeg.execute(command);
//                    if (rc == RETURN_CODE_SUCCESS) {
//                        // conversion success
//                        listener.onItemClickConvert(0);
//                        Toast.makeText(context, "Convert successful", Toast.LENGTH_SHORT).show();
//                        popupWindow1.dismiss();
//
//                    } else if (rc == RETURN_CODE_CANCEL) {
//                        Toast.makeText(context, "Convert cancelled", Toast.LENGTH_SHORT).show();
//                    } else {
//                        Toast.makeText(context, "Convert failed", Toast.LENGTH_SHORT).show();
//                    }
//                });
//
//                toMP3.setOnClickListener(view113 -> {
//                    Audio item = audioList.get(holder.getAbsoluteAdapterPosition());
//                    String fileName = item.getName();
//                    File inputFilePath   = new File(Environment.getExternalStorageDirectory().toString()+"/Recordings/"+ folderName + "/" + fileName); // Lấy đường dẫn đầy đủ đến tệp
//                    File outputFilePath = new File(Environment.getExternalStorageDirectory().toString() + "/Recordings/"+ folderName + "/" + FilenameUtils.removeExtension(fileName) + ".mp3");
//                    String[] command = {"-i", String.valueOf(inputFilePath), "-acodec", "libmp3lame", "-b:a", "192k", "-f", "mp3", String.valueOf(outputFilePath)};
//                    int rc = FFmpeg.execute(command);
//                    if (rc == RETURN_CODE_SUCCESS) {
//                        listener.onItemClickConvert(0);
//                        popupWindow1.dismiss();
//                        Toast.makeText(context, "Convert successful", Toast.LENGTH_SHORT).show();
//                    } else if (rc == RETURN_CODE_CANCEL) {
//                        Toast.makeText(context, "Convert cancelled", Toast.LENGTH_SHORT).show();
//                    } else {
//                        Toast.makeText(context, "Convert failed", Toast.LENGTH_SHORT).show();
//                    }
//                });
//            });
//
//            //Di chuyển đến thư mục...
//
//
//        });
        holder.homeTrashBtn.setOnClickListener(view -> {

            Audio audio1 = audioList.get(holder.getAbsoluteAdapterPosition());
            String fileNameTrash = audio1.getName();
            Log.e("TAG", "onClick: " + fileNameTrash);
            File sourceFile  = new File(Environment.getExternalStorageDirectory() + "/Recordings/"+ folderName + "/"+ fileNameTrash); // Lấy đường dẫn đầy đủ đến tệp
            File destinationFolder = new File(Environment.getExternalStorageDirectory() + "/Recordings/TrashAudio/");

            // Tạo thư mục thùng rác nếu chưa tồn tại
            if (!destinationFolder.exists()) {
                destinationFolder.mkdir();
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

    public void filterList(List<Audio> filteredList) {
        audioList = filteredList;
        notifyDataSetChanged();
    }

    public void setNameFolder(String name) {
        folderName = name;
    }

    private List<FolderCLassContent> getListFolder() {
        DecimalFormat decimalFormat = new DecimalFormat("#.##");

        List<FolderCLassContent> folderList = new ArrayList<>();
        File recordingsDirectory = new File(Environment.getExternalStorageDirectory().toString() + "/Recordings/");

        if (recordingsDirectory.exists()) {
            File[] folders = recordingsDirectory.listFiles();
            if (folders != null) {
                for (File folder : folders) {
                    int amount = 0;
                    long size = 0;

                    if (folder.isDirectory()) {
                        File[] files = folder.listFiles();
                        if (files != null) {
                            for (File file : files) {
                                if (file.isFile()) {
                                    amount = amount + 1;
                                    size = size + file.length();
                                }
                            }
                        }
                        String fileName = folder.getName();
                        String amountFileOfFolder = String.valueOf(amount);
                        String fileSize = decimalFormat.format(1.0 * size / (1024*1024));
                        sizeFolder = sizeFolder + 1.0 * size / (1024*1024);
                        Date lastModifiedDate = new Date(folder.lastModified());
                        @SuppressLint("SimpleDateFormat") String formattedDate = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(lastModifiedDate);
                        folderList.add(new FolderCLassContent(fileName, amountFileOfFolder + " " + context.getString(R.string.folder_file) + " ",fileSize+" MB", formattedDate, R.drawable.ic_folder_pink500));
                    }
                }
            }
        }
        return folderList;
    }

    public class FileInFolderHolder extends RecyclerView.ViewHolder {
        private ImageView icAudioHome;
        private TextView nameAudio, timeOfAudio, sizeAudio, createDateAudio;
        private SwipeRevealLayout swipeRevealLayout;
        private RelativeLayout relativeLayout;
        private ImageView homeTrashBtn, homeMoreBtn, homeShareBtn;
        public FileInFolderHolder(@NonNull View itemView) {
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
