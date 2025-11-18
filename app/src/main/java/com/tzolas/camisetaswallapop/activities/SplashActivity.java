package com.tzolas.camisetaswallapop.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.tzolas.camisetaswallapop.R;

public class SplashActivity extends AppCompatActivity {

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        auth = FirebaseAuth.getInstance();
        animateSplash();
    }

    private void animateSplash() {
        ImageView splashIcon = findViewById(R.id.splashIcon);
        View background = findViewById(R.id.background);

        // Resetear propiedades para la animación
        splashIcon.setScaleX(0f);
        splashIcon.setScaleY(0f);
        splashIcon.setAlpha(0f);
        background.setAlpha(0f);

        // Animación épica
        AnimatorSet animatorSet = new AnimatorSet();

        ObjectAnimator bgFadeIn = ObjectAnimator.ofFloat(background, "alpha", 0f, 1f);
        ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(splashIcon, "scaleX", 0f, 1.2f, 1f);
        ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(splashIcon, "scaleY", 0f, 1.2f, 1f);
        ObjectAnimator rotation = ObjectAnimator.ofFloat(splashIcon, "rotation", 0f, 720f);
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(splashIcon, "alpha", 0f, 1f);

        bgFadeIn.setDuration(400);
        scaleUpX.setDuration(1200);
        scaleUpY.setDuration(1200);
        rotation.setDuration(1000);
        fadeIn.setDuration(800);

        animatorSet.play(bgFadeIn).before(scaleUpX);
        animatorSet.playTogether(scaleUpX, scaleUpY, rotation, fadeIn);
        animatorSet.setInterpolator(new OvershootInterpolator(0.8f));

        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                new Handler().postDelayed(() -> {
                    checkAuthAndRedirect();
                }, 300);
            }
        });

        animatorSet.start();
    }

    private void checkAuthAndRedirect() {
        FirebaseUser currentUser = auth.getCurrentUser();

        Intent intent;

        if (currentUser != null) {
            // Usuario YA está autenticado con Firebase - ir directamente al Main
            intent = new Intent(SplashActivity.this, MainActivity.class);
        } else {
            // Usuario NO autenticado - ir al Login
            intent = new Intent(SplashActivity.this, LoginActivity.class);
        }

        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
}