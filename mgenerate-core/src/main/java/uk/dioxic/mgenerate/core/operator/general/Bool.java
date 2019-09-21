package uk.dioxic.mgenerate.core.operator.general;

import uk.dioxic.mgenerate.common.Resolvable;
import uk.dioxic.mgenerate.common.annotation.Operator;
import uk.dioxic.mgenerate.core.util.FakerUtil;

@Operator({"bool", "boolean"})
public class Bool implements Resolvable<Boolean> {

    @Override
    public Boolean resolve() {
        return FakerUtil.randomBoolean();
    }

}
