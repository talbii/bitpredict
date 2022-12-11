package com.talbii.bitpredict;

import android.content.Intent;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MainRecyclerAdapter extends RecyclerView.Adapter<MainRecyclerAdapter.ViewHolder> {
    private final List<CoinStruct> list;
    private final Handler handler;

    public MainRecyclerAdapter(List<CoinStruct> list, Handler handler) {
        this.list = list;
        this.handler = handler;
    }

    protected int getItemResource() {
        return R.layout.mainactivity_coin_list;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
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

    public View getViewFromViewHolder(ViewGroup viewGroup, int viewType) {
        return LayoutInflater.from(viewGroup.getContext())
                .inflate(getItemResource(), viewGroup, false);
    }

    @NonNull
    public MainRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        var v = getViewFromViewHolder(viewGroup, viewType);
        return new ViewHolder(v);
    }

    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        var data = list.get(position);
        viewHolder.bindData(data);
        viewHolder.itemView.setOnClickListener(view -> {
            if(MainActivity.currentNetworkState) {
                var c = view.getContext();
                var intent = new Intent(c, CoinActivity.class);
                intent.putExtra("name", data.name);
                intent.putExtra("symbol", data.iconref.getPath());
                intent.putExtra("historical", data.historical.getPath());
                intent.putExtra("latest_quote", data.latest);

                c.startActivity(intent);
            } else handler.sendEmptyMessage(MainActivityConstants.MESSAGE_NO_INTENT);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }


}
