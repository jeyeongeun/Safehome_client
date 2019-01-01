package org.khucd.cdapp;

import android.graphics.drawable.Drawable;
import android.widget.ImageButton;

    /**
     * Created by YOUNGEUN on 2016-05-22.
     */
    public class ListViewItem {
        private String titleStr;
        private String subscription;
        private String subscription2;

        public void setTitleStr(String title){
            titleStr = title;
        }

        public void setSubscription(String sub){
            subscription = sub;
        }

        public void setSubscription2(String sub) { subscription2 = sub; }

        public String getTitleStr(){
            return titleStr;
        }

        public String getSubscription(){
            return subscription;
        }

        public String getSubscription2() { return subscription2; }
    }

