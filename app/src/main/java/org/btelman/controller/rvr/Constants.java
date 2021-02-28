package org.btelman.controller.rvr;

public class Constants {
    public static byte[] powerDown = new byte[]{
            (byte) -115,
            (byte) 24,
            (byte) 1,
            (byte) 19,
            (byte) 1,
            (byte) 1,
            (byte) -47,
            (byte) -40
    };

    public static byte[] wakeUp = new byte[]{
            (byte) -115,
            (byte) 24,
            (byte) 0,
            (byte) 19,
            (byte) 13,
            (byte) 1,
            (byte) -58,
            (byte) -40
    };
}
