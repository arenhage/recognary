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
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class MainActivity extends Activity {
	private CameraBridgeViewBase mOpenCvCameraView;
	private MenuItem mItemPreviewRGBA;
	private MenuItem mItemPreviewTresholded;
	private MenuItem mItemCircleTrack;
	private MenuItem mItemClearScribble;
	private MenuItem mItemSettings;
	public static boolean bShowTresholded = false;
	public static boolean bCircleTrack = false;
	private String TAG = "RECOGNARY";

	Dialog settingsDialog;
	
	SeekBar seekBar_lower_h;
	SeekBar seekBar_lower_s;
	SeekBar seekBar_lower_v;

	SeekBar seekBar_upper_h;
	SeekBar seekBar_upper_s;
	SeekBar seekBar_upper_v;
	
	Button btn_minus_lower_h;
	Button btn_minus_lower_s;
	Button btn_minus_lower_v;
	Button btn_plus_lower_h;
	Button btn_plus_lower_s;
	Button btn_plus_lower_v;

	Button btn_minus_upper_h;
	Button btn_minus_upper_s;
	Button btn_minus_upper_v;
	Button btn_plus_upper_h;
	Button btn_plus_upper_s;
	Button btn_plus_upper_v;
	
	int progress_lower_h;
	int progress_lower_s;
	int progress_lower_v;
	int progress_upper_h;
	int progress_upper_s;
	int progress_upper_v;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "called onCreate");
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(new ObjTrackView(this));

		//create a new dialog that will contain the settings
		settingsDialog = new Dialog(this);
		LayoutInflater inflater = (LayoutInflater)this.getSystemService(LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.activity_settings, (ViewGroup)findViewById(R.id.dialog_settings));
		settingsDialog.setContentView(layout);
		settingsDialog.setTitle("HSV Settings");
		settingsDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
		//move the dialog to horizontal most right
		Window window = settingsDialog.getWindow();
		WindowManager.LayoutParams wlp = window.getAttributes();
		wlp.gravity = Gravity.RIGHT;
		wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
		window.setAttributes(wlp);
		
		seekBar_lower_h = (SeekBar)layout.findViewById(R.id.seekBar_lower_h);
		seekBar_lower_s = (SeekBar)layout.findViewById(R.id.seekBar_lower_s);
		seekBar_lower_v = (SeekBar)layout.findViewById(R.id.seekBar_lower_v);
		seekBar_upper_h = (SeekBar)layout.findViewById(R.id.seekBar_upper_h);
		seekBar_upper_s = (SeekBar)layout.findViewById(R.id.seekBar_upper_s);
		seekBar_upper_v = (SeekBar)layout.findViewById(R.id.seekBar_upper_v);
		seekBar_lower_h.setOnSeekBarChangeListener(seekBar_lower_h_listener);
		seekBar_lower_s.setOnSeekBarChangeListener(seekBar_lower_s_listener);
		seekBar_lower_v.setOnSeekBarChangeListener(seekBar_lower_v_listener);
		seekBar_upper_h.setOnSeekBarChangeListener(seekBar_upper_h_listener);
		seekBar_upper_s.setOnSeekBarChangeListener(seekBar_upper_s_listener);
		seekBar_upper_v.setOnSeekBarChangeListener(seekBar_upper_v_listener);
		
		btn_minus_lower_h = (Button)layout.findViewById(R.id.btn_minus_lower_h);
		btn_minus_lower_s = (Button)layout.findViewById(R.id.btn_minus_lower_s);
		btn_minus_lower_v = (Button)layout.findViewById(R.id.btn_minus_lower_v);
		btn_plus_lower_h = (Button)layout.findViewById(R.id.btn_plus_lower_h);
		btn_plus_lower_s = (Button)layout.findViewById(R.id.btn_plus_lower_s);
		btn_plus_lower_v = (Button)layout.findViewById(R.id.btn_plus_lower_v);
		
		btn_minus_upper_h = (Button)layout.findViewById(R.id.btn_minus_upper_h);
		btn_minus_upper_s = (Button)layout.findViewById(R.id.btn_minus_upper_s);
		btn_minus_upper_v = (Button)layout.findViewById(R.id.btn_minus_upper_v);
		btn_plus_upper_h = (Button)layout.findViewById(R.id.btn_plus_upper_h);
		btn_plus_upper_s = (Button)layout.findViewById(R.id.btn_plus_upper_s);
		btn_plus_upper_v = (Button)layout.findViewById(R.id.btn_plus_upper_v);
		
		btn_minus_lower_h.setOnClickListener(btn_minus_lower_h_listener);
		btn_minus_lower_s.setOnClickListener(btn_minus_lower_s_listener);
		btn_minus_lower_v.setOnClickListener(btn_minus_lower_v_listener);
		btn_plus_lower_h.setOnClickListener(btn_plus_lower_h_listener);
		btn_plus_lower_s.setOnClickListener(btn_plus_lower_s_listener);
		btn_plus_lower_v.setOnClickListener(btn_plus_lower_v_listener);
		
		btn_minus_upper_h.setOnClickListener(btn_minus_upper_h_listener);
		btn_minus_upper_s.setOnClickListener(btn_minus_upper_s_listener);
		btn_minus_upper_v.setOnClickListener(btn_minus_upper_v_listener);
		btn_plus_upper_h.setOnClickListener(btn_plus_upper_h_listener);
		btn_plus_upper_s.setOnClickListener(btn_plus_upper_s_listener);
		btn_plus_upper_v.setOnClickListener(btn_plus_upper_v_listener);
	}
	
	//H=1, S=2, V=3
	//lower=1, upper=2
	
	OnClickListener btn_minus_lower_h_listener = new OnClickListener() {
		public void onClick(View v) {
			int number = Integer.parseInt(((TextView)settingsDialog.getWindow().getDecorView().findViewById(android.R.id.content).findViewById(R.id.lower_h_value)).getText().toString());
			if(number-1 >= 0) {
				changeHSVbound(1,1,number-1);
				((TextView)settingsDialog.getWindow().getDecorView().findViewById(android.R.id.content).findViewById(R.id.lower_h_value)).setText((number-1)+"");
				seekBar_lower_h.setProgress(progress_lower_h = number-1);
			}
		}
	};
	
	OnClickListener btn_minus_lower_s_listener = new OnClickListener() {
		public void onClick(View v) {
			int number = Integer.parseInt(((TextView)settingsDialog.getWindow().getDecorView().findViewById(android.R.id.content).findViewById(R.id.lower_s_value)).getText().toString());
			if(number-1 >= 0) {
				changeHSVbound(1,2,number-1);
				((TextView)settingsDialog.getWindow().getDecorView().findViewById(android.R.id.content).findViewById(R.id.lower_s_value)).setText((number-1)+"");
				seekBar_lower_s.setProgress(progress_lower_s = number-1);
			}
			
		}
	};	
	
	OnClickListener btn_minus_lower_v_listener = new OnClickListener() {
		public void onClick(View v) {
			int number = Integer.parseInt(((TextView)settingsDialog.getWindow().getDecorView().findViewById(android.R.id.content).findViewById(R.id.lower_v_value)).getText().toString());
			if(number-1 >= 0) {
				changeHSVbound(1,3,number-1);
				((TextView)settingsDialog.getWindow().getDecorView().findViewById(android.R.id.content).findViewById(R.id.lower_v_value)).setText((number-1)+"");
				seekBar_lower_v.setProgress(progress_lower_v = number-1);
			}
			
		}
	};

	OnClickListener btn_plus_lower_h_listener = new OnClickListener() {
		public void onClick(View v) {
			int number = Integer.parseInt(((TextView)settingsDialog.getWindow().getDecorView().findViewById(android.R.id.content).findViewById(R.id.lower_h_value)).getText().toString());
			if(number+1 <= 180) {
				changeHSVbound(1,1,number+1);
				((TextView)settingsDialog.getWindow().getDecorView().findViewById(android.R.id.content).findViewById(R.id.lower_h_value)).setText((number+1)+"");
				seekBar_lower_h.setProgress(progress_lower_h = number+1);
			}
			
		}
	};
	
	OnClickListener btn_plus_lower_s_listener = new OnClickListener() {
		public void onClick(View v) {
			int number = Integer.parseInt(((TextView)settingsDialog.getWindow().getDecorView().findViewById(android.R.id.content).findViewById(R.id.lower_s_value)).getText().toString());
			if(number+1 <= 255) {
				changeHSVbound(1,2,number+1);
				((TextView)settingsDialog.getWindow().getDecorView().findViewById(android.R.id.content).findViewById(R.id.lower_s_value)).setText((number+1)+"");
				seekBar_lower_s.setProgress(progress_lower_s = number+1);
			}
			
		}
	};
	
	OnClickListener btn_plus_lower_v_listener = new OnClickListener() {
		public void onClick(View v) {
			int number = Integer.parseInt(((TextView)settingsDialog.getWindow().getDecorView().findViewById(android.R.id.content).findViewById(R.id.lower_v_value)).getText().toString());
			if(number+1 <= 255) {
				changeHSVbound(1,2,number+1);
				((TextView)settingsDialog.getWindow().getDecorView().findViewById(android.R.id.content).findViewById(R.id.lower_v_value)).setText((number+1)+"");
				seekBar_lower_v.setProgress(progress_lower_v = number+1);
			}
			
		}
	};
	
	OnClickListener btn_minus_upper_h_listener = new OnClickListener() {
		public void onClick(View v) {
			int number = Integer.parseInt(((TextView)settingsDialog.getWindow().getDecorView().findViewById(android.R.id.content).findViewById(R.id.upper_h_value)).getText().toString());
			if(number-1 >= 0) {
				changeHSVbound(1,1,number-1);
				((TextView)settingsDialog.getWindow().getDecorView().findViewById(android.R.id.content).findViewById(R.id.upper_h_value)).setText((number-1)+"");
				seekBar_upper_h.setProgress(progress_upper_h = number-1);
			}
			
		}
	};
	
	OnClickListener btn_minus_upper_s_listener = new OnClickListener() {
		public void onClick(View v) {
			int number = Integer.parseInt(((TextView)settingsDialog.getWindow().getDecorView().findViewById(android.R.id.content).findViewById(R.id.upper_s_value)).getText().toString());
			if(number-1 >= 0) {
				changeHSVbound(1,2,number-1);
				((TextView)settingsDialog.getWindow().getDecorView().findViewById(android.R.id.content).findViewById(R.id.upper_s_value)).setText((number-1)+"");
				seekBar_upper_s.setProgress(progress_upper_s = number-1);
			}
			
		}
	};
	
	OnClickListener btn_minus_upper_v_listener = new OnClickListener() {
		public void onClick(View v) {
			int number = Integer.parseInt(((TextView)settingsDialog.getWindow().getDecorView().findViewById(android.R.id.content).findViewById(R.id.upper_v_value)).getText().toString());
			if(number-1 >= 0) {
				changeHSVbound(1,3,number-1);
				((TextView)settingsDialog.getWindow().getDecorView().findViewById(android.R.id.content).findViewById(R.id.upper_v_value)).setText((number-1)+"");
				seekBar_upper_v.setProgress(progress_upper_v = number-1);
			}
			
		}
	};

	OnClickListener btn_plus_upper_h_listener = new OnClickListener() {
		public void onClick(View v) {
			int number = Integer.parseInt(((TextView)settingsDialog.getWindow().getDecorView().findViewById(android.R.id.content).findViewById(R.id.upper_h_value)).getText().toString());
			if(number+1 <= 180) {
				changeHSVbound(1,1,number+1);
				((TextView)settingsDialog.getWindow().getDecorView().findViewById(android.R.id.content).findViewById(R.id.upper_h_value)).setText((number+1)+"");
				seekBar_upper_h.setProgress(progress_upper_h = number+1);
			}
			
		}
	};
	
	OnClickListener btn_plus_upper_s_listener = new OnClickListener() {
		public void onClick(View v) {
			int number = Integer.parseInt(((TextView)settingsDialog.getWindow().getDecorView().findViewById(android.R.id.content).findViewById(R.id.upper_s_value)).getText().toString());
			if(number+1 <= 255) {
				changeHSVbound(1,2,number+1);
				((TextView)settingsDialog.getWindow().getDecorView().findViewById(android.R.id.content).findViewById(R.id.upper_s_value)).setText((number+1)+"");
				seekBar_upper_s.setProgress(progress_upper_s = number+1);
			}
			
		}
	};
	
	OnClickListener btn_plus_upper_v_listener = new OnClickListener() {
		public void onClick(View v) {
			int number = Integer.parseInt(((TextView)settingsDialog.getWindow().getDecorView().findViewById(android.R.id.content).findViewById(R.id.upper_v_value)).getText().toString());
			if(number+1 <= 255) {
				changeHSVbound(1,2,number+1);
				((TextView)settingsDialog.getWindow().getDecorView().findViewById(android.R.id.content).findViewById(R.id.upper_v_value)).setText((number+1)+"");
				seekBar_upper_v.setProgress(progress_upper_v = number+1);
			}
			
		}
	};
	
	OnSeekBarChangeListener seekBar_lower_h_listener = new OnSeekBarChangeListener() {
		public void onProgressChanged(SeekBar seekBark, int progress, boolean fromUser) {
			Log.i(TAG, "VALUE LOWER H:" + progress);
			progress_lower_h = progress;
		}

		public void onStartTrackingTouch(SeekBar seekBar) {}

		public void onStopTrackingTouch(SeekBar seekBar) {
			changeHSVbound(1,1,progress_lower_h);	//lower,H,progress
			((TextView)settingsDialog.getWindow().getDecorView().findViewById(android.R.id.content).findViewById(R.id.lower_h_value)).setText(progress_lower_h+"");
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
			((TextView)settingsDialog.getWindow().getDecorView().findViewById(android.R.id.content).findViewById(R.id.lower_s_value)).setText(progress_lower_s+"");
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
			((TextView)settingsDialog.getWindow().getDecorView().findViewById(android.R.id.content).findViewById(R.id.lower_v_value)).setText(progress_lower_v+"");
		}
	};
	
	OnSeekBarChangeListener seekBar_upper_h_listener = new OnSeekBarChangeListener() {
		public void onProgressChanged(SeekBar seekBark, int progress, boolean fromUser) {
			Log.i(TAG, "VALUE upper H:" + progress);
			progress_upper_h = progress;
		}
		public void onStartTrackingTouch(SeekBar seekBar) {}

		public void onStopTrackingTouch(SeekBar seekBar) {
			changeHSVbound(2,1,progress_upper_h);	//upper,H,progress
			((TextView)settingsDialog.getWindow().getDecorView().findViewById(android.R.id.content).findViewById(R.id.upper_h_value)).setText(progress_upper_h+"");
		}
	};
	
	OnSeekBarChangeListener seekBar_upper_s_listener = new OnSeekBarChangeListener() {
		public void onProgressChanged(SeekBar seekBark, int progress, boolean fromUser) {
			Log.i(TAG, "VALUE upper S:" + progress);
			progress_upper_s = progress;
		}
		public void onStartTrackingTouch(SeekBar seekBar) {}

		public void onStopTrackingTouch(SeekBar seekBar) {
			changeHSVbound(2,2,progress_upper_s);	//upper,S,progress
			((TextView)settingsDialog.getWindow().getDecorView().findViewById(android.R.id.content).findViewById(R.id.upper_s_value)).setText(progress_upper_s+"");
		}
	};
	
	OnSeekBarChangeListener seekBar_upper_v_listener = new OnSeekBarChangeListener() {
		public void onProgressChanged(SeekBar seekBark, int progress, boolean fromUser) {
			Log.i(TAG, "VALUE upper V:" + progress);
			progress_upper_v = progress;
		}

		public void onStartTrackingTouch(SeekBar seekBar) {}

		public void onStopTrackingTouch(SeekBar seekBar) {
			changeHSVbound(2,3,progress_upper_v);	//upper,V,progress
			((TextView)settingsDialog.getWindow().getDecorView().findViewById(android.R.id.content).findViewById(R.id.upper_v_value)).setText(progress_upper_v+"");
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
		mItemCircleTrack = menu.add("Circle Track");
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
		else if (item == mItemCircleTrack)
			if(bCircleTrack) bCircleTrack = false; else bCircleTrack = true;
		else if(item == mItemClearScribble) {
			clearScribble();
			if(bShowTresholded) {		//only clear the hsv settings if in threshold view
				resetHSVSettings();
				resetDialogSeekbar();
			}
		}
		else if(item == mItemSettings)
			settingsDialog.show();
		return true;
	}
	
	public void resetDialogSeekbar() {
		seekBar_lower_h.setProgress(170);
		seekBar_lower_s.setProgress(160);
		seekBar_lower_v.setProgress(60);
		seekBar_upper_h.setProgress(180);
		seekBar_upper_s.setProgress(255);
		seekBar_upper_v.setProgress(255);
		View dialogContent = settingsDialog.getWindow().getDecorView().findViewById(android.R.id.content);
		((TextView)dialogContent.findViewById(R.id.lower_h_value)).setText(170+"");
		((TextView)dialogContent.findViewById(R.id.lower_s_value)).setText(160+"");
		((TextView)dialogContent.findViewById(R.id.lower_v_value)).setText(60+"");
		((TextView)dialogContent.findViewById(R.id.upper_h_value)).setText(180+"");
		((TextView)dialogContent.findViewById(R.id.upper_s_value)).setText(255+"");
		((TextView)dialogContent.findViewById(R.id.upper_v_value)).setText(255+"");
	}

	public native boolean clearScribble();
	public native boolean resetHSVSettings();
	public native int changeHSVbound(int lu, int id, int val);
}
