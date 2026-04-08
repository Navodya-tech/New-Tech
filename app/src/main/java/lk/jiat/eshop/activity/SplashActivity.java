package lk.jiat.eshop.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import lk.jiat.eshop.R;

public class SplashActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore mDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        mDb = FirebaseFirestore.getInstance();

        EdgeToEdge.enable(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getWindow().setDecorFitsSystemWindows(false);
            WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
            }
        } else {
            getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN
            );
        }

        setContentView(R.layout.activity_splash);

        ImageView imageView = findViewById(R.id.splashLogo);
        Glide.with(this)
                .asBitmap()
                .load(R.drawable.app_logo)
                .override(300)
                .into(imageView);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            findViewById(R.id.splashProgressBar).setVisibility(View.VISIBLE);
        }, 1000);

        new Handler(Looper.getMainLooper()).postDelayed(this::checkSession, 3000);
    }

    private void checkSession() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // No user logged in, go to User side as guest
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            finish();
        } else {
            // User is logged in, check if they are an admin
            mDb.collection("admins").document(currentUser.getUid()).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                            // User is an admin
                            startActivity(new Intent(SplashActivity.this, AdminMainActivity.class));
                        } else {
                            // User is a regular customer
                            startActivity(new Intent(SplashActivity.this, MainActivity.class));
                        }
                        finish();
                    });
        }
    }
}
