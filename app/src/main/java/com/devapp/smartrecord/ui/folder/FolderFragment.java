package com.devapp.smartrecord.ui.folder;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.text.DecimalFormat;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.devapp.smartrecord.R;
import com.devapp.smartrecord.databinding.FragmentFolderBinding;
import com.devapp.smartrecord.ui.home.HomeFragment;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class FolderFragment extends Fragment {
    private FragmentFolderBinding binding;
    private RecyclerView rcvFolder;
    private FolderClassContentAdapter adapterFolder;
    private ArrayList<FolderCLassContent> listFolder;
    private List<FolderCLassContent> searchListFolder = new ArrayList<>();


    private boolean mSortByNameAscending = true;
    private Double sizeFolder = 0.0;
    private boolean isFirstPrivate = true;

    private boolean mSortByNameAscending = true;
    private Double sizeFolder = 0.0;
    private boolean isFirstPrivate = true;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FolderViewModel folderViewModel = new ViewModelProvider(this).get(FolderViewModel.class);
        binding = FragmentFolderBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

//        final TextView textView = binding.textFolder;
        final LinearLayout menuFilter = binding.folderWrapDay;
        final TextView folderFilterTitle = binding.folderDay;
        final SearchView searchView = binding.searchFolderEdt;

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

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
                adapterFolder.filterList(filteredList);
                return true;
            }
        });



        menuFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // inflate the layout of the popup window
                LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View popupView = inflater.inflate(R.layout.folder_context_filter_menu, null);

                // create the popup window
                int width = LinearLayout.LayoutParams.MATCH_PARENT;
                int height = LinearLayout.LayoutParams.MATCH_PARENT ;
                boolean focusable = true; // lets taps outside the popup also dismiss it
                final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

                // show the popup window
                // which view you pass in doesn't matter, it is only used for the window token
                popupWindow.showAtLocation(v, Gravity.BOTTOM, 0, 0);

                // dismiss the popup window when touched
                popupView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        popupWindow.dismiss();
                        return true;
                    }
                });

                // handle button clicks
                TextView filterTitle = popupView.findViewById(R.id.folder_filter_item_title);
                TextView filterDay = popupView.findViewById(R.id.folder_filter_item_day);
                TextView filterName = popupView.findViewById(R.id.folder_filter_item_name);
                TextView filterSize = popupView.findViewById(R.id.folder_filter_item_size);
                TextView filterDestroy = popupView.findViewById(R.id.file_convert_item_destroy);

                filterTitle.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        popupWindow.update();
                    }
                });

                filterDay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        folderFilterTitle.setText(filterDay.getText());

                        List<FolderCLassContent> sortedList = sortList(listFolder,"day", mSortByNameAscending);
                        adapterFolder.updateList(sortedList);

                        popupWindow.dismiss();
                    }
                });

                filterName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        folderFilterTitle.setText(filterName.getText());

                        List<FolderCLassContent> sortedList = sortList(listFolder,"name", mSortByNameAscending);
                        adapterFolder.updateList(sortedList);

                        popupWindow.dismiss();
                    }
                });

                filterSize.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        folderFilterTitle.setText(filterSize.getText());

                        List<FolderCLassContent> sortedList = sortList(listFolder,"size", mSortByNameAscending);
                        adapterFolder.updateList(sortedList);

                        popupWindow.dismiss();
                    }
                });

                filterDestroy.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        popupWindow.dismiss();
                    }
                });
            }
        });

        final TextView totalFolder, totalSizeFolder;

        rcvFolder = binding.folderRcvList;
        totalFolder = binding.folderAmount;
        totalSizeFolder = binding.folderCapacity;
        rcvFolder.setLayoutManager(new LinearLayoutManager(getContext()));
        listFolder = new ArrayList<>();
        listFolder = (ArrayList<FolderCLassContent>) getListFolder();
        adapterFolder = new FolderClassContentAdapter(getContext());

        final RelativeLayout privateFolder = binding.folderItemPrivateRow;

        privateFolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences prefs = getContext().getSharedPreferences("MyPrivateFolder", Context.MODE_PRIVATE);
                boolean isFirstTime = prefs.getBoolean("isFirstTime", true);
//                String password = prefs.getString("password", "");

                if (isFirstTime) {
                    // inflate the layout of the popup window
                    LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    View popupView = inflater.inflate(R.layout.folder_context_create_password_private, null);

                    // create the popup window
                    int width = LinearLayout.LayoutParams.MATCH_PARENT;
                    int height = LinearLayout.LayoutParams.MATCH_PARENT ;
                    boolean focusable = true; // lets taps outside the popup also dismiss it
                    final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

                    // show the popup window
                    // which view you pass in doesn't matter, it is only used for the window token
                    popupWindow.showAtLocation(v, Gravity.CENTER, 0, 100);

                    TextView btnDestroyMenuCreatePrivate = (TextView) popupView.findViewById(R.id.folder_create_destroy_menu_private);
                    TextView btnOkMenuCreatePrivate = (TextView) popupView.findViewById(R.id.folder_create_ok_menu_private);
                    EditText edtCreateassword = (EditText) popupView.findViewById(R.id.folder_create_password_private);

                    btnDestroyMenuCreatePrivate.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            popupWindow.dismiss();
                        }
                    });

                    btnOkMenuCreatePrivate.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String password = edtCreateassword.getText().toString();
                            String hashedPassword = hashSHA256(password);
                            if (edtCreateassword.getText().length() == 0){
                                Toast.makeText(popupView.getContext(), "Bạn chưa nhập password", Toast.LENGTH_LONG).show();
                            }
                            else{
                                // Sau đó, lưu trạng thái đã truy cập vào SharedPreferences
                                SharedPreferences.Editor editor = prefs.edit();
                                editor.putBoolean("isFirstTime", false);
                                editor.putString("password", hashedPassword);
                                editor.apply();
                                Toast.makeText(popupView.getContext(), "Tạo mật khẩu thành công", Toast.LENGTH_LONG).show();
                                popupWindow.dismiss();
                            }
                        }
                    });
                } else {
                    // Ứng dụng đã được truy cập trước đó
                    // Thực hiện các thao tác bình thường của ứng dụng ở đây
                    // inflate the layout of the popup window
                    LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    View popupView = inflater.inflate(R.layout.folder_context_input_password_private, null);

                    // create the popup window
                    int width = LinearLayout.LayoutParams.MATCH_PARENT;
                    int height = LinearLayout.LayoutParams.MATCH_PARENT ;
                    boolean focusable = true; // lets taps outside the popup also dismiss it
                    final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

                    // show the popup window
                    // which view you pass in doesn't matter, it is only used for the window token
                    popupWindow.showAtLocation(v, Gravity.CENTER, 0, 0);

                    TextView btnDestroyMenuInputPrivate = (TextView) popupView.findViewById(R.id.folder_input_destroy_menu_private);
                    TextView btnOkMenuInputPrivate = (TextView) popupView.findViewById(R.id.folder_input_ok_menu_private);
                    EditText edtInputPassword = (EditText) popupView.findViewById(R.id.folder_input_password_private);

                    btnDestroyMenuInputPrivate.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            popupWindow.dismiss();
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.clear();
                            editor.apply();
                        }
                    });

                    btnOkMenuInputPrivate.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (edtInputPassword.getText().length() == 0){
                                Toast.makeText(popupView.getContext(), "Bạn chưa nhập password", Toast.LENGTH_LONG).show();
                            }
                            else{
                                String password = edtInputPassword.getText().toString();
                                String storedPassword = prefs.getString("password", "");
                                String hashedPassword = hashSHA256(password);

                                if (hashedPassword.equals(storedPassword)) {
                                    Toast.makeText(popupView.getContext(), "Password đúng", Toast.LENGTH_LONG).show();
                                    Intent intent = new Intent(getActivity(), HomeFragment.class);
                                    popupWindow.dismiss();
                                } else {
                                    Toast.makeText(popupView.getContext(), "Password sai", Toast.LENGTH_LONG).show();
                                    edtInputPassword.setText("");
                                }
                            }
                        }
                    });
                }
            }
        });

        rcvFolder = binding.folderRcvList;
        totalFolder = binding.folderAmount;
        totalSizeFolder = binding.folderCapacity;
        rcvFolder.setLayoutManager(new LinearLayoutManager(getContext()));
        listFolder = new ArrayList<>();
        listFolder = (ArrayList<FolderCLassContent>) getListFolder();
        adapterFolder = new FolderClassContentAdapter(getContext());

        List<FolderCLassContent> sortedList = sortList(listFolder,"day", mSortByNameAscending);

        totalFolder.setText(sortedList.size() + "");
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        totalSizeFolder.setText(decimalFormat.format(sizeFolder));
        adapterFolder.setData(sortedList);
        rcvFolder.setAdapter(adapterFolder);

        final ImageButton addFolder = binding.folderAddItem;
        addFolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // inflate the layout of the popup window
                LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View popupView = inflater.inflate(R.layout.folder_context_create_folder, null);

                // create the popup window
                int width = LinearLayout.LayoutParams.MATCH_PARENT;
                int height = LinearLayout.LayoutParams.MATCH_PARENT ;
                boolean focusable = true; // lets taps outside the popup also dismiss it
                final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

                // show the popup window
                // which view you pass in doesn't matter, it is only used for the window token
                popupWindow.showAtLocation(v, Gravity.CENTER, 0, 0);

                TextView btnDestroyCreateFolder = (TextView) popupView.findViewById(R.id.folder_create_destroy_folder);
                TextView btnOkCreateFolder = (TextView) popupView.findViewById(R.id.folder_create_ok_folder);
                EditText edtInputNameFolder = (EditText) popupView.findViewById(R.id.folder_create_folder);

                btnDestroyCreateFolder.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        popupWindow.dismiss();
                    }
                });

                btnOkCreateFolder.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (edtInputNameFolder.getText().length() == 0){
                            Toast.makeText(popupView.getContext(), "Bạn chưa nhập tên thư mục", Toast.LENGTH_LONG).show();
                        }
                        else{
                            File privateDir = new File(Environment.getExternalStorageDirectory().toString() + "/Recordings/", edtInputNameFolder.getText().toString()); // Lấy đường dẫn tới thư mục bộ nhớ trong của ứng dụng và tạo một thư mục mới tên là "privateFolder"
                            if(!privateDir.exists()) {
                                popupWindow.dismiss();
                                privateDir.mkdir(); // Tạo thư mục mới nếu chưa tồn tại
                                Toast.makeText(getContext(), "Tạo thành công", Toast.LENGTH_LONG).show();
                                long length = 0;
                                File[] files = privateDir.listFiles();

                                for (File file : files) {
                                    if (file.isFile()) {
                                        length += file.length();
                                    }
                                }
//                                String fileSize = decimalFormat.format(1.0 * length / (1024*1024));

                                long lastModifiedTime = privateDir.lastModified();
                                Date lastModifiedDate = new Date(lastModifiedTime);
                                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                                String formattedDate = dateFormat.format(lastModifiedDate);
                                listFolder.add(new FolderCLassContent(edtInputNameFolder.getText().toString(), 0 + " bản ghi - ", 0 + " MB", formattedDate ,R.drawable.ic_pink500_folder));
                                if (folderFilterTitle.getText().toString().equals("Ngày")){
                                    List<FolderCLassContent> sortedList = sortList(listFolder,"day", mSortByNameAscending);
                                }
                                else if(folderFilterTitle.getText().toString().equals("Tên")){
                                    List<FolderCLassContent> sortedList = sortList(listFolder,"name", mSortByNameAscending);
                                }
                                else if(folderFilterTitle.getText().toString().equals("Kích Thước")){
                                    List<FolderCLassContent> sortedList = sortList(listFolder,"size", mSortByNameAscending);
                                }
                                adapterFolder.notifyDataSetChanged();
                            }
                            else{
                                Toast.makeText(popupView.getContext(), "Tên thư mục đã tồn tại", Toast.LENGTH_LONG).show();
                                edtInputNameFolder.setText("");
                            }
                        }
                    }
                });
            }
        });

//        folderViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
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
                    if (folder.getName().equalsIgnoreCase("Thư mục riêng tư")){
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
                        String formattedDate = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(lastModifiedDate);
                        folderList.add(new FolderCLassContent(fileName, amountFileOfFolder + " bản ghi - ",fileSize+" MB", formattedDate, R.drawable.ic_pink500_folder));
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
            String hashedPassword = Base64.encodeToString(hash, Base64.DEFAULT);
            return hashedPassword;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    private List<FolderCLassContent> sortList(List<FolderCLassContent> listFolder, String sortBy, boolean ascending) {
        if (sortBy.equals("name")) {
            Collections.sort(listFolder, new Comparator<FolderCLassContent>() {
                @Override
                public int compare(FolderCLassContent folder1, FolderCLassContent folder2) {
                    return folder1.getTitle().compareToIgnoreCase(folder2.getTitle());
                }
            });
        } else if (sortBy.equals("size")) {
            Collections.sort(listFolder, new Comparator<FolderCLassContent>() {
                @Override
                public int compare(FolderCLassContent folder1, FolderCLassContent folder2) {
                    double size1 = Double.parseDouble(folder1.getSize().split(" ")[0].replace(",", "."));
                    double size2 = Double.parseDouble(folder2.getSize().split(" ")[0].replace(",", "."));
                    return Double.compare(size1, size2);
                }
            });
        } else if (sortBy.equals("day")) {
            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            Collections.sort(listFolder, new Comparator<FolderCLassContent>() {
                @Override
                public int compare(FolderCLassContent folder1, FolderCLassContent folder2) {
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
                }
            });
        }

        if (!ascending) {
            Collections.reverse(listFolder);
        }

        return listFolder;
    }

    private void addAllFolders(List<FolderCLassContent> folderList) {
        listFolder.addAll(folderList);
        searchListFolder.addAll(folderList);
        adapterFolder.notifyDataSetChanged();
    }

    private void filter(String query) {
        searchListFolder.clear();
        for (FolderCLassContent folder : listFolder) {
            if (folder.getTitle().toLowerCase().contains(query.toLowerCase())) {
                searchListFolder.add(folder);
            }
        }
        adapterFolder.notifyDataSetChanged();
    }

}
