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

public class AdminOrdersAdapter extends RecyclerView.Adapter<AdminOrdersAdapter.ViewHolder> {

    private List<Order> orders;
    private OnOrderActionListener listener;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());

    public AdminOrdersAdapter(List<Order> orders, OnOrderActionListener listener) {
        this.orders = orders;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Order order = orders.get(position);
        
        // Show the FULL Order ID as it appears in Firestore
        holder.orderId.setText("Order ID: " + order.getOrderId());
        
        holder.status.setText(order.getStatus().toUpperCase());
        holder.amount.setText(String.format("Total: Rs. %.2f", order.getTotalAmount()));
        
        if (order.getOrderDate() != null) {
            holder.date.setText(dateFormat.format(order.getOrderDate().toDate()));
        }

        if (order.getShippingAddress() != null) {
            holder.customerName.setText("Customer: " + order.getShippingAddress().getName());
        }

        // Color coding for status
        updateStatusStyle(holder.status, order.getStatus());

        holder.btnUpdate.setOnClickListener(v -> {
            if (listener != null) {
                listener.onUpdateStatus(order);
            }
        });

        holder.btnCall.setOnClickListener(v -> {
            if (listener != null && order.getShippingAddress() != null) {
                listener.onCallCustomer(order.getShippingAddress().getContact());
            }
        });
    }

    private void updateStatusStyle(TextView statusView, String status) {
        if (status.equalsIgnoreCase("paid")) {
            statusView.setBackgroundResource(R.drawable.circle_shape);
            statusView.setBackgroundTintList(statusView.getContext().getColorStateList(R.color.dash_color_categories)); // Teal for Paid
        } else if (status.equalsIgnoreCase("shipped")) {
            statusView.setBackgroundResource(R.drawable.circle_shape);
            statusView.setBackgroundTintList(statusView.getContext().getColorStateList(R.color.dash_color_products)); // Blue for Shipped
        } else if (status.equalsIgnoreCase("delivered")) {
            statusView.setBackgroundResource(R.drawable.circle_shape);
            statusView.setBackgroundTintList(statusView.getContext().getColorStateList(R.color.md_theme_outline)); // Grey for Delivered
        } else {
            statusView.setBackgroundResource(R.drawable.circle_shape);
            statusView.setBackgroundTintList(statusView.getContext().getColorStateList(R.color.admin_secondary)); // Orange for others
        }
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView orderId, status, date, customerName, amount;
        View btnUpdate, btnCall;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            orderId = itemView.findViewById(R.id.tv_admin_order_id);
            status = itemView.findViewById(R.id.tv_admin_order_status);
            date = itemView.findViewById(R.id.tv_admin_order_date);
            customerName = itemView.findViewById(R.id.tv_admin_customer_name);
            amount = itemView.findViewById(R.id.tv_admin_order_amount);
            btnUpdate = itemView.findViewById(R.id.btn_update_status);
            btnCall = itemView.findViewById(R.id.btn_call_customer);
        }
    }

    public interface OnOrderActionListener {
        void onUpdateStatus(Order order);
        void onCallCustomer(String phoneNumber);
    }
}
