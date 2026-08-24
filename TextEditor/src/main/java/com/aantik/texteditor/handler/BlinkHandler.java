package com.aantik.texteditor.handler;

import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;

import com.aantik.texteditor.EditView;
import com.aantik.texteditor.config.EditorConstants;
import java.lang.ref.WeakReference;


public class BlinkHandler extends Handler {

    public static final int MSG_BLINK = 4;
    public static final int MSG_FADE = 3;

    private static final long FRAME_DELAY = 16;

    private final WeakReference<EditView> mViewReference;

    private boolean mEnabled = true;
    private boolean mFading = false;


    public BlinkHandler(EditView view) {

        mViewReference = new WeakReference<EditView>(view);
    }

    @Override
    public void handleMessage(Message message) {

        EditView view = mViewReference.get();


        if (view == null) {

            removeCallbacksAndMessages(null);

            return;
        }


        switch (message.what) {

            case MSG_BLINK:

                if (mEnabled && view.isFocused()) {

                    view.invalidate();

                    sendEmptyMessageDelayed(MSG_BLINK, FRAME_DELAY);
                }

                break;


            case MSG_FADE:

                long elapsed = SystemClock.uptimeMillis() - view.getLastScrollTime();


                long fadeTime = elapsed - EditorConstants.FADE_DELAY;


                if (fadeTime >= 0 && fadeTime < EditorConstants.FADE_DURATION) {

                    mFading = true;

                    view.invalidate();

                    sendEmptyMessageDelayed(MSG_FADE, FRAME_DELAY);

                } else {

                    if (mFading) {

                        mFading = false;

                        view.invalidate();
                    }
                }

                break;
        }
    }
}