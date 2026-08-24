package com.aantik.texteditor.controller;

import android.content.Context;
import android.graphics.Paint;
import android.os.SystemClock;
import android.view.GestureDetector;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.inputmethod.InputMethodManager;

import com.aantik.texteditor.EditView;
import com.aantik.texteditor.VTable;
import com.aantik.texteditor.blockComment.java;
import com.aantik.texteditor.config.EditorConstants;
import com.aantik.texteditor.handler.BlinkHandler;
import com.aantik.texteditor.model.Span;

import java.util.List;
import java.util.regex.Matcher;


public class EditorTinput {

    private final EditView mView;
    private final VTable mTable;


    public EditorTinput(EditView view, VTable table) {

        mView = view;
        mTable = table;
    }


    
    private static <T> T get(List<T> list, int index) {

        return list.get(index);
    }


    
    private static <T> boolean exists(T value) {

        return value != null;
    }


    
    private static <T> boolean same(T first, T second) {

        if (first == null) {
            return second == null;
        }

        return first.equals(second);
    }


    
    private static int clamp(int value, int min, int max) {

        if (value < min) {
            return min;
        }

        if (value > max) {
            return max;
        }

        return value;
    }


    private static float clamp(float value, float min, float max) {

        if (value < min) {
            return min;
        }

        if (value > max) {
            return max;
        }

        return value;
    }


    public void setupGestureDetectors() {

        Context context = mView.getContext();


        mTable.gestureDetector = new GestureDetector(context, new EditorGestureListener());


        mTable.scaleDetector = new ScaleGestureDetector(context, new EditorScaleListener());
    }


    public void startBlink() {

        if (mTable.blinkHandler == null) {

            mTable.blinkHandler = new BlinkHandler(mView);
        }


        mTable.blinkStartTime = SystemClock.uptimeMillis();


        mTable.blinkHandler.removeMessages(BlinkHandler.MSG_BLINK);


        mTable.blinkHandler.sendEmptyMessageAtTime(BlinkHandler.MSG_BLINK, mTable.blinkStartTime + EditorConstants.BLINK_DELAY);
    }


    public void onInteraction() {

        mTable.lastInteractionTime = SystemClock.uptimeMillis();


        startBlink();


        mView.invalidate();
    }


    public boolean onTouchEvent(MotionEvent event) {

        
        if (event.getPointerCount() == 1 && !mTable.scaling) {

            if (mTable.selector.onTouchEvent(event)) {
                return true;
            }
        }


        mTable.scaleDetector.onTouchEvent(event);


        if (!mTable.scaleDetector.isInProgress() && !mTable.scaling) {

            mTable.gestureDetector.onTouchEvent(event);
        }


        return true;
    }


    public void computeScroll() {

        if (!mTable.scroller.computeScrollOffset()) {
            return;
        }


        int oldY = mTable.editorScrollY;


        mView.scrollToClamped(mTable.scroller.getCurrX(), mTable.scroller.getCurrY());


        if (oldY != mTable.editorScrollY) {

            onScrollChanged();
        }


        mView.awakenScrollBars();

        mView.postInvalidateOnAnimation();
    }


    private void onScrollChanged() {

        mTable.lastScrollTime = SystemClock.uptimeMillis();


        if (mTable.blinkHandler == null) {
            return;
        }


        mTable.blinkHandler.removeMessages(BlinkHandler.MSG_FADE);


        mTable.blinkHandler.sendEmptyMessageDelayed(BlinkHandler.MSG_FADE, EditorConstants.FADE_DELAY);
    }


    public void scrollToCursor(int position) {

        position = clamp(position, 0, mTable.text.length());


        float cursorX = mView.getXForPosition(position);


        float baseline = mView.getBaseLineForPosition(position);


        Paint.FontMetrics metrics = mTable.textPaint.getFontMetrics();


        float cursorTop = baseline + metrics.ascent;


        float cursorBottom = baseline + metrics.descent;


        int scrollX = calculateHorizontalScroll(cursorX);


        int scrollY = calculateVerticalScroll(cursorTop, cursorBottom);


        mView.scrollToClamped(scrollX, scrollY);
    }


    private int calculateHorizontalScroll(float cursorX) {

        int scrollX = mTable.editorScrollX;


        float areaWidth = mView.getWidth() - mTable.gutterWidth - mTable.textLeftPadding;


        float rightMargin = mTable.textSize;


        float screenX = cursorX - scrollX;


        if (screenX < 0) {

            scrollX = (int) cursorX;

        } else if (screenX > areaWidth - rightMargin) {

            scrollX = (int) (cursorX - areaWidth + rightMargin);
        }


        return scrollX;
    }


    private int calculateVerticalScroll(float cursorTop, float cursorBottom) {

        int scrollY = mTable.editorScrollY;


        float screenTop = cursorTop - scrollY;


        float screenBottom = cursorBottom - scrollY;


        if (screenTop < 0) {

            scrollY = (int) cursorTop;

        } else if (screenBottom > mView.getHeight()) {

            scrollY = (int) (cursorBottom - mView.getHeight());
        }


        return scrollY;
    }


    public void insertNewLine() {

        replaceSelectionOrInsert("\n");


        finishTextChange();
    }


    public void insert(CharSequence content) {

        if (content == null) {
            return;
        }


        if (content.length() == 0) {
            return;
        }


        replaceSelectionOrInsert(content);


        clearComposingRegion();


        finishTextChange();
    }


    private void replaceSelectionOrInsert(CharSequence content) {

        if (mTable.selector.hasSelection()) {

            replaceSelection(content);

            return;
        }


        int position = mTable.cursorPosition;


        mTable.text.insert(position, content);


        mTable.cursorPosition += content.length();
    }


    private void replaceSelection(CharSequence content) {

        int start = mTable.selector.getMinSelection();


        int end = mTable.selector.getMaxSelection();


        mTable.text.replace(start, end, content);


        mTable.cursorPosition = start + content.length();


        mTable.selector.clearSelection();
    }


    private void clearComposingRegion() {

        mTable.composingStart = -1;

        mTable.composingEnd = -1;
    }


    private void finishTextChange() {

        mView.updateAfterTextChanged();


        scrollToCursor(mTable.cursorPosition);
    }


    public void showKeyboard() {

        Context context = mView.getContext();


        InputMethodManager manager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);


        if (manager == null) {
            return;
        }


        manager.showSoftInput(mView, InputMethodManager.SHOW_IMPLICIT);
    }


    public void rebuildHighlightCache() {

        mTable.tokens.clear();


        int length = mTable.text.length();


        if (length == 0) {
            return;
        }


        int position = 0;


        while (position < length) {

            int nextPosition = findCommentAt(position, length);


            if (nextPosition > position) {

                position = nextPosition;

            } else {

                position++;
            }
        }
    }


    private int findCommentAt(int position, int textLength) {

        for (java type : java.values()) {

            Matcher startMatcher = type.getStartPattern().matcher(mTable.text);


            startMatcher.region(position, textLength);


            if (!startMatcher.lookingAt()) {
                continue;
            }


            int end = findCommentEnd(type, startMatcher.end(), textLength);


            addCommentSpan(position, end);


            return end;
        }


        return position;
    }


    private int findCommentEnd(java type, int start, int textLength) {

        Matcher endMatcher = type.getEndPattern().matcher(mTable.text);


        endMatcher.region(start, textLength);


        if (endMatcher.find()) {

            return endMatcher.end();
        }


        return textLength;
    }


    private void addCommentSpan(int start, int end) {

        Span span = new Span(start, end, EditorConstants.COLOR_COMMENT);


        mTable.tokens.add(span);
    }


    private boolean isTouchInGutter(MotionEvent event) {

        float gutterRight = mTable.gutterWidth - mTable.editorScrollX;


        return event.getX() < gutterRight;
    }


    private int getTouchOffset(MotionEvent event) {

        return mView.findOffsetForPosition(event.getX(), event.getY());
    }


    private void selectWordAt(MotionEvent event) {

        int offset = getTouchOffset(event);


        mTable.selector.selectWord(offset);


        if (mTable.selector.hasSelection()) {

            mTable.cursorPosition = mTable.selector.getSelectionEnd();
        }


        mView.invalidate();
    }


    private class EditorGestureListener extends GestureDetector.SimpleOnGestureListener {


        @Override
        public boolean onDown(MotionEvent event) {

            stopFling();

            return true;
        }


        @Override
        public boolean onSingleTapUp(MotionEvent event) {

            if (mTable.scaling) {
                return true;
            }


            mView.requestFocus();

            mView.showKeyboard();

            onInteraction();


            if (isTouchInGutter(event)) {
                return true;
            }


            int offset = getTouchOffset(event);


            mTable.cursorPosition = offset;


            mTable.selector.clearSelection();


            scrollToCursor(offset);


            mView.invalidate();


            return true;
        }


        @Override
        public boolean onDoubleTap(MotionEvent event) {

            if (mTable.scaling) {
                return true;
            }


            onInteraction();


            if (isTouchInGutter(event)) {
                return true;
            }


            selectWordAt(event);


            return true;
        }


        @Override
        public void onLongPress(MotionEvent event) {

            if (mTable.scaling) {
                return;
            }


            onInteraction();


            if (isTouchInGutter(event)) {
                return;
            }


            mView.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);


            selectWordAt(event);
        }


        @Override
        public boolean onScroll(MotionEvent first, MotionEvent second, float distanceX, float distanceY) {

            if (mTable.scaling) {
                return false;
            }


            scrollByGesture(distanceX, distanceY);


            return true;
        }


        @Override
        public boolean onFling(MotionEvent first, MotionEvent second, float velocityX, float velocityY) {

            if (mTable.scaling) {
                return false;
            }


            startFling(velocityX, velocityY);


            return true;
        }
    }


    private void stopFling() {

        if (mTable.scroller.isFinished()) {
            return;
        }


        mTable.scroller.forceFinished(true);
    }


    private void scrollByGesture(float distanceX, float distanceY) {

        int targetX = mTable.editorScrollX;


        int targetY = mTable.editorScrollY;


        
        if (Math.abs(distanceX) > Math.abs(distanceY)) {

            targetX += (int) distanceX;

        } else {

            targetY += (int) distanceY;
        }


        mView.scrollToClamped(targetX, targetY);


        mTable.lastScrollTime = SystemClock.uptimeMillis();


        mView.awakenScrollBars();
    }


    private void startFling(float velocityX, float velocityY) {

        int maxX = mView.getMaxScrollX();


        int maxY = mView.getMaxScrollY();


        int flingX = 0;

        int flingY = 0;


        
        if (Math.abs(velocityY) > Math.abs(velocityX)) {

            flingY = -(int) velocityY;

        } else {

            flingX = -(int) velocityX;
        }


        mTable.scroller.fling(mTable.editorScrollX, mTable.editorScrollY, flingX, flingY, 0, maxX, 0, maxY, mTable.overFlingDistance, mTable.overFlingDistance);


        mView.postInvalidateOnAnimation();
    }


    private class EditorScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {


        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {

            mTable.scaling = true;


            stopFling();


            return true;
        }


        @Override
        public boolean onScale(ScaleGestureDetector detector) {

            float oldSize = mTable.textSize;


            float newSize = oldSize * detector.getScaleFactor();


            newSize = clamp(newSize, mTable.minTextSize, mTable.maxTextSize);


            if (Math.abs(newSize - oldSize) < 0.01f) {

                return true;
            }


            scaleEditor(detector, oldSize, newSize);


            return true;
        }


        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {

            mTable.scaling = false;


            mView.rebuildLineCache();

            mView.updateGutterWidth();


            mView.scrollToClamped(mTable.editorScrollX, mTable.editorScrollY);


            mView.invalidate();
        }
    }


    private void scaleEditor(ScaleGestureDetector detector, float oldSize, float newSize) {

        float ratio = newSize / oldSize;


        float focusX = detector.getFocusX();


        float focusY = detector.getFocusY();


        int oldScrollX = mTable.editorScrollX;


        int oldScrollY = mTable.editorScrollY;


        updateTextSize(newSize);


        float textFocusX = focusX - mTable.gutterWidth;


        int newScrollX = calculateScaleScrollX(oldScrollX, textFocusX, ratio);


        int newScrollY = (int) ((oldScrollY + focusY) * ratio - focusY);


        mView.scrollToClamped(newScrollX, newScrollY);


        mView.invalidate();
    }


    private void updateTextSize(float textSize) {

        mTable.textSize = textSize;


        mTable.textPaint.setTextSize(textSize);


        mTable.lineNumberPaint.setTextSize(textSize);


        mView.updateFontMetrics();

        mView.updateGutterWidth();
    }


    private int calculateScaleScrollX(int oldScrollX, float focusX, float ratio) {

        if (focusX <= 0) {

            return oldScrollX;
        }


        return (int) ((oldScrollX + focusX) * ratio - focusX);
    }
}