package com.sdidsa.bondcheck.abs.data;

public interface TriConsumer<T,U,V,R> {
    R apply(T t, U u, V v);
}
