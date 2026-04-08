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
import lk.jiat.eshop.model.Category;

public class AdminCategoryAdapter extends RecyclerView.Adapter<AdminCategoryAdapter.ViewHolder> {

    private List<Category> categories;
    private OnCategoryActionListener listener;

    public AdminCategoryAdapter(List<Category> categories, OnCategoryActionListener listener) {
        this.categories = categories;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Category category = categories.get(position);
        holder.name.setText(category.getName());

        Glide.with(holder.itemView.getContext())
                .load(category.getImageUrl())
                .circleCrop()
                .placeholder(R.drawable.app_logo)
                .into(holder.image);

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDelete(category);
            }
        });
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView name;
        View btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.admin_cat_image);
            name = itemView.findViewById(R.id.admin_cat_name);
            btnDelete = itemView.findViewById(R.id.btn_delete_cat);
        }
    }

    public interface OnCategoryActionListener {
        void onDelete(Category category);
    }
}
