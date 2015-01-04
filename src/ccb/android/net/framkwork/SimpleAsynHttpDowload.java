package ccb.android.net.framkwork;

import ccb.android.net.framkwork.HttpLoader.Task;
import android.os.AsyncTask;

public class SimpleAsynHttpDowload extends AsyncTask<Task, Integer, String> {

	HttpLoader loader = null;
	Task task;
	public SimpleAsynHttpDowload( HttpLoader l){
		loader = l;
	}
	@Override
	protected String doInBackground(Task... params) {
		if ( loader == null || params == null){
			return null;
		}
		task = params[0];
		return loader.doDownload(task);
	}

	@Override
	protected void onCancelled() {
		// TODO Auto-generated method stub
		super.onCancelled();
	}

	@Override
	protected void onPostExecute(String result) {
		super.onPostExecute(result);
		if ( task.mListener != null){
			task.mListener.onDownLoadDone(task.taskId, result);
		}

	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		// TODO Auto-generated method stub
		super.onProgressUpdate(values);
	}

}
