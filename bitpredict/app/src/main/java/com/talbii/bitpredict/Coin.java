package com.talbii.bitpredict;

import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class Coin {
    ImageView icon;
    TextView name;
    TextView price;

    private CoinStruct c;

    void bind(CoinStruct c) {
        this.c = c;
        Glide.with(icon.getContext())
             .load(this.c.iconref)
             .into(icon);
        name.setText(this.c.name);
        price.setText(String.valueOf(this.c.latest));
    }
}