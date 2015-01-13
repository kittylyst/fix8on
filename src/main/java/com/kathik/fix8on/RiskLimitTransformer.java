package com.kathik.fix8on;

import java.util.function.Function;
import quickfix.Message;

public final class RiskLimitTransformer implements Function<Message, Message> {

    public RiskLimitTransformer(long limit) {
        // TODO Auto-generated constructor stub
    }

    public RiskLimitTransformer() {
        this(10_000_000);
    }

    public Message apply(Message t) {
        // TODO Implement
        return t;
    }
}
