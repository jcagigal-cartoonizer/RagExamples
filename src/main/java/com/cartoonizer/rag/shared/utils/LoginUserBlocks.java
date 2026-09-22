package com.cartoonizer.rag.shared.utils;

import static com.cartoonizer.rag.shared.utils.GeneralAnswerProcessor.PREFIX;

public class LoginUserBlocks implements IGeneralBlocks {

    @Override
    public String getEndTag() {
        return "# 6) ";
    } 

}
