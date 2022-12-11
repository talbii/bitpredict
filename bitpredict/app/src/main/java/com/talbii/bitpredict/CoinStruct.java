package com.talbii.bitpredict;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.storage.StorageReference;

public class CoinStruct {
    String name;
    double latest;
    DocumentReference historical;
    StorageReference iconref;
}
