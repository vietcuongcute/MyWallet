package com.example.mywallet;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ProfileFragment extends Fragment {

    TextView tvAvatar, tvProfileName, tvProfileEmail;
    TextView tvEditProfile, tvAppInfo, tvResetData, tvLogout;

    DatabaseHelper databaseHelper;
    SharedPreferences sharedPreferences;

    public ProfileFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        tvAvatar = view.findViewById(R.id.tvAvatar);
        tvProfileName = view.findViewById(R.id.tvProfileName);
        tvProfileEmail = view.findViewById(R.id.tvProfileEmail);

        tvEditProfile = view.findViewById(R.id.tvEditProfile);
        tvAppInfo = view.findViewById(R.id.tvAppInfo);
        tvResetData = view.findViewById(R.id.tvResetData);
        tvLogout = view.findViewById(R.id.tvLogout);

        sharedPreferences = requireContext().getSharedPreferences("MyWalletPrefs", Context.MODE_PRIVATE);
        databaseHelper = new DatabaseHelper(requireContext());

        loadUserInfo();

        tvEditProfile.setOnClickListener(v -> showEditNameDialog());

        tvAppInfo.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("MyWallet")
                    .setMessage("Ứng dụng quản lý chi tiêu cá nhân.\nPhiên bản: 1.0\nĐồ án môn Lập trình di động.")
                    .setPositiveButton("OK", null)
                    .show();
        });

        tvResetData.setOnClickListener(v -> showResetDataDialog());
        tvLogout.setOnClickListener(v -> showLogoutDialog());

        return view;
    }

    private void loadUserInfo() {
        String name = sharedPreferences.getString("user_name", "MyWallet User");
        String email = sharedPreferences.getString("user_email", "user@mywallet.com");

        tvProfileName.setText(name);
        tvProfileEmail.setText(email);

        if (!name.isEmpty()) {
            tvAvatar.setText(String.valueOf(name.charAt(0)).toUpperCase());
        }
    }

    private void showEditNameDialog() {
        EditText editText = new EditText(requireContext());
        editText.setHint("Nhập tên của bạn");
        editText.setSingleLine(true);
        editText.setText(tvProfileName.getText().toString());
        editText.setSelection(editText.getText().length());

        new AlertDialog.Builder(requireContext())
                .setTitle("Cập nhật tên")
                .setView(editText)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    String name = editText.getText().toString().trim();

                    if (name.isEmpty()) {
                        Toast.makeText(getContext(), "Tên không được để trống", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    sharedPreferences.edit()
                            .putString("user_name", name)
                            .apply();

                    loadUserInfo();

                    Toast.makeText(getContext(), "Đã cập nhật tên", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
    private void showResetDataDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xóa toàn bộ dữ liệu")
                .setMessage("Bạn có chắc muốn xóa tất cả giao dịch không? Hành động này không thể hoàn tác.")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    boolean deleted = databaseHelper.deleteAllTransactions();

                    if (deleted) {
                        Toast.makeText(getContext(), "Đã xóa toàn bộ dữ liệu", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Xóa dữ liệu thất bại", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
    private void showLogoutDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc muốn đăng xuất không?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> {
                    sharedPreferences.edit()
                            .putBoolean("is_logged_in", false)
                            .apply();

                    Intent intent = new Intent(requireContext(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}