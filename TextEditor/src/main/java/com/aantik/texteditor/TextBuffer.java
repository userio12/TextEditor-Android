package com.aantik.texteditor;

public class TextBuffer implements CharSequence {

    private static final int DEFAULT_GAP_SIZE = 128;

    private char[] mBuffer;

    private int mGapStart;
    private int mGapLength;


    public TextBuffer() {
        this("");
    }


    public TextBuffer(CharSequence text) {

        if (text == null) {
            text = "";
        }

        int textLength = text.length();

        mBuffer = new char[textLength + DEFAULT_GAP_SIZE];

        for (int i = 0; i < textLength; i++) {
            mBuffer[i] = text.charAt(i);
        }

        mGapStart = textLength;
        mGapLength = DEFAULT_GAP_SIZE;
    }


    @Override
    public int length() {

        return mBuffer.length - mGapLength;
    }


    @Override
    public char charAt(int index) {

        checkIndex(index);

        if (index < mGapStart) {
            return mBuffer[index];
        }

        return mBuffer[index + mGapLength];
    }


    @Override
    public CharSequence subSequence(int start, int end) {

        checkRange(start, end);

        int length = end - start;

        char[] result = new char[length];

        for (int i = 0; i < length; i++) {
            result[i] = charAt(start + i);
        }

        return new String(result);
    }


    public void insert(int position, CharSequence text) {

        if (text == null) {
            return;
        }

        int textLength = text.length();

        if (textLength == 0) {
            return;
        }

        checkPosition(position);

        moveGap(position);

        ensureGap(textLength);

        for (int i = 0; i < textLength; i++) {

            mBuffer[mGapStart] = text.charAt(i);

            mGapStart++;
            mGapLength--;
        }
    }


    public void delete(int start, int end) {

        checkRange(start, end);

        if (start == end) {
            return;
        }

        moveGap(start);

        int deleteLength = end - start;

        mGapLength += deleteLength;
    }


    public void replace(int start, int end, CharSequence text) {

        checkRange(start, end);

        delete(start, end);

        insert(start, text);
    }


    private void moveGap(int position) {

        if (position == mGapStart) {
            return;
        }

        if (position < mGapStart) {

            int moveLength = mGapStart - position;

            System.arraycopy(mBuffer, position, mBuffer, position + mGapLength, moveLength);

            mGapStart = position;

            return;
        }

        int moveLength = position - mGapStart;

        System.arraycopy(mBuffer, mGapStart + mGapLength, mBuffer, mGapStart, moveLength);

        mGapStart = position;
    }


    private void ensureGap(int requiredSize) {

        if (mGapLength >= requiredSize) {
            return;
        }

        int oldBufferLength = mBuffer.length;

        int growSize = oldBufferLength / 2;

        if (growSize < DEFAULT_GAP_SIZE) {
            growSize = DEFAULT_GAP_SIZE;
        }

        if (growSize < requiredSize) {
            growSize = requiredSize;
        }


        int newBufferLength = oldBufferLength + growSize;

        char[] newBuffer = new char[newBufferLength];


        System.arraycopy(mBuffer, 0, newBuffer, 0, mGapStart);


        int oldTailStart = mGapStart + mGapLength;

        int tailLength = oldBufferLength - oldTailStart;


        int textLength = length();

        int newGapLength = newBufferLength - textLength;

        int newTailStart = mGapStart + newGapLength;

        System.arraycopy(mBuffer, oldTailStart, newBuffer, newTailStart, tailLength);


        mBuffer = newBuffer;

        mGapLength = newGapLength;
    }


    private void checkPosition(int position) {

        int textLength = length();

        if (position < 0 || position > textLength) {

            throw new IndexOutOfBoundsException("position=" + position + ", length=" + textLength);
        }
    }


    private void checkIndex(int index) {

        int textLength = length();

        if (index < 0 || index >= textLength) {

            throw new IndexOutOfBoundsException("index=" + index + ", length=" + textLength);
        }
    }


    private void checkRange(int start, int end) {

        int textLength = length();

        if (start < 0 || end < start || end > textLength) {

            throw new IndexOutOfBoundsException("start=" + start + ", end=" + end + ", length=" + textLength);
        }
    }


    @Override
    public String toString() {

        int textLength = length();

        char[] result = new char[textLength];


        System.arraycopy(mBuffer, 0, result, 0, mGapStart);


        int tailLength = textLength - mGapStart;

        System.arraycopy(mBuffer, mGapStart + mGapLength, result, mGapStart, tailLength);


        return new String(result);
    }
}