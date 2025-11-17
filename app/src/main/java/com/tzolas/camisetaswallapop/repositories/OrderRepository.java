package com.tzolas.camisetaswallapop.repositories;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Transaction;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class OrderRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * Comprar producto (transacción segura)
     * price en el product es double -> usamos Math.round para convertir a int (puntos)
     */
    public Task<Void> buyProduct(String productId, String buyerId) {

        DocumentReference productRef = db.collection("products").document(productId);

        return db.runTransaction((Transaction.Function<Void>) transaction -> {

            // obtener producto
            var prodSnap = transaction.get(productRef);
            if (!prodSnap.exists()) throw new RuntimeException("Producto no encontrado");

            Boolean sold = prodSnap.getBoolean("sold");
            if (sold != null && sold) throw new RuntimeException("Producto ya vendido");

            String sellerId = prodSnap.getString("userId");
            Double priceD = prodSnap.getDouble("price");
            int price = priceD == null ? 0 : (int) Math.round(priceD);

            DocumentReference buyerRef = db.collection("users").document(buyerId);
            DocumentReference sellerRef = db.collection("users").document(sellerId);

            var buyerSnap = transaction.get(buyerRef);
            var sellerSnap = transaction.get(sellerRef);

            if (!buyerSnap.exists() || !sellerSnap.exists()) throw new RuntimeException("Usuarios no encontrados");

            long buyerPoints = buyerSnap.contains("points") ? ((Number) buyerSnap.get("points")).longValue() : 0L;
            long sellerPoints = sellerSnap.contains("points") ? ((Number) sellerSnap.get("points")).longValue() : 0L;

            if (buyerPoints < price) throw new RuntimeException("No tienes suficientes puntos");

            // actualizar puntos
            transaction.update(buyerRef, "points", buyerPoints - price);
            // opcional: sumar spentPoints al comprador
            long newSpent = buyerSnap.contains("spentPoints") ? ((Number) buyerSnap.get("spentPoints")).longValue() + price : price;
            transaction.update(buyerRef, "spentPoints", newSpent);

            // sumar puntos al vendedor
            transaction.update(sellerRef, "points", sellerPoints + price);

            // actualizar producto: sold, buyerId, soldAt
            Map<String, Object> pUp = new HashMap<>();
            pUp.put("sold", true);
            pUp.put("buyerId", buyerId);
            pUp.put("soldAt", System.currentTimeMillis());

            transaction.update(productRef, pUp);

            return null;
        });
    }

    /**
     * Enviar oferta (crea documento en /products/{productId}/offers/{offerId})
     */
    public Task<Void> sendOffer(String productId, String chatId, String buyerId, int price) {

        String offerId = buyerId; // 1 oferta por comprador

        // guardar oferta dentro de products
        Map<String, Object> offer = new HashMap<>();
        offer.put("buyerId", buyerId);
        offer.put("price", price);
        offer.put("timestamp", System.currentTimeMillis());
        offer.put("status", "pending");

        Task<Void> t1 = db.collection("products")
                .document(productId)
                .collection("offers")
                .document(offerId)
                .set(offer);

        // ===== MENSAJE DE OFERTA EN EL CHAT =====
        String msgId = UUID.randomUUID().toString();

        Map<String, Object> msg = new HashMap<>();
        msg.put("id", msgId);
        msg.put("senderId", buyerId);
        msg.put("text", "Oferta de " + price + "puntos"); // texto visible
        msg.put("timestamp", System.currentTimeMillis());
        msg.put("delivered", false);
        msg.put("read", false);

        // CAMPOS IMPORTANTES
        msg.put("type", "offer");
        msg.put("offerPrice", price);
        msg.put("status", "pending");

        Task<Void> t2 = db.collection("chats")
                .document(chatId)
                .collection("messages")
                .document(msgId)
                .set(msg);

        return Tasks.whenAll(t1, t2);
    }
    /**
     * Aceptar oferta: marca oferta accepted y marca producto como vendido
     * Nota: NO modificamos puntos aquí porque la transferencia de puntos puede hacerse
     * cuando vendedor acepta (o validar que comprador sigue teniendo puntos).
     */
    public Task<Void> acceptOffer(String productId, String offerId, String sellerId) {

        DocumentReference offerRef = db.collection("products").document(productId).collection("offers").document(offerId);
        DocumentReference productRef = db.collection("products").document(productId);

        return db.runTransaction((Transaction.Function<Void>) transaction -> {

            var offerSnap = transaction.get(offerRef);
            if (!offerSnap.exists()) throw new RuntimeException("Oferta no encontrada");

            String buyerId = offerSnap.getString("buyerId");
            Number priceN = (Number) offerSnap.get("price");
            int price = priceN == null ? 0 : priceN.intValue();

            var prodSnap = transaction.get(productRef);
            if (!prodSnap.exists()) throw new RuntimeException("Producto no encontrado");

            Boolean sold = prodSnap.getBoolean("sold");
            if (sold != null && sold) throw new RuntimeException("Producto ya vendido");

            // Actualizar oferta
            transaction.update(offerRef, "status", "accepted");

            // Marcar producto vendido
            Map<String, Object> pUp = new HashMap<>();
            pUp.put("sold", true);
            pUp.put("buyerId", buyerId);
            pUp.put("soldAt", System.currentTimeMillis());
            transaction.update(productRef, pUp);

            // Opcional: aquí NO movemos puntos. Haremos el movimiento de puntos con buyProduct()
            // o bien: si quieres que la aceptación conlleve el pago, añade lectura y actualización de users aquí.

            return null;
        });
    }

    public Task<Void> rejectOffer(String productId, String offerId) {
        return db.collection("products").document(productId)
                .collection("offers")
                .document(offerId)
                .update("status", "rejected");
    }

    /**
     * Guardar shipping info en el producto (o podrías crear /orders/{id})
     */
    public Task<Void> saveShippingInfo(String productId, String address, String postalCode, String phone) {
        Map<String, Object> data = new HashMap<>();
        data.put("shippingAddress", address);
        data.put("postalCode", postalCode);
        data.put("phone", phone);

        return db.collection("products").document(productId).update(data);
    }
}
