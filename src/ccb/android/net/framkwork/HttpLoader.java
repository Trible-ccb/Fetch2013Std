package ccb.android.net.framkwork;

import java.io.IOException;
import java.io.InputStream;

import junit.framework.Assert;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;

import ccb.java.android.utils.LogWorker;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

public  class HttpLoader extends Thread{

	private static final String LOG_TAG = HttpLoader.class.getSimpleName();
	public static final String DOWLOAD_SUCCESS = "success";
	public static final String DOWLOAD_ERROR = "error";
	protected static Context mContext;
	private Handler mHandler;
	private static HttpLoader mLoader;
	private static int mDownLoadId;
	private int mCancelID;
	String message;
	
	private HttpLoader ( Context c ){
		mContext = c;
		mDownLoadId = 0;
		mCancelID = -1;
		setPriority(Thread.NORM_PRIORITY);
		setName(LOG_TAG);
		start();
		
	}
	public static void initLoader ( Context c){
		if ( mLoader != null){
			mLoader.interrupt();
			mLoader = null;
		} 
		mLoader = new HttpLoader(c);
	}
	public static void destoryLoader () {
		if ( mLoader != null && mLoader.mHandler != null){
			if ( mLoader.mHandler.getLooper() != null){
				mLoader.mHandler.getLooper().quit();
			}
		}
	}
	public static HttpLoader getInstance (Context c){
		if (mLoader == null){
			initLoader(c);
		}
		return mLoader;
	}
	public void downloadTask ( Task t ){
		String ret = null;
		ret = doDownload(t);
		if (t.mListener != null ){
			t.mListener.onDownLoadDone( t.taskId , ret);
		}
	}
	public String doDownload( Task task){

		HttpClient client = new DefaultHttpClient();
		HttpUriRequest request = null;
		HttpResponse response = null;
		InputStream is = null;
		Assert.assertNotNull(task);
		try {
			if ( task.mCacheHelper != null){
				is = task.mCacheHelper.getCache(task);
				LogWorker.i(LOG_TAG+" getCache:" + is);
			}
			if ( is == null){
				
				if ( !isConnectInternet() ){
					return DOWLOAD_ERROR;
				}
				if ( task != null && task.mParams != null){
					request = task.mParams.getURLRequest();
				}
				if ( request == null){
					return DOWLOAD_ERROR;
				}
				client.getParams().setIntParameter(HttpConnectionParams.CONNECTION_TIMEOUT, 7000);
				client.getParams().setIntParameter(HttpConnectionParams.SO_TIMEOUT, 10000 * 6);
				response = client.execute(request);
				int ret = response.getStatusLine().getStatusCode();
				LogWorker.i(LOG_TAG + " response status:" + ret);
				if ( ret == HttpStatus.SC_OK){
					is = response.getEntity().getContent();
				} else {
					return DOWLOAD_ERROR;
				}	
			}
				LogWorker.i(LOG_TAG + " content " + is.getClass());
				if ( task.mCacheHelper != null){
					LogWorker.i(LOG_TAG + " cache content..." );
					boolean flg = task.mCacheHelper.onStoreDataInCache(task,is);
					InputStream newIs = null;
					if (flg){
						newIs = task.mCacheHelper.getCache(task);
					}
					if ( task.mParser != null){
						LogWorker.i(LOG_TAG + " paser file content...");
						return (task.mParser.onDataParser(task, newIs) ? DOWLOAD_SUCCESS:DOWLOAD_ERROR);
					} else {
						return DOWLOAD_SUCCESS;
					}
				} else {
					if ( task.mParser != null){
						LogWorker.i(LOG_TAG + " paser stream content...");
						return (task.mParser.onDataParser(task, is) ? DOWLOAD_SUCCESS:DOWLOAD_ERROR);
					}
				}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			LogWorker.i(LOG_TAG + "requset:" + e.getMessage() + " error.");
			e.printStackTrace();
		} finally{
			client.getConnectionManager().shutdown();
		}
		
		return DOWLOAD_ERROR;
	}
	public class Task {
		int taskId ;
		String storeCachePath;

		BaseHttpParams mParams;
		CacheHelper mCacheHelper;
		downLoadListener mListener;
		dataParser mParser;
		private Task ( BaseHttpParams p){
			taskId = ++mDownLoadId;
			this.mParams = p;
		}
		private Task( BaseHttpParams p , downLoadListener l ,
				dataParser parser , CacheHelper cacheHelper){
			this(p);
			mListener = l;
			mParser = parser;
			mCacheHelper = cacheHelper;

		}
		public String getStoreCachePath() {
			return storeCachePath;
		}
		public void setStoreCachePath(String storeCachePath) {
			this.storeCachePath = storeCachePath;
		}
		public int getTaskId (){
			return taskId;
		}
		public void setDownLoadDoneListener ( downLoadListener l){
			this.mListener = l;
		}
		public void setDataParser( dataParser p){
			this.mParser = p;
		}
		public void setCacheHelper ( CacheHelper cacheHelper){
			this.mCacheHelper = cacheHelper;
		}
	}
	public Task getTask (BaseHttpParams p){
		return new Task(p);
	}
	public Task getTask (BaseHttpParams p , downLoadListener l ,
			dataParser parser , CacheHelper h){
		return new Task(p, l, parser,h);
		
	}
	public int runTask ( Task t ){
		if ( mHandler == null) return -1;
		mHandler.sendMessage(Message.obtain(mHandler, t.taskId ,t));
		return t.taskId;
	}
	public void delTask ( int id){
		if ( mHandler.hasMessages(id) ){
			mHandler.removeMessages(id);
		} else {
			mCancelID = id;
		}
	}
	public boolean isConnectInternet() {

		ConnectivityManager conManager = 
			(ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = conManager.getActiveNetworkInfo();
		if (networkInfo != null) {
			return networkInfo.isAvailable();
		}
		return false;
	}
	public interface downLoadListener {
		public void onDownLoadDone( int taskId , String result);
	}
	public interface dataParser {
		public boolean onDataParser ( Task t,  InputStream is );
	}
	public interface CacheHelper {
		public boolean onStoreDataInCache ( Task t , InputStream is);
		public InputStream getCache (Task t);
	}

	@Override
	public void run() {
		Looper.prepare();
		mHandler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				downloadTask(( Task ) msg.obj);
				LogWorker.i(LOG_TAG + " handlering task:" + ((Task ) msg.obj).taskId);
			}
			
		};
		Looper.loop();
	}
	
}
