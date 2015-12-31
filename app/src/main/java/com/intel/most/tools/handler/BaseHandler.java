package com.intel.most.tools.handler;

import android.app.Activity;
import android.os.Handler;

import java.lang.ref.WeakReference;

public class BaseHandler<T extends Activity> extends Handler {
    protected final WeakReference<T> mActivity;

    public BaseHandler(T activity) {
        mActivity = new WeakReference<>(activity);
    }

    protected T getContext() {
        return mActivity.get();
    }
}
