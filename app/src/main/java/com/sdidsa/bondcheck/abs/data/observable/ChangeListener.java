package com.sdidsa.bondcheck.abs.data.observable;

public interface ChangeListener<T> {
    void changed(T oldVal, T newVal);
}
