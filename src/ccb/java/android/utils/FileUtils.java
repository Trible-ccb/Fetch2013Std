package ccb.java.android.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

public class FileUtils {

	public static boolean storeInputStream ( String filename , InputStream is,String cs){

		File f = new File(filename);
		if ( f.exists()){
			f.delete();
		}
		try {
			f.createNewFile();
			BufferedReader r =  new BufferedReader( new InputStreamReader(is,cs));
			BufferedWriter w = new BufferedWriter( 
					new OutputStreamWriter(new FileOutputStream(f), cs));
			String tmp ;
			while ( ( tmp = r.readLine()) != null){
				w.write(tmp + "\n");
			}
			w.flush();
			w.close();
			r.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	public static InputStream openFileInputStream ( String path){
		
		InputStream is = null;
		try {
			is = new FileInputStream( new File(path));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return is;
	}

	public static boolean createDir ( String dir ){
		File f = new File(dir);
		if ( !f.exists() ){
			return f.mkdirs();
		} else {
			return true;
		}
	}
}
