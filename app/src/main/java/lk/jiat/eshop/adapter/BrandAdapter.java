package lk.jiat.eshop.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import lk.jiat.eshop.R;

public class BrandAdapter extends RecyclerView.Adapter<BrandAdapter.ViewHolder> {

    private List<String> brands;
    private OnBrandClickListener listener;

    public BrandAdapter(List<String> brands, OnBrandClickListener listener) {
        this.brands = brands;
        this.listener = listener;
    }

    @NonNull
    @Override
    public BrandAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_brand, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BrandAdapter.ViewHolder holder, int position) {
        String brand = brands.get(position);
        holder.brandName.setText(brand);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onBrandClick(brand);
            }
        });
    }

    @Override
    public int getItemCount() {
        return brands.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView brandName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            brandName = itemView.findViewById(R.id.brand_name);
        }
    }

    public interface OnBrandClickListener {
        void onBrandClick(String brand);
    }
}
