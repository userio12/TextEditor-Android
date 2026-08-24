package com.aantik.texteditor.layout;

import android.graphics.Paint;

import com.aantik.texteditor.EditView;
import com.aantik.texteditor.VTable;

import java.util.List;


public class TextLayout {

    private final EditView mView;
    private final VTable mTable;


    public TextLayout(EditView view, VTable table) {

        mView = view;
        mTable = table;
    }


    
    private static <T> T get(List<T> list, int index) {

        return list.get(index);
    }


    
    private static <T> boolean same(List<T> list, int first, int second) {


        T firstValue = get(list, first);


        T secondValue = get(list, second);


        if (firstValue == null) {

            return secondValue == null;
        }


        return firstValue.equals(secondValue);
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


    public void updateFontMetrics() {

        Paint.FontMetrics metrics = mTable.textPaint.getFontMetrics();


        mTable.lineHeight = metrics.descent - metrics.ascent;


        mTable.baseLine = -metrics.ascent;


        initAsciiCache();


        updateWrapStartWidth();

        updateWrapEndWidth();
    }


    private void updateWrapStartWidth() {

        if (mTable.wrapStartIcon == null) {

            mTable.wrapStartWidth = 0;

            return;
        }


        int width = mTable.wrapStartIcon.getIntrinsicWidth();


        int height = mTable.wrapStartIcon.getIntrinsicHeight();


        if (height <= 0) {

            mTable.wrapStartWidth = 0;

            return;
        }


        float scale = (mTable.lineHeight * 0.7f) / height;


        float spaceWidth = mTable.textPaint.measureText(" ");


        mTable.wrapStartWidth = width * scale + spaceWidth;
    }


    private void updateWrapEndWidth() {

        if (mTable.wrapEndIcon == null) {

            mTable.wrapEndWidth = 0;

            return;
        }


        int width = mTable.wrapEndIcon.getIntrinsicWidth();


        int height = mTable.wrapEndIcon.getIntrinsicHeight();


        if (height <= 0) {

            mTable.wrapEndWidth = 0;

            return;
        }


        float scale = (mTable.lineHeight * 0.7f) / height;


        float spaceWidth = mTable.textPaint.measureText(" ");


        mTable.wrapEndWidth = width * scale + spaceWidth;
    }


    public void updateGutterWidth() {

        int lineCount = getLineCount();


        if (lineCount < 1) {

            lineCount = 1;
        }


        String number = String.valueOf(lineCount);


        float numberWidth = mTable.lineNumberPaint.measureText(number);


        mTable.gutterWidth = numberWidth + mTable.gutterPadding * 2.0f;
    }


    public void rebuildLineCache() {

        mTable.lineStarts.clear();

        mTable.logicalLineIndices.clear();


        int length = mTable.text.length();


        if (length == 0) {

            addLine(0, 1);


            mTable.maxLineWidth = 0;

            return;
        }


        if (mTable.wordWrap) {

            rebuildWrappedLineCache();

        } else {

            rebuildNormalLineCache();
        }
    }


    private void rebuildNormalLineCache() {

        int length = mTable.text.length();


        int start = 0;

        int logicalLine = 1;


        mTable.maxLineWidth = 0;


        while (start < length) {

            int newline = findNextNewline(start);


            int end;


            if (newline == -1) {

                end = length;

            } else {

                end = newline;
            }


            addLine(start, logicalLine);


            updateMaxLineWidth(start, end);


            if (newline == -1) {

                start = length;

            } else {

                start = newline + 1;


                logicalLine++;
            }
        }


        checkEmptyLastLine(logicalLine);
    }


    private void rebuildWrappedLineCache() {

        int length = mTable.text.length();


        float maxWidth = getAvailableTextWidth();


        int start = 0;

        int logicalLine = 1;


        mTable.maxLineWidth = maxWidth;


        while (start < length) {

            int newline = findNextNewline(start);


            int end;


            if (newline == -1) {

                end = length;

            } else {

                end = newline;
            }


            if (maxWidth > 0) {

                wrapSegment(start, end, logicalLine, maxWidth);

            } else {

                addLine(start, logicalLine);
            }


            if (newline == -1) {

                start = length;

            } else {

                start = newline + 1;


                logicalLine++;
            }
        }


        checkEmptyLastLine(logicalLine);
    }


    private void addLine(int start, int logicalLine) {


        mTable.lineStarts.add(start);


        mTable.logicalLineIndices.add(logicalLine);
    }


    private void updateMaxLineWidth(int start, int end) {


        float width = mTable.textPaint.measureText(mTable.text, start, end);


        if (width > mTable.maxLineWidth) {

            mTable.maxLineWidth = width;
        }
    }


    private float getAvailableTextWidth() {

        return mView.getWidth() - mTable.gutterWidth - mTable.textLeftPadding;
    }


    private int findNextNewline(int start) {


        int length = mTable.text.length();


        for (int i = start; i < length; i++) {


            if (mTable.text.charAt(i) == '\n') {

                return i;
            }
        }


        return -1;
    }


    private void wrapSegment(int start, int end, int logicalLine, float maxWidth) {


        int current = start;


        int lineStart = start;


        int lastBreak = -1;


        float currentWidth = 0;


        while (current < end) {

            char character = mTable.text.charAt(current);


            float characterWidth = getCharacterWidth(character);




            float lineWidth = getWrapLineWidth(start, lineStart, maxWidth);


            if (currentWidth + characterWidth > lineWidth) {


                if (lastBreak >= lineStart) {

                    addLine(lineStart, logicalLine);


                    int breakPosition = lastBreak + 1;


                    lineStart = breakPosition;


                    current = breakPosition;


                    currentWidth = 0;

                    lastBreak = -1;


                    continue;
                }


                if (current > lineStart) {

                    addLine(lineStart, logicalLine);


                    lineStart = current;


                    currentWidth = characterWidth;


                    lastBreak = -1;


                    current++;


                    continue;
                }


                currentWidth += characterWidth;


                current++;


                continue;
            }


            currentWidth += characterWidth;


            current++;
        }


        addLine(lineStart, logicalLine);
    }


    private float getCharacterWidth(char character) {


        if (character < 128) {

            return mTable.asciiWidths[character];
        }


        return mTable.textPaint.measureText(String.valueOf(character));
    }


    private float getWrapLineWidth(int segmentStart, int lineStart, float maxWidth) {


        float width = maxWidth;


        if (lineStart > segmentStart) {

            width -= mTable.wrapStartWidth;
        }


        width -= mTable.wrapEndWidth;


        return width;
    }


    private void checkEmptyLastLine(int logicalLine) {


        int length = mTable.text.length();


        if (length <= 0) {
            return;
        }


        if (mTable.text.charAt(length - 1) != '\n') {

            return;
        }


        addLine(length, logicalLine);
    }


    public int findOffsetForPosition(float touchX, float touchY) {


        float textLeft = getVisualTextAreaLeft();


        if (touchX < textLeft) {

            touchX = textLeft;
        }


        float localX = touchX - textLeft;


        float localY = touchY + mView.computeVerticalScrollOffset();


        int line = getLineForY(localY);


        if (isContinuationLine(line)) {


            localX -= mTable.wrapStartWidth;
        }


        return findOffsetInLine(line, localX);
    }


    private float getVisualTextAreaLeft() {

        return mTable.gutterWidth + mTable.textLeftPadding - mView.computeHorizontalScrollOffset();
    }


    private boolean isContinuationLine(int line) {


        if (!mTable.wordWrap) {
            return false;
        }


        if (line <= 0) {
            return false;
        }


        return same(mTable.logicalLineIndices, line - 1, line);
    }


    private int findOffsetInLine(int line, float x) {


        int start = getLineStartOffset(line);


        int end = getLineEndOffset(line);


        if (x <= 0) {

            return start;
        }


        int count = end - start;


        if (count <= 0) {

            return start;
        }


        float[] widths = new float[count];


        mTable.textPaint.getTextWidths(mTable.text, start, end, widths);


        float currentX = 0;


        for (int i = 0; i < count; i++) {


            float width = widths[i];


            float center = currentX + width / 2.0f;


            if (x < center) {

                return start + i;
            }


            currentX += width;
        }


        return end;
    }


    public int getLineForY(float contentY) {


        Paint.FontMetrics metrics = mTable.textPaint.getFontMetrics();


        float top = mTable.baseLine + metrics.ascent;


        int line = (int) ((contentY - top) / mTable.lineHeight);


        return clamp(line, 0, getLineCount() - 1);
    }


    public int getLineCount() {

        return mTable.lineStarts.size();
    }


    public int getLineEndOffset(int line) {


        if (line < 0) {

            return 0;
        }


        int count = mTable.lineStarts.size();


        if (line + 1 < count) {

            return get(mTable.lineStarts, line + 1) - 1;
        }


        return mTable.text.length();
    }


    public int getLineStartOffset(int line) {


        if (line < 0) {

            return 0;
        }


        int count = mTable.lineStarts.size();


        if (line >= count) {

            return mTable.text.length();
        }


        return get(mTable.lineStarts, line);
    }


    public int getLineForPosition(int position) {


        int length = mTable.text.length();


        position = clamp(position, 0, length);


        int low = 0;


        int high = mTable.lineStarts.size() - 1;


        while (low <= high) {

            int middle = (low + high) >>> 1;


            int start = get(mTable.lineStarts, middle);


            if (start == position) {

                return middle;
            }


            if (start < position) {

                low = middle + 1;

            } else {

                high = middle - 1;
            }
        }


        return Math.max(0, low - 1);
    }


    public float getBaseLineForPosition(int position) {


        int line = getLineForPosition(position);


        return mTable.baseLine + line * mTable.lineHeight;
    }


    public float getXForPosition(int position) {


        int length = mTable.text.length();


        position = clamp(position, 0, length);


        int line = getLineForPosition(position);


        int start = getLineStartOffset(line);


        float x = mTable.textPaint.measureText(mTable.text, start, position);


        if (isContinuationLine(line)) {


            x += mTable.wrapStartWidth;
        }


        return x;
    }


    public float getContentHeight() {

        return getLineCount() * mTable.lineHeight + mTable.textSize * 2.0f;
    }


    public float getContentWidth() {

        if (mTable.wordWrap) {

            return mView.getWidth();
        }


        return mTable.gutterWidth + mTable.textLeftPadding + mTable.maxLineWidth + mTable.textSize * 2.0f;
    }


    public void initAsciiCache() {

        for (int i = 0; i < 128; i++) {


            char character = (char) i;


            mTable.asciiWidths[i] = mTable.textPaint.measureText(String.valueOf(character));
        }
    }
}