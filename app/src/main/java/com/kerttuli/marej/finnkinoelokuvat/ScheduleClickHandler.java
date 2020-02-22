package com.kerttuli.marej.finnkinoelokuvat;

import android.view.View;
import android.widget.ImageView;

public interface ScheduleClickHandler {
    void onScheduleClick(String finnkinoID, View sharedImageView, View sharedBackgroundView,
                         String iconTransitonName, String backgroundTransitionName, String eventID,
                         String origianlTitle);

    void onApplyButtonClick(String location, String date, String time, String rating);
}
