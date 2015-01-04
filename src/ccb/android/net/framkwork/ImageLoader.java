package ccb.android.net.framkwork;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;


import ccb.android.net.framkwork.HttpLoader.CacheHelper;
import ccb.android.net.framkwork.HttpLoader.Task;
import ccb.android.net.framkwork.HttpLoader.dataParser;
import ccb.android.net.framkwork.HttpLoader.downLoadListener;
import ccb.java.android.utils.GraphicTool;
import ccb.java.android.utils.LogWorker;
import ccb.java.android.utils.StorageManager;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;




public class ImageLoader implements dataParser,
	downLoadListener
	{
	Map<Integer , String> mUrl;
	ImageLoaderListener mListener;
	Handler mHandler;
	Context mContext;
	public interface ImageLoaderListener{
		public void imgDidLoad(String imgUrl);
	}
	
	public ImageLoader(Context c) {
		mContext = c;
		mUrl = new HashMap<Integer, String>();
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if (msg.what == 0) {
					String url = (String)msg.obj;
					if (mListener != null) {
						mListener.imgDidLoad(url);
					}
				}
			}
		};
	}
	
	public void fetch(String url) {
		
		if (url!=null && url.length()>0 && !mUrl.containsValue(url)) {
			LogWorker.i("getImgurl:"+url);
			
			Task getImgTask = HttpLoader.getInstance(mContext).getTask(new ImageHttpParams(url));
			getImgTask.setDownLoadDoneListener(this);
			getImgTask.setDataParser(this);
			int id = getImgTask.getTaskId();
			
			mUrl.put(Integer.valueOf(id), url);
			HttpLoader.getInstance(mContext).runTask(getImgTask);
		}
	}
	
	public void cancelAll(){
		Integer[] ids =(Integer [])( mUrl.keySet().toArray(new Integer[1]));
		int i=0;
		for( ; i<ids.length; ++i){
			cancel(mUrl.get(ids[i]));
		}
	}
	
	public void cancel(String url){
		if(mUrl.containsValue(url)){
			Integer[] ids =(Integer [])( mUrl.keySet().toArray(new Integer[1]));
			int i=0;
			for( ; i<ids.length; ++i){
				if(url.equals(mUrl.get(ids[i]))){
					break;
				}
			}
			mUrl.remove(ids[i]);
			HttpLoader.getInstance(mContext).delTask(ids[i]);
		}
	}
	
	public void setListener(ImageLoaderListener listener){
		mListener = listener;
	}

	@Override
	public void onDownLoadDone(int id, String result) {
		String url = mUrl.remove(id);
		if(url == null){
			return;
		}
		if(result.equals(HttpLoader.DOWLOAD_SUCCESS)){
			LogWorker.i("imageloader::result suc");
			mHandler.sendMessage(mHandler.obtainMessage(0, url));
		}else{
			LogWorker.i("imageloader::result failed");
			mHandler.sendMessage(mHandler.obtainMessage(0, null));
		}
		
	}
	@Override
	public boolean onDataParser(Task t, InputStream is) {
		Bitmap bm = BitmapFactory.decodeStream(is);
		if (scaleByScreenWidthPercentage != -1){
			bm = GraphicTool.getScaleImageByScaleOfWinWidth(mContext, bm, scaleByScreenWidthPercentage);
		}
		if(!StorageManager.instance().setImageCache(mUrl.get(t.getTaskId()), bm)){
			LogWorker.i("imageloader::setLocalCache failed");
			return false;
		}
		return true;
	}
	public static Bitmap getImageCacheByUrl(String url){
		LogWorker.i(" getImageCacheByUrl= " + url);
		Bitmap bm = StorageManager.instance().getImageCache(url, -1, -1);
		return bm;
	}
	float scaleByScreenWidthPercentage = -1;//when do not want to scale,and set it -1 then to fetch
	public void setScaleByScreenWidthPercentage(float s){
		scaleByScreenWidthPercentage = s;
	}
}
