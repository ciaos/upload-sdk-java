Upload SDK JAVA
=====================
华为云加速上传JAVA SDK使用指南
* * *

A . 概述
-----------
该 SDK 适用于 JDK6 及其以上版本，便于开发者快速上传文件。依赖包（commons-logging，jackson-core-asl，jackson-mapper-asl）

B . 使用示例
----------
### B1 . 在项目中添加对upload-sdk.jar等包依赖 ###


### B2 . 配置APP相关信息并上传文件 ###

```java
package test;

import java.io.File;
import java.io.IOException;

import com.dbank.speedup.upload.*;

public class UploadTest {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		String appId = "appid";
		String secret = "appsecret";
		String appName = "appname";
		File file = new File("E://chat.rar");
		String path = "/dl/"+appName+"/chat.rar";
//		String callbackUrl = "http://www.yourhost.com/callback";
//		String callbackStatus = "200";
		HuaweiDbankCloud huaweiDbankCloud = new HuaweiDbankCloud(appId, appName,secret); 
		huaweiDbankCloud.upload(path, file, null, null);

	}
}
```

如果设置了回调函数和状态码，则上传完毕后服务器端会访问此url通知上传完毕。

交互流程如下

1. 客户端(服务器)直接上传至 距自己最近速度最快的云存储服务器
2. 上传成功后客户端进行后续业务操作

![](http://zl.hwpan.com/u12134807/demo1.png)
