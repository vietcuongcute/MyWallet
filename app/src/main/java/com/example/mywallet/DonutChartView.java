package com.example.mywallet;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class DonutChartView extends View {

    public static class ChartItem {
        public String name;
        public double value;
        public int color;

        public ChartItem(String name, double value, int color) {
            this.name = name;
            this.value = value;
            this.color = color;
        }
    }

    private ArrayList<ChartItem> items = new ArrayList<>();
    private double total = 0;

    private Paint paint;
    private Paint textPaint;
    private Paint centerPaint;
    private Paint smallTextPaint;

    private DecimalFormat decimalFormat = new DecimalFormat("#,###");

    public DonutChartView(Context context) {
        super(context);
        init();
    }

    public DonutChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DonutChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);

        centerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        centerPaint.setStyle(Paint.Style.FILL);
        centerPaint.setColor(Color.WHITE);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.BLACK);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(34f);

        smallTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        smallTextPaint.setColor(Color.WHITE);
        smallTextPaint.setTextAlign(Paint.Align.CENTER);
        smallTextPaint.setTextSize(24f);
    }

    public void setData(ArrayList<ChartItem> newItems) {
        items.clear();
        total = 0;

        if (newItems != null) {
            items.addAll(newItems);

            for (ChartItem item : items) {
                total += item.value;
            }
        }

        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        int size = Math.min(width, height);
        float padding = 28f;

        float left = (width - size) / 2f + padding;
        float top = (height - size) / 2f + padding;
        float right = (width + size) / 2f - padding;
        float bottom = (height + size) / 2f - padding;

        RectF rectF = new RectF(left, top, right, bottom);

        float centerX = width / 2f;
        float centerY = height / 2f;

        if (items.isEmpty() || total <= 0) {
            paint.setColor(Color.parseColor("#E5E7EB"));
            canvas.drawArc(rectF, 0, 360, true, paint);

            float holeRadius = size * 0.23f;
            canvas.drawCircle(centerX, centerY, holeRadius, centerPaint);

            textPaint.setColor(Color.parseColor("#111827"));
            textPaint.setTextSize(30f);
            canvas.drawText("Chưa có dữ liệu", centerX, centerY + 10f, textPaint);
            return;
        }

        float startAngle = -90f;

        for (ChartItem item : items) {
            float sweepAngle = (float) ((item.value / total) * 360f);

            paint.setColor(item.color);
            canvas.drawArc(rectF, startAngle, sweepAngle, true, paint);

            startAngle += sweepAngle;
        }

        float holeRadius = size * 0.24f;
        centerPaint.setColor(Color.WHITE);
        canvas.drawCircle(centerX, centerY, holeRadius, centerPaint);

        Paint ringPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        ringPaint.setStyle(Paint.Style.STROKE);
        ringPaint.setStrokeWidth(16f);
        ringPaint.setColor(Color.parseColor("#BBF7D0"));
        canvas.drawCircle(centerX, centerY, holeRadius + 8f, ringPaint);

        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(34f);
        canvas.drawText("Chi tiêu", centerX, centerY + 10f, textPaint);

        if (items.size() == 1) {
            ChartItem item = items.get(0);

            float labelY = bottom - 80f;

            smallTextPaint.setColor(Color.BLACK);
            smallTextPaint.setTextSize(26f);
            canvas.drawText("100", centerX, labelY, smallTextPaint);

            smallTextPaint.setColor(Color.WHITE);
            smallTextPaint.setTextSize(24f);
            canvas.drawText(item.name, centerX, labelY + 32f, smallTextPaint);
        } else {
            drawSliceLabels(canvas, rectF, centerX, centerY);
        }
    }

    private void drawSliceLabels(Canvas canvas, RectF rectF, float centerX, float centerY) {
        float startAngle = -90f;
        float radius = rectF.width() / 2.8f;

        for (ChartItem item : items) {
            float sweepAngle = (float) ((item.value / total) * 360f);
            float middleAngle = startAngle + sweepAngle / 2f;

            double radians = Math.toRadians(middleAngle);

            float x = centerX + (float) Math.cos(radians) * radius;
            float y = centerY + (float) Math.sin(radians) * radius;

            int percent = (int) Math.round((item.value / total) * 100);

            smallTextPaint.setColor(Color.WHITE);
            smallTextPaint.setTextSize(22f);

            if (percent >= 8) {
                canvas.drawText(percent + "%", x, y, smallTextPaint);
                canvas.drawText(item.name, x, y + 28f, smallTextPaint);
            }

            startAngle += sweepAngle;
        }
    }
}