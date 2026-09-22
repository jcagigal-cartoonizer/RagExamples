package com.cartoonizer.rag.shared.utils;

public interface IGeneralBlocks {
    String getEndTag();
    default boolean isEndTag(String line) {
        boolean isEnd = line.contains(getEndTag()) || line.contains("In Fragment-based navigation") ||
                line.contains("then you can tweak");
        return isEnd;
    }
}
