package ru.qdev.lnotes.ui.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;
import androidx.core.content.ContextCompat;
import androidx.appcompat.widget.AppCompatImageView;
import android.view.LayoutInflater;
import android.view.View;

import ru.qdev.lnotes.R;

/**
 * Created by Vladimir Kudashov on 09.10.18.
 */

@UiThread
public class QDVViewFabric {
    Context context;
    LayoutInflater inflater;

    public QDVViewFabric(@NonNull Context context, @NonNull LayoutInflater inflater) {
        this.context = context;
        this.inflater = inflater;
    }

    @UiThread
    public View createRatingView()
    {
        View ratingView = inflater.inflate(R.layout.rating_view, null);
        AppCompatImageView imageStar1 = ratingView.findViewById(R.id.star1);
        AppCompatImageView imageStar2 = ratingView.findViewById(R.id.star2);
        AppCompatImageView imageStar3 = ratingView.findViewById(R.id.star3);
        AppCompatImageView imageStar4 = ratingView.findViewById(R.id.star4);
        AppCompatImageView imageStar5 = ratingView.findViewById(R.id.star5);

        AppCompatImageView[] appCompatImageViews = new AppCompatImageView[5];
        appCompatImageViews[0] = imageStar1;
        appCompatImageViews[1] = imageStar2;
        appCompatImageViews[2] = imageStar3;
        appCompatImageViews[3] = imageStar4;
        appCompatImageViews[4] = imageStar5;

        int filterStartColor = ContextCompat.getColor(context, R.color.transparentColor);
        int filterSelectedColor =
                ContextCompat.getColor(context, R.color.rateStarSelectedColor);
        ArgbEvaluator argbEvaluator = new ArgbEvaluator();
        String colorFilterProperty = "colorFilter";
        long selectStarDuration = 250;

        AnimatorSet selectAnimatorSet = new AnimatorSet();
        ObjectAnimator objectAnimator = ObjectAnimator.ofObject(
                imageStar1,
                colorFilterProperty,
                argbEvaluator,
                filterStartColor,
                filterSelectedColor);
        objectAnimator.setStartDelay(0);
        objectAnimator.setRepeatCount(0);
        objectAnimator.setDuration(selectStarDuration);

        selectAnimatorSet.play(objectAnimator);

        for (AppCompatImageView imageStar : appCompatImageViews) {
            if (imageStar == imageStar1) {
                continue;
            }
            ObjectAnimator objectAnimatorNext = objectAnimator.clone();
            objectAnimatorNext.setTarget(imageStar);
            selectAnimatorSet.play(objectAnimatorNext).after(objectAnimator);
            objectAnimator = objectAnimatorNext;
        }

        final AnimatorSet scaleAnimatorSet = new AnimatorSet();

        String scaleXProperty = "scaleX";
        String scaleYProperty = "scaleY";
        long scaleStarDuration = 250;
        final long scaleStarRepeatDelay = 250;

        ObjectAnimator objectAnimatorScale = ObjectAnimator.ofFloat(
                imageStar1,
                scaleXProperty, 1.0f, 1.5f);
        objectAnimatorScale.setStartDelay(0);
        objectAnimatorScale.setRepeatCount(1);
        objectAnimatorScale.setRepeatMode(ValueAnimator.REVERSE);
        objectAnimatorScale.setDuration(scaleStarDuration);

        for (AppCompatImageView imageStar : appCompatImageViews) {
            ObjectAnimator objectAnimatorScaleX = objectAnimatorScale.clone();
            objectAnimatorScaleX.setTarget(imageStar);
            objectAnimatorScaleX.setPropertyName(scaleXProperty);
            scaleAnimatorSet.play(objectAnimatorScaleX);

            ObjectAnimator objectAnimatorScaleY = objectAnimatorScale.clone();
            objectAnimatorScaleY.setTarget(imageStar);
            objectAnimatorScaleY.setPropertyName(scaleYProperty);
            scaleAnimatorSet.play(objectAnimatorScaleY);
        }
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(selectAnimatorSet).before(scaleAnimatorSet);

        final int[] repeatCount = {0};

        scaleAnimatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                repeatCount[0]++;
                if (repeatCount[0]<3) {
                    animation.setStartDelay(scaleStarRepeatDelay);
                    animation.start();
                }
            }
        });
        animatorSet.setStartDelay(1000);
        animatorSet.start();

        return ratingView;
    }
}
