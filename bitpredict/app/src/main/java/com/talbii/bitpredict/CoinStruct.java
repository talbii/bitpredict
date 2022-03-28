package com.talbii.bitpredict;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.storage.StorageReference;
import java.util.ArrayList;

public class CoinStruct {
    public String name;
    public double latest;
    public DocumentReference historical;
    public StorageReference iconref;
}
