package com.aantik.texteditor.controller;

import android.view.KeyEvent;
import android.view.inputmethod.BaseInputConnection;

import com.aantik.texteditor.EditView;
import com.aantik.texteditor.VTable;

public class mEditorInputIDL extends BaseInputConnection {

    private final EditView mView;
    private final VTable mTable;

    public mEditorInputIDL(EditView view, VTable table) {
        super(view, true);

        mView = view;
        mTable = table;
    }

    @Override
    public boolean commitText(CharSequence text, int newCursorPosition) {

        if (text == null) {
            text = "";
        }

        int length = text.length();

        if (hasComposingText()) {

            int start = mTable.composingStart;
            int end = mTable.composingEnd;

            mTable.text.replace(start, end, text);
            mTable.cursorPosition = start + length;

            clearComposingText();

        } else if (mTable.selector.hasSelection()) {

            int start = mTable.selector.getMinSelection();
            int end = mTable.selector.getMaxSelection();

            mTable.text.replace(start, end, text);
            mTable.cursorPosition = start + length;

            mTable.selector.clearSelection();

        } else {

            mTable.text.insert(mTable.cursorPosition, text);

            mTable.cursorPosition += length;
        }

        updateEditor();

        return true;
    }

    @Override
    public boolean finishComposingText() {

        clearComposingText();

        return true;
    }

    @Override
    public boolean setComposingText(CharSequence text, int newCursorPosition) {

        if (text == null) {
            text = "";
        }

        int length = text.length();

        if (mTable.selector.hasSelection()) {

            int start = mTable.selector.getMinSelection();
            int end = mTable.selector.getMaxSelection();

            mTable.text.replace(start, end, text);

            mTable.composingStart = start;
            mTable.composingEnd = start + length;
            mTable.cursorPosition = mTable.composingEnd;

            mTable.selector.clearSelection();

        } else if (hasComposingText()) {

            int start = mTable.composingStart;

            mTable.text.replace(start, mTable.composingEnd, text);

            mTable.composingEnd = start + length;
            mTable.cursorPosition = mTable.composingEnd;

        } else {

            int start = mTable.cursorPosition;

            mTable.composingStart = start;

            mTable.text.insert(start, text);

            mTable.composingEnd = start + length;
            mTable.cursorPosition = mTable.composingEnd;
        }

        updateEditor();

        return true;
    }

    @Override
    public boolean deleteSurroundingText(int beforeLength, int afterLength) {

        if (mTable.selector.hasSelection()) {

            int start = mTable.selector.getMinSelection();
            int end = mTable.selector.getMaxSelection();

            mTable.text.delete(start, end);
            mTable.cursorPosition = start;

            mTable.selector.clearSelection();
            clearComposingText();

            updateEditor();

            return true;
        }

        int cursor = mTable.cursorPosition;
        int textLength = mTable.text.length();

        int start = cursor - beforeLength;
        int end = cursor + afterLength;

        if (start < 0) {
            start = 0;
        }

        if (end > textLength) {
            end = textLength;
        }

        if (start == end) {

            return super.deleteSurroundingText(beforeLength, afterLength);
        }

        mTable.text.delete(start, end);
        mTable.cursorPosition = start;

        clearComposingText();

        updateEditor();

        return true;
    }

    @Override
    public boolean sendKeyEvent(KeyEvent event) {

        if (event.getAction() != KeyEvent.ACTION_DOWN) {

            return super.sendKeyEvent(event);
        }

        int keyCode = event.getKeyCode();

        if (keyCode == KeyEvent.KEYCODE_DEL) {

            return deleteSurroundingText(1, 0);
        }

        if (keyCode == KeyEvent.KEYCODE_ENTER) {

            mView.insertNewLine();

            return true;
        }

        return super.sendKeyEvent(event);
    }

    private boolean hasComposingText() {

        return mTable.composingStart >= 0 && mTable.composingEnd >= mTable.composingStart;
    }

    private void clearComposingText() {

        mTable.composingStart = -1;
        mTable.composingEnd = -1;
    }

    private void updateEditor() {

        mView.updateAfterTextChanged();

        mView.scrollToCursor(mTable.cursorPosition);
    }
}