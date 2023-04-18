package com.devapp.smartrecord.ui.folder;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.icu.text.DecimalFormat;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
import android.util.SparseBooleanArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.devapp.smartrecord.R;
import com.devapp.smartrecord.databinding.FragmentFolderBinding;
import com.devapp.smartrecord.editmenu.adjust.AdjustActivity;
import com.devapp.smartrecord.editmenu.insertion.InsertionActivity;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

public class FolderFragment extends Fragment implements FolderClassContentAdapter.OnItemClickListener, FolderChildFragment.DataPassListener {
    private FragmentFolderBinding binding;
    private FolderClassContentAdapter adapterFolder;
    private ArrayList<FolderCLassContent> listFolder;
    private final boolean mSortByNameAscending = true;
    private Double sizeFolder = 0.0;
    private TextView totalFolder, totalSizeFolder, textFolder, textCatholic, textTotalChoice;
    private RelativeLayout infoLayout;
    private LinearLayout recordLayout;
    private ImageButton imageTotalChoice, addFolder;
    private RecyclerView rcvFolder;
    private OnClickButtonItem dataPasser;
    private boolean isEdit = false, isTotalChecked = false;
    private boolean[] selectedItems;
    private int[] listItemChoice;
    private FolderChildFragment folderChild;

    public interface OnClickButtonItem {
        void onClickButton(String data);
    }

//    @Override
//    public void onAttach(Context context) {
//        super.onAttach(context);
//        dataPasser = (OnClickButtonItem) context;
//    }
    @Override
    public void onItemClick(int position){
        if (isEdit){
            if (selectedItems[position]){
                listFolder.get(position).setImage(R.drawable.ic_circle_folder);
                adapterFolder.notifyDataSetChanged();
                selectedItems[position] = false;
            }
            else {
                listFolder.get(position).setImage(R.drawable.ic_circle_checked_folder);
                adapterFolder.notifyDataSetChanged();
                selectedItems[position] = true;
//                if (isTotalChecked){
//                    imageTotalChoice.setImageResource(R.drawable.ic_circle_folder);
//                    adapterFolder.notifyDataSetChanged();
//                    isTotalChecked = false;
//                }
            }
        }
    }

    @Override
    public void handleRemoveMultiFolder() {
        handleValueSelected();
        //Tạo ra dialog để xác nhận xóa hay không
        AlertDialog.Builder builder = new AlertDialog.Builder(getView().getContext());
        builder.setMessage(getView().getContext().getString(R.string.question_delete));
        builder.setPositiveButton(getView().getContext().getString(R.string.answer_yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int j) {
                int count = 0;
                for (int i = 0; i < listItemChoice.length; i++) {
                    if (i != 0){
                        listItemChoice[i] -= count;
                    }
                    FolderCLassContent folder = listFolder.get(listItemChoice[i]);
                    String folderName = folder.getTitle();
                    File sourceFile  = new File(Environment.getExternalStorageDirectory().toString() + "/Recordings/" + folderName); // Lấy đường dẫn đầy đủ đến tệp
                    File destinationFolder = new File(Environment.getExternalStorageDirectory().toString() + "/Recordings/", "Thùng rác");

                    try {
                        File destinationFile = new File(destinationFolder, folderName); // Tạo tệp đích mới
                        boolean success = sourceFile.renameTo(destinationFile);
                        if (success) { // Di chuyển tệp đến thư mục đích và kiểm tra kết quả
                            listFolder.remove(listItemChoice[i]);
                            adapterFolder.notifyItemRemoved(listItemChoice[i]);
                            Toast.makeText(getContext(), getView().getContext().getString(R.string.announce_moved_successfully), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), getView().getContext().getString(R.string.announce_moved_unsuccessfully), Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), getView().getContext().getString(R.string.announce_moved_unsuccessfully) + ": " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    count++;
                }


                listItemChoice = null;
                selectedItems = new boolean[listFolder.size()];
                Arrays.fill(selectedItems, false);
            }
        });
        builder.setNegativeButton(getView().getContext().getString(R.string.answer_no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.show();
    }
    public void handleMoveMultiFolder(View view){
        handleValueSelected();
        //Tạo ra dialog để xác nhận xóa hay không
        AlertDialog.Builder builder = new AlertDialog.Builder(getView().getContext());
        builder.setMessage(getView().getContext().getString(R.string.question_delete));
        builder.setPositiveButton(getView().getContext().getString(R.string.answer_yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int j) {
                int count = 0;
                for (int i = 0; i < listItemChoice.length; i++) {
                    if (i != 0){
                        listItemChoice[i] -= count;
                    }
                    FolderCLassContent folder = listFolder.get(listItemChoice[i]);
                    String folderName = folder.getTitle();
                    File sourceFile  = new File(Environment.getExternalStorageDirectory().toString() + "/Recordings/" + folderName); // Lấy đường dẫn đầy đủ đến tệp
                    File destinationFolder = new File(Environment.getExternalStorageDirectory().toString() + "/Recordings/", "Thư mục riêng tư");

                    try {
                        File destinationFile = new File(destinationFolder, folderName); // Tạo tệp đích mới
                        boolean success = sourceFile.renameTo(destinationFile);
                        if (success) { // Di chuyển tệp đến thư mục đích và kiểm tra kết quả
                            listFolder.remove(listItemChoice[i]);
                            adapterFolder.notifyItemRemoved(listItemChoice[i]);
                            Toast.makeText(getContext(), getView().getContext().getString(R.string.announce_moved_successfully), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), getView().getContext().getString(R.string.announce_moved_unsuccessfully), Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), getView().getContext().getString(R.string.announce_moved_unsuccessfully) + ": " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    count++;
                }


//                listItemChoice = null;
//                selectedItems = new boolean[listFolder.size()];
//                Arrays.fill(selectedItems, false);
            }
        });
        builder.setNegativeButton(getView().getContext().getString(R.string.answer_no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.show();
    }


    @SuppressLint({"ClickableViewAccessibility", "SetTextI18n", "NotifyDataSetChanged"})
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentFolderBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        folderView(binding);
        showInfoAdapter();
        createFolderPrivate("Thư mục riêng tư");

//         Lấy dữ liệu được truyền vào từ activity
        Bundle args = getArguments();
        if (args != null){
            isEdit = args.getBoolean("isEdit");
        }

        showMultiFolder();

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        isEdit = false;
        for (int i = 0; i < listFolder.size(); i++){
            listFolder.get(i).setImage(R.drawable.ic_pink500_folder);
        }
        adapterFolder.notifyDataSetChanged();
    }
    private void showMultiFolder(){
        if (isEdit) {
            totalSizeFolder.setVisibility(View.GONE);
            textFolder.setVisibility(View.GONE);
            textCatholic.setVisibility(View.GONE);
            addFolder.setVisibility(View.GONE);
            recordLayout.setVisibility(View.GONE);

            imageTotalChoice = binding.folderTotalChoice;
            imageTotalChoice.setImageResource(R.drawable.ic_circle_folder);
            totalFolder.setText("Chọn tất cả");
            totalFolder.setTextSize(16);

            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) infoLayout.getLayoutParams();
            RelativeLayout.LayoutParams paramsText = (RelativeLayout.LayoutParams) totalFolder.getLayoutParams();
            params.leftMargin = 30;
            paramsText.leftMargin = 14;

            imageTotalChoice.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!isTotalChecked) {
                        imageTotalChoice.setImageResource(R.drawable.ic_circle_checked_folder);
                        for (int i = 0; i < listFolder.size(); i++) {
                            listFolder.get(i).setImage(R.drawable.ic_circle_checked_folder);
                        }
                        adapterFolder.notifyDataSetChanged();
                        Arrays.fill(selectedItems, true);
//                        isTotalChecked = true;
                    } else {
                        imageTotalChoice.setImageResource(R.drawable.ic_circle_folder);
                        for (int i = 0; i < listFolder.size(); i++) {
                            listFolder.get(i).setImage(R.drawable.ic_circle_folder);
                        }
                        adapterFolder.notifyDataSetChanged();
//                        Arrays.fill(selectedItems, false);
                        isTotalChecked = false;
                    }
                }
            });

            choiceMultiFolder();
            showChildFragment();
        }
    }

    private void folderView(FragmentFolderBinding binding){
        LinearLayout menuFilter = binding.folderWrapDay;
        infoLayout = binding.folderWrapInfo;
        recordLayout = binding.folderWrapRecord;
        TextView folderFilterTitle = binding.folderDay;
        androidx.appcompat.widget.SearchView searchView = binding.searchFolderEdt;
        RelativeLayout privateFolder = binding.folderItemPrivateRow;
        addFolder = binding.folderAddItem;
        totalFolder = binding.folderAmount;
        totalSizeFolder = binding.folderCapacity;
        textFolder = binding.folder;
        textCatholic = binding.folderCatholic;
        rcvFolder = binding.folderRcvList;

        // Lấy InputMethodManager từ Context
        InputMethodManager imm = (InputMethodManager) Objects.requireNonNull(getActivity()).getSystemService(Context.INPUT_METHOD_SERVICE);

        // Lắng nghe sự kiện chạm màn hình
        getActivity().findViewById(android.R.id.content).setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                // Kiểm tra xem SearchView có đang được chỉnh sửa không
                if (!searchView.isIconified()) {
                    // Ẩn bàn phím và hủy focus của SearchView
                    searchView.clearFocus();
                    imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
                }
            }
            return false;
        });

        searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public boolean onQueryTextChange(String newText) {
                String searchText = newText.toLowerCase().trim();
                List<FolderCLassContent> filteredList = new ArrayList<>();

                for (FolderCLassContent item : listFolder) {
                    String itemText = item.getTitle().toLowerCase();
                    if (itemText.contains(searchText)) {
                        filteredList.add(item);
                    }
                }
                if (filteredList.isEmpty()) {
                    // Hiển thị message thông báo không tìm thấy kết quả tương ứng
                    Toast.makeText(getActivity(), "Không tìm thấy kết quả phù hợp", Toast.LENGTH_SHORT).show();
                }
                // Đặt danh sách đã lọc vào adapter và cập nhật adapter
                adapterFolder.filterList(filteredList);
                adapterFolder.notifyDataSetChanged();

                return true;
            }
        });

        menuFilter.setOnClickListener(v -> {
            searchView.clearFocus();
            imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);

            // inflate the layout of the popup window
            LayoutInflater inflater1 = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            @SuppressLint("InflateParams") View popupView = inflater1.inflate(R.layout.folder_context_filter_menu, null);

            // create the popup window
            int width = LinearLayout.LayoutParams.MATCH_PARENT;
            int height = LinearLayout.LayoutParams.MATCH_PARENT ;
            boolean focusable = true; // lets taps outside the popup also dismiss it
            final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

            // show the popup window
            // which view you pass in doesn't matter, it is only used for the window token
            popupWindow.showAtLocation(v, Gravity.BOTTOM, 0, 0);

            // dismiss the popup window when touched
            popupView.setOnTouchListener((v1, event) -> {
                popupWindow.dismiss();
                return true;
            });

            // handle button clicks
            TextView filterTitle = popupView.findViewById(R.id.folder_filter_item_title);
            TextView filterDay = popupView.findViewById(R.id.folder_filter_item_day);
            TextView filterName = popupView.findViewById(R.id.folder_filter_item_name);
            TextView filterSize = popupView.findViewById(R.id.folder_filter_item_size);
            TextView filterDestroy = popupView.findViewById(R.id.file_convert_item_destroy);

            filterTitle.setOnClickListener(v12 -> popupWindow.update());

            filterDay.setOnClickListener(v13 -> {
                folderFilterTitle.setText(filterDay.getText());

                List<FolderCLassContent> sortedList = sortList(listFolder,"day", mSortByNameAscending);
                adapterFolder.updateList(sortedList);

                popupWindow.dismiss();
            });

            filterName.setOnClickListener(v14 -> {
                folderFilterTitle.setText(filterName.getText());

                List<FolderCLassContent> sortedList = sortList(listFolder,"name", mSortByNameAscending);
                adapterFolder.updateList(sortedList);

                popupWindow.dismiss();
            });

            filterSize.setOnClickListener(v15 -> {
                folderFilterTitle.setText(filterSize.getText());

                List<FolderCLassContent> sortedList = sortList(listFolder,"size", mSortByNameAscending);
                adapterFolder.updateList(sortedList);

                popupWindow.dismiss();
            });

            filterDestroy.setOnClickListener(v16 -> popupWindow.dismiss());
        });

        privateFolder.setOnClickListener(v -> {
//            searchView.clearFocus();
//            imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
//
//            SharedPreferences prefs = Objects.requireNonNull(getContext()).getSharedPreferences("MyPrivateFolder", Context.MODE_PRIVATE);
//            boolean isFirstTime = prefs.getBoolean("isFirstTime", true);
////                String password = prefs.getString("password", "");
//
//            if (isFirstTime) {
//                // inflate the layout of the popup window
//                LayoutInflater inflater12 = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//                @SuppressLint("InflateParams") View popupView = inflater12.inflate(R.layout.folder_context_create_password_private, null);
//
//                // create the popup window
//                int width = LinearLayout.LayoutParams.MATCH_PARENT;
//                int height = LinearLayout.LayoutParams.MATCH_PARENT ;
//                boolean focusable = true; // lets taps outside the popup also dismiss it
//                final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
//
//                // show the popup window
//                // which view you pass in doesn't matter, it is only used for the window token
//                popupWindow.showAtLocation(v, Gravity.CENTER, 0, 100);
//
//                TextView btnDestroyMenuCreatePrivate = popupView.findViewById(R.id.folder_create_destroy_menu_private);
//                TextView btnOkMenuCreatePrivate = popupView.findViewById(R.id.folder_create_ok_menu_private);
//                EditText edtCreateassword = popupView.findViewById(R.id.folder_create_password_private);
//
//                btnDestroyMenuCreatePrivate.setOnClickListener(v17 -> popupWindow.dismiss());
//
//                btnOkMenuCreatePrivate.setOnClickListener(v18 -> {
//                    String password = edtCreateassword.getText().toString();
//                    String hashedPassword = hashSHA256(password);
//                    if (edtCreateassword.getText().length() == 0){
//                        Toast.makeText(popupView.getContext(), "Bạn chưa nhập password", Toast.LENGTH_LONG).show();
//                    }
//                    else{
//                        // Sau đó, lưu trạng thái đã truy cập vào SharedPreferences
//                        SharedPreferences.Editor editor = prefs.edit();
//                        editor.putBoolean("isFirstTime", false);
//                        editor.putString("password", hashedPassword);
//                        editor.apply();
//                        Toast.makeText(popupView.getContext(), "Tạo mật khẩu thành công", Toast.LENGTH_LONG).show();
//                        popupWindow.dismiss();
//                    }
//                });
//            } else {
//                // Ứng dụng đã được truy cập trước đó
//                // Thực hiện các thao tác bình thường của ứng dụng ở đây
//                // inflate the layout of the popup window
//                LayoutInflater inflater12 = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//                @SuppressLint("InflateParams") View popupView = inflater12.inflate(R.layout.folder_context_input_password_private, null);
//
//                // create the popup window
//                int width = LinearLayout.LayoutParams.MATCH_PARENT;
//                int height = LinearLayout.LayoutParams.MATCH_PARENT ;
//                boolean focusable = true; // lets taps outside the popup also dismiss it
//                final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
//
//                // show the popup window
//                // which view you pass in doesn't matter, it is only used for the window token
//                popupWindow.showAtLocation(v, Gravity.CENTER, 0, 0);
//
//                TextView btnDestroyMenuInputPrivate = popupView.findViewById(R.id.folder_input_destroy_menu_private);
//                TextView btnOkMenuInputPrivate = popupView.findViewById(R.id.folder_input_ok_menu_private);
//                EditText edtInputPassword = popupView.findViewById(R.id.folder_input_password_private);
//
//                btnDestroyMenuInputPrivate.setOnClickListener(v19 -> {
//                    popupWindow.dismiss();
//                    SharedPreferences.Editor editor = prefs.edit();
//                    editor.clear();
//                    editor.apply();
//                });
//
//                btnOkMenuInputPrivate.setOnClickListener(v110 -> {
//                    if (edtInputPassword.getText().length() == 0){
//                        Toast.makeText(popupView.getContext(), "Bạn chưa nhập password", Toast.LENGTH_LONG).show();
//                    }
//                    else{
//                        String password = edtInputPassword.getText().toString();
//                        String storedPassword = prefs.getString("password", "");
//                        String hashedPassword = hashSHA256(password);
//
//                        if (Objects.equals(hashedPassword, storedPassword)) {
//                            Toast.makeText(popupView.getContext(), "Password đúng", Toast.LENGTH_LONG).show();
//                            Intent intent = new Intent(getActivity(), HomeFragment.class);
//                            startActivity(intent);
//                            popupWindow.dismiss();
//                        } else {
//                            Toast.makeText(popupView.getContext(), "Password sai", Toast.LENGTH_LONG).show();
//                            edtInputPassword.setText("");
//                        }
//                    }
//                });
//            }
            Intent intent = new Intent(getActivity(), AdjustActivity.class);
            startActivity(intent);
        });

        addFolder.setOnClickListener(v -> {
            // inflate the layout of the popup window
            LayoutInflater inflater13 = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            @SuppressLint("InflateParams") View popupView = inflater13.inflate(R.layout.folder_context_create_folder, null);

            // create the popup window
            int width = LinearLayout.LayoutParams.MATCH_PARENT;
            int height = LinearLayout.LayoutParams.MATCH_PARENT ;
            boolean focusable = true; // lets taps outside the popup also dismiss it
            final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

            // show the popup window
            // which view you pass in doesn't matter, it is only used for the window token
            popupWindow.showAtLocation(v, Gravity.CENTER, 0, 0);

            TextView btnDestroyCreateFolder = popupView.findViewById(R.id.folder_create_destroy_folder);
            TextView btnOkCreateFolder = popupView.findViewById(R.id.folder_create_ok_folder);
            EditText edtInputNameFolder = popupView.findViewById(R.id.folder_create_folder);

            btnDestroyCreateFolder.setOnClickListener(v111 -> popupWindow.dismiss());

            btnOkCreateFolder.setOnClickListener(v112 -> {
                if (edtInputNameFolder.getText().length() == 0){
                    Toast.makeText(popupView.getContext(), "Bạn chưa nhập tên thư mục", Toast.LENGTH_LONG).show();
                }
                else{
                    File newFolder = new File(Environment.getExternalStorageDirectory().toString() + "/Recordings/", edtInputNameFolder.getText().toString()); // Lấy đường dẫn tới thư mục bộ nhớ trong của ứng dụng và tạo một thư mục mới tên là "privateFolder"
                    if(!newFolder.exists()) {
                        popupWindow.dismiss();

                        newFolder.mkdir(); // Tạo thư mục mới nếu chưa tồn tại

                        imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);

                        Toast.makeText(getContext(), "Tạo thành công", Toast.LENGTH_LONG).show();
                        File[] files = newFolder.listFiles();

                        assert files != null;
                        for (File file : files) {
                            file.isFile();
                        }

                        long lastModifiedTime = newFolder.lastModified();
                        Date lastModifiedDate = new Date(lastModifiedTime);
                        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                        String formattedDate = dateFormat.format(lastModifiedDate);
                        listFolder.add(new FolderCLassContent(edtInputNameFolder.getText().toString(), 0 + " bản ghi - ", 0 + " MB", formattedDate ,R.drawable.ic_pink500_folder));
                        if (folderFilterTitle.getText().toString().equals("Ngày")){
                            sortList(listFolder, "day", mSortByNameAscending);
                        }
                        else if(folderFilterTitle.getText().toString().equals("Tên")){
                            sortList(listFolder, "name", mSortByNameAscending);
                        }
                        else if(folderFilterTitle.getText().toString().equals("Kích Thước")){
                            sortList(listFolder, "size", mSortByNameAscending);
                        }
                        adapterFolder.notifyDataSetChanged();
                    }
                    else{
                        Toast.makeText(popupView.getContext(), "Tên thư mục đã tồn tại", Toast.LENGTH_LONG).show();
                        edtInputNameFolder.setText("");
                    }
                }
            });
        });
    }

    private void showInfoAdapter(){
        rcvFolder.setLayoutManager(new LinearLayoutManager(getContext()));
        listFolder = new ArrayList<>();
        listFolder = (ArrayList<FolderCLassContent>) getListFolder();
        adapterFolder = new FolderClassContentAdapter(getContext(), this);
        DecimalFormat decimalFormat = new DecimalFormat("#.##");

        sortList(listFolder,"day", mSortByNameAscending);
        totalFolder.setText(listFolder.size() + "");
        totalSizeFolder.setText(decimalFormat.format(sizeFolder));
        adapterFolder.setData(listFolder);
        rcvFolder.setAdapter(adapterFolder);

        selectedItems = new boolean[listFolder.size()];
        Arrays.fill(selectedItems, false);
    }

    private void createFolderPrivate(String name){
        //Tạo thư mục private
        File folderPrivate = new File(Environment.getExternalStorageDirectory().toString() + "/Recordings/", name); // Lấy đường dẫn tới thư mục bộ nhớ trong của ứng dụng và tạo một thư mục mới tên là "privateFolder"
        if(!folderPrivate.exists()) {
            folderPrivate.mkdir(); // Tạo thư mục mới nếu chưa tồn tại
        }
    }

    private void choiceMultiFolder(){
        for (int i = 0; i < listFolder.size(); i++){
            listFolder.get(i).setImage(R.drawable.ic_circle_folder);
        }
        adapterFolder.notifyDataSetChanged();
    }

    private void showChildFragment() {
        // Replace this fragment with the child fragment
        folderChild = new FolderChildFragment(this);
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.folder_wrap_fragment, folderChild);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void handleValueSelected(){
        listItemChoice = IntStream.range(0, selectedItems.length)
                .filter(i -> selectedItems[i])
                .toArray();
    }

    public interface OnButtonClickListener {
        void onButtonClicked();
    }

    @NonNull
    private List<FolderCLassContent> getListFolder() {
        DecimalFormat decimalFormat = new DecimalFormat("#.##");

        List<FolderCLassContent> folderList = new ArrayList<>();
        File recordingsDirectory = new File(Environment.getExternalStorageDirectory().toString() + "/Recordings/");

        if (recordingsDirectory.exists()) {
            File[] folders = recordingsDirectory.listFiles();
            if (folders != null) {
                for (File folder : folders) {
                    if (folder.getName().equalsIgnoreCase("Thư mục riêng tư") || folder.getName().equalsIgnoreCase("Thùng rác")){
                        continue;
                    }
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
                        folderList.add(new FolderCLassContent(fileName, amountFileOfFolder + " " + getContext().getString(R.string.folder_file) + " ",fileSize+" MB", formattedDate, R.drawable.ic_pink500_folder));
                    }
                }
            }
        }
        return folderList;
    }

    private String hashSHA256(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.encodeToString(hash, Base64.DEFAULT);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    private List<FolderCLassContent> sortList(List<FolderCLassContent> listFolder, String sortBy, boolean ascending) {
        switch (sortBy) {
            case "name":
                listFolder.sort((folder1, folder2) -> folder1.getTitle().compareToIgnoreCase(folder2.getTitle()));
                break;
            case "size":
                listFolder.sort((folder1, folder2) -> {
                    double size1 = Double.parseDouble(folder1.getSize().split(" ")[0].replace(",", "."));
                    double size2 = Double.parseDouble(folder2.getSize().split(" ")[0].replace(",", "."));
                    return Double.compare(size1, size2);
                });
                break;
            case "day":
                @SuppressLint("SimpleDateFormat") SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                listFolder.sort((folder1, folder2) -> {
                    Date date1 = null, date2 = null;
                    try {
                        date1 = format.parse(folder1.getHour());
                        date2 = format.parse(folder2.getHour());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    if (date1 != null && date2 != null) {
                        return date2.compareTo(date1); // sắp xếp tăng dần
                    } else {
                        return 0;
                    }
                });
                break;
        }

        if (!ascending) {
            Collections.reverse(listFolder);
        }

        return listFolder;
    }
}
