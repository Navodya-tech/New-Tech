package lk.jiat.eshop.fragment;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lk.jiat.eshop.R;
import lk.jiat.eshop.activity.SignInActivity;
import lk.jiat.eshop.adapter.ProductSliderAdapter;
import lk.jiat.eshop.adapter.SectionAdapter;
import lk.jiat.eshop.database.WishlistDbHelper;
import lk.jiat.eshop.databinding.FragmentProductDetailsBinding;
import lk.jiat.eshop.model.CartItem;
import lk.jiat.eshop.model.Product;


public class ProductDetailsFragment extends Fragment {

    private FragmentProductDetailsBinding binding;
    private String productId;
    private int quantity = 1;
    private int avbQuantity;
    private Product currentProduct;
    private WishlistDbHelper dbHelper;

    private Map<String, ChipGroup> attributeGroups = new HashMap<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            productId = getArguments().getString("productId");
        }
        dbHelper = new WishlistDbHelper(getContext());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProductDetailsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getActivity() != null) {
            getActivity().findViewById(R.id.bottom_navigation_view).setVisibility(View.GONE);
            getActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
                @Override
                public void handleOnBackPressed() {
                    requireActivity().getSupportFragmentManager().popBackStack();
                }
            });
        }


        // Load Product Details
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("products").whereEqualTo("productId", productId).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot qds) {
                if (!qds.isEmpty()) {
                    currentProduct = qds.getDocuments().get(0).toObject(Product.class);
                    if (currentProduct == null) return;

                    ProductSliderAdapter adapter = new ProductSliderAdapter(currentProduct.getImages());
                    binding.productImageSlider.setAdapter(adapter);

                    binding.dotsIndicator.attachTo(binding.productImageSlider);

                    binding.productDetailsTitle.setText(currentProduct.getTitle());
                    binding.productDetailsDescription.setText(currentProduct.getDescription());
                    binding.productDetailsRating.setRating(currentProduct.getRating());
                    binding.productDetailsPrice.setText("LKR " + currentProduct.getPrice());

                    binding.productDetailsAvbQty.setText(String.valueOf(currentProduct.getStockCount()));
                    avbQuantity = currentProduct.getStockCount();

                    if (currentProduct.getAttributes() != null) {
                        currentProduct.getAttributes().forEach(attribute -> {
                            renderAttribute(attribute, binding.productDetailsAttributeContainer);
                        });
                    }
                    
                    updateWishlistIcon();
                }
            }
        });


        binding.productDetailsBtnMinus.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                binding.productDetailsQuantity.setText(String.valueOf(quantity));
            }
        });

        binding.productDetailsBtnPlus.setOnClickListener(v -> {
            if (quantity < avbQuantity) {
                quantity++;
                binding.productDetailsQuantity.setText(String.valueOf(quantity));
            }
        });


        loadRelatedProducts();


        binding.productDetailsBtnAddCart.setOnClickListener(v -> {
            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
            if (firebaseAuth.getCurrentUser() == null) {
                Intent intent = new Intent(getActivity(), SignInActivity.class);
                startActivity(intent);
            } else {
                List<CartItem.Attribute> attributes = getFinalSelections();
                CartItem cartItem = new CartItem(productId, quantity, attributes);
                String uid = firebaseAuth.getCurrentUser().getUid();

                db.collection("users").document(uid).collection("cart").document()
                        .set(cartItem)
                        .addOnSuccessListener(unused -> {
                            Toast.makeText(getContext(), "Item added to cart!", Toast.LENGTH_SHORT).show();
                        });
            }
        });

        binding.btnAddWishlist.setOnClickListener(v -> {
            if (currentProduct == null) return;
            
            if (dbHelper.isWishlisted(productId)) {
                dbHelper.removeFromWishlist(productId);
                Toast.makeText(getContext(), "Removed from Wishlist", Toast.LENGTH_SHORT).show();
            } else {
                dbHelper.addToWishlist(currentProduct);
                Toast.makeText(getContext(), "Added to Wishlist", Toast.LENGTH_SHORT).show();
            }
            updateWishlistIcon();
        });
    }

    private void updateWishlistIcon() {
        if (dbHelper.isWishlisted(productId)) {
            binding.btnAddWishlist.setIconResource(R.drawable.favorite_24);
            binding.btnAddWishlist.setIconTint(ColorStateList.valueOf(getResources().getColor(R.color.md_theme_error)));
        } else {
            binding.btnAddWishlist.setIconResource(R.drawable.favorite_24);
            binding.btnAddWishlist.setIconTint(ColorStateList.valueOf(getResources().getColor(R.color.md_theme_outline)));
        }
    }

    private void loadRelatedProducts() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("products").whereNotEqualTo("productId", productId).limit(10).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot qds) {
                if (!qds.isEmpty()) {
                    List<Product> products = qds.toObjects(Product.class);
                    LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
                    binding.productDetailsTopSellSection.itemSectionContainer.setLayoutManager(layoutManager);

                    SectionAdapter adapter = new SectionAdapter(products, product -> {
                        Bundle bundle = new Bundle();
                        bundle.putString("productId", product.getProductId());
                        ProductDetailsFragment productDetailsFragment = new ProductDetailsFragment();
                        productDetailsFragment.setArguments(bundle);
                        getParentFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, productDetailsFragment)
                                .addToBackStack(null)
                                .commit();
                    });

                    binding.productDetailsTopSellSection.itemSectionTitle.setText("Recommended for You");
                    binding.productDetailsTopSellSection.itemSectionContainer.setAdapter(adapter);
                }
            }
        });
    }

    private void renderAttribute(Product.Attribute attribute, ViewGroup container) {
        LinearLayout row = new LinearLayout(getContext());
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(0, 16, 0, 0);

        TextView label = new TextView(getContext());
        label.setText(attribute.getName());
        label.setTypeface(null, Typeface.BOLD);
        label.setTextColor(getResources().getColor(R.color.md_theme_onSurface));
        row.addView(label);

        ChipGroup group = new ChipGroup(getContext());
        group.setSelectionRequired(true);
        group.setSingleSelection(true);
        group.setChipSpacingVertical(8);

        attribute.getValues().forEach(value -> {
            Chip chip = new Chip(getContext());
            chip.setCheckable(true);
            chip.setTag(value);

            if ("color".equals(attribute.getType())) {
                chip.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor(value)));
                chip.setText("");
                chip.setChipMinHeight(100f);
                chip.setChipIconVisible(false);
            } else {
                chip.setText(value);
            }
            group.addView(chip);
        });

        row.addView(group);
        container.addView(row);
        attributeGroups.put(attribute.getName(), group);
    }


    private List<CartItem.Attribute> getFinalSelections() {
        List<CartItem.Attribute> attributes = new ArrayList<>();
        for (Map.Entry<String, ChipGroup> entry : attributeGroups.entrySet()) {
            String attributeName = entry.getKey();
            ChipGroup chipGroup = entry.getValue();
            int checkedChipId = chipGroup.getCheckedChipId();
            if (checkedChipId != -1) {
                Chip chip = chipGroup.findViewById(checkedChipId);
                String value = chip.getTag().toString();
                attributes.add(new CartItem.Attribute(attributeName, value));
            }
        }
        return attributes;
    }


    @Override
    public void onStop() {
        super.onStop();
        if (getActivity() != null) {
            getActivity().findViewById(R.id.bottom_navigation_view).setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null) {
            getActivity().findViewById(R.id.bottom_navigation_view).setVisibility(View.GONE);
        }
    }
}
