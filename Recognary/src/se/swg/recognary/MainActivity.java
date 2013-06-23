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
import android.app.Dialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class MainActivity extends Activity {
	private CameraBridgeViewBase mOpenCvCameraView;
	private MenuItem mItemPreviewRGBA;
	private MenuItem mItemPreviewTresholded;
	private MenuItem mItemClearScribble;
	private MenuItem mItemSettings;
	public static boolean bShowTresholded = false;
	private String TAG = "RECOGNARY";

	Dialog settingsDialog;
	SeekBar seekBar_lower_h;
	SeekBar seekBar_lower_s;
	SeekBar seekBar_lower_v;

	SeekBar seekBar_higher_h;
	SeekBar seekBar_higher_s;
	SeekBar seekBar_higher_v;
	
	int progress_lower_h;
	int progress_lower_s;
	int progress_lower_v;
	int progress_higher_h;
	int progress_higher_s;
	int progress_higher_v;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "called onCreate");
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(new ObjTrackView(this));

		settingsDialog = new Dialog(this);
		LayoutInflater inflater = (LayoutInflater)this.getSystemService(LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.activity_settings, (ViewGroup)findViewById(R.id.dialog_settings));
		settingsDialog.setContentView(layout);
		settingsDialog.setTitle("HSV Settings");
		
		seekBar_lower_h = (SeekBar)layout.findViewById(R.id.seekBar_lower_h);
		seekBar_lower_s = (SeekBar)layout.findViewById(R.id.seekBar_lower_s);
		seekBar_lower_v = (SeekBar)layout.findViewById(R.id.seekBar_lower_v);
		seekBar_higher_h = (SeekBar)layout.findViewById(R.id.seekBar_higher_h);
		seekBar_higher_s = (SeekBar)layout.findViewById(R.id.seekBar_higher_s);
		seekBar_higher_v = (SeekBar)layout.findViewById(R.id.seekBar_higher_v);
		seekBar_lower_h.setOnSeekBarChangeListener(seekBar_lower_h_listener);
		seekBar_lower_s.setOnSeekBarChangeListener(seekBar_lower_s_listener);
		seekBar_lower_v.setOnSeekBarChangeListener(seekBar_lower_v_listener);
		seekBar_higher_h.setOnSeekBarChangeListener(seekBar_higher_h_listener);
		seekBar_higher_s.setOnSeekBarChangeListener(seekBar_higher_s_listener);
		seekBar_higher_v.setOnSeekBarChangeListener(seekBar_higher_v_listener);

	}
	
	//H=1, S=2, V=3
	//lower=1, upper=2

	OnSeekBarChangeListener seekBar_lower_h_listener = new OnSeekBarChangeListener() {
		public void onProgressChanged(SeekBar seekBark, int progress, boolean fromUser) {
			Log.i(TAG, "VALUE LOWER H:" + progress);
			progress_lower_h = progress;
		}

		public void onStartTrackingTouch(SeekBar seekBar) {}

		public void onStopTrackingTouch(SeekBar seekBar) {
			changeHSVbound(1,1,progress_lower_h);	//lower,H,progress
		}
	};

	OnSeekBarChangeListener seekBar_lower_s_listener = new OnSeekBarChangeListener() {
		public void onProgressChanged(SeekBar seekBark, int progress, boolean fromUser) {
			Log.i(TAG, "VALUE LOWER S:" + progress);
			progress_lower_s = progress;
		}

		public void onStartTrackingTouch(SeekBar seekBar) {}

		public void onStopTrackingTouch(SeekBar seekBar) {
			changeHSVbound(1,2,progress_lower_s);	//lower,S,progress
		}
	};


	OnSeekBarChangeListener seekBar_lower_v_listener = new OnSeekBarChangeListener() {
		public void onProgressChanged(SeekBar seekBark, int progress, boolean fromUser) {
			Log.i(TAG, "VALUE LOWER V:" + progress);
			progress_lower_v = progress;
		}

		public void onStartTrackingTouch(SeekBar seekBar) {}

		public void onStopTrackingTouch(SeekBar seekBar) {
			changeHSVbound(1,3,progress_lower_v);	//lower,V,progress
		}
	};
	
	OnSeekBarChangeListener seekBar_higher_h_listener = new OnSeekBarChangeListener() {
		public void onProgressChanged(SeekBar seekBark, int progress, boolean fromUser) {
			Log.i(TAG, "VALUE HIGHER H:" + progress);
			progress_higher_h = progress;
		}
		public void onStartTrackingTouch(SeekBar seekBar) {}

		public void onStopTrackingTouch(SeekBar seekBar) {
			changeHSVbound(2,1,progress_higher_h);	//higher,H,progress
		}
	};
	
	OnSeekBarChangeListener seekBar_higher_s_listener = new OnSeekBarChangeListener() {
		public void onProgressChanged(SeekBar seekBark, int progress, boolean fromUser) {
			Log.i(TAG, "VALUE HIGHER S:" + progress);
			progress_higher_s = progress;
		}
		public void onStartTrackingTouch(SeekBar seekBar) {}

		public void onStopTrackingTouch(SeekBar seekBar) {
			changeHSVbound(2,2,progress_higher_s);	//higher,S,progress
		}
	};
	
	OnSeekBarChangeListener seekBar_higher_v_listener = new OnSeekBarChangeListener() {
		public void onProgressChanged(SeekBar seekBark, int progress, boolean fromUser) {
			Log.i(TAG, "VALUE HIGHER V:" + progress);
			progress_higher_v = progress;
		}

		public void onStartTrackingTouch(SeekBar seekBar) {}

		public void onStopTrackingTouch(SeekBar seekBar) {
			changeHSVbound(2,3,progress_higher_v);	//higher,V,progress
		}
	};

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
		mItemClearScribble = menu.add("Clear scribble");
		mItemSettings = menu.add("Settings");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		Log.i(TAG, "Menu Item selected " + item);
		if (item == mItemPreviewRGBA)
			bShowTresholded = false;
		else if (item == mItemPreviewTresholded)
			bShowTresholded = true;
		else if(item == mItemClearScribble)
			clearScribble();
		else if(item == mItemSettings)
			settingsDialog.show();
		return true;
	}

	public native boolean clearScribble();
	public native int changeHSVbound(int lu, int id, int val);
}
