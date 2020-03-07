package com.juane.arduino.gpstracker.service;

public final class MessageType {
    public static final int PROBLEM_STOP = 0;
    public static final int SENDING_LOCATION = 1;
    public static final int START_REQUEST = 2;
    public static final int REGISTER_CLIENT = 3;
    public static final int UNREGISTER_CLIENT = 4;
    public static final int FIRST_TIME_SWITCH = 5;
    public static final int SHOW_TOAST = 6;

    private MessageType(){}
}
