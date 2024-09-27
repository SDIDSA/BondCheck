package com.sdidsa.bondcheck.abs.locale;

import com.sdidsa.bondcheck.abs.utils.ErrorHandler;
import com.sdidsa.bondcheck.abs.utils.Platform;
import com.sdidsa.bondcheck.abs.data.ConcurrentArrayList;
import com.sdidsa.bondcheck.abs.data.observable.ChangeListener;
import com.sdidsa.bondcheck.abs.data.property.Property;

import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicBoolean;

public interface Localized {
	void applyLocale(Locale locale);
	void applyLocale(Property<Locale> locale);

	static void bindLocale(Localized node, Property<Locale> locale) {
		try {
			bindLocaleWeak(node, locale);
		}catch(Exception x) {
			ErrorHandler.handle(x, "binding locale");
		}
	}

	ConcurrentArrayList<WeakReference<Localized>> bound_cache = new ConcurrentArrayList<>();

	private static boolean isBound(Localized node) {
		bound_cache.removeIf(n -> n.get() == null);
		AtomicBoolean res = new AtomicBoolean(false);
		bound_cache.forEach(nodeRef -> {
			if(nodeRef.get() == node) res.set(true);
		});
		return res.get();
	}

	private static void bindLocaleWeak(Localized node, Property<Locale> locale) {
		node.applyLocale(locale.get());
		if(isBound(node)) return;
		WeakReference<Localized> weakNode = new WeakReference<>(node);
		ChangeListener<Locale> listener = new ChangeListener<>() {
			@Override
			public void changed(Locale ov, Locale nv) {
				if (weakNode.get() != null) {
					if (nv != ov) {
						weakNode.get().applyLocale(nv);
					}
				} else {
					Platform.runBack(() -> locale.removeListener(this));
				}
			}
		};
		locale.addListener(listener);
		bound_cache.add(weakNode);
	}
}
