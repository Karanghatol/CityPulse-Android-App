package com.citypulse.utils;

import android.util.Patterns;

public class ValidationUtils {
    public static boolean isValidEmail(String email) {
        return email != null && !email.isEmpty()
                && Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches();
    }
}
