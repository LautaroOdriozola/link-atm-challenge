package com.link.atm.util;

public final class MaskUtil {

    private MaskUtil() {
    }

    public static String maskCardNumber(String cardNumber) {
        if (cardNumber == null) {
            return null;
        }
        int length = cardNumber.length();
        if (length <= 4) {
            return cardNumber;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length - 4; i++) {
            sb.append('*');
        }
        sb.append(cardNumber.substring(length - 4));
        return sb.toString();
    }
}
