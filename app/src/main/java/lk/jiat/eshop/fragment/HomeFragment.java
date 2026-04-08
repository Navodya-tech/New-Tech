package lk.jiat.eshop.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import lk.jiat.eshop.R;
import lk.jiat.eshop.adapter.BrandAdapter;
import lk.jiat.eshop.adapter.ListingAdapter;
import lk.jiat.eshop.databinding.FragmentHomeBinding;
import lk.jiat.eshop.model.Product;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loadStaticBrands();
        loadHighRatedProducts();
    }

    private void loadStaticBrands() {
        // Since you don't have a brand field in the model, we use a static list
        List<String> brands = List.of("Apple", "Samsung", "Sony", "Dell", "HP", "Canon", "Nintendo", "OnePlus", "Google");

        BrandAdapter adapter = new BrandAdapter(brands, brand -> {
            // Future implementation: filter products by checking if title contains brand name
            filterByBrand(brand);
        });
        
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        binding.recyclerViewBrands.setLayoutManager(layoutManager);
        binding.recyclerViewBrands.setAdapter(adapter);
    }

    private void loadHighRatedProducts() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        // Fetching high rated products (rating > 4.5)
        db.collection("products")
                .whereGreaterThan("rating", 4.4)
                .get()
                .addOnSuccessListener(qds -> {
                    if (!qds.isEmpty()) {
                        List<Product> products = qds.toObjects(Product.class);
                        
                        // Sort by rating in descending order (highest first) manually to avoid Firestore Index requirement
                        products.sort((p1, p2) -> Float.compare(p2.getRating(), p1.getRating()));

                        ListingAdapter adapter = new ListingAdapter(products, product -> {
                            // Navigate to details
                            Bundle bundle = new Bundle();
                            bundle.putString("productId", product.getProductId());
                            ProductDetailsFragment fragment = new ProductDetailsFragment();
                            fragment.setArguments(bundle);
                            getParentFragmentManager().beginTransaction()
                                    .replace(R.id.fragment_container, fragment)
                                    .addToBackStack(null)
                                    .commit();
                        });
                        binding.recyclerViewHighRated.setAdapter(adapter);
                    }
                });
    }

    private void filterByBrand(String brand) {
        // Implementation for filtering could check product.getTitle().contains(brand)
    }
}
