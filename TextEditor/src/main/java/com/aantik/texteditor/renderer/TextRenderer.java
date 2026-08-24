package com.aantik.texteditor.renderer;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.SystemClock;

import com.aantik.texteditor.EditView;
import com.aantik.texteditor.VTable;
import com.aantik.texteditor.config.EditorConstants;
import com.aantik.texteditor.model.Span;

public class TextRenderer {

    private final EditView mView;
    private final VTable mTable;


    public TextRenderer(EditView view, VTable table) {

        mView = view;
        mTable = table;
    }


    public void draw(Canvas canvas) {

        boolean drawCursor = isCursorVisible();
        boolean drawHandle = isHandleVisible();

        float handleAlpha = getHandleAlpha();

        if (handleAlpha <= 0.0f) {
            drawHandle = false;
        }


        int saveCount = canvas.save();

        canvas.translate(-mTable.editorScrollX, 0);

        drawGutter(canvas);


        canvas.save();

        canvas.translate(mTable.gutterWidth + mTable.textLeftPadding, -mTable.editorScrollY);


        drawCurrentLine(canvas);

        mTable.selector.drawSelectionBackground(canvas);

        drawText(canvas);

        mTable.selector.drawHandles(canvas, drawHandle, handleAlpha);


        if (!mTable.selector.hasSelection() && drawCursor) {

            drawCursor(canvas);
        }


        canvas.restoreToCount(saveCount);
    }


    private boolean isCursorVisible() {

        if (mTable.forceCursorVisible) {
            return true;
        }

        if (!mView.isFocused()) {
            return false;
        }

        if (mTable.blinkStartTime == 0) {
            return false;
        }


        long time = SystemClock.uptimeMillis() - mTable.blinkStartTime;


        return (time % 1000) < 500;
    }


    private boolean isHandleVisible() {

        if (mTable.forceCursorVisible || mTable.forceHandleVisible) {

            return true;
        }


        if (!mView.isFocused()) {
            return false;
        }


        long time = SystemClock.uptimeMillis() - mTable.lastInteractionTime;


        return time < EditorConstants.INTERACTION_TIMEOUT;
    }


    private float getHandleAlpha() {

        if (mTable.wordWrap) {
            return 1.0f;
        }


        long time = SystemClock.uptimeMillis() - mTable.lastScrollTime;


        if (time <= EditorConstants.FADE_DELAY) {
            return 1.0f;
        }


        long fadeTime = time - EditorConstants.FADE_DELAY;


        if (fadeTime >= EditorConstants.FADE_DURATION) {
            return 0.0f;
        }


        return 1.0f - (fadeTime / (float) EditorConstants.FADE_DURATION);
    }


    private void drawCurrentLine(Canvas canvas) {

        if (!mView.isFocused()) {
            return;
        }


        int textLength = mTable.text.length();

        int position = mTable.cursorPosition;


        if (position < 0) {
            position = 0;
        }

        if (position > textLength) {
            position = textLength;
        }


        int cursorLine = mView.getLineForPosition(position);

        int logicalLine = mTable.logicalLineIndices.get(cursorLine);


        int firstLine = getFirstVisibleLine();

        int lastLine = getLastVisibleLine();


        for (int line = firstLine; line <= lastLine; line++) {

            int currentLogicalLine = mTable.logicalLineIndices.get(line);


            if (currentLogicalLine != logicalLine) {
                continue;
            }


            float top = line * mTable.lineHeight;

            float bottom = top + mTable.lineHeight;


            canvas.drawRect(-mTable.textLeftPadding, top, mView.getWidth() + mTable.editorScrollX, bottom, mTable.currentLinePaint);
        }
    }


    private void drawText(Canvas canvas) {

        int firstLine = getFirstVisibleLine();

        int lastLine = getLastVisibleLine();


        for (int line = firstLine; line <= lastLine; line++) {

            drawLine(canvas, line);
        }
    }


    private void drawLine(Canvas canvas, int line) {

        int start = mView.getLineStartOffset(line);

        int end = mView.getLineEndOffset(line);


        float lineTop = line * mTable.lineHeight;

        float baseline = mTable.baseLine + lineTop;


        float textX = 0.0f;


        if (mTable.wordWrap) {

            textX = drawWrapIndicators(canvas, line, start, end, lineTop);
        }


        drawTextWithSpans(canvas, start, end, textX, baseline);
    }


    private float drawWrapIndicators(Canvas canvas, int line, int start, int end, float lineTop) {


        float textX = 0.0f;

        int lineCount = mView.getLineCount();


        int logicalLine = mTable.logicalLineIndices.get(line);


        boolean continuation = false;

        boolean wrapped = false;


        if (line > 0) {

            int previousLogicalLine = mTable.logicalLineIndices.get(line - 1);

            continuation = previousLogicalLine == logicalLine;
        }


        if (line < lineCount - 1) {

            int nextLogicalLine = mTable.logicalLineIndices.get(line + 1);

            wrapped = nextLogicalLine == logicalLine;
        }


        if (continuation && mTable.wrapStartIcon != null) {

            drawWrapStartIcon(canvas, lineTop);

            textX = mTable.wrapStartWidth;
        }


        if (wrapped && mTable.wrapEndIcon != null) {

            drawWrapEndIcon(canvas, start, end, textX, lineTop);
        }


        return textX;
    }


    private void drawWrapStartIcon(Canvas canvas, float lineTop) {


        int width = mTable.wrapStartIcon.getIntrinsicWidth();

        int height = mTable.wrapStartIcon.getIntrinsicHeight();


        if (height <= 0) {
            return;
        }


        float scale = (mTable.lineHeight * 0.7f) / height;


        float drawWidth = width * scale;

        float drawHeight = height * scale;


        float top = lineTop + (mTable.lineHeight - drawHeight) / 2.0f;


        mTable.wrapStartIcon.setBounds(0, (int) top, (int) drawWidth, (int) (top + drawHeight));


        mTable.wrapStartIcon.draw(canvas);
    }


    private void drawWrapEndIcon(Canvas canvas, int start, int end, float textX, float lineTop) {


        int width = mTable.wrapEndIcon.getIntrinsicWidth();

        int height = mTable.wrapEndIcon.getIntrinsicHeight();


        if (height <= 0) {
            return;
        }


        float scale = (mTable.lineHeight * 0.7f) / height;


        float drawWidth = width * scale;

        float drawHeight = height * scale;


        float top = lineTop + (mTable.lineHeight - drawHeight) / 2.0f;


        float textWidth = mTable.textPaint.measureText(mTable.text, start, end);


        float left = textX + textWidth + (mTable.wrapEndWidth - drawWidth);


        float right = textX + textWidth + mTable.wrapEndWidth;


        mTable.wrapEndIcon.setBounds((int) left, (int) top, (int) right, (int) (top + drawHeight));


        mTable.wrapEndIcon.draw(canvas);
    }


    private void drawTextWithSpans(Canvas canvas, int start, int end, float x, float y) {


        int current = start;


        while (current < end) {

            Span span = findNextSpan(current, end);


            if (span == null) {

                drawTextSegment(canvas, current, end, x, y, EditorConstants.COLOR_TEXT);

                return;
            }


            if (span.start > current) {

                int segmentEnd = Math.min(span.start, end);


                drawTextSegment(canvas, current, segmentEnd, x, y, EditorConstants.COLOR_TEXT);


                x += measureText(current, segmentEnd);


                current = segmentEnd;

                continue;
            }


            int segmentEnd = Math.min(span.end, end);


            drawTextSegment(canvas, current, segmentEnd, x, y, span.color);


            x += measureText(current, segmentEnd);


            current = segmentEnd;
        }
    }


    private Span findNextSpan(int position, int lineEnd) {


        Span result = null;


        for (Span span : mTable.tokens) {

            if (span.end <= position) {
                continue;
            }

            if (span.start >= lineEnd) {
                continue;
            }


            if (result == null || span.start < result.start) {

                result = span;
            }
        }


        return result;
    }


    private void drawTextSegment(Canvas canvas, int start, int end, float x, float y, int color) {


        mTable.textPaint.setColor(color);


        canvas.drawText(mTable.text, start, end, x, y, mTable.textPaint);
    }


    private float measureText(int start, int end) {


        return mTable.textPaint.measureText(mTable.text, start, end);
    }


    private void drawCursor(Canvas canvas) {

        int textLength = mTable.text.length();

        int position = mTable.cursorPosition;


        if (position < 0) {
            position = 0;
        }

        if (position > textLength) {
            position = textLength;
        }


        int line = mView.getLineForPosition(position);


        float x = mView.getXForPosition(position);


        float baseline = mTable.baseLine + line * mTable.lineHeight;


        Paint.FontMetrics metrics = mTable.textPaint.getFontMetrics();


        float top = baseline + metrics.ascent;

        float bottom = baseline + metrics.descent;


        canvas.drawLine(x, top, x, bottom, mTable.cursorPaint);
    }


    private void drawGutter(Canvas canvas) {

        canvas.drawRect(0, 0, mTable.gutterWidth, mView.getHeight(), mTable.gutterPaint);


        int firstLine = getFirstVisibleLine();

        int lastLine = getLastVisibleLine();


        float numberX = mTable.gutterWidth - mTable.gutterPadding;


        for (int line = firstLine; line <= lastLine; line++) {


            int logicalLine = mTable.logicalLineIndices.get(line);


            if (!isFirstVisualLine(line, logicalLine)) {

                continue;
            }


            float baseline = mTable.baseLine + line * mTable.lineHeight - mTable.editorScrollY;


            canvas.drawText(String.valueOf(logicalLine), numberX, baseline, mTable.lineNumberPaint);
        }


        canvas.drawLine(mTable.gutterWidth, 0, mTable.gutterWidth, mView.getHeight(), mTable.dividerPaint);
    }


    private boolean isFirstVisualLine(int line, int logicalLine) {


        if (line == 0) {
            return true;
        }


        int previousLogicalLine = mTable.logicalLineIndices.get(line - 1);


        return previousLogicalLine != logicalLine;
    }


    private int getFirstVisibleLine() {

        int line = (int) (mTable.editorScrollY / mTable.lineHeight) - 1;


        if (line < 0) {
            line = 0;
        }


        return line;
    }


    private int getLastVisibleLine() {

        int lineCount = mView.getLineCount();


        int line = (int) ((mTable.editorScrollY + mView.getHeight()) / mTable.lineHeight) + 1;


        if (line >= lineCount) {
            line = lineCount - 1;
        }


        return line;
    }
}