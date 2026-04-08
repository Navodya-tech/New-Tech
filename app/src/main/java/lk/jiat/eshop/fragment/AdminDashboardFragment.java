package lk.jiat.eshop.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.FirebaseFirestore;

import lk.jiat.eshop.databinding.FragmentAdminDashboardBinding;

public class AdminDashboardFragment extends Fragment {

    private FragmentAdminDashboardBinding binding;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminDashboardBinding.inflate(inflater, container, false);
        db = FirebaseFirestore.getInstance();

        fetchCounts();

        return binding.getRoot();
    }

    private void fetchCounts() {
        // Fetch total products
        db.collection("products").get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (binding != null) {
                binding.tvTotalProducts.setText(String.valueOf(queryDocumentSnapshots.size()));
            }
        });

        // Fetch total categories - matched to "categories" collection
        db.collection("categories").get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (binding != null) {
                binding.tvTotalCategories.setText(String.valueOf(queryDocumentSnapshots.size()));
            }
        });

        // Fetch total users
        db.collection("users").get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (binding != null) {
                binding.tvTotalUsers.setText(String.valueOf(queryDocumentSnapshots.size()));
            }
        });

        // Fetch total orders
        db.collection("orders").get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (binding != null) {
                binding.tvTotalOrders.setText(String.valueOf(queryDocumentSnapshots.size()));
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
