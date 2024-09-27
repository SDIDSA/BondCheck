package com.sdidsa.bondcheck.models;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.sdidsa.bondcheck.abs.utils.ErrorHandler;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

public class ModelAdapter<T> extends TypeAdapter<T> {
    private final TypeAdapter<JsonObject> delegate;
    private final Class<T> type;

    public ModelAdapter(TypeAdapter<JsonObject> delegate, Class<T> type) {
        this.delegate = delegate;
        this.type = type;
    }

    @Override
    public void write(JsonWriter out, T value) throws IOException {
        JsonObject res = new JsonObject();

        Field[] fields = type.getDeclaredFields();
        for(Field field : fields) {
            try {
                Object v = type.getMethod(getGetterName(field.getName())).invoke(value);
                if (v != null) {
                    if (v instanceof String) {
                        res.addProperty(field.getName(), (String) v);
                    } else if (v instanceof Number) {
                        res.addProperty(field.getName(), (Number) v);
                    } else if (v instanceof Boolean) {
                        res.addProperty(field.getName(), (Boolean) v);
                    } else {
                        res.addProperty(field.getName(), v.toString());  // Fallback to string representation
                    }
                } else {
                    res.add(field.getName(), null);
                }
            } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                ErrorHandler.handle(e, "serializing model object of type: " + type.getName());
            }

        }

        delegate.write(out, res);
    }

    @Override
    public T read(JsonReader in) throws IOException {
        JsonObject object = delegate.fromJsonTree(delegate.read(in));

        try {
            T obj = type.getConstructor().newInstance();

            for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
                String key = entry.getKey();
                JsonElement jsonElement = entry.getValue();
                if (jsonElement != null && !jsonElement.isJsonNull()) {
                    try {
                        Method setter = findSetterMethod(key, obj);
                        Class<?> paramType = setter.getParameterTypes()[0];
                        Object value = convertJsonElement(jsonElement, paramType);
                        setter.invoke(obj, value);
                    } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                        //ignore field
                    }
                }
            }

            return obj;
        } catch (IllegalAccessException |
                 InstantiationException |
                 InvocationTargetException |
                 NoSuchMethodException e) {
            ErrorHandler.handle(e, "parsing model object of type: " + type.getName());
        }

        return null;
    }

    private Object convertJsonElement(JsonElement jsonElement, Class<?> paramType) {
        if (paramType == String.class) {
            return jsonElement.getAsString();
        } else if (paramType == int.class || paramType == Integer.class) {
            return jsonElement.getAsInt();
        } else if (paramType == boolean.class || paramType == Boolean.class) {
            return jsonElement.getAsBoolean();
        } else if (paramType == long.class || paramType == Long.class) {
            return jsonElement.getAsLong();
        } else if (paramType == double.class || paramType == Double.class) {
            return jsonElement.getAsDouble();
        } else if (paramType == float.class || paramType == Float.class) {
            return jsonElement.getAsFloat();
        }
        // Add other types as needed
        return null;
    }

    private Method findSetterMethod(String fieldName, T obj) throws NoSuchMethodException {
        String setterName = getSetterName(fieldName);
        for (Method method : obj.getClass().getMethods()) {
            if (method.getName().equals(setterName)) {
                return method;
            }
        }
        throw new NoSuchMethodException("No setter found for field: " + fieldName);
    }

    private static String getSetterName(String field) {
        if (!field.isEmpty()) {
            return "set" + field.substring(0, 1).toUpperCase() + field.substring(1);
        } else {
            return "";
        }
    }

    private static String getGetterName(String field) {
        if (!field.isEmpty()) {
            return "get" + field.substring(0, 1).toUpperCase() + field.substring(1);
        } else {
            return "";
        }
    }
}