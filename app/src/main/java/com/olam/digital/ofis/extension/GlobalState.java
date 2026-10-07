package com.olam.digital.ofis.extension;

public class GlobalState {

    public interface OnValueChangeListener {
        void onValueChanged(int newValue);
    }

    private static int counter = 0;
    private static OnValueChangeListener listener;

    public static void setOnValueChangeListener(OnValueChangeListener l) {
        listener = l;
    }

    public static int getCounter() {
        return counter;
    }

    public static void setCounter(int value) {
        counter = value;
        if (listener != null) {
            listener.onValueChanged(value);
        }
    }
}
