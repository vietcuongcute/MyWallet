package com.example.mywallet;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class BudgetAdapter extends RecyclerView.Adapter<BudgetAdapter.BudgetViewHolder> {

    Context context;
    ArrayList<BudgetModel> budgetList;
    DatabaseHelper databaseHelper;
    DecimalFormat decimalFormat;

    boolean hideBalance = false;
    boolean hideProgress = false;

    public BudgetAdapter(Context context, ArrayList<BudgetModel> budgetList, double totalExpense) {
        this.context = context;
        this.budgetList = budgetList;
        this.databaseHelper = new DatabaseHelper(context);
        this.decimalFormat = new DecimalFormat("#,###");
    }

    @NonNull
    @Override
    public BudgetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_budget, parent, false);
        return new BudgetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BudgetViewHolder holder, int position) {
        BudgetModel budget = budgetList.get(position);

        holder.tvBudgetIcon.setText(budget.getIcon());
        holder.tvBudgetName.setText(budget.getName());

        String color = budget.getColor();
        if (color == null || color.trim().isEmpty()) {
            color = "#FF9AAD";
        }

        holder.layoutBudgetHeader.setBackgroundColor(Color.parseColor(color));

        double target = budget.getLimitAmount();
        double saved = budget.getCurrentAmount();
        int cycleDays = budget.getCycleDays();

        if (cycleDays <= 0) {
            cycleDays = 12;
        }

        int percent = 0;

        if (target > 0) {
            percent = (int) Math.round((saved / target) * 100);
        }

        if (percent > 100) {
            percent = 100;
        }

        double remainingToSave = target - saved;

        if (remainingToSave < 0) {
            remainingToSave = 0;
        }

        double needSavePerDay = remainingToSave / cycleDays;

        if (hideBalance) {
            holder.tvBudgetRemain.setText("****");
            holder.tvBudgetLimit.setText("****");
            holder.tvBudgetSpent.setText("****");
            holder.tvBudgetDailyLimit.setText("**** / ngày");
        } else {
            holder.tvBudgetRemain.setText(decimalFormat.format(saved));
            holder.tvBudgetLimit.setText(decimalFormat.format(target));
            holder.tvBudgetSpent.setText(decimalFormat.format(saved));
            holder.tvBudgetDailyLimit.setText(decimalFormat.format(needSavePerDay) + " / ngày");
        }

        if (hideProgress) {
            holder.tvBudgetPercent.setText("-- %");
            holder.progressBudget.setProgress(0);
            holder.progressBudget.setAlpha(0.35f);
        } else {
            holder.tvBudgetPercent.setText(target > 0 ? percent + " %" : "-- %");
            holder.progressBudget.setProgress(percent);
            holder.progressBudget.setAlpha(1f);
        }

        holder.tvBudgetCycleDays.setText(cycleDays + " ngày");

        holder.itemView.setOnClickListener(v -> showBudgetActionDialog(budget, holder.getAdapterPosition()));

        holder.itemView.setOnLongClickListener(v -> {
            showDeleteDialog(budget, holder.getAdapterPosition());
            return true;
        });
    }

    public void setHideBalance(boolean hideBalance) {
        this.hideBalance = hideBalance;
        notifyDataSetChanged();
    }

    public void setHideProgress(boolean hideProgress) {
        this.hideProgress = hideProgress;
        notifyDataSetChanged();
    }

    private void showBudgetActionDialog(BudgetModel budget, int position) {
        String[] options = {
                "Sửa hũ",
                "Thêm tiết kiệm",
                "Rút khỏi hũ"
        };

        new AlertDialog.Builder(context)
                .setTitle(budget.getName())
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        openEditBudget(budget);
                    } else if (which == 1) {
                        showMoneyDialog(budget, position, true);
                    } else if (which == 2) {
                        showMoneyDialog(budget, position, false);
                    }
                })
                .show();
    }

    private void openEditBudget(BudgetModel budget) {
        Intent intent = new Intent(context, AddBudgetActivity.class);

        intent.putExtra("id", budget.getId());
        intent.putExtra("name", budget.getName());
        intent.putExtra("icon", budget.getIcon());
        intent.putExtra("limit", budget.getLimitAmount());
        intent.putExtra("current", budget.getCurrentAmount());
        intent.putExtra("cycle_days", budget.getCycleDays());
        intent.putExtra("color", budget.getColor());

        context.startActivity(intent);
    }

    private void showMoneyDialog(BudgetModel budget, int position, boolean isDeposit) {
        EditText editText = new EditText(context);
        editText.setHint("Nhập số tiền");
        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        editText.setSingleLine(true);
        editText.setPadding(40, 20, 40, 20);

        String title = isDeposit ? "Thêm tiền tiết kiệm" : "Rút tiền khỏi hũ";

        double walletBalance = databaseHelper.getBalance();

        String message;

        if (isDeposit) {
            double remainingGoal = budget.getLimitAmount() - budget.getCurrentAmount();

            if (remainingGoal < 0) {
                remainingGoal = 0;
            }

            message = "Hũ: " + budget.getName()
                    + "\nSố dư ví hiện tại: " + decimalFormat.format(walletBalance) + " đ"
                    + "\nCòn thiếu mục tiêu: " + decimalFormat.format(remainingGoal) + " đ";
        } else {
            message = "Hũ: " + budget.getName()
                    + "\nĐã tiết kiệm: " + decimalFormat.format(budget.getCurrentAmount()) + " đ";
        }

        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setView(editText)
                .setPositiveButton(isDeposit ? "Thêm" : "Rút", (dialog, which) -> {
                    String amountText = editText.getText().toString().trim();

                    if (amountText.isEmpty()) {
                        Toast.makeText(context, "Vui lòng nhập số tiền", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    double amount;

                    try {
                        amount = Double.parseDouble(amountText);
                    } catch (NumberFormatException e) {
                        Toast.makeText(context, "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (amount <= 0) {
                        Toast.makeText(context, "Số tiền phải lớn hơn 0", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (isDeposit) {
                        if (amount > databaseHelper.getBalance()) {
                            Toast.makeText(
                                    context,
                                    "Số dư ví không đủ. Số dư hiện tại: "
                                            + decimalFormat.format(databaseHelper.getBalance()) + " đ",
                                    Toast.LENGTH_LONG
                            ).show();
                            return;
                        }

                        double newAmount = budget.getCurrentAmount() + amount;

                        if (newAmount > budget.getLimitAmount()) {
                            Toast.makeText(
                                    context,
                                    "Số tiền thêm vượt quá mục tiêu tiết kiệm",
                                    Toast.LENGTH_LONG
                            ).show();
                            return;
                        }

                        boolean updated = databaseHelper.addMoneyToBudgetAndLog(budget, amount);

                        if (updated) {
                            budget.setCurrentAmount(newAmount);
                            notifyItemChanged(position);
                            Toast.makeText(context, "Đã thêm tiền tiết kiệm", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        if (amount > budget.getCurrentAmount()) {
                            Toast.makeText(context, "Số tiền rút lớn hơn số tiền đã tiết kiệm", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        double newAmount = budget.getCurrentAmount() - amount;

                        boolean updated = databaseHelper.withdrawMoneyFromBudgetAndLog(budget, amount);

                        if (updated) {
                            budget.setCurrentAmount(newAmount);
                            notifyItemChanged(position);
                            Toast.makeText(context, "Đã rút tiền khỏi hũ", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showDeleteDialog(BudgetModel budget, int position) {
        new AlertDialog.Builder(context)
                .setTitle("Xóa hũ tiết kiệm")
                .setMessage("Bạn có chắc muốn xóa hũ \"" + budget.getName() + "\" không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    boolean deleted = databaseHelper.deleteBudget(budget.getId());

                    if (deleted) {
                        budgetList.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, budgetList.size());
                        Toast.makeText(context, "Đã xóa hũ", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Xóa hũ thất bại", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    public int getItemCount() {
        return budgetList.size();
    }

    public void updateList(ArrayList<BudgetModel> newList, double newTotalExpense) {
        budgetList = newList;
        notifyDataSetChanged();
    }

    public static class BudgetViewHolder extends RecyclerView.ViewHolder {

        LinearLayout layoutBudgetHeader;
        TextView tvBudgetIcon, tvBudgetName, tvBudgetRemain, tvBudgetPercent;
        TextView tvBudgetLimit, tvBudgetSpent, tvBudgetCycleDays, tvBudgetDailyLimit;
        ProgressBar progressBudget;

        public BudgetViewHolder(@NonNull View itemView) {
            super(itemView);

            layoutBudgetHeader = itemView.findViewById(R.id.layoutBudgetHeader);

            tvBudgetIcon = itemView.findViewById(R.id.tvBudgetIcon);
            tvBudgetName = itemView.findViewById(R.id.tvBudgetName);
            tvBudgetRemain = itemView.findViewById(R.id.tvBudgetRemain);
            tvBudgetPercent = itemView.findViewById(R.id.tvBudgetPercent);
            tvBudgetLimit = itemView.findViewById(R.id.tvBudgetLimit);
            tvBudgetSpent = itemView.findViewById(R.id.tvBudgetSpent);
            tvBudgetCycleDays = itemView.findViewById(R.id.tvBudgetCycleDays);
            tvBudgetDailyLimit = itemView.findViewById(R.id.tvBudgetDailyLimit);

            progressBudget = itemView.findViewById(R.id.progressBudget);
        }
    }
}