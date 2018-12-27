package com.security.demo.web;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ReactiveArrayList<E> {

    private CopyOnWriteArrayList<E> delegate = new CopyOnWriteArrayList<>();

    public ReactiveArrayList add(E e) {
        delegate.add(e);
        return this;
    }


    public ReactiveArrayList addAll(ReactiveArrayList<E> e) {
        addAll(e.toList());
        return this;
    }


    public ReactiveArrayList addAll(List<E> e) {
        delegate.addAll(e);
        return this;
    }



    public List<E> toList() {
        return Collections.unmodifiableList(delegate);
    }

    public int size(){
        return delegate.size();
    }


    public void clear() {
        delegate.clear();
    }

}
