package com.dioxic.mgenerate.operator;

import com.dioxic.mgenerate.OperatorFactory;
import com.dioxic.mgenerate.annotation.OperatorClass;
import com.dioxic.mgenerate.annotation.OperatorProperty;

import java.util.List;

@OperatorClass
public class Join implements Operator<String> {

    private static final String DEFAULT_SEP = "";

    @OperatorProperty(required = true)
    Operator<List<? extends CharSequence>> array;

    @OperatorProperty
    Operator<String> sep = OperatorFactory.wrap(DEFAULT_SEP);

    @Override
    public String resolve() {
        return String.join(sep.resolve(), array.resolve());
    }
}
