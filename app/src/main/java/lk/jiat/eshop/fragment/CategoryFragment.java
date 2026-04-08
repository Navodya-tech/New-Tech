package lk.jiat.eshop.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import lk.jiat.eshop.R;
import lk.jiat.eshop.adapter.CategoryAdapter;
import lk.jiat.eshop.databinding.FragmentCategoryBinding;
import lk.jiat.eshop.model.Category;
import lk.jiat.eshop.model.Product;


public class CategoryFragment extends Fragment {

    private FragmentCategoryBinding binding;
    private CategoryAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentCategoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.recyclerViewCategories.setLayoutManager(new GridLayoutManager(getContext(), 3));

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // --- CLEAN AND RE-SEED LOGIC ---
        // Uncommented to apply the new attributes
        // resetDatabase(db);

        loadCategories(db);
    }

    private void resetDatabase(FirebaseFirestore db) {
        Toast.makeText(getContext(), "Adding Products with Attributes... Please wait.", Toast.LENGTH_LONG).show();

        db.collection("products").get().addOnSuccessListener(queryDocumentSnapshots -> {
            WriteBatch batch = db.batch();
            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                batch.delete(doc.getReference());
            }
            
            db.collection("categories").get().addOnSuccessListener(catSnapshots -> {
                for (DocumentSnapshot doc : catSnapshots) {
                    batch.delete(doc.getReference());
                }

                List<Category> cats = List.of(
                        new Category("cat1", "Mobiles", "https://images.pexels.com/photos/607812/pexels-photo-607812.jpeg?auto=compress&cs=tinysrgb&w=400"),
                        new Category("cat2", "Laptops", "https://images.pexels.com/photos/18105/pexels-photo.jpg?auto=compress&cs=tinysrgb&w=400"),
                        new Category("cat3", "Tablets", "https://images.pexels.com/photos/1334597/pexels-photo-1334597.jpeg?auto=compress&cs=tinysrgb&w=400"),
                        new Category("cat4", "Audio", "https://images.pexels.com/photos/3394651/pexels-photo-3394651.jpeg?auto=compress&cs=tinysrgb&w=400"),
                        new Category("cat5", "Watches", "https://images.pexels.com/photos/437037/pexels-photo-437037.jpeg?auto=compress&cs=tinysrgb&w=400"),
                        new Category("cat6", "Cameras", "https://images.pexels.com/photos/51383/photo-camera-subject-photographer-51383.jpeg?auto=compress&cs=tinysrgb&w=400"),
                        new Category("cat7", "Gaming", "https://images.pexels.com/photos/442576/pexels-photo-442576.jpeg?auto=compress&cs=tinysrgb&w=400"),
                        new Category("cat8", "Accessories", "https://images.pexels.com/photos/356056/pexels-photo-356056.jpeg?auto=compress&cs=tinysrgb&w=400")
                );

                for (Category c : cats) {
                    batch.set(db.collection("categories").document(c.getCategoryId()), c);
                }

                List<Product> products = new ArrayList<>();

                // Define Common Attributes
                Product.Attribute storageAttr = new Product.Attribute("Storage", "text", List.of("128GB", "256GB", "512GB"));
                Product.Attribute colorAttr = new Product.Attribute("Color", "color", List.of("#000000", "#FFFFFF", "#C0C0C0"));
                Product.Attribute ramAttr = new Product.Attribute("RAM", "text", List.of("8GB", "16GB", "32GB"));
                Product.Attribute ssdAttr = new Product.Attribute("SSD", "text", List.of("256GB", "512GB", "1TB"));
                Product.Attribute sizeAttr = new Product.Attribute("Size", "text", List.of("Small", "Medium", "Large"));

                // cat1 - Mobiles
                products.add(createProduct("cat1", "iPhone 15 Pro", "A17 Pro chip, Titanium design.", 350000.0, "https://images.pexels.com/photos/18525333/pexels-photo-18525333/free-photo-of-iphone-15-pro-max-in-white-titanium-on-white-background.jpeg?auto=compress&cs=tinysrgb&w=400", List.of(storageAttr, colorAttr)));
                products.add(createProduct("cat1", "Samsung S24 Ultra", "200MP Camera, Galaxy AI.", 320000.0, "https://images.pexels.com/photos/404280/pexels-photo-404280.jpeg?auto=compress&cs=tinysrgb&w=400", List.of(storageAttr, colorAttr)));
                products.add(createProduct("cat1", "Google Pixel 8", "Magic Editor, Best AI Phone.", 190000.0, "https://images.pexels.com/photos/1092670/pexels-photo-1092670.jpeg?auto=compress&cs=tinysrgb&w=400", List.of(storageAttr, colorAttr)));
                products.add(createProduct("cat1", "OnePlus 12", "Powerful Snapdragon 8 Gen 3.", 220000.0, "https://images.pexels.com/photos/1474132/pexels-photo-1474132.jpeg?auto=compress&cs=tinysrgb&w=400", List.of(storageAttr, colorAttr)));
                products.add(createProduct("cat1", "Xiaomi 14 Pro", "Leica professional optics.", 210000.0, "https://images.pexels.com/photos/163064/play-stone-network-networked-163064.jpeg?auto=compress&cs=tinysrgb&w=400", List.of(storageAttr, colorAttr)));

                // cat2 - Laptops
                products.add(createProduct("cat2", "MacBook Pro M3", "The most advanced Mac laptop.", 450000.0, "https://images.pexels.com/photos/459654/pexels-photo-459654.jpeg?auto=compress&cs=tinysrgb&w=400", List.of(ramAttr, ssdAttr)));
                products.add(createProduct("cat2", "Dell XPS 13", "Stunning InfinityEdge display.", 380000.0, "https://images.pexels.com/photos/1229861/pexels-photo-1229861.jpeg?auto=compress&cs=tinysrgb&w=400", List.of(ramAttr, ssdAttr)));
                products.add(createProduct("cat2", "Razer Blade 15", "Ultimate gaming performance.", 520000.0, "https://images.pexels.com/photos/2047905/pexels-photo-2047905.jpeg?auto=compress&cs=tinysrgb&w=400", List.of(ramAttr, ssdAttr)));
                products.add(createProduct("cat2", "HP Spectre x360", "Elegant and versatile 2-in-1.", 340000.0, "https://images.pexels.com/photos/205421/pexels-photo-205421.jpeg?auto=compress&cs=tinysrgb&w=400", List.of(ramAttr, ssdAttr)));
                products.add(createProduct("cat2", "Lenovo Yoga", "Premium craftsmanship.", 280000.0, "https://images.pexels.com/photos/7974/pexels-photo.jpg?auto=compress&cs=tinysrgb&w=400", List.of(ramAttr, ssdAttr)));

                // cat3 - Tablets
                products.add(createProduct("cat3", "iPad Pro 12.9", "Liquid Retina XDR display.", 290000.0, "https://images.pexels.com/photos/1334597/pexels-photo-1334597.jpeg?auto=compress&cs=tinysrgb&w=400", List.of(storageAttr, colorAttr)));
                products.add(createProduct("cat3", "Samsung Tab S9", "Our most powerful tablet.", 180000.0, "https://images.pexels.com/photos/4065890/pexels-photo-4065890.jpeg?auto=compress&cs=tinysrgb&w=400", List.of(storageAttr, colorAttr)));
                products.add(createProduct("cat3", "Surface Pro 9", "Tablet portability, laptop power.", 260000.0, "https://images.pexels.com/photos/38544/pexels-photo-38544.jpeg?auto=compress&cs=tinysrgb&w=400", List.of(storageAttr, colorAttr)));
                products.add(createProduct("cat3", "Kindle Paperwhite", "The best reading experience.", 45000.0, "https://images.pexels.com/photos/2067568/pexels-photo-2067568.jpeg?auto=compress&cs=tinysrgb&w=400", List.of(storageAttr)));
                products.add(createProduct("cat3", "Xiaomi Pad 6", "Built for work and play.", 110000.0, "https://images.pexels.com/photos/221185/pexels-photo-221185.jpeg?auto=compress&cs=tinysrgb&w=400", List.of(storageAttr, colorAttr)));

                // cat4 - Audio
                products.add(createProduct("cat4", "Sony WH-1000XM5", "Industry-leading noise cancelling.", 115000.0, "https://images.pexels.com/photos/3394651/pexels-photo-3394651.jpeg?auto=compress&cs=tinysrgb&w=400", List.of(colorAttr)));
                products.add(createProduct("cat4", "AirPods Pro 2", "Magical noise cancellation.", 85000.0, "https://images.pexels.com/photos/3780681/pexels-photo-3780681.jpeg?auto=compress&cs=tinysrgb&w=400", List.of(colorAttr)));
                products.add(createProduct("cat4", "Bose QC45", "Iconic quiet, comfort, and sound.", 105000.0, "https://images.pexels.com/photos/3587477/pexels-photo-3587477.jpeg?auto=compress&cs=tinysrgb&w=400", List.of(colorAttr)));
                products.add(createProduct("cat4", "JBL Boombox 3", "Massive sound, all day long.", 130000.0, "https://images.pexels.com/photos/1706694/pexels-photo-1706694.jpeg?auto=compress&cs=tinysrgb&w=400", List.of(colorAttr)));
                products.add(createProduct("cat4", "Sennheiser M4", "Incredible sound quality.", 120000.0, "https://images.pexels.com/photos/164821/pexels-photo-164821.jpeg?auto=compress&cs=tinysrgb&w=400", List.of(colorAttr)));

                // cat5 - Watches
                products.add(createProduct("cat5", "Apple Watch Ultra", "The most rugged and capable.", 260000.0, "https://images.pexels.com/photos/437037/pexels-photo-437037.jpeg?auto=compress&cs=tinysrgb&w=400", List.of(sizeAttr, colorAttr)));
                products.add(createProduct("cat5", "Galaxy Watch 6 Classic", "Iconic rotating bezel.", 110000.0, "https://images.pexels.com/photos/110471/pexels-photo-110471.jpeg?auto=compress&cs=tinysrgb&w=400", List.of(sizeAttr, colorAttr)));
                products.add(createProduct("cat5", "Garmin Fenix 7X", "Multisport GPS watch.", 210000.0, "https://images.pexels.com/photos/393047/pexels-photo-393047.jpeg?auto=compress&cs=tinysrgb&w=400", List.of(sizeAttr, colorAttr)));
                products.add(createProduct("cat5", "Fitbit Sense 2", "Health and fitness smartwatch.", 75000.0, "https://images.pexels.com/photos/267394/pexels-photo-267394.jpeg?auto=compress&cs=tinysrgb&w=400", List.of(colorAttr)));
                products.add(createProduct("cat5", "HUAWEI Watch GT 4", "Classic design, smart power.", 85000.0, "https://images.pexels.com/photos/277390/pexels-photo-277390.jpeg?auto=compress&cs=tinysrgb&w=400", List.of(colorAttr)));

                // cat6 - Cameras
                products.add(createProduct("cat6", "Sony A7 IV", "Full-frame hybrid standard.", 650000.0, "https://images.pexels.com/photos/51383/photo-camera-subject-photographer-51383.jpeg?auto=compress&cs=tinysrgb&w=400", null));
                products.add(createProduct("cat6", "Canon EOS R6 II", "Fastest mirrorless performance.", 620000.0, "https://images.pexels.com/photos/1203803/pexels-photo-1203803.jpeg?auto=compress&cs=tinysrgb&w=400", null));
                products.add(createProduct("cat6", "Fujifilm X-T5", "High-res photography first.", 480000.0, "https://images.pexels.com/photos/168447/pexels-photo-168447.jpeg?auto=compress&cs=tinysrgb&w=400", null));
                products.add(createProduct("cat6", "GoPro Hero 12", "The ultimate action camera.", 145000.0, "https://images.pexels.com/photos/1253413/pexels-photo-1253413.jpeg?auto=compress&cs=tinysrgb&w=400", null));
                products.add(createProduct("cat6", "DJI Mini 4 Pro", "Professional aerial imagery.", 280000.0, "https://images.pexels.com/photos/1034812/pexels-photo-1034812.jpeg?auto=compress&cs=tinysrgb&w=400", null));

                // cat7 - Gaming
                products.add(createProduct("cat7", "PlayStation 5", "Play Has No Limits.", 185000.0, "https://images.pexels.com/photos/442576/pexels-photo-442576.jpeg?auto=compress&cs=tinysrgb&w=400", List.of(new Product.Attribute("Edition", "text", List.of("Standard", "Digital")))));
                products.add(createProduct("cat7", "Xbox Series X", "Power Your Dreams.", 175000.0, "https://images.pexels.com/photos/1298601/pexels-photo-1298601.jpeg?auto=compress&cs=tinysrgb&w=400", null));
                products.add(createProduct("cat7", "Nintendo Switch OLED", "Vivid OLED screen.", 125000.0, "https://images.pexels.com/photos/371924/pexels-photo-371924.jpeg?auto=compress&cs=tinysrgb&w=400", List.of(colorAttr)));
                products.add(createProduct("cat7", "Steam Deck 512GB", "The handheld PC gaming.", 195000.0, "https://images.pexels.com/photos/9072221/pexels-photo-9072221.jpeg?auto=compress&cs=tinysrgb&w=400", null));
                products.add(createProduct("cat7", "ROG Ally", "Play all your games.", 235000.0, "https://images.pexels.com/photos/7841474/pexels-photo-7841474.jpeg?auto=compress&cs=tinysrgb&w=400", null));

                // cat8 - Accessories
                products.add(createProduct("cat8", "Logitech MX Master 3S", "The master mouse for pros.", 35000.0, "https://images.pexels.com/photos/356056/pexels-photo-356056.jpeg?auto=compress&cs=tinysrgb&w=400", List.of(colorAttr)));
                products.add(createProduct("cat8", "Anker 737 Power Bank", "Ultra-fast laptop charging.", 45000.0, "https://images.pexels.com/photos/3945667/pexels-photo-3945667.jpeg?auto=compress&cs=tinysrgb&w=400", null));
                products.add(createProduct("cat8", "Sandisk 1TB SSD", "Extreme portable speed.", 55000.0, "https://images.pexels.com/photos/3740744/pexels-photo-3740744.jpeg?auto=compress&cs=tinysrgb&w=400", null));
                products.add(createProduct("cat8", "Mechanical Keyboard", "RGB customized experience.", 45000.0, "https://images.pexels.com/photos/1779487/pexels-photo-1779487.jpeg?auto=compress&cs=tinysrgb&w=400", List.of(colorAttr)));
                products.add(createProduct("cat8", "Thunderbolt 4 Hub", "Connect everything at once.", 65000.0, "https://images.pexels.com/photos/3802510/pexels-photo-3802510.jpeg?auto=compress&cs=tinysrgb&w=400", null));

                for (Product p : products) {
                    batch.set(db.collection("products").document(p.getProductId()), p);
                }

                batch.commit().addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Products Seeded with Attributes!", Toast.LENGTH_SHORT).show();
                    loadCategories(db);
                });
            });
        });
    }

    private void loadCategories(FirebaseFirestore db) {
        db.collection("categories").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<Category> categories = task.getResult().toObjects(Category.class);
                        adapter = new CategoryAdapter(categories, category -> {
                            Bundle bundle = new Bundle();
                            bundle.putString("categoryId", category.getCategoryId());
                            ListingFragment fragment = new ListingFragment();
                            fragment.setArguments(bundle);
                            getParentFragmentManager().beginTransaction()
                                    .replace(R.id.fragment_container, fragment)
                                    .addToBackStack(null)
                                    .commit();
                        });
                        binding.recyclerViewCategories.setAdapter(adapter);
                    }
                });
    }

    private Product createProduct(String catId, String title, String desc, double price, String imgUrl, List<Product.Attribute> attributes) {
        return Product.builder()
                .productId(UUID.randomUUID().toString())
                .title(title)
                .description(desc)
                .price(price)
                .categoryId(catId)
                .images(List.of(imgUrl))
                .stockCount(20)
                .status(true)
                .rating(4.5f)
                .attributes(attributes)
                .build();
    }
}
