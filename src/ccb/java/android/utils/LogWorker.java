package ccb.java.android.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

public class LogWorker{
	static File logFile;
	static FileOutputStream os;
	static boolean DEBUG = true;
	static String TAG = "";
	public static void initLogWorker(String tag,boolean isdebug){
		TAG = tag;
		DEBUG = isdebug;
	}
	static File getLogFile(){
		if(logFile == null){
			logFile = new File(Environment.getExternalStorageDirectory(), 
					"myLog.log");
			if(!logFile.exists()){
				try {
					logFile.createNewFile();
					os = new FileOutputStream(logFile, true);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					os = null;
				}
			}else{
				try {
					os = new FileOutputStream(logFile, true);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					os = null;
				}
			}
				
		}
		return logFile;
	}
	
	public static void writeToFile(String s){
		if(os == null){
			getLogFile();
			if(os == null)
			return;
		}
		
		try {
			os.write(s.getBytes());
			os.flush();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	public static void printException(Exception e)
	{
		e.printStackTrace();
		i(e.getMessage() == null ? "no message" : e.getMessage());
	}
	
	public static void i(String msg) {
		if(DEBUG){
			Log.i(TAG, msg+"(T:"+System.currentTimeMillis()+")");
	        
//			Calendar rightNow = Calendar.getInstance();
//	        writeToFile("HotelFinder(" +rightNow.getTime().toString()+")"+
//	        		"--->"+msg+"(T:"+System.currentTimeMillis()+")"+"\n");
		}
    }
	 public  static void showToast(Context context, CharSequence text){
		  if(text == null){
			  return ;
		  }
		  Toast t;
		  if(text.length() > 28){
			  t = Toast.makeText(context, text, Toast.LENGTH_LONG);
			  
		  }else{
			  t = Toast.makeText(context, text, Toast.LENGTH_SHORT);
		  }
		  t.show();
	  }
}
