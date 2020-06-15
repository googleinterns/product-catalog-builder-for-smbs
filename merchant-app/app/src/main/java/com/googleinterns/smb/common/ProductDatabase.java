package com.googleinterns.smb.common;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.googleinterns.smb.model.Product;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import me.xdrop.fuzzywuzzy.FuzzySearch;
import me.xdrop.fuzzywuzzy.ToStringFunction;
import me.xdrop.fuzzywuzzy.model.BoundExtractedResult;

/**
 * Class to simulate in memory database.
 */
public class ProductDatabase {

    private static final String TAG = ProductDatabase.class.getName();
    private static final int CONFIDENCE_THRESHOLD = 90;

    private List<Product> products = new ArrayList<>();

    /**
     * Load database into memory
     */
    public ProductDatabase(CollectionReference collectionReference) {
        collectionReference.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot documentSnapshots) {
                        for (DocumentChange documentChange : documentSnapshots.getDocumentChanges()) {
                            DocumentSnapshot documentSnapshot = documentChange.getDocument();
                            Product product = new Product(documentSnapshot);
                            products.add(product);
                        }
                    }
                });
        // TODO handle on failure listener
    }

    /**
     * Get products consisting of names similar to search strings
     *
     * @param searchStrings query strings to be searched
     * @return products with confidence no less than CONFIDENCE_THRESHOLD
     */
    public List<Product> fuzzySearch(List<String> searchStrings) {
        // matched products
        Set<Product> matchedProducts = new HashSet<>();
        for (String searchString : searchStrings) {
            List<BoundExtractedResult<Product>> extractedResults = FuzzySearch.extractSorted(searchString, products, new ToStringFunction<Product>() {
                @Override
                public String apply(Product item) {
                    return item.getProductName();
                }
            }, CONFIDENCE_THRESHOLD);
            for (BoundExtractedResult<Product> extractedResult : extractedResults) {
                matchedProducts.add(extractedResult.getReferent());
            }
        }
        return new ArrayList<>(matchedProducts);
    }
}
