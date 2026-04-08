package lk.jiat.eshop.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import lk.jiat.eshop.R;
import lk.jiat.eshop.databinding.ActivityAdminMainBinding;
import lk.jiat.eshop.databinding.SideNavHeaderBinding;
import lk.jiat.eshop.fragment.AdminDashboardFragment;
import lk.jiat.eshop.fragment.AdminOrdersFragment;
import lk.jiat.eshop.fragment.ManageCategoriesFragment;
import lk.jiat.eshop.fragment.ManageProductsFragment;
import lk.jiat.eshop.model.Admin;

public class AdminMainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private ActivityAdminMainBinding binding;
    private SideNavHeaderBinding headerBinding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mDb;
    private FirebaseStorage mStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        mDb = FirebaseFirestore.getInstance();
        mStorage = FirebaseStorage.getInstance();

        setupNavigation();
        syncAdminSession();

        // Initial Fragment
        if (savedInstanceState == null) {
            navigateTo(new AdminDashboardFragment());
            binding.adminSideNavigationView.setCheckedItem(R.id.admin_nav_dashboard);
        }
    }

    private void setupNavigation() {
        setSupportActionBar(binding.adminToolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, binding.adminDrawerLayout, binding.adminToolbar, R.string.drawer_open, R.string.drawer_close);
        binding.adminDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        binding.adminSideNavigationView.setNavigationItemSelectedListener(this);

        // Header Binding
        View headerView = binding.adminSideNavigationView.getHeaderView(0);
        headerBinding = SideNavHeaderBinding.bind(headerView);
    }

    private void syncAdminSession() {
        FirebaseUser currentAdmin = mAuth.getCurrentUser();
        if (currentAdmin != null) {
            mDb.collection("admins").document(currentAdmin.getUid()).get()
                    .addOnSuccessListener(doc -> {
                        if (doc != null && doc.exists()) {
                            Admin admin = doc.toObject(Admin.class);
                            if (admin != null) {
                                headerBinding.headerUserName.setText(admin.getName() + " (Admin)");
                                headerBinding.headerUserEmail.setText(admin.getEmail());
                                loadProfileImage(admin.getProfilePicUrl());
                            }
                        }
                    });
        }
    }

    private void loadProfileImage(String profilePic) {
        if (profilePic == null || profilePic.isEmpty()) return;

        if (profilePic.startsWith("http")) {
            // It's a direct web URL
            Glide.with(AdminMainActivity.this)
                    .load(profilePic)
                    .circleCrop()
                    .placeholder(R.drawable.person_24)
                    .into(headerBinding.headerProfilePic);
        } else {
            // It's a Firebase Storage filename
            mStorage.getReference("profile-images/" + profilePic).getDownloadUrl()
                    .addOnSuccessListener(uri -> {
                        Glide.with(AdminMainActivity.this)
                                .load(uri)
                                .circleCrop()
                                .placeholder(R.drawable.person_24)
                                .into(headerBinding.headerProfilePic);
                    }).addOnFailureListener(e -> {
                        Log.e("AdminMainActivity", "Failed to load profile image from storage: " + e.getMessage());
                    });
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.admin_nav_dashboard) {
            navigateTo(new AdminDashboardFragment());
        } else if (id == R.id.admin_nav_categories) {
            navigateTo(new ManageCategoriesFragment());
        } else if (id == R.id.admin_nav_products) {
            navigateTo(new ManageProductsFragment());
        } else if (id == R.id.admin_nav_orders) {
            navigateTo(new AdminOrdersFragment());
        } else if (id == R.id.admin_nav_logout) {
            mAuth.signOut();
            startActivity(new Intent(this, SignInActivity.class));
            finish();
        }

        binding.adminDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void navigateTo(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.admin_fragment_container, fragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit();
    }

    @Override
    public void onBackPressed() {
        if (binding.adminDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.adminDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
