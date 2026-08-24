package com.aantik.texteditor.blockComment;

import java.util.regex.Pattern;

public enum java {

    LINE_COMMENT("//", "$"),

    BLOCK_COMMENT("/\\*", "\\*/");

    private final Pattern startPattern;
    private final Pattern endPattern;

    java(String sRegex, String eRegex) {
        this.startPattern = Pattern.compile(sRegex);
        this.endPattern = Pattern.compile(eRegex);
    }

    public Pattern getStartPattern() {
        return startPattern;
    }

    public Pattern getEndPattern() {
        return endPattern;
    }
}
