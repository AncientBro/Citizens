package net.citizensnpcs.properties;

import net.citizensnpcs.utils.Messaging;

public class RawYAMLObject {
    private final Object value;

    public RawYAMLObject(Object value) {
        this.value = value;
    }

    public boolean getBoolean() {
        try {
            return (Boolean) value;
        } catch (NullPointerException e) {
            Messaging.log("Report this error ASAP.");
            e.printStackTrace();
            return false;
        }
    }

    public double getDouble() {
        try {
            if (value instanceof Number) {
                return ((Number) value).doubleValue();
            } else if (value instanceof Float) {
                return (Float) value;
            } else if (value instanceof Double) {
                return (Double) value;
            } else {
                return (Integer) value;
            }
        } catch (NullPointerException e) {
            Messaging.log("Report this error ASAP.");
            e.printStackTrace();
            return 0;
        }
    }

    public int getInt() {
        try {
            if (value instanceof Number) {
                return ((Number) value).intValue();
            }
            return (Integer) value;
        } catch (NullPointerException e) {
            Messaging.log("Report this error ASAP.");
            e.printStackTrace();
            return 0;
        }
    }

    public String getString() {
        try {
            return (String) value;
        } catch (NullPointerException e) {
            Messaging.log("Report this error ASAP.");
            e.printStackTrace();
            return "";
        }
    }
}
