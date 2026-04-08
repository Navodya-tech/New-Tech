package lk.jiat.eshop.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import lk.jiat.eshop.adapter.AdminProductAdapter;
import lk.jiat.eshop.databinding.FragmentManageProductsBinding;
import lk.jiat.eshop.model.Category;
import lk.jiat.eshop.model.Product;

public class ManageProductsFragment extends Fragment {

    private FragmentManageProductsBinding binding;
    private AdminProductAdapter adapter;
    private List<Product> products = new ArrayList<>();
    private List<Category> categories = new ArrayList<>();
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentManageProductsBinding.inflate(inflater, container, false);
        db = FirebaseFirestore.getInstance();

        setupRecyclerView();
        loadProducts();
        loadCategories();

        binding.fabAddProduct.setOnClickListener(v -> showAddProductDialog());

        return binding.getRoot();
    }

    private void setupRecyclerView() {
        adapter = new AdminProductAdapter(products, product -> showDeleteConfirmation(product));
        binding.rvManageProducts.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvManageProducts.setAdapter(adapter);
    }

    private void loadProducts() {
        db.collection("products").get().addOnSuccessListener(queryDocumentSnapshots -> {
            products.clear();
            products.addAll(queryDocumentSnapshots.toObjects(Product.class));
            adapter.notifyDataSetChanged();
        });
    }

    private void loadCategories() {
        db.collection("categories").get().addOnSuccessListener(queryDocumentSnapshots -> {
            categories.clear();
            categories.addAll(queryDocumentSnapshots.toObjects(Category.class));
        });
    }

    private void showAddProductDialog() {
        if (categories.isEmpty()) {
            Toast.makeText(getContext(), "Please add a category first", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Add New Product");

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 16, 48, 16);

        final EditText inputTitle = new EditText(getContext());
        inputTitle.setHint("Product Title");
        layout.addView(inputTitle);

        final EditText inputDesc = new EditText(getContext());
        inputDesc.setHint("Description");
        layout.addView(inputDesc);

        final EditText inputPrice = new EditText(getContext());
        inputPrice.setHint("Price (Rs.)");
        inputPrice.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        layout.addView(inputPrice);

        final EditText inputStock = new EditText(getContext());
        inputStock.setHint("Stock Count");
        inputStock.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        layout.addView(inputStock);

        final EditText inputImageUrl = new EditText(getContext());
        inputImageUrl.setHint("Image URL");
        layout.addView(inputImageUrl);

        final Spinner categorySpinner = new Spinner(getContext());
        List<String> categoryNames = categories.stream().map(Category::getName).collect(Collectors.toList());
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, categoryNames);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(spinnerAdapter);
        layout.addView(categorySpinner);

        builder.setView(layout);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String title = inputTitle.getText().toString().trim();
            String desc = inputDesc.getText().toString().trim();
            String priceStr = inputPrice.getText().toString().trim();
            String stockStr = inputStock.getText().toString().trim();
            String imageUrl = inputImageUrl.getText().toString().trim();
            int selectedCatIndex = categorySpinner.getSelectedItemPosition();

            if (!title.isEmpty() && !priceStr.isEmpty() && !imageUrl.isEmpty()) {
                double price = Double.parseDouble(priceStr);
                int stock = Integer.parseInt(stockStr);
                String catId = categories.get(selectedCatIndex).getCategoryId();
                saveProduct(title, desc, price, stock, imageUrl, catId);
            } else {
                Toast.makeText(getContext(), "Title, Price and Image URL are required", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void saveProduct(String title, String desc, double price, int stock, String imageUrl, String catId) {
        String id = UUID.randomUUID().toString();
        Product product = Product.builder()
                .productId(id)
                .title(title)
                .description(desc)
                .price(price)
                .stockCount(stock)
                .categoryId(catId)
                .images(List.of(imageUrl))
                .status(true)
                .rating(0.0f)
                .build();

        db.collection("products").document(id).set(product)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Product Added!", Toast.LENGTH_SHORT).show();
                    loadProducts();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void showDeleteConfirmation(Product product) {
        new AlertDialog.Builder(getContext())
                .setTitle("Delete Product")
                .setMessage("Are you sure you want to delete " + product.getTitle() + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    db.collection("products").document(product.getProductId()).delete()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(getContext(), "Product Deleted!", Toast.LENGTH_SHORT).show();
                                loadProducts();
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
