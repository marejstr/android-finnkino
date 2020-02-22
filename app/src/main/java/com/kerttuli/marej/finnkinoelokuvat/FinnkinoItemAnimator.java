package com.kerttuli.marej.finnkinoelokuvat;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.Point;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;

public class FinnkinoItemAnimator extends DefaultItemAnimator {

    @Override
    public boolean animateAdd(final RecyclerView.ViewHolder holder) {
        if (holder.getItemViewType() == ScheduleAdapter.VIEW_TYPE_MOVIE) {

            WindowManager wm = (WindowManager) holder.itemView.getContext().getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int screenHeight = size.y;

            holder.itemView.setTranslationY(screenHeight);
            holder.itemView.animate()
                    .translationY(0)
                    .setInterpolator(new DecelerateInterpolator(3.f))
                    .setDuration(700)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            dispatchAddFinished(holder);
                        }
                    })
                    .start();
            return false;
        }
        dispatchAddFinished(holder);
        return false;
    }
}
