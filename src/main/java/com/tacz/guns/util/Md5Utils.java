package com.tacz.guns.util;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class Md5Utils {
   private static final int STREAM_BUFFER_LENGTH = 1024;
   private static final MessageDigest DIGEST;

   public static String md5Hex(InputStream inputStream) throws IOException {
      return toHexString(md5(inputStream));
   }

   public static String md5Hex(byte[] data) {
      return toHexString(DIGEST.digest(data));
   }

   public static byte[] md5(InputStream inputStream) throws IOException {
      byte[] buffer = new byte[1024];

      for (int read = inputStream.read(buffer, 0, 1024); read > -1; read = inputStream.read(buffer, 0, 1024)) {
         DIGEST.update(buffer, 0, read);
      }

      return DIGEST.digest();
   }

   public static byte[] md5(byte[] data) {
      return DIGEST.digest(data);
   }

   public static String toHexString(byte[] bytes) {
      StringBuilder hexString = new StringBuilder();

      for (byte b : bytes) {
         String hex = Integer.toHexString(255 & b);
         if (hex.length() == 1) {
            hexString.append('0');
         }

         hexString.append(hex);
      }

      return hexString.toString();
   }

   static {
      try {
         DIGEST = MessageDigest.getInstance("MD5");
      } catch (NoSuchAlgorithmException e) {
         throw new RuntimeException(e);
      }
   }
}
