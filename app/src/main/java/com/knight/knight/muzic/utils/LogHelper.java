package com.knight.knight.muzic.utils;

import android.content.ComponentName;
import android.util.Log;

/**
 * Created by kn1gh7 on 22/6/16.
 */
public class LogHelper {
    ComponentName tag;

    public LogHelper(ComponentName tag) {
        this.tag = tag;
    }

    public void loge(String msg) {
        Log.e(tag.getClassName(), msg);
    }
}
