package ccb.java.android.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;

public class GraphicTool {
	/**
	 *
	 */
	public static int dip2px(Context context, float dpValue) {
	final float scale = context.getResources().getDisplayMetrics().density;
	return (int) (dpValue * scale + 0.5f);
	}
	 
	/**
	 * 
	 */
	public static int px2dip(Context context, float pxValue) {
	final float scale = context.getResources().getDisplayMetrics().density;
	return (int) (pxValue / scale + 0.5f);
	}
	public static boolean inRangeOfView(View view, MotionEvent ev){
		int[] location = new int[2];
		view.getLocationOnScreen(location);
		int x = location[0];
		int y = location[1];
		if(ev.getX() < x || ev.getX() > (x + view.getWidth()) || ev.getY() < y || ev.getY() > (y + view.getHeight())){
		return false;
		}
		return true;
		}
//	public static float getHeighBywidth(Drawable d,float width){
//		return 0;
//	}
	public static Bitmap getScaleImageByScaleOfWinWidth(Context c ,int resId,float p){
		if ( resId == 0)return null;
		Drawable d = c.getResources().getDrawable(resId);
		if ( d == null)return null;
		BitmapDrawable defaultImg = (BitmapDrawable)(d);
		Bitmap bmp = defaultImg.getBitmap();
		
//		BitmapFactory.Options opt = new Options();
//		opt.inJustDecodeBounds = true;
//		BitmapFactory.decodeResource(c.getResources(), resId, opt);
//		
//		int inSampleSize = calInSampleSize(c,opt,p);
//		opt.inSampleSize = inSampleSize;
//		
//		opt.inJustDecodeBounds = false;
//		return BitmapFactory.decodeResource(c.getResources(), resId, opt);
		return getScaleImageByScaleOfWinWidth(c,bmp,p);
	}
	private static int calInSampleSize (Context c,Options opt,float p){
		DisplayMetrics mDisplayMetrics = new DisplayMetrics();
		((Activity)c).getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);
		int h = opt.outHeight;
		int w = opt.outWidth;
		int inSampleSize = 1;
		int reqW = (int) (mDisplayMetrics.widthPixels * p);
		int reqH = (int) (reqW * ((double)h / w));
		
		if (reqH < h || reqW < w){
	        if (w > h) {  
	            inSampleSize = Math.round((float)h / (float)reqH);  
	        } else {  
	            inSampleSize = Math.round((float)w / (float)reqW);  
	        }
		}
		return inSampleSize;
	}
	public static Bitmap convertDrawable2BitmapByCanvas(Drawable drawable) {
		Bitmap bitmap = Bitmap
		.createBitmap(
		drawable.getIntrinsicWidth(),
		drawable.getIntrinsicHeight(),
		drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
		: Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(bitmap);
		// canvas.setBitmap(bitmap);
		drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
		drawable.getIntrinsicHeight());
		drawable.draw(canvas);
		return bitmap;
	}
	
	public static Bitmap getScaleImageByScaleOfWinWidth(Context c ,Bitmap bm,float scaleOfWinWidth){
//		Drawable d = c.getResources().getDrawable(resId);
//		if ( d == null)return null;
		if (bm == null)return null;
		DisplayMetrics mDisplayMetrics = new DisplayMetrics();
		((Activity)c).getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);
//		BitmapDrawable defaultImg = (BitmapDrawable)(d);
//		Bitmap bmp = convertDrawable2BitmapByCanvas(d);
		Bitmap bmp = bm;
		int bmpW = bmp.getWidth();
		int bmpH = bmp.getHeight();
		float scaleW = 0,scaleH;
		int imgVisualW = (int) (mDisplayMetrics.widthPixels * scaleOfWinWidth);
		int imgVisualH = (int) (imgVisualW * ((double)bmpH / bmpW));
		scaleW = ((float)imgVisualW / bmpW);
		scaleH = ((float)imgVisualH / bmpH);
		
		Matrix m = new Matrix();
		m.postScale(scaleW, scaleH);
		
		Bitmap newBitMap = Bitmap.createBitmap(bmp, 0, 0, bmpW, bmpH, m, true);
		return newBitMap;
	}
	public static Bitmap getScaleImageByScaleOfWinWidthAndHeight(Context c ,Bitmap bm,float scaleOfWinWidth,float scaleOfWinHeight){
//		Drawable d = c.getResources().getDrawable(resId);
//		if ( d == null)return null;
		if (bm == null)return null;
		DisplayMetrics mDisplayMetrics = new DisplayMetrics();
		((Activity)c).getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);
//		BitmapDrawable defaultImg = (BitmapDrawable)(d);
//		Bitmap bmp = convertDrawable2BitmapByCanvas(d);
		Bitmap bmp = bm;
		int bmpW = bmp.getWidth();
		int bmpH = bmp.getHeight();
		float scaleW = 0,scaleH;
		int imgVisualW = (int) (mDisplayMetrics.widthPixels * scaleOfWinWidth);
		int imgVisualH = (int) (mDisplayMetrics.widthPixels * scaleOfWinHeight);
		scaleW = ((float)imgVisualW / bmpW);
		scaleH = ((float)imgVisualH / bmpH);
		
		Matrix m = new Matrix();
		m.postScale(scaleW, scaleH);
		
		Bitmap newBitMap = Bitmap.createBitmap(bmp, 0, 0, bmpW, bmpH, m, true);
		return newBitMap;
	}
	public static Bitmap getRoundedCornerBitmap(Context context, Bitmap input, int pixels , int w , int h , boolean squareTL, boolean squareTR, boolean squareBL, boolean squareBR  ) {

	    Bitmap output = Bitmap.createBitmap(w, h, Config.ARGB_8888);
	    Canvas canvas = new Canvas(output);
	    final float densityMultiplier = context.getResources().getDisplayMetrics().density;

	    final int color = 0xff424242;
	    final Paint paint = new Paint();
	    final Rect rect = new Rect(0, 0, w, h);
	    final RectF rectF = new RectF(rect);

	    //make sure that our rounded corner is scaled appropriately
	    final float roundPx = pixels*densityMultiplier;

	    paint.setAntiAlias(true);
	    canvas.drawARGB(0, 0, 0, 0);
	    paint.setColor(color);
	    canvas.drawRoundRect(rectF, roundPx, roundPx, paint);


	    //draw rectangles over the corners we want to be square
	    if (squareTL ){
	        canvas.drawRect(0, 0, w/2, h/2, paint);
	    }
	    if (squareTR ){
	        canvas.drawRect(w/2, 0, w, h/2, paint);
	    }
	    if (squareBL ){
	        canvas.drawRect(0, h/2, w/2, h, paint);
	    }
	    if (squareBR ){
	        canvas.drawRect(w/2, h/2, w, h, paint);
	    }

	    paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
	    canvas.drawBitmap(input, 0,0, paint);

	    return output;
	}
}
