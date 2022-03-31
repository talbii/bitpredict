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

    void bind(CoinStruct c) {
        Glide.with(icon.getContext())
             .load(c.iconref)
             .into(icon);
        name.setText(c.name);
        price.setText(Formatting.formatDouble(c.latest));
    }
}