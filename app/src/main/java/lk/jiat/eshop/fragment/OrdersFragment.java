package lk.jiat.eshop.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import lk.jiat.eshop.adapter.OrderAdapter;
import lk.jiat.eshop.databinding.FragmentOrdersBinding;
import lk.jiat.eshop.model.Order;
import lk.jiat.eshop.model.Product;

public class OrdersFragment extends Fragment {

    private FragmentOrdersBinding binding;
    private OrderAdapter adapter;
    private List<Order> orders = new ArrayList<>();
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentOrdersBinding.inflate(inflater, container, false);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        setupRecyclerView();
        loadUserOrders();

        return binding.getRoot();
    }

    private void setupRecyclerView() {
        adapter = new OrderAdapter(orders, order -> showOrderDetails(order));
        binding.rvUserOrders.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvUserOrders.setAdapter(adapter);
    }

    private void loadUserOrders() {
        if (mAuth.getCurrentUser() == null) return;

        String uid = mAuth.getCurrentUser().getUid();
        binding.ordersProgressBar.setVisibility(View.VISIBLE);

        db.collection("orders")
                .whereEqualTo("userId", uid)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (binding == null) return;
                    binding.ordersProgressBar.setVisibility(View.GONE);
                    
                    List<Order> fetchedOrders = queryDocumentSnapshots.toObjects(Order.class);
                    
                    fetchedOrders.sort((o1, o2) -> {
                        if (o1.getOrderDate() == null || o2.getOrderDate() == null) return 0;
                        return o2.getOrderDate().compareTo(o1.getOrderDate());
                    });

                    orders.clear();
                    orders.addAll(fetchedOrders);
                    
                    if (orders.isEmpty()) {
                        binding.tvNoOrders.setVisibility(View.VISIBLE);
                    } else {
                        binding.tvNoOrders.setVisibility(View.GONE);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    if (binding == null) return;
                    binding.ordersProgressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showOrderDetails(Order order) {
        if (order.getOrderItems() == null || order.getOrderItems().isEmpty()) {
            Toast.makeText(getContext(), "No items in this order", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder details = new StringBuilder();
        details.append("Status: ").append(order.getStatus().toUpperCase()).append("\n\n");
        details.append("Items Ordered:\n");

        List<Order.OrderItem> items = order.getOrderItems();
        AtomicInteger counter = new AtomicInteger(0);

        for (Order.OrderItem item : items) {
            // Fetch product name for each item
            db.collection("products").document(item.getProductId()).get()
                    .addOnSuccessListener(doc -> {
                        String productName = "Unknown Product";
                        if (doc.exists()) {
                            Product p = doc.toObject(Product.class);
                            if (p != null) productName = p.getTitle();
                        }

                        details.append("- ").append(productName)
                                .append(" (x").append(item.getQuantity()).append(")\n")
                                .append("  Price: Rs. ").append(item.getUnitPrice()).append("\n\n");

                        if (counter.incrementAndGet() == items.size()) {
                            // All product names fetched, show dialog
                            showDetailsDialog(details.toString(), order.getTotalAmount());
                        }
                    })
                    .addOnFailureListener(e -> {
                        if (counter.incrementAndGet() == items.size()) {
                            showDetailsDialog(details.toString(), order.getTotalAmount());
                        }
                    });
        }
    }

    private void showDetailsDialog(String content, double total) {
        String finalContent = content + "Total: Rs. " + String.format("%.2f", total);
        new AlertDialog.Builder(getContext())
                .setTitle("Order Details")
                .setMessage(finalContent)
                .setPositiveButton("Close", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
