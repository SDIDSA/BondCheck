package com.sdidsa.bondcheck.abs.style;

import com.sdidsa.bondcheck.abs.utils.ErrorHandler;
import com.sdidsa.bondcheck.abs.utils.Platform;
import com.sdidsa.bondcheck.abs.data.ConcurrentArrayList;
import com.sdidsa.bondcheck.abs.data.observable.ChangeListener;
import com.sdidsa.bondcheck.abs.data.property.Property;

import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicBoolean;

public interface Styleable {
    void applyStyle(Style style);
    void applyStyle(Property<Style> style);

    static void bindStyle(Styleable node, Property<Style> style) {
        try {
            bindStyleWeak(node, style);
        }catch(Exception x) {
            ErrorHandler.handle(x, "binding style");
        }
    }

    ConcurrentArrayList<WeakReference<Styleable>> bound_cache = new ConcurrentArrayList<>();

    private static boolean isBound(Styleable node) {
        bound_cache.removeIf(n -> n.get() == null);
        AtomicBoolean res = new AtomicBoolean(false);
        bound_cache.forEach(nodeRef -> {
            if(nodeRef.get() == node) res.set(true);
        });
        return res.get();
    }

    private static void bindStyleWeak(Styleable node, Property<Style> style) {
        node.applyStyle(style.get());
        if(isBound(node)) return;
        WeakReference<Styleable> weakNode = new WeakReference<>(node);
        ChangeListener<Style> listener = new ChangeListener<>() {
            @Override
            public void changed(Style ov, Style nv) {
                if (weakNode.get() != null) {
                    if (nv != ov) {
                        weakNode.get().applyStyle(nv);
                    }
                } else {
                    Platform.runBack(() -> style.removeListener(this));
                }
            }
        };
        style.addListener(listener);
        bound_cache.add(weakNode);
    }
}
