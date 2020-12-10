package com.olam.digital.ofis.coffeesurveyconstraints.utils;

import android.content.res.Resources;

public class CommonUtilities {

    public static String getLocaleSuffix(Resources res) {
        String result;
        result = res.getConfiguration().locale.getLanguage();
        if (result.equalsIgnoreCase("in")) {
            result = "id";
        }
        result= result.substring(0, 1).toUpperCase() + result.substring(1);
        return result;
    }
}
