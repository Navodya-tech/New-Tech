package lk.jiat.eshop.fragment;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import lk.jiat.eshop.R;
import lk.jiat.eshop.adapter.ListingAdapter;
import lk.jiat.eshop.databinding.FragmentListingBinding;
import lk.jiat.eshop.model.Product;

import java.util.List;
import java.util.stream.Collectors;

public class ListingFragment extends Fragment {

    private FragmentListingBinding binding;
    private ListingAdapter adapter;
    private String categoryId;
    private String searchQuery;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            categoryId = getArguments().getString("categoryId");
            searchQuery = getArguments().getString("searchQuery");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentListingBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.recyclerViewListing.setLayoutManager(new GridLayoutManager(getContext(), 2));

        if (searchQuery != null) {
            searchProducts(searchQuery);
        } else if (categoryId != null) {
            loadCategoryProducts(categoryId);
        }

        if (getActivity() != null) {
            getActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
                @Override
                public void handleOnBackPressed() {
                    requireActivity().getSupportFragmentManager().popBackStack();
                }
            });
        }
    }

    private void loadCategoryProducts(String catId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("products")
                .whereEqualTo("categoryId", catId)
                .get()
                .addOnSuccessListener(ds -> {
                    if (ds != null && !ds.isEmpty()) {
                        updateUI(ds.toObjects(Product.class));
                    } else {
                        showEmpty("No products found for this category.");
                    }
                }).addOnFailureListener(e -> Log.e("Firestore", "Error: " + e.getMessage()));
    }

    private void searchProducts(String query) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // Since Firestore doesn't support full-text search easily, we fetch all and filter locally
        // This works well for small datasets like yours (42 products)
        db.collection("products")
                .get()
                .addOnSuccessListener(ds -> {
                    if (ds != null && !ds.isEmpty()) {
                        List<Product> allProducts = ds.toObjects(Product.class);
                        List<Product> filtered = allProducts.stream()
                                .filter(p -> p.getTitle().toLowerCase().contains(query.toLowerCase()) || 
                                            p.getDescription().toLowerCase().contains(query.toLowerCase()))
                                .collect(Collectors.toList());
                        
                        if (!filtered.isEmpty()) {
                            updateUI(filtered);
                        } else {
                            showEmpty("No products match '" + query + "'");
                        }
                    }
                });
    }

    private void updateUI(List<Product> products) {
        adapter = new ListingAdapter(products, product -> {
            Bundle bundle = new Bundle();
            bundle.putString("productId", product.getProductId());
            ProductDetailsFragment detailsFragment = new ProductDetailsFragment();
            detailsFragment.setArguments(bundle);

            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, detailsFragment)
                    .addToBackStack(null)
                    .commit();
        });
        binding.recyclerViewListing.setAdapter(adapter);
    }

    private void showEmpty(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
}
