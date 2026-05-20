package com.example.mywallet;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    Context context;
    ArrayList<TransactionModel> transactionList;
    DatabaseHelper databaseHelper;
    DecimalFormat decimalFormat;

    public TransactionAdapter(Context context, ArrayList<TransactionModel> transactionList) {
        this.context = context;
        this.transactionList = transactionList;
        this.databaseHelper = new DatabaseHelper(context);
        this.decimalFormat = new DecimalFormat("#,###");
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        TransactionModel transaction = transactionList.get(position);

        String type = transaction.getType();
        String category = transaction.getCategory();
        String note = transaction.getNote();
        String date = transaction.getDate();

        holder.tvCategory.setText(category);

        if (note == null || note.trim().isEmpty()) {
            holder.tvNoteDate.setText(date);
        } else {
            holder.tvNoteDate.setText(note + " • " + date);
        }

        if ("Thu nhập".equals(type)) {
            holder.tvAmount.setText("+" + decimalFormat.format(transaction.getAmount()) + " đ");
            holder.tvAmount.setTextColor(context.getColor(R.color.income));
        } else {
            holder.tvAmount.setText("-" + decimalFormat.format(transaction.getAmount()) + " đ");
            holder.tvAmount.setTextColor(context.getColor(R.color.expense));
        }

        setupTransactionIcon(holder, transaction);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, AddTransactionActivity.class);

            intent.putExtra("id", transaction.getId());
            intent.putExtra("amount", transaction.getAmount());
            intent.putExtra("type", transaction.getType());
            intent.putExtra("category", transaction.getCategory());
            intent.putExtra("date", transaction.getDate());
            intent.putExtra("note", transaction.getNote());

            context.startActivity(intent);
        });

        holder.itemView.setOnLongClickListener(v -> {
            showDeleteDialog(transaction, holder.getAdapterPosition());
            return true;
        });
    }

    private void setupTransactionIcon(TransactionViewHolder holder, TransactionModel transaction) {
        String type = transaction.getType();
        String category = transaction.getCategory();

        if ("Nạp hũ".equals(category)) {
            holder.tvIcon.setText("⇩");
            holder.tvIcon.setTextColor(context.getColor(R.color.primary));
            holder.tvIcon.setBackgroundTintList(
                    ColorStateList.valueOf(context.getColor(R.color.primary_light))
            );
            return;
        }

        if ("Rút hũ".equals(category)) {
            holder.tvIcon.setText("⇧");
            holder.tvIcon.setTextColor(context.getColor(R.color.income));
            holder.tvIcon.setBackgroundTintList(
                    ColorStateList.valueOf(context.getColor(R.color.primary_light))
            );
            return;
        }

        if ("Thu nhập".equals(type)) {
            holder.tvIcon.setText("+");
            holder.tvIcon.setTextColor(context.getColor(R.color.income));
            holder.tvIcon.setBackgroundTintList(
                    ColorStateList.valueOf(context.getColor(R.color.primary_light))
            );
        } else {
            holder.tvIcon.setText("-");
            holder.tvIcon.setTextColor(context.getColor(R.color.expense));
            holder.tvIcon.setBackgroundTintList(
                    ColorStateList.valueOf(context.getColor(R.color.primary_light))
            );
        }
    }

    private void showDeleteDialog(TransactionModel transaction, int position) {
        new AlertDialog.Builder(context)
                .setTitle("Xóa giao dịch")
                .setMessage("Bạn có chắc muốn xóa giao dịch này không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    boolean deleted = databaseHelper.deleteTransaction(transaction.getId());

                    if (deleted) {
                        transactionList.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, transactionList.size());
                        Toast.makeText(context, "Đã xóa giao dịch", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Xóa thất bại", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    public void updateList(ArrayList<TransactionModel> newList) {
        transactionList = newList;
        notifyDataSetChanged();
    }

    public static class TransactionViewHolder extends RecyclerView.ViewHolder {

        TextView tvIcon, tvCategory, tvNoteDate, tvAmount;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);

            tvIcon = itemView.findViewById(R.id.tvIcon);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvNoteDate = itemView.findViewById(R.id.tvNoteDate);
            tvAmount = itemView.findViewById(R.id.tvAmount);
        }
    }
}