package com.staybook.service;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

/**
 * Creates booking references like {@code SB-K7QM4XPA}.
 * Uses an alphabet without look-alike characters (0/O, 1/I/L) so references are
 * easy to read out over the phone, and SecureRandom so they cannot be guessed.
 * 31^8 (about 8.5 x 10^11) combinations make collisions rare; the service retries
 * on a clash and the database unique key is the final guard.
 */
@Component
public class ReferenceGenerator {

    static final String PREFIX = "SB-";
    private static final char[] ALPHABET = "ABCDEFGHJKMNPQRSTUVWXYZ23456789".toCharArray();
    private static final int LENGTH = 8;

    private final SecureRandom random = new SecureRandom();

    public String next() {
        StringBuilder sb = new StringBuilder(PREFIX);
        for (int i = 0; i < LENGTH; i++) {
            sb.append(ALPHABET[random.nextInt(ALPHABET.length)]);
        }
        return sb.toString();
    }
}
