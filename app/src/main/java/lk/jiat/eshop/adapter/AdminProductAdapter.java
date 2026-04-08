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

public class AdminProductAdapter extends RecyclerView.Adapter<AdminProductAdapter.ViewHolder> {

    private List<Product> products;
    private OnProductActionListener listener;

    public AdminProductAdapter(List<Product> products, OnProductActionListener listener) {
        this.products = products;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = products.get(position);
        holder.title.setText(product.getTitle());
        holder.price.setText(String.format("Rs. %.2f", product.getPrice()));
        holder.stock.setText("Stock: " + product.getStockCount());

        if (product.getImages() != null && !product.getImages().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(product.getImages().get(0))
                    .circleCrop()
                    .placeholder(R.drawable.app_logo)
                    .into(holder.image);
        }

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDelete(product);
            }
        });
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title, price, stock;
        View btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.admin_product_image);
            title = itemView.findViewById(R.id.admin_product_title);
            price = itemView.findViewById(R.id.admin_product_price);
            stock = itemView.findViewById(R.id.admin_product_stock);
            btnDelete = itemView.findViewById(R.id.btn_delete_product);
        }
    }

    public interface OnProductActionListener {
        void onDelete(Product product);
    }
}
