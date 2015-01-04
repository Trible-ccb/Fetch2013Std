package ccb.test.fetch2013std;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ccb.android.net.framkwork.BaseHttpParams;
import ccb.android.net.framkwork.HttpLoader;
import ccb.android.net.framkwork.HttpLoader.Task;
import ccb.android.net.framkwork.HttpLoader.dataParser;
import ccb.android.net.framkwork.HttpLoader.downLoadListener;
import ccb.android.net.framkwork.SimpleAsynHttpDowload;
import ccb.java.android.utils.FileUtils;
import ccb.java.android.utils.LogWorker;
import ccb.java.android.utils.StorageManager;
import android.os.Bundle;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.res.AssetManager;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity implements OnClickListener{

	Button btnShahe,btnQxh;
	String shFile = "shahe2013std.txt";
	String qxhFile = "qingshuihe2013std.txt";
	public static String InfoURL = "http://gr.uestc.edu.cn/welcome/queryRegistrationState.shtml";
	Pattern bianhaoRule = Pattern.compile("^[0-9]{3}[\u4E00-\u9FA5]+$");
	Pattern zhuanyeRule = Pattern.compile("^10614[0-9]{10}$");
	Pattern sfzRule = Pattern.compile("^[0-9]{6}[\u4E00-\u9FA5]+$");
	Pattern leibieRule = Pattern.compile("^[0-9]{2}[\u4E00-\u9FA5]+$");
//	Pattern kuazhuanyeRule = Pattern.compile("");
	Pattern daoshiRule = Pattern.compile("^[0-9]{5}[\u4E00-\u9FA5]+$");
//	Pattern jxjRule = Pattern.compile("");
	Pattern zhaopianRule = Pattern.compile("^photo[0-9A-Za-z._/]+.jpg$");
//	Pattern stdInfoRule = Pattern.compile("^([0-9]{3}[\u4E00-\u9FA5]+)</span></li>" +
//			"<li><span class=\"leftpart\">考生编号：</span>([0-9]{15})</li>" +
//					"<li><span class=\"leftpart\">录取专业：</span><span class=\"rightpart\">([0-9]{6}[\u4E00-\u9FA5]+)</span></li>" +
//							"<li><span class=\"leftpart\">身份证号：</span>([0-9]{18}|[0-9]{15}|[0-9]{17}x)</li>" +
//									"<li><span class=\"leftpart\">录取类别：</span>([0-9]{2}[\u4E00-\u9FA5]+)</li>" +
//											"<li><span class=\"leftpart\">跨专业：</span>([\u4E00-\u9FA5]+)</li>" +
//													"<li><span class=\"leftpart\">录取导师：</span>([0-9]{5}[\u4E00-\u9FA5]+)</li>" +
//															"<li><span class=\"leftpart\">奖学金：</span>([\u4E00-\u9FA5]+)</li></ul>" +
//																	"<div class=\"photo\">" +
//																			"<img src=\'(photo[0-9A-Za-z._/]+.jpg)' " +
//																			"onload=\"javascript:if((this.width!=75)&&(this.height!=100)){(this.width=75)&&(this.height=100);}\"/></div>$");
	Pattern stdInfoRule2 = Pattern.compile("^>" +
			"([0-9]{3}[\u4E00-\u9FA5]+).*" +//学院
			"([0-9]{15}).*" +//编号
			"([0-9]{6}[\u4E00-\u9FA5]+).*" +//专业
			"([0-9]{18}|[0-9]{15}|[0-9]{17}x).*" +//身份证
			"([0-9]{2}[\u4E00-\u9FA5]+).*" +//类别
			"([\u4E00-\u9FA5]+).*" +//是否夸专业
			"([0-9]{5}[\u4E00-\u9FA5]+).*" +//导师
			"([\u4E00-\u9FA5]+).*" +//奖学金
			"(photo[0-9A-Za-z._/]+.jpg).*$");//照片
	ArrayList<String> typeList = new ArrayList<String>();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		LogWorker.initLogWorker("fetch2013std", true);
		StorageManager.initStorage(this, "fetch2013std");
		HttpLoader.initLoader(this);
		
		btnShahe = (Button) findViewById(R.id.btn_shahe_fetch);
		btnQxh = (Button) findViewById(R.id.btn_qxh_fetch);
		btnQxh.setOnClickListener(this);
		btnShahe.setOnClickListener(this);
	}
	void initTypeList(){
		typeList.add("学院");
		typeList.add("编号");
		typeList.add("专业");
		typeList.add("身份证");
		typeList.add("是否夸专业");
		typeList.add("导师");
		typeList.add("奖学金");
		typeList.add("照片");
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	Task fetchStdInfoTask;
	void prepareFetchTask(final String xm,final String hm){
		fetchStdInfoTask = HttpLoader.getInstance(this).getTask(new StdInfoParams(InfoURL,xm,hm));
		
		fetchStdInfoTask.setDataParser(new dataParser() {
			
			@Override
			public boolean onDataParser(Task t, InputStream fis) {
				File f = StorageManager.instance().setDownLoadCache(Integer.valueOf(hm.substring(4)), fis);
				return f == null ? false : true;
			}
		});
		fetchStdInfoTask.setDownLoadDoneListener(new downLoadListener() {
			
			@Override
			public void onDownLoadDone(int taskId, String result) {
				if ( result.equals(HttpLoader.DOWLOAD_SUCCESS )){
					LogWorker.showToast(MainActivity.this,"SUCCESS:" + taskId);
					LogWorker.i("SUCCESS:" + taskId);
				} else {
					LogWorker.showToast(MainActivity.this,"ERROR:" + taskId);
					LogWorker.i("ERROR:" + taskId);
				}
			}
		});

	}
	void startFetchInfo(String xm,String hm){
		prepareFetchTask(xm, hm);
		
		SimpleAsynHttpDowload load = new SimpleAsynHttpDowload(HttpLoader.getInstance(this));
		load.execute(fetchStdInfoTask);
	}
	void loadInfoAsset(String f ){
		String filename = f;
		AssetManager assetMng = getAssets();
		BufferedReader bis = null;
		try {
			bis = new BufferedReader(new InputStreamReader(assetMng.open(filename)));
			String line = null;
			while ( (line = bis.readLine()) != null){
				String[] args = line.split(" ");
				LogWorker.i(args.toString());
				if ( args == null || args.length < 3)continue;
				String xm = args[1];
				String hm = args[2];
				startFetchInfo(xm, hm);
			}
		} catch (IOException e) {
			
			e.printStackTrace();
		}finally{
			
			try {
				if ( bis != null )bis.close();
			} catch (IOException e) {
				
				e.printStackTrace();
			}
		}
	}
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_shahe_fetch:
			loadInfoAsset(shFile);
			break;
		case R.id.btn_qxh_fetch:
			loadInfoAsset(qxhFile);
			break;
		default:
			break;
		}
	}
	class StdInfoParams extends BaseHttpParams{

		public StdInfoParams(String url,String xm,String hm) {
			super(url);
			addParams("xm", xm);
			addParams("hm", hm);
		}
		
	}
}
