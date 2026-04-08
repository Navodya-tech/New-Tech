package lk.jiat.eshop.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import lk.jiat.eshop.R;
import lk.jiat.eshop.databinding.ActivityAdminSignInBinding;

public class AdminSignInActivity extends AppCompatActivity {

    private ActivityAdminSignInBinding binding;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminSignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        binding.adminSigninBtnUserPortal.setOnClickListener(v -> finish());

        binding.adminSigninBtnSignup.setOnClickListener(v -> {
            Intent intent = new Intent(AdminSignInActivity.this, AdminSignUpActivity.class);
            startActivity(intent);
        });

        binding.adminSigninBtnSignin.setOnClickListener(view -> {
            String email = binding.adminSigninInputEmail.getText().toString().trim();
            String password = binding.adminSigninInputPassword.getText().toString().trim();

            if (email.isEmpty()) {
                binding.adminSigninInputEmail.setError("Email is required");
                binding.adminSigninInputEmail.requestFocus();
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.adminSigninInputEmail.setError("Enter valid email");
                binding.adminSigninInputEmail.requestFocus();
                return;
            }

            if (password.isEmpty()) {
                binding.adminSigninInputPassword.setError("Password is required");
                binding.adminSigninInputPassword.requestFocus();
                return;
            }

            firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        checkAdminExists(firebaseAuth.getCurrentUser().getUid());
                    } else {
                        Toast.makeText(AdminSignInActivity.this, "Authentication failed", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        });
    }

    private void checkAdminExists(String uid) {
        firebaseFirestore.collection("admins").document(uid).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                            // UID exists in admins collection
                            Intent intent = new Intent(AdminSignInActivity.this, AdminMainActivity.class);
                            startActivity(intent);
                            finishAffinity(); // Close all other activities
                        } else {
                            // Not an admin
                            firebaseAuth.signOut();
                            Toast.makeText(AdminSignInActivity.this, "Access Denied: Not an Admin account", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}
