package lk.jiat.eshop.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import lk.jiat.eshop.R;
import lk.jiat.eshop.model.Product;

public class WishlistAdapter extends RecyclerView.Adapter<WishlistAdapter.ViewHolder> {

    private List<Product> products;
    private OnWishlistActionListener listener;

    public WishlistAdapter(List<Product> products, OnWishlistActionListener listener) {
        this.products = products;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_listing, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = products.get(position);
        holder.title.setText(product.getTitle());
        holder.price.setText(String.format("Rs. %.2f", product.getPrice()));

        if (product.getImages() != null && !product.getImages().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(product.getImages().get(0))
                    .placeholder(R.drawable.app_logo)
                    .into(holder.image);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onProductClick(product);
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) listener.onRemove(product);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title, price;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.listing_item_image);
            title = itemView.findViewById(R.id.listing_item_name);
            price = itemView.findViewById(R.id.listing_item_price);
        }
    }

    public interface OnWishlistActionListener {
        void onProductClick(Product product);
        void onRemove(Product product);
    }
}
