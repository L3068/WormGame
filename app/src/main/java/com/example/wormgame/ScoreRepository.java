package com.example.wormgame;

import android.content.Context;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

/**
 * A pontszámok mentése és lekérdezése a Realtime Database-ben.
 *
 * <p>Az adatszerkezet a regisztrációnál használt {@code users/<uid>} ágat követi:
 * <ul>
 *     <li>{@code users/<uid>/highScore} – a felhasználó eddigi legjobb eredménye</li>
 *     <li>{@code users/<uid>/scores/<id>} – az egyes játszmák eredménye időbélyeggel</li>
 * </ul>
 *
 * <p>Ha nincs Firebase konfiguráció vagy nincs bejelentkezett felhasználó, az
 * osztály nem elérhető ({@link #isAvailable()}), és minden művelete csendben
 * kimarad – a játék offline is játszható marad.
 */
final class ScoreRepository {

    private static final String TAG = "ScoreRepository";

    /** A legjobb pontszám aszinkron megérkezésekor hívódik. */
    interface BestScoreCallback {
        void onBestScore(int bestScore);
    }

    @Nullable
    private final DatabaseReference userRef;

    private ScoreRepository(@Nullable DatabaseReference userRef) {
        this.userRef = userRef;
    }

    /** Bejelentkezett felhasználóhoz kötött tároló, vagy nem elérhető példány. */
    static ScoreRepository create(Context context) {
        if (FirebaseApp.getApps(context).isEmpty()) {
            return new ScoreRepository(null);
        }
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            return new ScoreRepository(null);
        }
        return new ScoreRepository(
                FirebaseDatabase.getInstance().getReference("users").child(user.getUid()));
    }

    boolean isAvailable() {
        return userRef != null;
    }

    /**
     * Egy befejezett játszma eredményének mentése. A legjobb pontszámot
     * tranzakcióval frissítjük, hogy párhuzamos írásnál se romoljon el.
     */
    void saveScore(int score) {
        if (userRef == null) {
            return;
        }

        Map<String, Object> entry = new HashMap<>();
        entry.put("score", score);
        entry.put("timestamp", ServerValue.TIMESTAMP);
        userRef.child("scores").push().setValue(entry)
                .addOnFailureListener(e -> Log.w(TAG, "A pontszám mentése nem sikerült", e));

        userRef.child("highScore").runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData currentData) {
                Integer best = currentData.getValue(Integer.class);
                if (best != null && best >= score) {
                    return Transaction.abort();
                }
                currentData.setValue(score);
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed,
                                   @Nullable DataSnapshot currentData) {
                if (error != null) {
                    Log.w(TAG, "A legjobb pontszám frissítése nem sikerült", error.toException());
                }
            }
        });
    }

    /** A legjobb pontszám lekérdezése. Hiba vagy hiányzó adat esetén nem hív vissza. */
    void loadBestScore(BestScoreCallback callback) {
        if (userRef == null) {
            return;
        }
        userRef.child("highScore").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Integer best = snapshot.getValue(Integer.class);
                if (best != null) {
                    callback.onBestScore(best);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.w(TAG, "A legjobb pontszám lekérdezése nem sikerült", error.toException());
            }
        });
    }
}
