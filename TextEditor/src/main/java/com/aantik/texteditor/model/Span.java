package com.aantik.texteditor.model;

public class Span {
    public int start;
    public int end;
    public int color;

    public Span(int start, int end, int color) {
        this.start = start;
        this.end = end;
        this.color = color;
    }
}
