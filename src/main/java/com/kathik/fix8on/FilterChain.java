package com.kathik.fix8on;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

import quickfix.Message;

public class FilterChain {
    private final Map<String, List<Function<Message, Message>>> filters = new HashMap<>();

    private final Lock lock = new ReentrantLock();

    public void add(String sessID, Function<Message, Message> m) {
        filters.putIfAbsent(sessID, new ArrayList<>());
        List<Function<Message, Message>> stages = filters.get(sessID);
        stages.add(m);
    }

    public void lock() {
        lock.lock();
    }

    public void unlock() {
        lock.unlock();
    }

    public void apply(FIX8ONMsg m) {
        filters.get(m.getUuid()).stream().forEach(t -> m.setCurrent(t.apply(m.getCurrent())));
    }

    Map<String,List<Function<Message, Message>>> getTransforms() {
        return filters;
    }

}
