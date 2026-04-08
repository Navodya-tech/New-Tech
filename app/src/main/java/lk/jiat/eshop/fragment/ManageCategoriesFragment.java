package lk.jiat.eshop.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import lk.jiat.eshop.adapter.AdminCategoryAdapter;
import lk.jiat.eshop.databinding.FragmentManageCategoriesBinding;
import lk.jiat.eshop.model.Category;

public class ManageCategoriesFragment extends Fragment {

    private FragmentManageCategoriesBinding binding;
    private AdminCategoryAdapter adapter;
    private List<Category> categories = new ArrayList<>();
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentManageCategoriesBinding.inflate(inflater, container, false);
        db = FirebaseFirestore.getInstance();

        setupRecyclerView();
        loadCategories();

        binding.fabAddCategory.setOnClickListener(v -> showAddCategoryDialog());

        return binding.getRoot();
    }

    private void setupRecyclerView() {
        adapter = new AdminCategoryAdapter(categories, category -> showDeleteConfirmation(category));
        binding.rvManageCategories.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvManageCategories.setAdapter(adapter);
    }

    private void loadCategories() {
        db.collection("categories").get().addOnSuccessListener(queryDocumentSnapshots -> {
            categories.clear();
            categories.addAll(queryDocumentSnapshots.toObjects(Category.class));
            adapter.notifyDataSetChanged();
        });
    }

    private void showAddCategoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Add New Category");

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 16, 48, 16);

        final EditText inputName = new EditText(getContext());
        inputName.setHint("Category Name");
        layout.addView(inputName);

        final EditText inputImageUrl = new EditText(getContext());
        inputImageUrl.setHint("Image URL");
        layout.addView(inputImageUrl);

        builder.setView(layout);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String name = inputName.getText().toString().trim();
            String imageUrl = inputImageUrl.getText().toString().trim();

            if (!name.isEmpty() && !imageUrl.isEmpty()) {
                saveCategory(name, imageUrl);
            } else {
                Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void saveCategory(String name, String imageUrl) {
        String id = UUID.randomUUID().toString();
        Category category = new Category(id, name, imageUrl);

        db.collection("categories").document(id).set(category)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Category Added!", Toast.LENGTH_SHORT).show();
                    loadCategories();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void showDeleteConfirmation(Category category) {
        new AlertDialog.Builder(getContext())
                .setTitle("Delete Category")
                .setMessage("Are you sure you want to delete " + category.getName() + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    db.collection("categories").document(category.getCategoryId()).delete()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(getContext(), "Deleted!", Toast.LENGTH_SHORT).show();
                                loadCategories();
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
