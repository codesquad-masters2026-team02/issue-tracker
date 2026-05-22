package com.codesquad.issueTracker.security;
import at.favre.lib.crypto.bcrypt.*;

public class PasswordHelper {

    public static String hashPassword(String plainPassword){
        return BCrypt.withDefaults().hashToString(12, plainPassword.toCharArray());
    }

    public static boolean  verifyPassword(String plainPassword, String hashedPassword){
        return (BCrypt.verifyer().verify(plainPassword.toCharArray(), hashedPassword.toCharArray())).verified;
    }

}
