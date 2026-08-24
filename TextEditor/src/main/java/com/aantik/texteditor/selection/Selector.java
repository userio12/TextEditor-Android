package com.aantik.texteditor.selection;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;

import com.aantik.texteditor.EditView;
import com.aantik.texteditor.TextBuffer;
import com.aantik.texteditor.config.EditorConstants;
import androidx.appcompat.content.res.AppCompatResources;
import java.text.BreakIterator;
import com.aantik.texteditor.R;

public class Selector {

    private final EditView mEditView;

    private final Paint mSelectionPaint;

    private final BreakIterator mWordIterator;


    private Drawable mLeftHandle;

    private Drawable mMiddleHandle;

    private Drawable mRightHandle;


    private final RectF mLeftHandleRect = new RectF();

    private final RectF mMiddleHandleRect = new RectF();

    private final RectF mRightHandleRect = new RectF();


    private int mSelectionStart = -1;

    private int mSelectionEnd = -1;


    private boolean mCursorDragging;

    private boolean mSelectionDragging;

    private boolean mDraggingLeft;

    private boolean mHandleGesture;


    private float mTouchOffsetX;

    private float mTouchOffsetY;


    public Selector(EditView editView) {

        mEditView = editView;


        mSelectionPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        mSelectionPaint.setColor(EditorConstants.COLOR_SELECTION);


        mWordIterator = BreakIterator.getWordInstance();


        Handles();
    }


    private void Handles() {

        mLeftHandle = AppCompatResources.getDrawable(
                mEditView.getContext(),
                androidx.appcompat.R.drawable.abc_text_select_handle_left_mtrl
        );

        mMiddleHandle = AppCompatResources.getDrawable(
                mEditView.getContext(),
                androidx.appcompat.R.drawable.abc_text_select_handle_middle_mtrl
        );

        mRightHandle = AppCompatResources.getDrawable(
                mEditView.getContext(),
                androidx.appcompat.R.drawable.abc_text_select_handle_right_mtrl
        );

        int color = EditorConstants.COLOR_HANDLE;


        if (mLeftHandle != null) {

            mLeftHandle = mLeftHandle.mutate();

            mLeftHandle.setColorFilter(color, PorterDuff.Mode.SRC_IN);

            mLeftHandle.setAlpha(210);
        }


        if (mMiddleHandle != null) {

            mMiddleHandle = mMiddleHandle.mutate();

            mMiddleHandle.setColorFilter(color, PorterDuff.Mode.SRC_IN);
        }


        if (mRightHandle != null) {

            mRightHandle = mRightHandle.mutate();

            mRightHandle.setColorFilter(color, PorterDuff.Mode.SRC_IN);

            mRightHandle.setAlpha(210);
        }
    }


    public boolean hasSelection() {

        if (mSelectionStart < 0) {
            return false;
        }

        if (mSelectionEnd < 0) {
            return false;
        }


        return mSelectionStart != mSelectionEnd;
    }


    public void clearSelection() {

        mSelectionStart = -1;

        mSelectionEnd = -1;


        mLeftHandleRect.setEmpty();

        mRightHandleRect.setEmpty();


        mEditView.invalidate();
    }


    public void setSelection(int start, int end) {


        int length = mEditView.getTextBuffer().length();


        if (start < 0) {
            start = 0;
        }

        if (start > length) {
            start = length;
        }


        if (end < 0) {
            end = 0;
        }

        if (end > length) {
            end = length;
        }


        mSelectionStart = start;

        mSelectionEnd = end;


        mEditView.invalidate();
    }


    public int getSelectionStart() {

        return mSelectionStart;
    }


    public int getSelectionEnd() {

        return mSelectionEnd;
    }


    public int getMinSelection() {

        return Math.min(mSelectionStart, mSelectionEnd);
    }


    public int getMaxSelection() {

        return Math.max(mSelectionStart, mSelectionEnd);
    }


    public void selectWord(int offset) {

        TextBuffer text = mEditView.getTextBuffer();


        if (text == null) {

            clearSelection();

            return;
        }


        int length = text.length();


        if (length == 0) {

            clearSelection();

            return;
        }


        if (offset < 0) {
            offset = 0;
        }

        if (offset > length) {
            offset = length;
        }


        
        if (offset == length) {

            offset--;


            if (offset < 0) {

                clearSelection();

                return;
            }
        }


        
        if (offset < length && text.charAt(offset) == '\n' && offset > 0) {

            offset--;
        }


        mWordIterator.setText(text.toString());


        int start = mWordIterator.preceding(offset + 1);


        int end = mWordIterator.following(offset);


        if (start == BreakIterator.DONE) {
            start = 0;
        }


        if (end == BreakIterator.DONE) {
            end = length;
        }


        if (start >= end) {

            clearSelection();

            return;
        }


        setSelection(start, end);
    }


    public void drawSelectionBackground(Canvas canvas) {

        drawSelection(canvas);
    }


    public void drawHandles(Canvas canvas, boolean showHandles, float alpha) {


        if (!showHandles) {
            return;
        }


        int handleAlpha = (int) (alpha * 255.0f);


        if (mLeftHandle != null) {

            mLeftHandle.setAlpha(handleAlpha);
        }


        if (mMiddleHandle != null) {

            mMiddleHandle.setAlpha(handleAlpha);
        }


        if (mRightHandle != null) {

            mRightHandle.setAlpha(handleAlpha);
        }


        drawHandles(canvas);
    }


    private void drawSelection(Canvas canvas) {


        if (!hasSelection()) {
            return;
        }


        int start = getMinSelection();

        int end = getMaxSelection();


        int startLine = mEditView.getLineForPosition(start);


        int endLine = mEditView.getLineForPosition(end);


        Paint.FontMetrics metrics = mEditView.getTextPaint().getFontMetrics();


        
        if (startLine == endLine) {

            drawSingleLineSelection(canvas, start, end, metrics);

            return;
        }


        drawMultiLineSelection(canvas, start, end, startLine, endLine, metrics);
    }


    private void drawSingleLineSelection(Canvas canvas, int start, int end, Paint.FontMetrics metrics) {


        float left = mEditView.getXForPosition(start);


        float right = mEditView.getXForPosition(end);


        float baseline = mEditView.getBaseLineForPosition(start);


        float top = baseline + metrics.ascent;


        float bottom = baseline + metrics.descent;


        canvas.drawRect(left, top, right, bottom, mSelectionPaint);
    }


    private void drawMultiLineSelection(Canvas canvas, int start, int end, int startLine, int endLine, Paint.FontMetrics metrics) {


        float spaceWidth = mEditView.getTextPaint().measureText(" ");


        for (int line = startLine; line <= endLine; line++) {


            float left = getSelectionLeft(line, startLine, start);


            float right = getSelectionRight(line, endLine, end, spaceWidth);


            int lineStart = mEditView.getLineStartOffset(line);


            float baseline = mEditView.getBaseLineForPosition(lineStart);


            float top = baseline + metrics.ascent;


            float bottom = baseline + metrics.descent;


            canvas.drawRect(left, top, right, bottom, mSelectionPaint);
        }
    }


    private float getSelectionLeft(int line, int startLine, int start) {


        if (line == startLine) {

            return mEditView.getXForPosition(start);
        }


        int lineStart = mEditView.getLineStartOffset(line);


        return mEditView.getXForPosition(lineStart);
    }


    private float getSelectionRight(int line, int endLine, int end, float spaceWidth) {


        if (line == endLine) {

            return mEditView.getXForPosition(end);
        }


        int lineEnd = mEditView.getLineEndOffset(line);


        return mEditView.getXForPosition(lineEnd) + spaceWidth;
    }


    private void drawHandles(Canvas canvas) {


        Paint.FontMetrics metrics = mEditView.getTextPaint().getFontMetrics();


        if (hasSelection()) {

            drawSelectionHandles(canvas, metrics);

        } else {

            drawCursorHandle(canvas, metrics);
        }
    }


    private void drawSelectionHandles(Canvas canvas, Paint.FontMetrics metrics) {


        int start = getMinSelection();

        int end = getMaxSelection();


        float startX = mEditView.getXForPosition(start);


        float endX = mEditView.getXForPosition(end);


        float startBaseline = mEditView.getBaseLineForPosition(start);


        float endBaseline = mEditView.getBaseLineForPosition(end);


        float startBottom = startBaseline + metrics.descent;


        float endBottom = endBaseline + metrics.descent;


        drawLeftHandle(canvas, startX, startBottom);


        drawRightHandle(canvas, endX, endBottom);


        mMiddleHandleRect.setEmpty();
    }


    private void drawCursorHandle(Canvas canvas, Paint.FontMetrics metrics) {


        int cursor = mEditView.getCursorPosition();


        float cursorX = mEditView.getXForPosition(cursor);


        float baseline = mEditView.getBaseLineForPosition(cursor);


        float bottom = baseline + metrics.descent;


        drawMiddleHandle(canvas, cursorX, bottom);


        mLeftHandleRect.setEmpty();

        mRightHandleRect.setEmpty();
    }


    private void drawLeftHandle(Canvas canvas, float x, float y) {


        if (mLeftHandle == null) {
            return;
        }


        int width = mLeftHandle.getIntrinsicWidth();


        int height = mLeftHandle.getIntrinsicHeight();


        float density = mEditView.getResources().getDisplayMetrics().density;


        float left = x - width * 0.75f - density;


        float right = x + width * 0.25f - density;


        mLeftHandle.setBounds((int) left, (int) y, (int) right, (int) (y + height));


        mLeftHandleRect.set(left, y, right, y + height);


        mLeftHandle.draw(canvas);
    }


    private void drawRightHandle(Canvas canvas, float x, float y) {


        if (mRightHandle == null) {
            return;
        }


        int width = mRightHandle.getIntrinsicWidth();


        int height = mRightHandle.getIntrinsicHeight();


        float density = mEditView.getResources().getDisplayMetrics().density;


        float left = x - width * 0.25f + density;


        float right = x + width * 0.75f + density;


        mRightHandle.setBounds((int) left, (int) y, (int) right, (int) (y + height));


        mRightHandleRect.set(left, y, right, y + height);


        mRightHandle.draw(canvas);
    }


    private void drawMiddleHandle(Canvas canvas, float x, float y) {


        if (mMiddleHandle == null) {
            return;
        }


        int width = mMiddleHandle.getIntrinsicWidth();


        int height = mMiddleHandle.getIntrinsicHeight();


        float left = x - width / 2.0f;


        float right = x + width / 2.0f;


        mMiddleHandle.setBounds((int) left, (int) y, (int) right, (int) (y + height));


        mMiddleHandleRect.set(left, y, right, y + height);


        mMiddleHandle.draw(canvas);
    }


    public boolean onTouchEvent(MotionEvent event) {


        float screenX = event.getX();

        float screenY = event.getY();


        float scrollX = mEditView.computeHorizontalScrollOffset();


        float scrollY = mEditView.computeVerticalScrollOffset();


        float gutterWidth = mEditView.getGutterWidth();


        float textPadding = mEditView.getTextLeftPadding();


        float localX = screenX - (gutterWidth + textPadding - scrollX);


        float localY = screenY + scrollY;


        switch (event.getActionMasked()) {

            case MotionEvent.ACTION_DOWN:

                return onTouchDown(screenX, localX, localY, scrollX, gutterWidth);


            case MotionEvent.ACTION_MOVE:

                return onTouchMove(screenX, screenY);


            case MotionEvent.ACTION_UP:

            case MotionEvent.ACTION_CANCEL:

                return onTouchEnd();
        }


        return false;
    }


    private boolean onTouchDown(float screenX, float localX, float localY, float scrollX, float gutterWidth) {


        if (mMiddleHandleRect.contains(localX, localY)) {


            startCursorDrag(localX);

            return true;
        }


        if (mLeftHandleRect.contains(localX, localY)) {


            startLeftSelectionDrag(localX);

            return true;
        }


        if (mRightHandleRect.contains(localX, localY)) {


            startRightSelectionDrag(localX);

            return true;
        }


        
        if (screenX < gutterWidth - scrollX) {


            mHandleGesture = false;

            return false;
        }


        mHandleGesture = false;


        return false;
    }


    private void startCursorDrag(float localX) {


        mCursorDragging = true;

        mSelectionDragging = false;

        mHandleGesture = true;


        mTouchOffsetX = mMiddleHandleRect.centerX() - localX;


        mTouchOffsetY = -mMiddleHandleRect.height();
    }


    private void startLeftSelectionDrag(float localX) {


        mCursorDragging = false;

        mSelectionDragging = true;

        mDraggingLeft = true;

        mHandleGesture = true;


        mTouchOffsetX = mLeftHandleRect.left + mLeftHandleRect.width() * 0.75f - localX;


        mTouchOffsetY = -mLeftHandleRect.height();
    }


    private void startRightSelectionDrag(float localX) {


        mCursorDragging = false;

        mSelectionDragging = true;

        mDraggingLeft = false;

        mHandleGesture = true;


        mTouchOffsetX = mRightHandleRect.left + mRightHandleRect.width() * 0.25f - localX;


        mTouchOffsetY = -mRightHandleRect.height();
    }


    private boolean onTouchMove(float screenX, float screenY) {


        if (!mHandleGesture) {
            return false;
        }


        float dragX = screenX + mTouchOffsetX;


        float dragY = screenY + mTouchOffsetY;


        if (mCursorDragging) {

            moveCursorHandle(dragX, dragY);

            return true;
        }


        if (mSelectionDragging) {

            moveSelectionHandle(dragX, dragY);

            return true;
        }


        return true;
    }


    private void moveCursorHandle(float x, float y) {


        int offset = mEditView.findOffsetForPosition(x, y);


        mEditView.setCursorPosition(offset);
    }


    private void moveSelectionHandle(float x, float y) {


        int offset = mEditView.findOffsetForPosition(x, y);


        int start = getMinSelection();


        int end = getMaxSelection();


        if (mDraggingLeft) {

            moveLeftSelectionHandle(offset, end);

        } else {

            moveRightSelectionHandle(offset, start);
        }
    }


    private void moveLeftSelectionHandle(int offset, int end) {


        if (offset <= end) {

            setSelection(offset, end);

            return;
        }

        setSelection(end, offset);


        mDraggingLeft = false;
    }


    private void moveRightSelectionHandle(int offset, int start) {


        if (offset >= start) {

            setSelection(start, offset);

            return;
        }

        setSelection(offset, start);


        mDraggingLeft = true;
    }


    private boolean onTouchEnd() {

        if (!mHandleGesture) {
            return false;
        }


        mCursorDragging = false;

        mSelectionDragging = false;

        mHandleGesture = false;


        mEditView.invalidate();


        return true;
    }
}