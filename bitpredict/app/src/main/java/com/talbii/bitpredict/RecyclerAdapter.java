package com.talbii.bitpredict;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public abstract class RecyclerAdapter<TStruct> extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {
    protected final List<TStruct> list;

    public RecyclerAdapter(List<TStruct> list) {
        this.list = list;
    }

    /**
     * Provide a reference to the type of views that you are using (custom ViewHolder)
     */
    public static abstract class ViewHolder<TStruct> extends RecyclerView.ViewHolder {
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        public abstract void bindData(TStruct s);
    }

    protected abstract int getItemResource();
    protected View getViewFromViewHolder(ViewGroup viewGroup, int viewType) {
        return LayoutInflater.from(viewGroup.getContext())
                .inflate(getItemResource(), viewGroup, false);
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public abstract ViewHolder<TStruct> onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType);

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        // Get element from your dataset at this position and replace the contents of the view
        // with that element
        var data = list.get(position);
        viewHolder.bindData(data);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return list.size();
    }
}