package lk.jiat.eshop.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import java.util.ArrayList;
import java.util.List;

import lk.jiat.eshop.R;
import lk.jiat.eshop.adapter.WishlistAdapter;
import lk.jiat.eshop.database.WishlistDbHelper;
import lk.jiat.eshop.databinding.FragmentWishlistBinding;
import lk.jiat.eshop.model.Product;

public class WishlistFragment extends Fragment {

    private FragmentWishlistBinding binding;
    private WishlistDbHelper dbHelper;
    private WishlistAdapter adapter;
    private List<Product> wishlistItems = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentWishlistBinding.inflate(inflater, container, false);
        dbHelper = new WishlistDbHelper(getContext());

        setupRecyclerView();
        loadWishlist();

        return binding.getRoot();
    }

    private void setupRecyclerView() {
        adapter = new WishlistAdapter(wishlistItems, new WishlistAdapter.OnWishlistActionListener() {
            @Override
            public void onProductClick(Product product) {
                // Navigate to product details
                Bundle bundle = new Bundle();
                bundle.putString("productId", product.getProductId());
                ProductDetailsFragment fragment = new ProductDetailsFragment();
                fragment.setArguments(bundle);
                
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit();
            }

            @Override
            public void onRemove(Product product) {
                dbHelper.removeFromWishlist(product.getProductId());
                Toast.makeText(getContext(), "Removed from Wishlist", Toast.LENGTH_SHORT).show();
                loadWishlist();
            }
        });

        binding.rvWishlist.setLayoutManager(new GridLayoutManager(getContext(), 2));
        binding.rvWishlist.setAdapter(adapter);
    }

    private void loadWishlist() {
        wishlistItems.clear();
        wishlistItems.addAll(dbHelper.getAllWishlistItems());
        
        if (wishlistItems.isEmpty()) {
            binding.tvEmptyWishlist.setVisibility(View.VISIBLE);
        } else {
            binding.tvEmptyWishlist.setVisibility(View.GONE);
        }
        
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
