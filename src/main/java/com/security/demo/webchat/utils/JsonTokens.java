package com.security.demo.webchat.utils;

import com.nimbusds.jose.util.JSONObjectUtils;
import net.minidev.json.JSONObject;

import java.text.ParseException;

public class JsonTokens {

    public static String parse(JSONObject jsonObject, String attributeName) {
        try {
            return JSONObjectUtils.getString(jsonObject, attributeName);
        } catch (ParseException e) {
            throw new ArithmeticException("The " + attributeName + "in " + jsonObject + " is not found");
        }
    }

    public static long expire(JSONObject jsonObject, String attributeName) {
        try {
            return JSONObjectUtils.getLong(jsonObject, attributeName);
        } catch (ParseException e) {
            throw new ArithmeticException("The " + attributeName + "in " + jsonObject + " is not found");
        }
    }
}
