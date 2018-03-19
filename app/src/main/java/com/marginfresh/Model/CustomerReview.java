package com.marginfresh.Model;


import android.app.Activity;
import android.support.v4.app.Fragment;

public class CustomerReview extends Activity {

    public String ratingStars,ratingTitle,ratingDetail,ratingUser;

    public String getRatingStars() {
        return ratingStars;
    }

    public void setRatingStars(String ratingStars) {
        this.ratingStars = ratingStars;
    }

    public String getRatingTitle() {
        return ratingTitle;
    }

    public void setRatingTitle(String ratingTitle) {
        this.ratingTitle = ratingTitle;
    }

    public String getRatingDetail() {
        return ratingDetail;
    }

    public void setRatingDetail(String ratingDetail) {
        this.ratingDetail = ratingDetail;
    }

    public String getRatingUser() {
        return ratingUser;
    }

    public void setRatingUser(String ratingUser) {
        this.ratingUser = ratingUser;
    }
}

