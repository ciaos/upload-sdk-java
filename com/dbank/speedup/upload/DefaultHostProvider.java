package com.dbank.speedup.upload;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <一句话功能简述>
 * <功能详细描述>
 *
 * @author c57771
 * @version [版本号, 13-9-29]
 * @see  [相关类/方法]
 * @since [产品/模块版本]
 */
public class DefaultHostProvider implements HostProvider
{

    private static final Log log = LogFactory.getLog(DefaultHostProvider.class);

    private static final String DBANK_API_URL = "http://api.dbank.com/rest.php";

    public String getUploadHost(String appId, String secret, Map<String, String> params) throws IOException
    {
        TreeMap<String, String> mergedParams = new TreeMap<String, String>();
        mergedParams.put("nsp_app", appId);
        mergedParams.put("nsp_fmt", "JSON");
        mergedParams.put("nsp_ts", String.valueOf(System.currentTimeMillis() / 1000));
        mergedParams.put("nsp_ver", "1.0");
        mergedParams.put("nsp_svc", "nsp.ping.getupsrvip");

        if (params != null)
        {
            for (Map.Entry<String, String> entry : params.entrySet())
            {
                if (mergedParams.containsKey(entry.getKey()))
                {
                    throw new IllegalArgumentException("param name " + entry.getKey() + " is reserved.");
                }
                mergedParams.put(entry.getKey(), entry.getValue());
            }
        }

        mergedParams.put("nsp_key", generateNspKey(secret, mergedParams));

        final String postData = generatePostData(mergedParams);
        if (log.isDebugEnabled())
        {
            log.debug("get upload host,data=" + postData);
        }

        return Utils.httpPost(DBANK_API_URL, new Utils.Callback<String>()
        {
            @Override
            public void callback(OutputStream out) throws IOException
            {
                out.write(postData.getBytes(Utils.ENCODING));
            }

            @Override
            public String callback(int code, InputStream body) throws IOException
            {
                String content = Utils.toString(body);
                if (log.isDebugEnabled())
                {
                    log.debug("get upload host code=" + code + ",body=" + content);
                }
                if (code != 200)
                {
                    throw new IOException("can't get upload host,code=" + code + ",body=" + content);
                }
                Map<?, ?> result = Utils.toMap(content);
                String ip = (String)result.get("ip");
                if (ip == null || ip.isEmpty())
                {
                    throw new IOException("can't get upload host,code=" + code + ",body=" + content);
                }
                return ip;
            }
        });
    }

    private String generatePostData(TreeMap<String, String> mergedParams)
    {
        StringBuilder data = new StringBuilder(128);
        for (Map.Entry<String, String> entry : mergedParams.entrySet())
        {
            try
            {
                data.append(URLEncoder.encode(entry.getKey(), Utils.ENCODING))
                        .append('=')
                        .append(URLEncoder.encode(entry.getValue(), Utils.ENCODING))
                        .append('&');
            }
            catch (UnsupportedEncodingException e)
            {
                //never happen
                throw new IllegalStateException(e);
            }
        }
        data.deleteCharAt(data.length() - 1);
        return data.toString();
    }

    private String generateNspKey(String secret, TreeMap<String, String> mergedParams)
    {
        StringBuilder keyBuilder = new StringBuilder(64);
        keyBuilder.append(secret);
        for (Map.Entry<String, String> entry : mergedParams.entrySet())
        {
            keyBuilder.append(entry.getKey())
                    .append(entry.getValue());
        }
        return Utils.tomd5(keyBuilder.toString()).toUpperCase(Locale.getDefault());
    }
}
