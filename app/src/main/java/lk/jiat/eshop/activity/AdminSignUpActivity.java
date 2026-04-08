package lk.jiat.eshop.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import lk.jiat.eshop.databinding.ActivityAdminSignUpBinding;
import lk.jiat.eshop.model.Admin;

public class AdminSignUpActivity extends AppCompatActivity {

    private ActivityAdminSignUpBinding binding;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminSignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        binding.adminSignupBtnSignin.setOnClickListener(v -> finish());

        binding.adminSignupBtnSignup.setOnClickListener(view -> {
            String name = binding.adminSignupInputName.getText().toString().trim();
            String email = binding.adminSignupInputEmail.getText().toString().trim();
            String password = binding.adminSignupInputPassword.getText().toString().trim();
            String retypePassword = binding.adminSignupInputRetypePassword.getText().toString().trim();

            if (name.isEmpty()) {
                binding.adminSignupInputName.setError("Name is required");
                binding.adminSignupInputName.requestFocus();
                return;
            }

            if (email.isEmpty()) {
                binding.adminSignupInputEmail.setError("Email is required");
                binding.adminSignupInputEmail.requestFocus();
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.adminSignupInputEmail.setError("Enter valid email");
                binding.adminSignupInputEmail.requestFocus();
                return;
            }

            if (password.isEmpty()) {
                binding.adminSignupInputPassword.setError("Password is required");
                binding.adminSignupInputPassword.requestFocus();
                return;
            }

            if (password.length() < 6) {
                binding.adminSignupInputPassword.setError("Password must be at least 6 characters");
                binding.adminSignupInputPassword.requestFocus();
                return;
            }

            if (!retypePassword.equals(password)) {
                binding.adminSignupInputRetypePassword.setError("Passwords do not match");
                binding.adminSignupInputRetypePassword.requestFocus();
                return;
            }

            firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        String uid = task.getResult().getUser().getUid();
                        Admin admin = Admin.builder()
                                .uid(uid)
                                .name(name)
                                .email(email)
                                .profilePicUrl("")
                                .build();

                        // Save to "admins" collection
                        firebaseFirestore.collection("admins").document(uid).set(admin)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Toast.makeText(getApplicationContext(), "Admin account created!", Toast.LENGTH_SHORT).show();
                                        finish(); // Go back to Login
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(AdminSignUpActivity.this, "Signup failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        });
    }
}
