package uk.dioxic.mgenerate.core.operator.chrono;

import org.bson.BsonTimestamp;
import uk.dioxic.mgenerate.common.Resolvable;
import uk.dioxic.mgenerate.common.annotation.Operator;
import uk.dioxic.mgenerate.common.annotation.OperatorProperty;
import uk.dioxic.mgenerate.core.operator.AbstractOperator;
import uk.dioxic.mgenerate.core.operator.numeric.NumberInt;

@Operator({"ts", "mgTimestamp"})
public class Timestamp extends AbstractOperator<BsonTimestamp> {

    @OperatorProperty(primary = true)
    Resolvable<Integer> t = new NumberInt();

    @OperatorProperty
    Resolvable<Integer> i = new NumberInt();

    @Override
    public BsonTimestamp resolveInternal() {
        return new BsonTimestamp(t.resolve(), i.resolve());
    }


}
