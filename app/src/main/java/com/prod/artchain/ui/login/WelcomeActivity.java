package com.prod.artchain.ui.login;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import com.prod.artchain.R;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView; // Import ImageView
import androidx.appcompat.app.AppCompatActivity;

public class WelcomeActivity extends AppCompatActivity {

    private float x1, x2;
    static final int MIN_DISTANCE = 150; // Khoảng cách tối thiểu để xác nhận là vuốt

    private ImageView ivSwipeIcon; // Khai báo ImageView
    private ObjectAnimator swipeAnimator; // Khai báo animator

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        ivSwipeIcon = findViewById(R.id.iv_swipe_icon); // Ánh xạ ImageView từ layout

        // Khởi tạo và bắt đầu animation cho icon
        startSwipeAnimation();
    }

    private void startSwipeAnimation() {
        // Tạo animation di chuyển từ vị trí hiện tại sang phải 20dp rồi quay lại
        swipeAnimator = ObjectAnimator.ofFloat(ivSwipeIcon, "translationX", 0f, -20f, 0f);
        swipeAnimator.setDuration(1500); // Thời gian 1.5 giây cho một chu kỳ
        swipeAnimator.setInterpolator(new AccelerateDecelerateInterpolator()); // Kiểu chuyển động
        swipeAnimator.setRepeatCount(ValueAnimator.INFINITE); // Lặp vô hạn
        swipeAnimator.setRepeatMode(ValueAnimator.RESTART); // Bắt đầu lại từ đầu sau mỗi lần lặp
        swipeAnimator.start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent touchEvent) {
        switch (touchEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                x1 = touchEvent.getX();
                break;

            case MotionEvent.ACTION_UP:
                x2 = touchEvent.getX();
                float deltaX = x2 - x1;

                if (- deltaX > MIN_DISTANCE) {
                    // Dừng animation khi người dùng đã vuốt
                    if (swipeAnimator != null) {
                        swipeAnimator.cancel();
                    }
                    goToNextActivity();
                }
                break;
        }
        return super.onTouchEvent(touchEvent);
    }

    private void goToNextActivity() {
        // Đảm bảo LoginActivity.class là đúng đường dẫn của LoginActivity của bạn
        Intent intent = new Intent(WelcomeActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Dừng animation khi Activity bị hủy để tránh rò rỉ bộ nhớ
        if (swipeAnimator != null) {
            swipeAnimator.cancel();
            swipeAnimator = null;
        }
    }
}