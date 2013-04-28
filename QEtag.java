package com.qiniu;

import ch.qos.logback.core.encoder.ByteArrayUtil;
import org.apache.commons.codec.binary.Base64;

import java.io.*;
import java.security.MessageDigest;

/**  
 * 七牛qetag算法JAVA实现
 * User: njava
 * Date: 13-4-27
 * Time: PM5:04
 */
public class QEtag {
    private final static int BLOCK_BITS = 22; // Indicate that the blocksize is 4M
    private final static int BLOCK_SIZE = 1 << BLOCK_BITS;

    public static String getEtag(String filename) throws IOException {
        File file = new File(filename);
        long fileSize = file.length();
        int blockCnt = blockCount(fileSize);
        byte[] sha1Buf = new byte[21];
        byte[] sha1Code;

        if (blockCnt <= 1) {    //file size <=4m
            byte[] header = ByteArrayUtil.hexStringToByteArray("16");
            System.arraycopy(header, 0, sha1Buf, 0, header.length);
            sha1Code = calSha1(new FileInputStream(file));
            System.arraycopy(sha1Code, 0, sha1Buf, 1, sha1Code.length);
        } else { //file size > 4M
            byte[] header = ByteArrayUtil.hexStringToByteArray("96");
            System.arraycopy(header, 0, sha1Buf, 0, header.length);
            byte[] sha1BlockBuf = new byte[blockCnt * 20];
            FileInputStream fileInputStream = new FileInputStream(file);
            for (int i = 0; i < blockCnt; i++) {
                byte[] body = new byte[BLOCK_SIZE];
                int read = fileInputStream.read(body);
                sha1Code = calSha1(new ByteArrayInputStream(body, 0, read));
                System.arraycopy(sha1Code, 0, sha1BlockBuf, i * 20, sha1Code.length);
            }
            sha1Code = calSha1(new ByteArrayInputStream(sha1BlockBuf));
            System.arraycopy(sha1Code, 0, sha1Buf, 1, sha1Code.length);
        }

        return Base64.encodeBase64URLSafeString(sha1Buf);
    }

    private static int blockCount(long fsize) {
        return (int) ((fsize + (BLOCK_SIZE - 1)) >> BLOCK_BITS);
    }

    private static byte[] calSha1(InputStream inputStream) {
        MessageDigest sha1 = null;
        try {
            sha1 = MessageDigest.getInstance("SHA-1");
            byte[] buffer = new byte[8192];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                sha1.update(buffer, 0, length);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (null != sha1) ? sha1.digest() : null;
    }

    public static void main(String[] args) throws IOException {
        String file = "/data/tmp/romdisk/MIUI_V5.zip";
        String etagValue = QEtag.getEtag(file);
        System.out.println(etagValue);
    }
}
