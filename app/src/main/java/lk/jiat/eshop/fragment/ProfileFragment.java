package lk.jiat.eshop.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;

import lk.jiat.eshop.R;
import lk.jiat.eshop.activity.SignInActivity;
import lk.jiat.eshop.databinding.FragmentProfileBinding;
import lk.jiat.eshop.model.Order;
import lk.jiat.eshop.model.User;

public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";
    private FragmentProfileBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mDb;
    private FirebaseStorage mStorage;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        mDb = FirebaseFirestore.getInstance();
        mStorage = FirebaseStorage.getInstance();

        loadUserData();
        loadLastOrderDetails();

        binding.profileBtnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(getActivity(), SignInActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    private void loadUserData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            mDb.collection("users").document(currentUser.getUid()).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            User user = documentSnapshot.toObject(User.class);
                            if (user != null && binding != null) {
                                binding.profileName.setText(user.getName());
                                binding.profileEmail.setText(user.getEmail());
                                
                                if (user.getProfilePicUrl() != null && !user.getProfilePicUrl().isEmpty()) {
                                    if (user.getProfilePicUrl().startsWith("http")) {
                                        Glide.with(this).load(user.getProfilePicUrl()).placeholder(R.drawable.person_24).into(binding.profileImage);
                                    } else {
                                        mStorage.getReference("profile-images/" + user.getProfilePicUrl()).getDownloadUrl().addOnSuccessListener(uri -> {
                                            if (binding != null) Glide.with(this).load(uri).placeholder(R.drawable.person_24).into(binding.profileImage);
                                        });
                                    }
                                }
                            }
                        }
                    });
        }
    }

    private void loadLastOrderDetails() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            mDb.collection("orders")
                    .whereEqualTo("userId", currentUser.getUid())
                    .orderBy("orderDate", Query.Direction.DESCENDING)
                    .limit(1)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty() && binding != null) {
                            Order lastOrder = queryDocumentSnapshots.getDocuments().get(0).toObject(Order.class);
                            if (lastOrder != null && lastOrder.getShippingAddress() != null) {
                                Order.Address addr = lastOrder.getShippingAddress();
                                binding.profilePhone.setText(addr.getContact() != null ? addr.getContact() : "N/A");
                                
                                StringBuilder fullAddress = new StringBuilder();
                                if (addr.getAddress1() != null) fullAddress.append(addr.getAddress1());
                                if (addr.getAddress2() != null && !addr.getAddress2().isEmpty()) fullAddress.append(", ").append(addr.getAddress2());
                                if (addr.getCity() != null) fullAddress.append("\n").append(addr.getCity());
                                if (addr.getPostcode() != null) fullAddress.append(" - ").append(addr.getPostcode());
                                
                                binding.profileAddress.setText(fullAddress.length() > 0 ? fullAddress.toString() : "Address details missing");
                            }
                        } else if (binding != null) {
                            Log.d(TAG, "No orders found for user: " + currentUser.getUid());
                            binding.profilePhone.setText("No orders yet");
                            binding.profileAddress.setText("Place an order to see address");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Firestore Query Failed: " + e.getMessage());
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Check Logcat for Index Link: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
