package com.aantik.texteditor;

import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.view.GestureDetector;
import android.view.ScaleGestureDetector;
import android.widget.OverScroller;

import com.aantik.texteditor.handler.BlinkHandler;
import com.aantik.texteditor.model.Span;
import com.aantik.texteditor.selection.Selector;

import java.util.ArrayList;

/*
 * Created by aanti
 * 8/24/2026 5:44 AM
 *
 *   ⋆    ႔ ႔
 *     ᠸ^ ^ ⸝⸝
 *       |、˜㵵
 *       じしˍ,)⁐̤ᐷ
 *
 * Fox Mode 🍺
 */

public class VTable {
    public Paint textPaint;
    public final ArrayList<Span> tokens = new ArrayList<Span>();
    public Paint cursorPaint;
    public Drawable wrapStartIcon;
    public Drawable wrapEndIcon;
    public float wrapStartWidth = 0;
    public float wrapEndWidth = 0;

    public int editorScrollX = 0;
    public int editorScrollY = 0;
    public final ArrayList<Integer> lineStarts = new ArrayList<Integer>();
    public final ArrayList<Integer> logicalLineIndices = new ArrayList<Integer>();

    public final float[] asciiWidths = new float[128];
    public boolean wordWrap = false;
    public float maxLineWidth = 0f;

    public int composingStart = -1;
    public int composingEnd = -1;
    public Paint gutterPaint;
    public Paint lineNumberPaint;
    public Paint dividerPaint;
    public Paint currentLinePaint;

    public TextBuffer text = new TextBuffer();

    public float textSize;
    public float baseLine;
    public float lineHeight;

    public float gutterWidth;
    public float gutterPadding;
    public float textLeftPadding;
    public float dividerWidth;

    public float minTextSize;
    public float maxTextSize;

    public int cursorPosition = 0;

    public Selector selector;
    public GestureDetector gestureDetector;
    public ScaleGestureDetector scaleDetector;
    public OverScroller scroller;

    public boolean scaling = false;
    public int overFlingDistance;

    public long lastScrollTime;
    public long lastInteractionTime;
    public long blinkStartTime;
    public boolean forceCursorVisible;
    public boolean forceHandleVisible;
    public BlinkHandler blinkHandler;
}
