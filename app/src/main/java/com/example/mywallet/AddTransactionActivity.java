package com.example.mywallet;

import android.app.DatePickerDialog;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddTransactionActivity extends AppCompatActivity {

    TextView tvAddTitle, tvAddSubtitle;
    TextInputEditText edtAmount, edtNote, edtDate;
    AutoCompleteTextView dropdownType, dropdownCategory;
    MaterialButton btnSave, btnBack;

    Calendar calendar;
    SimpleDateFormat dateFormat;

    DatabaseHelper databaseHelper;

    int transactionId = -1;
    boolean isEditMode = false;

    String[] incomeCategories = {
            "Lương",
            "Thưởng",
            "Phụ cấp",
            "Đầu tư",
            "Khác"
    };

    String[] expenseCategories = {
            "Ăn uống",
            "Đi lại",
            "Mua sắm",
            "Học tập",
            "Hóa đơn",
            "Giải trí",
            "Khác"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);

        tvAddTitle = findViewById(R.id.tvAddTitle);
        tvAddSubtitle = findViewById(R.id.tvAddSubtitle);

        edtAmount = findViewById(R.id.edtAmount);
        edtNote = findViewById(R.id.edtNote);
        edtDate = findViewById(R.id.edtDate);

        dropdownType = findViewById(R.id.dropdownType);
        dropdownCategory = findViewById(R.id.dropdownCategory);

        btnSave = findViewById(R.id.btnSave);
        btnBack = findViewById(R.id.btnBack);

        databaseHelper = new DatabaseHelper(this);

        calendar = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        setupTypeDropdown();
        setupDatePicker();
        receiveData();

        btnBack.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveTransaction());
    }

    private void setupTypeDropdown() {
        String[] types = {"Thu nhập", "Chi tiêu"};

        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                types
        );

        dropdownType.setAdapter(typeAdapter);

        dropdownType.setOnItemClickListener((parent, view, position, id) -> {
            String selectedType = parent.getItemAtPosition(position).toString();

            updateScreenByType(selectedType);

            dropdownCategory.setText("", false);
            setupCategoryDropdown(selectedType);
        });
    }

    private void setupCategoryDropdown(String type) {
        String[] categories;

        if ("Thu nhập".equals(type)) {
            categories = incomeCategories;
        } else {
            categories = expenseCategories;
        }

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                categories
        );

        dropdownCategory.setAdapter(categoryAdapter);
    }

    private void setupDatePicker() {
        edtDate.setText(dateFormat.format(calendar.getTime()));

        edtDate.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (view, year, month, dayOfMonth) -> {
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, month);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        edtDate.setText(dateFormat.format(calendar.getTime()));
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );

            datePickerDialog.show();
        });
    }

    private void receiveData() {
        transactionId = getIntent().getIntExtra("id", -1);

        if (transactionId != -1) {
            isEditMode = true;

            double amount = getIntent().getDoubleExtra("amount", 0);
            String type = getIntent().getStringExtra("type");
            String category = getIntent().getStringExtra("category");
            String date = getIntent().getStringExtra("date");
            String note = getIntent().getStringExtra("note");

            edtAmount.setText(String.valueOf((long) amount));
            dropdownType.setText(type, false);

            setupCategoryDropdown(type);
            dropdownCategory.setText(category, false);

            edtDate.setText(date);
            edtNote.setText(note);

            tvAddTitle.setText("Sửa giao dịch");
            tvAddSubtitle.setText("Cập nhật thông tin giao dịch của bạn");
            btnSave.setText("Cập nhật giao dịch");

            updateSaveButtonColor(type);
        } else {
            String type = getIntent().getStringExtra("type");

            if (type != null) {
                dropdownType.setText(type, false);
                setupCategoryDropdown(type);
                updateScreenByType(type);
            } else {
                dropdownType.setText("Chi tiêu", false);
                setupCategoryDropdown("Chi tiêu");
                updateScreenByType("Chi tiêu");
            }
        }
    }

    private void updateScreenByType(String type) {
        if ("Thu nhập".equals(type)) {
            tvAddTitle.setText("Thêm thu nhập");
            tvAddSubtitle.setText("Ghi lại khoản tiền bạn nhận được");
        } else {
            tvAddTitle.setText("Thêm chi tiêu");
            tvAddSubtitle.setText("Ghi lại khoản tiền bạn đã chi");
        }

        updateSaveButtonColor(type);
    }

    private void updateSaveButtonColor(String type) {
        if ("Thu nhập".equals(type)) {
            btnSave.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.income)));
        } else if ("Chi tiêu".equals(type)) {
            btnSave.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.expense)));
        } else {
            btnSave.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.primary)));
        }
    }

    private void saveTransaction() {
        String amountText = edtAmount.getText().toString().trim();
        String note = edtNote.getText().toString().trim();
        String date = edtDate.getText().toString().trim();
        String transactionType = dropdownType.getText().toString().trim();
        String category = dropdownCategory.getText().toString().trim();

        if (amountText.isEmpty()) {
            edtAmount.setError("Nhập số tiền");
            return;
        }

        if (transactionType.isEmpty()) {
            Toast.makeText(this, "Chọn loại giao dịch", Toast.LENGTH_SHORT).show();
            return;
        }

        if (category.isEmpty()) {
            Toast.makeText(this, "Chọn danh mục", Toast.LENGTH_SHORT).show();
            return;
        }

        if (date.isEmpty()) {
            Toast.makeText(this, "Chọn ngày", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount;

        try {
            amount = Double.parseDouble(amountText);
        } catch (NumberFormatException e) {
            edtAmount.setError("Số tiền không hợp lệ");
            return;
        }

        boolean success;

        if (isEditMode) {
            TransactionModel transaction = new TransactionModel(
                    transactionId,
                    amount,
                    transactionType,
                    category,
                    date,
                    note
            );

            success = databaseHelper.updateTransaction(transaction);

            if (success) {
                Toast.makeText(this, "Đã cập nhật giao dịch", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
            }
        } else {
            TransactionModel transaction = new TransactionModel(
                    amount,
                    transactionType,
                    category,
                    date,
                    note
            );

            success = databaseHelper.insertTransaction(transaction);

            if (success) {
                Toast.makeText(this, "Đã lưu giao dịch", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Lưu thất bại", Toast.LENGTH_SHORT).show();
            }
        }
    }
}