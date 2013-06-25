package se.swg.recognary;

import org.opencv.android.OpenCVLoader;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

public class ObjTrackView extends SampleViewBase {
	private String TAG = "RECOGNARY";

	private int mFrameSize;
	private Bitmap mBitmap;
	private int[] mRGBA;

    public ObjTrackView(Context context) {
        super(context);
    }
    
	public native int findFeatures(int width, int height, byte yuv[], int[] rgba, boolean debug, boolean circleTrack);

	@Override
	protected void onPreviewStared(int previewWidth, int previewHeight) {
		Log.i(TAG, "onPreviewStarted");
		mFrameSize = previewWidth * previewHeight;
		mRGBA = new int[mFrameSize];
		mBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Bitmap.Config.ARGB_8888);
	}

	@Override
	protected void onPreviewStopped() {
		Log.i(TAG, "onPreviewStopped");
		if(mBitmap != null) {
			mBitmap.recycle();
			mBitmap = null;
		}
		mRGBA = null;
	}

    @Override
    protected Bitmap processFrame(byte[] data) {
    	//Log.i(TAG, "processFrame");
        int[] rgba = mRGBA;

        findFeatures(getFrameWidth(),getFrameHeight(), data, rgba, MainActivity.bShowTresholded, MainActivity.bCircleTrack);

        Bitmap bmp = mBitmap; 
        bmp.setPixels(rgba, 0/* offset */, getFrameWidth() /* stride */, 0, 0, getFrameWidth(), getFrameHeight());
        return bmp;
    }
}
