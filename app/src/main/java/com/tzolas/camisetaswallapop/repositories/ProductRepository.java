package com.tzolas.camisetaswallapop.repositories;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.tzolas.camisetaswallapop.models.Product;

import java.util.Map;

public class ProductRepository {

    private final FirebaseFirestore db;

    public ProductRepository() {
        db = FirebaseFirestore.getInstance();
    }

    /** =====================================================
     *  SUBIR PRODUCTO (crear)
     * ===================================================== */
    public Task<Void> uploadProduct(Product product) {
        return db.collection("products")
                .document(product.getId())
                .set(product);
    }

    /** =====================================================
     *  ACTUALIZAR PRODUCTO (editar)
     * ===================================================== */
    public Task<Void> updateProduct(String productId, Map<String, Object> updates) {
        return db.collection("products")
                .document(productId)
                .update(updates);
    }

    /** =====================================================
     *  ELIMINAR PRODUCTO
     * ===================================================== */
    public Task<Void> deleteProduct(String productId) {
        return db.collection("products")
                .document(productId)
                .delete();
    }

    /** =====================================================
     *  OBTENER TODOS LOS PRODUCTOS
     * ===================================================== */
    public Task<QuerySnapshot> getProducts() {
        return db.collection("products")
                .whereEqualTo("sold", false)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get();
    }


    /** =====================================================
     *  OBTENER PRODUCTOS RECIENTES (FEED)
     * ===================================================== */
    public Task<QuerySnapshot> getLatestProducts(int limit) {
        return db.collection("products")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(limit)
                .get();
    }

    /** =====================================================
     *  PRODUCTOS POR USUARIO (perfil vendedor)
     * ===================================================== */
    public Task<QuerySnapshot> getProductsByUser(String userId) {
        return db.collection("products")
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get();
    }

    /** =====================================================
     *  BÚSQUEDA (listo para filtros)
     * ===================================================== */
    public Task<QuerySnapshot> searchProducts(String title) {
        return db.collection("products")
                .orderBy("title")
                .startAt(title)
                .endAt(title + "\uf8ff")
                .get();
    }
}
