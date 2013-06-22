package se.swg.recognary;

import java.util.List;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;

public class MainActivity extends Activity {
	private CameraBridgeViewBase mOpenCvCameraView;
	private MenuItem mItemPreviewRGBA;
	private MenuItem mItemPreviewTresholded;
	public static boolean bShowTresholded = false;
	
	private String TAG = "RECOGNARY";
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(new ObjTrackView(this));
    }
    
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    System.loadLibrary("objtrack");
                    //mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    @Override
    public void onResume() {
    	Log.i(TAG, "onResumeActivity");
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_5, this, mLoaderCallback);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
    	Log.i(TAG, "onCreateOptionsMenu");
    	mItemPreviewRGBA = menu.add("Preview RGBA");
    	mItemPreviewTresholded = menu.add("Preview Thresholded");
    	return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
    	Log.i(TAG, "Menu Item selected " + item);
    	if (item == mItemPreviewRGBA)
    		bShowTresholded = false;
        else if (item == mItemPreviewTresholded)
        	bShowTresholded = true;
    	return true;
    }
}
