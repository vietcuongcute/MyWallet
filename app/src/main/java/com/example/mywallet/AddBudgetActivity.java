package com.example.mywallet;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class AddBudgetActivity extends AppCompatActivity {

    TextView tvBudgetFormTitle, tvBudgetFormSubtitle;
    TextInputEditText edtBudgetName, edtBudgetLimit, edtBudgetCycleDays;
    AutoCompleteTextView dropdownBudgetIcon;
    MaterialButton btnBackBudgetForm, btnSaveBudget;

    DatabaseHelper databaseHelper;

    String selectedColor = "#FF9AAD";

    int budgetId = -1;
    boolean isEditMode = false;
    double currentAmount = 0;
    int cycleDays = 12;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_budget);

        tvBudgetFormTitle = findViewById(R.id.tvBudgetFormTitle);
        tvBudgetFormSubtitle = findViewById(R.id.tvBudgetFormSubtitle);

        edtBudgetName = findViewById(R.id.edtBudgetName);
        edtBudgetLimit = findViewById(R.id.edtBudgetLimit);
        edtBudgetCycleDays = findViewById(R.id.edtBudgetCycleDays);
        dropdownBudgetIcon = findViewById(R.id.dropdownBudgetIcon);

        btnBackBudgetForm = findViewById(R.id.btnBackBudgetForm);
        btnSaveBudget = findViewById(R.id.btnSaveBudget);

        databaseHelper = new DatabaseHelper(this);

        setupIconDropdown();
        receiveData();

        btnBackBudgetForm.setOnClickListener(v -> finish());
        btnSaveBudget.setOnClickListener(v -> saveBudget());
    }

    private void setupIconDropdown() {
        String[] icons = {
                "💖",
                "🍽",
                "🛍",
                "🎮",
                "📚",
                "🏠",
                "🚗",
                "✈",
                "🎁",
                "💰"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                icons
        );

        dropdownBudgetIcon.setAdapter(adapter);
        dropdownBudgetIcon.setText("💖", false);

        dropdownBudgetIcon.setOnItemClickListener((parent, view, position, id) -> {
            String icon = parent.getItemAtPosition(position).toString();
            selectedColor = getColorByIcon(icon);
        });
    }

    private void receiveData() {
        budgetId = getIntent().getIntExtra("id", -1);

        if (budgetId != -1) {
            isEditMode = true;

            String name = getIntent().getStringExtra("name");
            String icon = getIntent().getStringExtra("icon");
            double limit = getIntent().getDoubleExtra("limit", 0);
            currentAmount = getIntent().getDoubleExtra("current", 0);
            cycleDays = getIntent().getIntExtra("cycle_days", 12);
            String color = getIntent().getStringExtra("color");

            if (cycleDays <= 0) {
                cycleDays = 12;
            }

            edtBudgetName.setText(name);
            dropdownBudgetIcon.setText(icon, false);
            edtBudgetLimit.setText(String.valueOf((long) limit));
            edtBudgetCycleDays.setText(String.valueOf(cycleDays));

            if (color != null && !color.trim().isEmpty()) {
                selectedColor = color;
            } else {
                selectedColor = getColorByIcon(icon);
            }

            tvBudgetFormTitle.setText("Sửa hũ chi tiêu");
            tvBudgetFormSubtitle.setText("Cập nhật thông tin hũ chi của bạn");
            btnSaveBudget.setText("Cập nhật hũ chi");
        } else {
            edtBudgetCycleDays.setText("12");
        }
    }

    private String getColorByIcon(String icon) {
        if ("🍽".equals(icon)) {
            return "#FF6F6F";
        } else if ("🛍".equals(icon)) {
            return "#FF9F43";
        } else if ("🎮".equals(icon)) {
            return "#6C5CE7";
        } else if ("📚".equals(icon)) {
            return "#00B894";
        } else if ("🏠".equals(icon)) {
            return "#0984E3";
        } else if ("🚗".equals(icon)) {
            return "#636E72";
        } else if ("✈".equals(icon)) {
            return "#00CEC9";
        } else if ("🎁".equals(icon)) {
            return "#E84393";
        } else if ("💰".equals(icon)) {
            return "#16A34A";
        } else {
            return "#FF9AAD";
        }
    }

    private void saveBudget() {
        String name = edtBudgetName.getText().toString().trim();
        String limitText = edtBudgetLimit.getText().toString().trim();
        String cycleText = edtBudgetCycleDays.getText().toString().trim();
        String icon = dropdownBudgetIcon.getText().toString().trim();

        if (name.isEmpty()) {
            edtBudgetName.setError("Nhập tên hũ");
            return;
        }

        if (icon.isEmpty()) {
            Toast.makeText(this, "Chọn icon", Toast.LENGTH_SHORT).show();
            return;
        }

        if (limitText.isEmpty()) {
            edtBudgetLimit.setError("Nhập ngân sách");
            return;
        }

        if (cycleText.isEmpty()) {
            edtBudgetCycleDays.setError("Nhập chu kỳ ngày");
            return;
        }

        double limit;
        int newCycleDays;

        try {
            limit = Double.parseDouble(limitText);
        } catch (NumberFormatException e) {
            edtBudgetLimit.setError("Ngân sách không hợp lệ");
            return;
        }

        try {
            newCycleDays = Integer.parseInt(cycleText);
        } catch (NumberFormatException e) {
            edtBudgetCycleDays.setError("Chu kỳ không hợp lệ");
            return;
        }

        if (limit <= 0) {
            edtBudgetLimit.setError("Ngân sách phải lớn hơn 0");
            return;
        }

        if (newCycleDays <= 0) {
            edtBudgetCycleDays.setError("Chu kỳ phải lớn hơn 0");
            return;
        }

        boolean success;

        if (isEditMode) {
            BudgetModel budget = new BudgetModel(
                    budgetId,
                    name,
                    icon,
                    limit,
                    currentAmount,
                    newCycleDays,
                    selectedColor
            );

            success = databaseHelper.updateBudget(budget);

            if (success) {
                Toast.makeText(this, "Đã cập nhật hũ chi", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Cập nhật hũ thất bại", Toast.LENGTH_SHORT).show();
            }
        } else {
            BudgetModel budget = new BudgetModel(
                    name,
                    icon,
                    limit,
                    0,
                    newCycleDays,
                    selectedColor
            );

            success = databaseHelper.insertBudget(budget);

            if (success) {
                Toast.makeText(this, "Đã thêm hũ chi", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Thêm hũ thất bại", Toast.LENGTH_SHORT).show();
            }
        }
    }
}