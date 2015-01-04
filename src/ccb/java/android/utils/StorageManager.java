package ccb.java.android.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;
import org.xmlpull.v1.XmlSerializer;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Environment;
import android.os.SystemClock;
import android.text.TextUtils;


public class StorageManager {

	private static StorageManager 	mInstance = null;
	private boolean isFirstOpen;
	private String 					mBasePath;
	private SaveData 				mSaveData;
	private static String APP_NAME = "";

	private static Context mContext;
	private int						mStatus; // 0:stop, 1:running

	
	private StorageManager() {
		
		mStatus = 1;

		initBasePath();

//		deleteDirectory(new File(getDownloadCacheDir()));
	}

	public static StorageManager instance() {
		if (mInstance == null || mInstance.mStatus == 0) {
			if(mInstance != null){
				mInstance = null;
			}
			mInstance = new StorageManager();
		}
		return mInstance;
	}
//	}

	public SharedPreferences getSPF(){
		SharedPreferences sp = mContext.getSharedPreferences(APP_NAME, Context.MODE_PRIVATE);
		return sp;
	}
	public boolean getIsFirstOpen(){
		return mInstance.isFirstOpen;
	}
	private boolean getIsFirstTimeLauch( ){
		boolean f = getSPF().getBoolean("FirstTimeTag", true);
		LogWorker.i("getIsFirstTimeLauch=" + f);
		return f;
	}
	private void setIsFirstTimeLauch(boolean f){
		LogWorker.i("setIsFirstTimeLauch=" + f);
		Editor edt = getSPF().edit();
		edt.putBoolean("FirstTimeTag", f);
		edt.commit();
	}
	public String getString(String key){
		String f = getSPF().getString(key, "");
		LogWorker.i(key + " = " + f);
		return f;
	}
	public boolean putString(String key,String v){
		LogWorker.i("putString " + key + " = " + key + " value = " + v);
		Editor edt = getSPF().edit();
		edt.putString(key, v);
		return edt.commit();
	}
	public int getInteger(String key){
		int f = getSPF().getInt(key, 0);
		LogWorker.i(key + " = " + f);
		return f;
	}
	public boolean putInteger(String key,int v){
		LogWorker.i("putInteger " + key + " = " + key + " value = " + v);
		Editor edt = getSPF().edit();
		edt.putInt(key, v);
		return edt.commit();
	}
	public boolean getBoolean(String key){
		boolean f = getSPF().getBoolean(key, false);
		LogWorker.i(key + " = " + f);
		return f;
	}
	public boolean putBoolean(String key,boolean v){
		LogWorker.i("putBoolean " + key + " = " + key + " value = " + v);
		Editor edt = getSPF().edit();
		edt.putBoolean(key, v);
		return edt.commit();
	}
	public boolean clearUserData(Context c){
		Editor e = getSPF().edit().clear();
		return e.commit();
	}

	public static void initStorage(Context c,String AppName) {
		
		APP_NAME = AppName;
		LogWorker.i("APP_NAME = " + APP_NAME);
		instance();
		mContext = c;

		mInstance.isFirstOpen = false;
		if (mInstance.getIsFirstTimeLauch()){
			mInstance.isFirstOpen = true;
			mInstance.setIsFirstTimeLauch(false);
		}
//		s.mAppcation = app;
//		s.deleteDirectory(new File(s.getImageCacheDir()));
	}
	
	public static void destory() {
		
		instance().mStatus = 0;
	}
	/**
	 * city hotel list
	 */
	static public String convertStreamToString(InputStream is){
		InputStreamReader reader = new InputStreamReader(is);
		StringBuilder sb = new StringBuilder();
		char []buff = new char[100*1024];
		int ret;
		try {
			while((ret=reader.read(buff)) != -1){
				sb.append(buff, 0, ret);
			}
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return sb.toString();
	}

	/**
	 * setDownLoadCache create temporary file
	 * 
	 * @param id
	 *            : HttpDownload task id
	 * @return null if error else cache file
	 */
	public File getDownLoadCache(String name) {
		return new File(getDownloadCacheDir(), name);
	}

	public File setDownLoadCache(int id, InputStream is) {
		File tmp = new File(getDownloadCacheDir(), String.valueOf(id) + ".tmp");
		try {
			if (!tmp.exists()) {
				tmp.createNewFile();
				tmp.deleteOnExit();
			}

			byte[] data = new byte[1024 * 10];
			OutputStream os = new FileOutputStream(tmp);
			int len;
			while ((len = is.read(data, 0, data.length)) != -1) {
				os.write(data, 0, len);
			}
			os.close();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		return tmp;
	}

	public InputStream getDownLoadCache(int id) {
		File tmp = new File(getDownloadCacheDir(), String.valueOf(id) + ".tmp");
		InputStream is = null;
		try {
			is = new FileInputStream(tmp);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		return is;
	}

	/**
	 * Image cache I/O
	 * 
	 * @param imgURL
	 *            image URL
	 * @return null if no cache
	 */
	// TODO 
	// catch the out of memory exception
	public Bitmap getImageCache(String imgURL, int width, int height) {
		File img = getImageCachePath(imgURL);
		if (img == null) {
			return null;
		}

		Bitmap ret = null;
		try {
			if (img.exists()) {
				ret = BitmapFactory.decodeFile(img.getPath());
				if(ret != null && width!= -1 && height != -1){
					return Bitmap.createScaledBitmap(ret, width, height, false);
				}
			}
		} catch (Exception e){
			img.delete();
			return null;
		}
		return ret;
	}

	public boolean setImageCache(String imgURL, Bitmap bm) {
		if (bm == null)return false;
		File img = getImageCachePath(imgURL);
		if (img != null) {
				try {
					bm.compress(CompressFormat.PNG, 100, new FileOutputStream(
							img));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					return false;
				}
			return true;
		}
		return false;
	}
	
	public boolean setImageCache(String imgURL,  InputStream is) {
		File img = getImageCachePath(imgURL);
		if (img != null) {
			if (!img.exists()) {
				return setFile(img, is, false);
			}
			return true;
		}
		return false;
	}

	private File getImageCachePath(String imagURL) {
		if (imagURL != null && imagURL.trim().length() > 0) {
			File path = new File(getImageCacheDir(), imagURL.replaceAll(
					"\\/|\\:|@|\\?|&|=", "_")+"_");
			return path;
		} else
			return null;
	}
	public String getImgCacheFullPath(String imgUrlName){
		File f = getImageCachePath(imgUrlName);
		if (f == null){
			return "";
			
		} else {
			return f.getPath();
		}
	}
	//set SearchHistoy
	public void setSearchHistoy(List<String> list,int nums){
		if (list == null)return ;
		File file = new File(getCachePath(),"search_histoy");
		BufferedWriter bw = null;
		int size = list.size();
		int index = 0;
		if (size>nums)index = size-nums;
		if (file.exists()){
			file.delete();
		}
		try {
			file.createNewFile();
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
			for (int i=index; i<size; i++){
				LogWorker.i("setSearchHistoy:"+list.get(i));
				bw.write((list.get(i)+"\n"));
			}
			bw.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			if (bw != null){
				try {
					bw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	public List<String> getSearchHistoy(){
		List<String> list = new ArrayList<String>();
		File file = new File(getCachePath(),"search_histoy");
		if (file != null && file.exists()){
			String tmp = "";
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
				while( (tmp = reader.readLine())!= null){
					LogWorker.i("getSearchHistoy:"+tmp);
					list.add(tmp);
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return list;
	}
	/**
	 * 
	 * @param path
	 * @return
	 */
	public boolean deleteDirectory(File path) {
		if (path.exists()) {
			File[] files = path.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
					deleteDirectory(files[i]);
				} else {
					files[i].delete();
				}
			}
		}
		return (path.delete());
	}

	// ************************************************************
	// Private helper function
	// ************************************************************
	private InputStream getFile(File f) {
		if(!f.exists()){
			return null;
		}
		
		try {
			return readFile(f);
		} catch (Exception e) {
			LogWorker.printException(e);
		}

		return null;
	}

	private boolean setFile(File f, InputStream is, boolean append) {
		if (f.exists()) {
			String name = f.getName();
			String path = f.getPath();
			File tmp = new File(path + name + "_tmp");
			if (tmp.exists()) {
				tmp.delete();
			}

			try {
				writeFile(tmp, is, append);
			} catch (IOException e) {
				LogWorker.printException(e);
				return false;
			}

			if (!f.delete()) {
				tmp.delete();
				return false;
			} else {
				if (!tmp.renameTo(f)) {
					tmp.delete();
					return false;
				}
			}

		} else {
			try {
				f.createNewFile();
				writeFile(f, is, false);

			} catch (IOException e) {
				LogWorker.printException(e);
				return false;
			}
		}
		return true;
	}

	private InputStream readFile(File dest) throws IOException {
		FileInputStream reader = null;
		reader = new FileInputStream(dest);
		return reader;
	}

	private void writeFile(File dest, InputStream is, boolean append)
			throws IOException {
		FileOutputStream writer = null;
		writer = new FileOutputStream(dest, append);
		byte[] buffer = new byte[1024 * 10];
		int byteCount = 0;
		while ((byteCount = is.read(buffer, 0, buffer.length)) >= 0) {
			writer.write(buffer, 0, byteCount);
		}
		writer.flush();
		writer.close();
	}

	private String initRootPath() {

		String path = getSDCardPath(APP_NAME);
		if (path == null) {
			path = getInternalBaseDataPath("data/" + APP_NAME);
		}
		return path;
	}
	
	 public String getAPPCachePath(){
		return mInstance.getCachePath();
	}

	private void initBasePath() {

		mBasePath = initRootPath();
		if (mBasePath != null) {
			getDataPath();
			getCachePath();
			getImageCacheDir();
		} else {
			// TODO handle exception
		}
		LogWorker.i("mBasePath:"+mBasePath);
	}

	private String getBasePath(){
		if(mBasePath == null){
			initBasePath();
		}
		
		assert mBasePath != null;
		
		return mBasePath;
	}
	private String getDataPath() {
		File f = new File(getBasePath() + "/data");
		if (!f.exists()) {
			f.mkdirs();
		}
		LogWorker.i("DataPath:" + f.getAbsolutePath());
		return f.getPath();
	}

	private String getCachePath() {
		File f = new File(getBasePath() + "/data" + "/cache");
		if (!f.exists()) {
			f.mkdirs();
		}
		LogWorker.i("CachePath:" + f.getAbsolutePath());
		return f.getPath();
	}
	public String getDebugPath() {
		File f = new File(getBasePath() + "/data" + "/debugfile");
		if (!f.exists()) {
			f.mkdirs();
		}
		LogWorker.i("DebugPath:" + f.getAbsolutePath());
		return f.getPath();
	}
	private String getDownloadCacheDir() {
		File f = new File(getBasePath() + "/data" + "/download");
		if (!f.exists()) {
			f.mkdirs();
		}
		return f.getPath();
	}

	private String getImageCacheDir() {
		File f = new File(getBasePath() + "/data" + "/imagecache");
		if (!f.exists()) {
			f.mkdirs();
		}
		return f.getPath();
	}

	private static String getSDCardPath(String path) {

		File sdPath = new File(Environment.getExternalStorageDirectory(), path);
		sdPath.deleteOnExit();
		LogWorker.i("getExternalStorageDirectory:"+sdPath.getAbsolutePath());
		if (!sdPath.exists()) {
			LogWorker.i(" not exitst sdPath:"+sdPath.getAbsolutePath());
			if (!sdPath.mkdirs()){
				LogWorker.i(" sdPath mkdir error:"+sdPath.getAbsolutePath());
				return null;
			}

		}

		return sdPath.getPath();
	}

	public String getInternalBaseDataPath(String path) {

		File internalPath = new File(Environment.getDataDirectory(), path);
		internalPath.deleteOnExit();
		LogWorker.i("getDataDirectory:"+internalPath.getAbsolutePath());
		if (!internalPath.exists()) {
			LogWorker.i(" not exitst sdPath:"+internalPath.getAbsolutePath());
			if (!internalPath.mkdirs()) {
				LogWorker.i("  internalPath mkdirs error:"+internalPath.getAbsolutePath());
				return null;
			}
		}
		return internalPath.getPath();
	}

	private String getFavorDir() {
		File f = new File(getBasePath() + "/data" + "/favor");
		if (!f.exists()) {
			f.mkdirs();
		}
		return f.getPath();
	}


	// ******************************************
	// SaveData
	// ******************************************
	public class SaveDataElement {
		private String mTag;
		private Map<String, String> mAttrs = new HashMap<String, String>();

		public SaveDataElement(String tag, String[] keys, String[] value) {
			mTag = tag;
			for (int i = 0; i < keys.length; i++) {
				mAttrs.put(keys[i], value[i]);
			}
		}

		boolean setAttribute(String attr, String value) {
			if (mAttrs.containsKey(attr)) {
				mAttrs.put(attr, value);
				return true;
			}
			return false;
		}

		String getAttribute(String attr) {
			if (mAttrs.containsKey(attr)) {
				return mAttrs.get(attr);
			}
			return null;
		}

		public void writeTo(XmlSerializer serializer) throws Exception {
			Iterator<String> it = mAttrs.keySet().iterator();
			serializer.startTag("", mTag);
			while (it.hasNext()) {
				String key = it.next();
				if (mAttrs.get(key) != null) {
					serializer.attribute("", key, mAttrs.get(key));
				}
			}
			serializer.endTag("", mTag);
		}

		public boolean readFrom(String tag, Attributes attrs) {
			if (!mTag.equals(tag)) {
				return false;
			}
			Iterator<String> it = mAttrs.keySet().iterator();
			while (it.hasNext()) {
				String key = it.next();
				mAttrs.put(key, attrs.getValue(key));
			}
			return true;
		}

		public String getTag() {
			return mTag;
		}

	};

	public class SaveData extends DefaultHandler {
		public static final String TAG_SUPPORT_CITY_VERSION = "SupportCityVersion";
		public static final String SUPPORT_CITY_ATTR_VERSION = "version";

		public static final String TAG_UID = "uid";
		public static final String UID = "uid";

		Map<String, SaveDataElement> mElements = new HashMap<String, SaveDataElement>();

		public SaveData() {
			super();
		}

		public boolean setElement(SaveDataElement element) {
			if (element.getTag() == null) {
				return false;
			}

			mElements.put(element.getTag(), element);
			return true;
		}

		public SaveDataElement getElementByTag(String tag) {
			if (mElements.containsKey(tag)) {
				return mElements.get(tag);
			}
			return null;
		}

		public void getElementsAsXml(XmlSerializer serializer) throws Exception {
			Iterator<String> it = mElements.keySet().iterator();
			while (it.hasNext()) {
				String key = it.next();
				mElements.get(key).writeTo(serializer);
			}
		}

		public void startElement(String uri, String localName, String name,
				Attributes attributes) {
			if (mElements.containsKey(localName)) {
				mElements.get(localName).readFrom(localName, attributes);
			}
		}
	};


	private static void saveBmpToFile(File file, Bitmap poster)
			throws IOException, FileNotFoundException {
		OutputStream stream;
		// if (!new File(path).createNewFile())
		// Log.e(MovieFinderActivity.LOG_TAG, "Cannot create file:" + path);
		stream = new FileOutputStream(file);
		poster.compress(CompressFormat.PNG, 100, stream);
	}

	private static void pipe(InputStream input, OutputStream output)
			throws IOException {
		byte[] buffer = new byte[1024];
		int n;
		while ((n = input.read(buffer)) > 0) {
			output.write(buffer, 0, n);
		}
	}

	/*
	 * this method is expensive..
	 */
	private static Bitmap resizeBmp(Bitmap bmp, int newW, int newH) {
		if (bmp == null)
			return null;

		// createa matrix for the manipulation
		Matrix matrix = new Matrix();
		int w = bmp.getWidth(), h = bmp.getHeight();
		float scaleWidth = ((float) newW) / w;
		float scaleHeight = ((float) newH) / h;

		// resize the bit map
		matrix.postScale(scaleWidth, scaleHeight);

		// recreate the new Bitmap
		Bitmap resizedBitmap = Bitmap.createBitmap(bmp, 0, 0, w, h, matrix,
				true);
		return resizedBitmap;
	}

	 public static void wipeOldDataOnSDCard() {
//	 File dir = getSDCardPath(APP_NAME);
//	 deleteDirectory(dir);
	 }


}
