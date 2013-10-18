package com.dbank.speedup.upload;

import java.io.IOException;
import java.util.Map;

/**
 * <一句话功能简述>
 * <功能详细描述>
 *
 * @author c57771
 * @version [版本号, 13-9-29]
 * @see  [相关类/方法]
 * @since [产品/模块版本]
 */
public interface HostProvider
{
    /**
     * 获取最佳上传服务器的地址
     * @return ip地址
     */
    String getUploadHost(String appId,String secret, Map<String, String> params) throws IOException;
}
