package com.dbank.speedup.upload;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.Map;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import sun.misc.BASE64Encoder;

/**
 * <一句话功能简述>
 * <功能详细描述>
 *
 * @author c57771
 * @version [版本号, 13-9-29]
 * @see  [相关类/方法]
 * @since [产品/模块版本]
 */
public class Utils
{
    public static final String ENCODING = "UTF-8";

    private static final String HMAC_SHA1 = "HmacSHA1";

    private static ObjectMapper objectMapper;


    private static final char[] DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    static
    {
        objectMapper = new ObjectMapper();
        SerializationConfig serializationConfig = objectMapper.getSerializationConfig();
        serializationConfig = serializationConfig.with(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS).withSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
        objectMapper.setSerializationConfig(serializationConfig);
        objectMapper.disable(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    private static String fileMd5;

    /**
     *
     * md5
     * @param str 源字符串
     * @return md5 字符串
     */
    public static String tomd5(String str)
    {
        try
        {
            return tomd5(str.getBytes(ENCODING));
        }
        catch (UnsupportedEncodingException e)
        {
            throw new IllegalStateException(e);
        }
    }

    /**
     *
     * md5
     * @param strBytes 源字符串
     * @return md5 字符串
     */
    public static String tomd5(byte[] strBytes)
    {
        return encode("MD5", strBytes);
    }

    public static String encode(String algorithm, byte[] source)
    {
        try
        {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            md.update(source);
            byte[] hash = md.digest();
            return toHexString(hash);
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new IllegalStateException(e);
        }
    }

    public static String hashMac(String data, String key)
    {
        return toHexString(byteHashMac(data, key)).toLowerCase(Locale.getDefault());
    }

    public static byte[] byteHashMac(String data, String key)
    {
        try
        {
            byte[] keyBytes = key.getBytes();
            SecretKeySpec signingKey = new SecretKeySpec(keyBytes, HMAC_SHA1);
            Mac mac = Mac.getInstance(HMAC_SHA1);
            mac.init(signingKey);
            return mac.doFinal(data.getBytes(ENCODING));
        }
        catch (Exception e)
        {
            throw new IllegalStateException(e);
        }
    }

    public static String base64HashMac(String data, String key)
    {
        BASE64Encoder encoder = new BASE64Encoder();
        return encoder.encode(byteHashMac(data, key));
    }

    /**
     * <一句话功能简述>
     * @param array 字节数组
     * @return 小写32为MD5值
     */
    private static String toHexString(byte[] array)
    {
        StringBuilder result = new StringBuilder();

        for (byte b : array)
        {
            result.append(DIGITS[(b >>> 4) & 0x0F]);
            result.append(DIGITS[b & 0x0F]);
        }
        return result.toString();
    }

    public static <T> T httpPost(String httpUrl, Callback<T> callback) throws IOException
    {
        return http(httpUrl, "POST", null, true, callback);
    }

    public static <T> T httpPut(String httpUrl, Map<String, String> headers, Callback<T> callback) throws IOException
    {
        return http(httpUrl, "PUT", headers, true, callback);
    }

    public static <T> T http(String httpUrl, String method, Map<String, String> headers, boolean doOutput, Callback<T> callback) throws IOException
    {
        URL url = new URL(httpUrl);
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.setRequestMethod(method);
        if (headers != null)
        {
            for (Map.Entry<String, String> entry : headers.entrySet())
            {
                connection.setRequestProperty(entry.getKey(), entry.getValue());
            }
        }
        if (doOutput)
        {
            connection.setDoOutput(true);
        }
        connection.connect();
        if (doOutput)
        {
            OutputStream out = null;
            try
            {
                out = connection.getOutputStream();
                callback.callback(out);
                out.flush();
            }
            finally
            {
                closeQuietly(out);
            }
        }

        InputStream in = null;
        try
        {
            in = connection.getInputStream();
            return callback.callback(connection.getResponseCode(), in);
        }
        finally
        {
            closeQuietly(in);
            connection.disconnect();
        }

    }

    public static void closeQuietly(Closeable closeable)
    {
        if (closeable == null)
        {
            return;
        }
        try
        {
            closeable.close();
        }
        catch (IOException e)
        {
            // do noting
        }
    }

    public static String toString(InputStream body) throws IOException
    {
        byte[] buffer = new byte[256];
        ByteArrayOutputStream bout = new ByteArrayOutputStream(256);
        int read;
        while ((read = body.read(buffer)) != -1)
        {
            bout.write(buffer, 0, read);
        }
        return new String(bout.toByteArray(), ENCODING);
    }


    public static Map toMap(String content) throws IOException
    {
        return (content == null || content.isEmpty()) ? null : objectMapper.readValue(content, Map.class);
    }

    public static <T> T toObject(String content,Class<T> type) throws IOException
    {
        return (content == null || content.isEmpty()) ? null : objectMapper.readValue(content, type);
    }

    public static String getFileMd5(File file, long offset, long length) throws IOException
    {
        String resultValue = null;
        int byteRead;
        byte[] buffer = new byte[256];
        RandomAccessFile raFile = null;
        try
        {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            raFile = new RandomAccessFile(file, "r");
            raFile.seek(offset);
            while ((byteRead = raFile.read(buffer)) != -1 && length > 0)
            {
                if (byteRead >= length)
                {
                    digest.update(buffer, 0, (int)length);
                    break;
                }
                else
                {
                    digest.update(buffer, 0, byteRead);
                    length -= byteRead;
                }
            }
            resultValue = toHexString(digest.digest());
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new IllegalStateException(e);
        }
        finally
        {
            if (raFile != null)
            {
                raFile.close();
            }
        }
        return resultValue.toLowerCase(Locale.getDefault());
    }

    public static void copyFile(File file, long offset, int length, OutputStream out) throws IOException
    {
        RandomAccessFile raf = null;
        try
        {
            raf = new RandomAccessFile(file, "r");
            raf.seek(offset);
            byte[] buff = new byte[1024];
            int remain = length;
            int read;
            while ((read = raf.read(buff, 0, remain < buff.length ? remain : buff.length)) != -1)
            {
                out.write(buff, 0, read);
                remain = remain - read;
                if (remain <= 0)
                {
                    break;
                }
            }
        }
        finally
        {
            if (raf != null)
            {
                raf.close();
            }
        }
    }

    public static String toJsonString(String[] md5s) throws IOException
    {
        return objectMapper.writeValueAsString(md5s);
    }

    public static abstract class Callback<T>
    {
        void callback(OutputStream out) throws IOException
        {
            //do nothing
        }

        protected abstract T callback(int code, InputStream body) throws IOException;
    }
}
