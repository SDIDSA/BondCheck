package com.sdidsa.bondcheck.abs.data;

public interface QuintConsumer<A, B, C, D, E, F> {
    F apply(A a, B b, C c, D d, E e);

}
