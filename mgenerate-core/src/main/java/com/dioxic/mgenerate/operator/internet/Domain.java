package com.dioxic.mgenerate.operator.internet;

import com.dioxic.mgenerate.FakerUtil;
import com.dioxic.mgenerate.annotation.Operator;
import uk.dioxic.faker.resolvable.FakerStringResolver;
import uk.dioxic.faker.resolvable.Resolvable;

@Operator
public class Domain implements Resolvable<String> {

    @Override
    public String resolve() {
        return String.join(".",
                "www",
                FakerUtil.getFake("name.last_name").replace("'", "").toLowerCase(),
                FakerUtil.getFake("internet.domain_suffix"));
    }

}
