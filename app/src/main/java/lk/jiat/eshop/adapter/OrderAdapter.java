package lk.jiat.eshop.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import lk.jiat.eshop.R;
import lk.jiat.eshop.model.Order;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {

    private List<Order> orders;
    private OnOrderClickListener listener;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

    public OrderAdapter(List<Order> orders, OnOrderClickListener listener) {
        this.orders = orders;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Order order = orders.get(position);
        
        holder.orderId.setText("Order ID: " + order.getOrderId());
        holder.status.setText(order.getStatus().toUpperCase());
        holder.total.setText(String.format("Total: Rs. %.2f", order.getTotalAmount()));
        
        if (order.getOrderDate() != null) {
            holder.date.setText(dateFormat.format(order.getOrderDate().toDate()));
        }

        int itemCount = order.getOrderItems() != null ? order.getOrderItems().size() : 0;
        holder.itemsSummary.setText("Items: " + itemCount + (itemCount == 1 ? " product" : " products"));

        // Set status color
        updateStatusStyle(holder.status, order.getStatus());

        holder.btnViewDetails.setOnClickListener(v -> {
            if (listener != null) {
                listener.onOrderClick(order);
            }
        });
    }

    private void updateStatusStyle(TextView statusView, String status) {
        if (status.equalsIgnoreCase("paid")) {
            statusView.setBackgroundTintList(statusView.getContext().getColorStateList(R.color.dash_color_categories));
        } else if (status.equalsIgnoreCase("shipped")) {
            statusView.setBackgroundTintList(statusView.getContext().getColorStateList(R.color.dash_color_products));
        } else if (status.equalsIgnoreCase("delivered")) {
            statusView.setBackgroundTintList(statusView.getContext().getColorStateList(R.color.md_theme_outline));
        } else {
            statusView.setBackgroundTintList(statusView.getContext().getColorStateList(R.color.md_theme_secondary));
        }
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView orderId, status, date, itemsSummary, total;
        View btnViewDetails;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            orderId = itemView.findViewById(R.id.order_id);
            status = itemView.findViewById(R.id.order_status);
            date = itemView.findViewById(R.id.order_date);
            itemsSummary = itemView.findViewById(R.id.order_items_summary);
            total = itemView.findViewById(R.id.order_total);
            btnViewDetails = itemView.findViewById(R.id.btn_view_details);
        }
    }

    public interface OnOrderClickListener {
        void onOrderClick(Order order);
    }
}
