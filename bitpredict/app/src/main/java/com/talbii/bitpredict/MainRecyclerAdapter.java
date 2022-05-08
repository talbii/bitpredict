package com.talbii.bitpredict;

import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import java.util.List;

public class MainRecyclerAdapter extends RecyclerAdapter<CoinStruct> {

    public MainRecyclerAdapter(List<CoinStruct> list) {
        super(list);
    }

    @Override
    protected int getItemResource() {
        return R.layout.mainactivity_coin_list;
    }

    public static class ViewHolder extends RecyclerAdapter.ViewHolder<CoinStruct> {
        private final Coin c;

        public ViewHolder(@NonNull View v) {
            super(v);

            c = new Coin();
            c.icon = v.findViewById(R.id.coin_symbol);
            c.name = v.findViewById(R.id.coin_name);
            c.price = v.findViewById(R.id.coin_price);
        }

        public void bindData(CoinStruct s) {
            c.bind(s);
        }
    }

    @NonNull
    @Override
    public MainRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        var v = getViewFromViewHolder(viewGroup, viewType);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerAdapter.ViewHolder viewHolder, int position) {
        super.onBindViewHolder(viewHolder, position);
        var data = list.get(position);
        viewHolder.itemView.setOnClickListener(view -> {
            var c = view.getContext();
            var intent = new Intent(c, CoinActivity.class);
            intent.putExtra("name", data.name);
            intent.putExtra("symbol", data.iconref.getPath());
            intent.putExtra("historical", data.historical.getPath());
            intent.putExtra("latest_quote", data.latest);

            c.startActivity(intent);
        });
    }
}
