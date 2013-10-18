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

