package io.github.leofuso.obs.demo.fixture;

import javax.annotation.*;

import java.lang.reflect.*;
import java.security.*;
import java.util.*;

public class UUIDFixture {

    private static final Constructor<UUID> byteArrayConstructor;

    static {
        try {
            byteArrayConstructor = UUID.class.getDeclaredConstructor(byte[].class);
            byteArrayConstructor.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new InternalError("Failed to access the private UUID constructor.", e);
        }
    }

    public static UUID fromNamespaceAndBytes(@Nonnull UUID namespace, @Nonnull byte[] name) {
        Objects.requireNonNull(namespace, "Namespace is required.");
        Objects.requireNonNull(name, "The name is required.");

        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new InternalError("SHA-1 is not supported", e);
        }

        md.update(toBytes(namespace));
        md.update(name);
        final byte[] digest = md.digest();
        digest[6] &= 0x0f;  /* clear version        */
        digest[6] |= 0x50;  /* set to version 5     */
        digest[8] &= 0x3f;  /* clear variant        */
        digest[8] |= 0x80;  /* set to IETF variant  */

        try {
            return byteArrayConstructor.newInstance((Object) digest);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new InternalError("Unexpected error while calling private constructor.", e);
        }
    }

    private static byte[] toBytes(final UUID uuid) {
        final byte[] out = new byte[16];
        final long msb = uuid.getMostSignificantBits();
        final long lsb = uuid.getLeastSignificantBits();
        for (int i = 0; i < 8; i++) {
            out[i] = (byte) ((msb >> ((7 - i) * 8)) & 0xff);
        }
        for (int i = 8; i < 16; i++) {
            out[i] = (byte) ((lsb >> ((15 - i) * 8)) & 0xff);
        }
        return out;
    }

}
