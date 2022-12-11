package com.talbii.bitpredict;

import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

public class Coin {
    ImageView icon;
    TextView name;
    TextView price;

    public void bind(CoinStruct c) {
        Glide.with(icon.getContext())
             .load(c.iconref)
             .into(icon);
        name.setText(c.name);
        price.setText(Utilities.formatDouble(c.latest));
    }
}