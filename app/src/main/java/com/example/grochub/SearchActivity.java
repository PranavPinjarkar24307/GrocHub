package com.example.grochub;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.grochub.adapter.ProductAdapter;
import com.example.grochub.model.ProductModel;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private SearchView searchView;
    private RecyclerView recyclerView;
    private ProductAdapter adapter;
    private final List<ProductModel> productList = new ArrayList<>();

    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        searchView = findViewById(R.id.search_view);
        recyclerView = findViewById(R.id.rv_products);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ProductAdapter(productList, this);
        recyclerView.setAdapter(adapter);

        firestore = FirebaseFirestore.getInstance();

        searchView.setIconified(false);
        searchView.requestFocus();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String text) {

                if (text == null || text.trim().isEmpty()) {
                    productList.clear();
                    adapter.notifyDataSetChanged();
                } else {
                    searchProducts(text.toLowerCase());
                }
                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
        });
    }

    private void searchProducts(String query) {

        firestore.collection("products")
                .get()
                .addOnSuccessListener(snapshot -> {

                    productList.clear();

                    List<ProductModel> exact = new ArrayList<>();
                    List<ProductModel> related = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : snapshot) {

                        ProductModel product = doc.toObject(ProductModel.class);
                        if (product.getName() == null) continue;

                        String name = product.getName().toLowerCase();

                        if (name.startsWith(query)) {
                            exact.add(product);
                        } else if (name.contains(query)) {
                            related.add(product);
                        }
                    }

                    productList.addAll(exact);
                    productList.addAll(related);
                    adapter.notifyDataSetChanged();

                    Log.d("SEARCH", "Results = " + productList.size());
                })
                .addOnFailureListener(e ->
                        Log.e("SEARCH", "Firestore error", e)
                );
    }
}
