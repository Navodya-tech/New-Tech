package lk.jiat.eshop.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.UUID;

import lk.jiat.eshop.R;
import lk.jiat.eshop.databinding.ActivityMainBinding;
import lk.jiat.eshop.databinding.SideNavHeaderBinding;
import lk.jiat.eshop.fragment.CartFragment;
import lk.jiat.eshop.fragment.CategoryFragment;
import lk.jiat.eshop.fragment.HomeFragment;
import lk.jiat.eshop.fragment.ListingFragment;
import lk.jiat.eshop.fragment.MessageFragment;
import lk.jiat.eshop.fragment.OrdersFragment;
import lk.jiat.eshop.fragment.ProfileFragment;
import lk.jiat.eshop.fragment.SettingsFragment;
import lk.jiat.eshop.fragment.WishlistFragment;
import lk.jiat.eshop.model.User;
import lk.jiat.eshop.util.NotificationHelper;
import lk.jiat.eshop.util.ShakeDetector;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MainActivity_NewTech";

    private ActivityMainBinding binding;
    private SideNavHeaderBinding headerBinding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mDb;
    private FirebaseStorage mStorage;

    // Shake Detector variables
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initializeFirebase();
        setupNavigation();
        setupSearchLogic();
        syncUserSession();
        setupShakeDetector();
        
        // Setup Notifications
        NotificationHelper.createNotificationChannel(this);
        checkNotificationPermission();

        if (savedInstanceState == null) {
            navigateTo(new HomeFragment());
            updateSelectedNavItems(R.id.side_nav_home, R.id.bottom_nav_home);
        }
    }

    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }

    private void setupShakeDetector() {
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mShakeDetector = new ShakeDetector();
        mShakeDetector.setOnShakeListener(count -> {
            handleShakeEvent();
        });
    }

    private void handleShakeEvent() {
        // Trigger a notification on shake
        NotificationHelper.showNotification(this, "Shake Event!", "You just shook the device. Opening report menu...");
        
        new AlertDialog.Builder(this)
                .setTitle("Shake Detected!")
                .setMessage("Would you like to report a problem or refresh the app?")
                .setPositiveButton("Refresh", (dialog, i) -> {
                    recreate();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mSensorManager != null && mAccelerometer != null) {
            mSensorManager.registerListener(mShakeDetector, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    protected void onPause() {
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(mShakeDetector);
        }
        super.onPause();
    }

    private void initializeFirebase() {
        mAuth = FirebaseAuth.getInstance();
        mDb = FirebaseFirestore.getInstance();
        mStorage = FirebaseStorage.getInstance();
    }

    private void setupNavigation() {
        setSupportActionBar(binding.toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, binding.drawerLayout, binding.toolbar, R.string.drawer_open, R.string.drawer_close);
        binding.drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    binding.drawerLayout.closeDrawer(GravityCompat.START);
                } else if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    getSupportFragmentManager().popBackStack();
                } else {
                    finish();
                }
            }
        });

        binding.sideNavigationView.setNavigationItemSelectedListener(this);
        binding.bottomNavigationView.setOnItemSelectedListener(item -> handleNavClick(item.getItemId()));

        View headerView = binding.sideNavigationView.getHeaderView(0);
        headerBinding = SideNavHeaderBinding.bind(headerView);
        headerBinding.headerProfilePic.setOnClickListener(v -> launchImagePicker());

        binding.btnTopLocation.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, MapsActivity.class));
        });
    }

    private void setupSearchLogic() {
        binding.textInputSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                performSearch(binding.textInputSearch.getText().toString());
                return true;
            }
            return false;
        });

        binding.textInputSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 2) {
                    Log.d(TAG, "User typing: " + s);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void performSearch(String query) {
        if (query.trim().isEmpty()) return;

        ListingFragment listingFragment = new ListingFragment();
        Bundle bundle = new Bundle();
        bundle.putString("searchQuery", query);
        listingFragment.setArguments(bundle);

        navigateTo(listingFragment);
        clearSelections();
    }

    private void syncUserSession() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        boolean isLoggedIn = (currentUser != null);

        updateMenuVisibility(isLoggedIn);

        if (isLoggedIn) {
            mDb.collection("users").document(currentUser.getUid()).get()
                    .addOnSuccessListener(doc -> {
                        if (doc != null && doc.exists()) {
                            User user = doc.toObject(User.class);
                            if (user != null) {
                                headerBinding.headerUserName.setText(user.getName());
                                headerBinding.headerUserEmail.setText(user.getEmail());
                                loadProfileImage(user.getProfilePicUrl());
                            }
                        }
                    });
        }
    }

    private void loadProfileImage(String profilePic) {
        if (profilePic == null || profilePic.isEmpty()) return;

        if (profilePic.startsWith("http")) {
            Glide.with(MainActivity.this)
                    .load(profilePic)
                    .circleCrop()
                    .placeholder(R.drawable.person_24)
                    .into(headerBinding.headerProfilePic);
        } else {
            mStorage.getReference("profile-images/" + profilePic).getDownloadUrl()
                    .addOnSuccessListener(uri -> {
                        Glide.with(MainActivity.this)
                                .load(uri)
                                .circleCrop()
                                .placeholder(R.drawable.person_24)
                                .into(headerBinding.headerProfilePic);
                    }).addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to load profile image from storage: " + e.getMessage());
                    });
        }
    }

    private void updateMenuVisibility(boolean isLoggedIn) {
        Menu menu = binding.sideNavigationView.getMenu();
        menu.findItem(R.id.side_nav_login).setVisible(!isLoggedIn);
        menu.findItem(R.id.side_nav_logout).setVisible(isLoggedIn);
        menu.findItem(R.id.side_nav_profile).setVisible(isLoggedIn);
        menu.findItem(R.id.side_nav_orders).setVisible(isLoggedIn);
        menu.findItem(R.id.side_nav_wishlist).setVisible(isLoggedIn);
        menu.findItem(R.id.side_nav_cart).setVisible(isLoggedIn);
        menu.findItem(R.id.side_nav_message).setVisible(isLoggedIn);
    }

    private boolean handleNavClick(int itemId) {
        clearSelections();

        if (itemId == R.id.side_nav_home || itemId == R.id.bottom_nav_home) {
            navigateTo(new HomeFragment());
            updateSelectedNavItems(R.id.side_nav_home, R.id.bottom_nav_home);
        } else if (itemId == R.id.side_nav_profile || itemId == R.id.bottom_nav_profile) {
            if (ensureUserLoggedIn()) return true;
            navigateTo(new ProfileFragment());
            updateSelectedNavItems(R.id.side_nav_profile, R.id.bottom_nav_profile);
        } else if (itemId == R.id.side_nav_orders) {
            navigateTo(new OrdersFragment());
            binding.sideNavigationView.getMenu().findItem(itemId).setChecked(true);
        } else if (itemId == R.id.side_nav_wishlist) {
            navigateTo(new WishlistFragment());
            binding.sideNavigationView.getMenu().findItem(itemId).setChecked(true);
        } else if (itemId == R.id.side_nav_cart || itemId == R.id.bottom_nav_cart) {
            if (ensureUserLoggedIn()) return true;
            navigateTo(new CartFragment());
            updateSelectedNavItems(R.id.side_nav_cart, R.id.bottom_nav_cart);
        } else if (itemId == R.id.side_nav_message) {
            navigateTo(new MessageFragment());
            binding.sideNavigationView.getMenu().findItem(itemId).setChecked(true);
        } else if (itemId == R.id.side_nav_settings) {
            navigateTo(new SettingsFragment());
            binding.sideNavigationView.getMenu().findItem(itemId).setChecked(true);
        } else if (itemId == R.id.bottom_nav_category) {
            navigateTo(new CategoryFragment());
            binding.bottomNavigationView.getMenu().findItem(itemId).setChecked(true);
        } else if (itemId == R.id.side_nav_login) {
            startActivity(new Intent(this, SignInActivity.class));
        } else if (itemId == R.id.side_nav_logout) {
            mAuth.signOut();
            recreate();
        }

        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        }
        return true;
    }

    private boolean ensureUserLoggedIn() {
        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(this, SignInActivity.class));
            return true;
        }
        return false;
    }

    private void navigateTo(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit();
    }

    private void clearSelections() {
        Menu sideMenu = binding.sideNavigationView.getMenu();
        for (int i = 0; i < sideMenu.size(); i++) sideMenu.getItem(i).setChecked(false);
        Menu bottomMenu = binding.bottomNavigationView.getMenu();
        for (int i = 0; i < bottomMenu.size(); i++) bottomMenu.getItem(i).setChecked(false);
    }

    private void updateSelectedNavItems(int sideId, int bottomId) {
        binding.sideNavigationView.getMenu().findItem(sideId).setChecked(true);
        binding.bottomNavigationView.getMenu().findItem(bottomId).setChecked(true);
    }

    private void launchImagePicker() {
        if (mAuth.getCurrentUser() == null) return;
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        activityResultLauncher.launch(intent);
    }

    private final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri == null) return;

                    Glide.with(this).load(uri).circleCrop().into(headerBinding.headerProfilePic);
                    uploadProfileImage(uri);
                }
            }
    );

    private void uploadProfileImage(Uri uri) {
        String imageId = UUID.randomUUID().toString();
        StorageReference ref = mStorage.getReference("profile-images").child(imageId);
        ref.putFile(uri).addOnSuccessListener(task -> {
            mDb.collection("users").document(mAuth.getUid())
                    .update("profilePicUrl", imageId)
                    .addOnSuccessListener(aVoid -> Toast.makeText(this, "Avatar Updated!", Toast.LENGTH_SHORT).show());
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return handleNavClick(item.getItemId());
    }
}
