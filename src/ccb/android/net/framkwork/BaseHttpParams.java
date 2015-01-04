package ccb.android.net.framkwork;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.message.BasicNameValuePair;

import ccb.java.android.utils.LogWorker;
import ccb.java.android.utils.StringUtil;

public class BaseHttpParams {

	Map<String, String> mParams;
	private String mURL ;
	public BaseHttpParams ( String url){
		mParams = new HashMap<String, String>();
//		addParams("IMEI", EattingApplication.IMEI);
		mURL = url;
	}
	
	public HttpUriRequest getHttpUriRequest (){
		if ( mURL == null || !StringUtil.isValidURL(mURL)){
			return null;
		}
		if ( mParams.size() == 0 ){
			return new HttpGet(mURL);
		} else {
			HttpPost post = new HttpPost( mURL );

			post.addHeader("Content-Type", "text/plain");
			List<NameValuePair> lp = new ArrayList<NameValuePair>();
			for ( Entry<String, String> p : mParams.entrySet()){
				lp.add(new BasicNameValuePair(p.getKey(), p.getValue()));
			}
			try {
				post.setEntity( new UrlEncodedFormEntity(lp,"UTF-8"));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			LogWorker.i("HttpURL:" + post.getURI());
			return post;
		}
	}
	public HttpUriRequest getURLRequest(){
		if ( mURL == null || !StringUtil.isValidURL(mURL)){
			return null;
		}
		StringBuilder sb = new StringBuilder();
		try {
			if ( mParams.size() == 0){
				return new HttpGet(mURL);
			} else {
				sb.append(mURL).append("?");
			}
			for ( Entry<String, String> p : mParams.entrySet()){
				sb.append(p.getKey()).append("=").append(p.getValue()).append("&");
			}
			String rs = sb.toString().substring(0, sb.lastIndexOf("&"));
			LogWorker.i("HttpURL" + rs);
			return new HttpPost(rs);
		}catch (Exception e) {
			LogWorker.i("getURLRequest error of url" + mURL);
			e.printStackTrace();
		}
		return null;
	}
	public void addParams( String k, String v){
		mParams.put(k, v);
	}
}
