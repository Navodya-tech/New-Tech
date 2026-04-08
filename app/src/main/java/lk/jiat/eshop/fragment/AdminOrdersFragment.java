package lk.jiat.eshop.fragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lk.jiat.eshop.adapter.AdminOrdersAdapter;
import lk.jiat.eshop.databinding.FragmentAdminOrdersBinding;
import lk.jiat.eshop.model.Order;

public class AdminOrdersFragment extends Fragment {

    private FragmentAdminOrdersBinding binding;
    private AdminOrdersAdapter adapter;
    private List<Order> orders = new ArrayList<>();
    private Map<String, String> orderToDocIdMap = new HashMap<>();
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminOrdersBinding.inflate(inflater, container, false);
        db = FirebaseFirestore.getInstance();

        setupRecyclerView();
        loadOrders();

        return binding.getRoot();
    }

    private void setupRecyclerView() {
        adapter = new AdminOrdersAdapter(orders, new AdminOrdersAdapter.OnOrderActionListener() {
            @Override
            public void onUpdateStatus(Order order) {
                showStatusUpdateDialog(order);
            }

            @Override
            public void onCallCustomer(String phoneNumber) {
                if (phoneNumber != null && !phoneNumber.isEmpty()) {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:" + phoneNumber));
                    startActivity(intent);
                } else {
                    Toast.makeText(getContext(), "Phone number not available", Toast.LENGTH_SHORT).show();
                }
            }
        });
        binding.rvAdminOrders.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvAdminOrders.setAdapter(adapter);
    }

    private void loadOrders() {
        binding.ordersProgressBar.setVisibility(View.VISIBLE);
        db.collection("orders")
                .orderBy("orderDate", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    binding.ordersProgressBar.setVisibility(View.GONE);
                    orders.clear();
                    orderToDocIdMap.clear();

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Order order = doc.toObject(Order.class);
                        if (order != null) {
                            String orderKey = order.getOrderId() != null ? order.getOrderId() : doc.getId();
                            orderToDocIdMap.put(orderKey, doc.getId());
                            orders.add(order);
                        }
                    }
                    
                    if (orders.isEmpty()) {
                        binding.tvNoOrders.setVisibility(View.VISIBLE);
                    } else {
                        binding.tvNoOrders.setVisibility(View.GONE);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    binding.ordersProgressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Error loading orders: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showStatusUpdateDialog(Order order) {
        String[] statuses = {"paid", "Processing", "Shipped", "Delivered", "Cancelled"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Update Order Status");

        Spinner spinner = new Spinner(getContext());
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, statuses);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);
        
        for (int i = 0; i < statuses.length; i++) {
            if (statuses[i].equalsIgnoreCase(order.getStatus())) {
                spinner.setSelection(i);
                break;
            }
        }

        builder.setView(spinner);
        builder.setPositiveButton("Update", (dialog, which) -> {
            String newStatus = spinner.getSelectedItem().toString();
            String docId = orderToDocIdMap.get(order.getOrderId());
            if (docId == null) docId = order.getOrderId();
            updateOrderStatus(docId, newStatus);
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void updateOrderStatus(String docId, String newStatus) {
        db.collection("orders").document(docId)
                .update("status", newStatus)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Status updated!", Toast.LENGTH_SHORT).show();
                    loadOrders();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Update failed!", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
