package com.aantik.texteditor;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.OverScroller;

import androidx.core.content.ContextCompat;

import com.aantik.texteditor.config.EditorConstants;
import com.aantik.texteditor.controller.EditorTinput;
import com.aantik.texteditor.controller.mEditorInputIDL;
import com.aantik.texteditor.layout.TextLayout;
import com.aantik.texteditor.renderer.TextRenderer;
import com.aantik.texteditor.selection.Selector;

public class EditView extends View {

    private final VTable vTable = new VTable();
    private TextLayout layout;
    private TextRenderer renderer;
    private EditorTinput controller;

    public EditView(Context context) {
        this(context, null);
    }

    public EditView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EditView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        layout = new TextLayout(this, vTable);
        renderer = new TextRenderer(this, vTable);
        controller = new EditorTinput(this, vTable);

        final float density = getResources().getDisplayMetrics().density;

        vTable.textSize = EditorConstants.DEFAULT_TEXT_SIZE * density;
        vTable.minTextSize = EditorConstants.MIN_TEXT_SIZE * density;
        vTable.maxTextSize = EditorConstants.MAX_TEXT_SIZE * density;
        vTable.gutterPadding = EditorConstants.GUTTER_PADDING * density;
        vTable.textLeftPadding = EditorConstants.TEXT_LEFT_PADDING * density;
        vTable.dividerWidth = EditorConstants.DIVIDER_WIDTH * density;

        vTable.textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        vTable.textPaint.setColor(EditorConstants.COLOR_TEXT);
        vTable.textPaint.setTextSize(vTable.textSize);

        vTable.gutterPaint = new Paint();
        vTable.gutterPaint.setColor(EditorConstants.COLOR_GUTTER);

        vTable.lineNumberPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        vTable.lineNumberPaint.setColor(EditorConstants.COLOR_LINE_NUMBER);
        vTable.lineNumberPaint.setTextSize(vTable.textSize);
        vTable.lineNumberPaint.setTextAlign(Paint.Align.RIGHT);

        vTable.dividerPaint = new Paint();
        vTable.dividerPaint.setColor(EditorConstants.COLOR_DIVIDER);
        vTable.dividerPaint.setStrokeWidth(vTable.dividerWidth);

        vTable.currentLinePaint = new Paint();
        vTable.currentLinePaint.setColor(EditorConstants.COLOR_CURRENT_LINE);

        vTable.cursorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        vTable.cursorPaint.setColor(EditorConstants.COLOR_CURSOR);
        vTable.cursorPaint.setStrokeWidth(1.5f * density);

        vTable.wrapStartIcon = ContextCompat.getDrawable(getContext(), R.drawable.ic_wrap_start);
        vTable.wrapEndIcon = ContextCompat.getDrawable(getContext(), R.drawable.ic_wrap_end);

        setBackgroundColor(EditorConstants.COLOR_BACKGROUND);

        setFocusable(true);
        setFocusableInTouchMode(true);
        setLongClickable(true);

        setHorizontalScrollBarEnabled(true);
        setVerticalScrollBarEnabled(true);

        updateGutterWidth();
        layout.initAsciiCache();
        updateFontMetrics();

        vTable.selector = new Selector(this);
        vTable.scroller = new OverScroller(getContext());
        vTable.overFlingDistance = ViewConfiguration.get(getContext()).getScaledOverflingDistance();

        controller.setupGestureDetectors();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (vTable.wordWrap && w != oldw) {
            updateAfterTextChanged();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        renderer.draw(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return controller.onTouchEvent(event);
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        controller.computeScroll();
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        outAttrs.inputType = EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE;
        outAttrs.imeOptions = EditorInfo.IME_FLAG_NO_ENTER_ACTION;
        return new mEditorInputIDL(this, vTable);
    }

    public void updateAfterTextChanged() {
        controller.rebuildHighlightCache();
        rebuildLineCache();
        updateGutterWidth();
        scrollToClamped(vTable.editorScrollX, vTable.editorScrollY);
        invalidate();
    }

    public void scrollToCursor(int position) {
        controller.scrollToCursor(position);
    }

    public void updateFontMetrics() {
        layout.updateFontMetrics();
    }

    public void updateGutterWidth() {
        layout.updateGutterWidth();
    }

    public void rebuildLineCache() {
        layout.rebuildLineCache();
    }

    public void scrollToClamped(int x, int y) {
        int maxX = getMaxScrollX();
        int maxY = getMaxScrollY();
        vTable.editorScrollX = Math.max(0, Math.min(x, maxX));
        vTable.editorScrollY = Math.max(0, Math.min(y, maxY));
        awakenScrollBars();
        invalidate();
    }

    public int getMaxScrollX() {
        return vTable.wordWrap ? 0 : Math.max(0, (int) Math.ceil(getContentWidth()) - getWidth());
    }

    public int getMaxScrollY() {
        return Math.max(0, (int) Math.ceil(getContentHeight()) - getHeight());
    }

    public void insertNewLine() {
        controller.insertNewLine();
    }

    public void insert(CharSequence content) {
        controller.insert(content);
    }

    public int findOffsetForPosition(float x, float y) {
        return layout.findOffsetForPosition(x, y);
    }

    public float getXForPosition(int pos) {
        return layout.getXForPosition(pos);
    }

    public float getBaseLineForPosition(int pos) {
        return layout.getBaseLineForPosition(pos);
    }

    public int getLineForPosition(int pos) {
        return layout.getLineForPosition(pos);
    }

    public int getLineCount() {
        return layout.getLineCount();
    }

    public int getLineStartOffset(int line) {
        return layout.getLineStartOffset(line);
    }

    public int getLineEndOffset(int line) {
        return layout.getLineEndOffset(line);
    }

    public float getContentHeight() {
        return layout.getContentHeight();
    }

    public float getContentWidth() {
        return layout.getContentWidth();
    }

    public void onInteraction() {
        controller.onInteraction();
    }

    public void showKeyboard() {
        controller.showKeyboard();
    }
    public TextBuffer getTextBuffer() {
        return vTable.text;
    }

    public Paint getTextPaint() {
        return vTable.textPaint;
    }


    public int getCursorPosition() {
        return vTable.cursorPosition;
    }

    public float getGutterWidth() {
        return vTable.gutterWidth;
    }

    public float getTextLeftPadding() {
        return vTable.textLeftPadding;
    }

    public long getLastScrollTime() {
        return vTable.lastScrollTime;
    }

    public void setWordWrap(boolean enabled) {
        if (vTable.wordWrap != enabled) {
            vTable.wordWrap = enabled;
            if (vTable.wordWrap) vTable.editorScrollX = 0;
            updateAfterTextChanged();
        }
    }

    public boolean isWordWrap() {
        return vTable.wordWrap;
    }

    public void setCursorPosition(int position) {
        vTable.cursorPosition = Math.max(0, Math.min(position, vTable.text.length()));
        scrollToCursor(vTable.cursorPosition);
        invalidate();
    }

    public void setText(String content) {
        vTable.text = new TextBuffer(content == null ? "" : content);
        if (vTable.cursorPosition > vTable.text.length()) vTable.cursorPosition = vTable.text.length();
        vTable.selector.clearSelection();
        updateAfterTextChanged();
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (!hasWindowFocus) {
            vTable.blinkStartTime = 0;
            vTable.lastInteractionTime = 0;
            invalidate();
        } else {
            controller.startBlink();
        }
    }

    @Override
    protected int computeHorizontalScrollRange() {
        return Math.max(getWidth(), (int) getContentWidth());
    }

    @Override
    protected int computeHorizontalScrollExtent() {
        return getWidth();
    }

    @Override
    public int computeHorizontalScrollOffset() {
        return vTable.editorScrollX;
    }

    @Override
    protected int computeVerticalScrollRange() {
        return Math.max(getHeight(), (int) getContentHeight());
    }

    @Override
    protected int computeVerticalScrollExtent() {
        return getHeight();
    }

    @Override
    public int computeVerticalScrollOffset() {
        return vTable.editorScrollY;
    }

    @Override
    public boolean awakenScrollBars() {
        return super.awakenScrollBars();
    }

    @Override
    public boolean onCheckIsTextEditor() {
        return true;
    }
}
