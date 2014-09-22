package org.opencv.samples.tutorial2;

import java.util.concurrent.atomic.AtomicReference;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.HOGDescriptor;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Paint.Style;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnTouchListener;

public class Tutorial2Activity extends Activity implements
		CvCameraViewListener2 {
	private static final String TAG = "OCVSample::Activity";

	private static final int VIEW_MODE_RGBA = 0;
	private static final int VIEW_MODE_GRAY = 1;
	private static final int VIEW_MODE_CANNY = 2;
	private static final int VIEW_MODE_FEATURES = 5;
	private static final int VIEW_MODE_CMT = 8;
	private static final int START_TLD = 6;
	private static final int START_CMT = 7;
	private static final int VIEW_MODE_BODY = 9;

	static final int WIDTH = 400 ;//240;// 320;
	static final int HEIGHT =240;// 135;// ;//240;0;

	private int _canvasImgYOffset;
	private int _canvasImgXOffset;

	static boolean uno = true;

	private int mViewMode;
	private Mat mRgba;
	private Mat mIntermediateMat;
	private Mat mGray;

	private MenuItem mItemPreviewRGBA;
	private MenuItem mItemPreviewGray;
	private MenuItem mItemPreviewCanny;
	private MenuItem mItemPreviewFeatures;
	private MenuItem mItemPreviewCMT;
	private MenuItem mItemPreviewBody;

	private Tutorial3View mOpenCvCameraView;
	SurfaceHolder _holder;

	private Rect _trackedBox = null;
	
	HOGDescriptor mHog;
	

	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS: {
				Log.i(TAG, "OpenCV loaded successfully");

				// Load native library after(!) OpenCV initialization
				System.loadLibrary("mixed_sample");

				mOpenCvCameraView.enableView();
				mOpenCvCameraView.enableFpsMeter();

				mHog=new HOGDescriptor();
				mHog.setSVMDetector(HOGDescriptor.getDefaultPeopleDetector()); 
				
			}
				break;
			default: {
				super.onManagerConnected(status);
			}
				break;
			}
		}
	};

	public Tutorial2Activity() {
		Log.i(TAG, "Instantiated new " + this.getClass());

	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "called onCreate");
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
        requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.tutorial2_surface_view);

		mOpenCvCameraView = (Tutorial3View) findViewById(R.id.tutorial2_activity_surface_view);
		mOpenCvCameraView.setCvCameraViewListener(this);

		final AtomicReference<Point> trackedBox1stCorner = new AtomicReference<Point>();
		final Paint rectPaint = new Paint();
		rectPaint.setColor(Color.rgb(0, 255, 0));
		rectPaint.setStrokeWidth(5);
		rectPaint.setStyle(Style.STROKE);
		_holder = mOpenCvCameraView.getHolder();

		mOpenCvCameraView.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// re-init

				final Point corner = new Point(
						event.getX() - _canvasImgXOffset, event.getY()
								- _canvasImgYOffset);
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					trackedBox1stCorner.set(corner);
					Log.i("TAG", "1st corner: " + corner);
					break;
				case MotionEvent.ACTION_UP:
					_trackedBox = new Rect(trackedBox1stCorner.get(), corner);
					if (_trackedBox.area() > 100) {
						Log.i("TAG", "Tracked box DEFINED: " + _trackedBox);
						if (mViewMode == VIEW_MODE_FEATURES)
							mViewMode = START_TLD;
						else
							mViewMode = START_CMT;

					} else
						_trackedBox = null;
					break;
				case MotionEvent.ACTION_MOVE:
					final android.graphics.Rect rect = new android.graphics.Rect(
							(int) trackedBox1stCorner.get().x
									+ _canvasImgXOffset,
							(int) trackedBox1stCorner.get().y
									+ _canvasImgYOffset, (int) corner.x
									+ _canvasImgXOffset, (int) corner.y
									+ _canvasImgYOffset);
					final Canvas canvas = _holder.lockCanvas(rect);
					canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR); // remove
																				// old
																				// rectangle
					canvas.drawRect(rect, rectPaint);
					_holder.unlockCanvasAndPost(canvas);

					break;
				}

				return true;
			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Log.i(TAG, "called onCreateOptionsMenu");
		mItemPreviewRGBA = menu.add("RGBA");
		mItemPreviewGray = menu.add("GRAY");
		mItemPreviewCanny = menu.add("Canny");
		mItemPreviewFeatures = menu.add("TLD");
		mItemPreviewCMT = menu.add("CMT");
		mItemPreviewBody = menu.add("Body");

		// mOpenCvCameraView.setResolution(640, 480);

		return true;
	}

	@Override
	public void onPause() {
		super.onPause();
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
	}

	@Override
	public void onResume() {
		super.onResume();
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this,
				mLoaderCallback);
	}

	public void onDestroy() {
		super.onDestroy();
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
	}

	public void onCameraViewStarted(int width, int height) {
		mRgba = new Mat(height, width, CvType.CV_8UC4);
		mIntermediateMat = new Mat(height, width, CvType.CV_8UC4);
		mGray = new Mat(height, width, CvType.CV_8UC1);
	}

	public void onCameraViewStopped() {
		mRgba.release();
		mGray.release();
		mIntermediateMat.release();
	}

	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		final int viewMode = mViewMode;

		switch (viewMode) {
		case VIEW_MODE_BODY:
		{
			 mRgba = inputFrame.rgba();
			 //Mat Gray =Reduce(inputFrame.gray());
			 Mat Gray = inputFrame.gray();
			 MatOfRect bodies = new MatOfRect();
			
			 MatOfDouble weights = new MatOfDouble();
			
		
			 mHog.detectMultiScale(Gray,bodies,weights); 
			 Rect r=null;
			 Rect [] rects = bodies.toArray();

			double px = (double) mRgba.width() / (double) Gray.width();
			double py = (double) mRgba.height() / (double) Gray.height();
			int imax=-1;
			double amax=0;
			 for (int i=0;i<rects.length;i++)
			{
				r=rects[i];
			
					
					r.x = (int) (r.x * px);
					r.y = (int) (r.y * py);
					r.width = (int) (r.width * px);
					r.height = (int) (r.height * py);
				
				if (r.x*r.y>amax)
				{
					amax=r.x*r.y;
					imax=i;
				}
				Core.rectangle(mRgba,  r.tl(), r.br(), new Scalar(0, 0, 255,
							0), 5);
			}
			 
			 if (imax>=0)
			 {
				 r=rects[imax];
				 double w = mRgba.width();
			     double h = mRgba.height();
				 double ppx = (w) / (double) (mOpenCvCameraView.getWidth());
			     double ppy = (h) / (double) (mOpenCvCameraView.getHeight());
			     r.x = (int) ((r.x / ppx)+0.1*r.width);
			     r.y = (int) ((r.y / ppy)+r.height*0.1);
				 r.width = (int) (0.8*r.width/ ppx);
				 r.height = (int) (0.8*r.height / ppy);
			     
				 _trackedBox=r;
				 mViewMode=START_CMT;
			 }
		}
			break;
		case VIEW_MODE_GRAY:
			// input frame has gray scale format
			Imgproc.cvtColor(inputFrame.gray(), mRgba, Imgproc.COLOR_GRAY2RGBA,
					4);
			break;
		case VIEW_MODE_RGBA:
			// input frame has RBGA format
			mRgba = inputFrame.rgba();
			break;
		case VIEW_MODE_CANNY:
			// input frame has gray scale format
			mRgba = inputFrame.rgba();
			Imgproc.Canny(inputFrame.gray(), mIntermediateMat, 80, 100);
			Imgproc.cvtColor(mIntermediateMat, mRgba, Imgproc.COLOR_GRAY2RGBA,
					4);
			break;
		case START_TLD: {
			mRgba = inputFrame.rgba();
			mGray = Reduce(inputFrame.gray());
			double w = mGray.width();
			double h = mGray.height();
			if (_trackedBox == null)
				OpenTLD(mGray.getNativeObjAddr(), mRgba.getNativeObjAddr(),
						(long) (w / 2 - w / 4), (long) (h / 2 - h / 4),
						(long) w / 2, (long) h / 2);
			else {

				Log.i("TAG", "START DEFINED: " + _trackedBox.x / 2 + " "
						+ _trackedBox.y / 2 + " " + _trackedBox.width / 2 + " "
						+ _trackedBox.height / 2);

				double px = (w) / (double) (mOpenCvCameraView.getWidth());
				double py = (h) / (double) (mOpenCvCameraView.getHeight());
				//
				OpenTLD(mGray.getNativeObjAddr(), mRgba.getNativeObjAddr(),
						(long) (_trackedBox.x * px),
						(long) (_trackedBox.y * py),
						(long) (_trackedBox.width * px),
						(long) (_trackedBox.height * py));
			}
			uno = false;
			mViewMode = VIEW_MODE_FEATURES;
		}
			break;
		case START_CMT: {
			mRgba = inputFrame.rgba();
			mGray = Reduce(inputFrame.gray());
			double w = mGray.width();
			double h = mGray.height();
			if (_trackedBox == null)
				// OpenTLD(mGray.getNativeObjAddr(),
				// mRgba.getNativeObjAddr(),(long)(w/2-w/4),(long)(
				// h/2-h/4),(long)w/2,(long)h/2);
				OpenCMT(mGray.getNativeObjAddr(), mRgba.getNativeObjAddr(),
						(long) (w / 2 - w / 4), (long) (h / 2 - h / 4),
						(long) w / 2, (long) h / 2);
			else {

				Log.i("TAG", "START DEFINED: " + _trackedBox.x / 2 + " "
						+ _trackedBox.y / 2 + " " + _trackedBox.width / 2 + " "
						+ _trackedBox.height / 2);

				double px = (w) / (double) (mOpenCvCameraView.getWidth());
				double py = (h) / (double) (mOpenCvCameraView.getHeight());
				//
				OpenCMT(mGray.getNativeObjAddr(), mRgba.getNativeObjAddr(),
						(long) (_trackedBox.x * px),
						(long) (_trackedBox.y * py),
						(long) (_trackedBox.width * px),
						(long) (_trackedBox.height * py));
			}
			uno = false;
			mViewMode = VIEW_MODE_CMT;
		}
			break;

		case VIEW_MODE_FEATURES:
			// input frame has RGBA format
			mRgba = inputFrame.rgba();
			mGray = inputFrame.gray();
			mGray = Reduce(mGray);

			Mat mRgba2 = ReduceColor(mRgba);

			// FindFeatures(mGray.getNativeObjAddr(), mRgba.getNativeObjAddr());
			if (uno) {
				int w = mGray.width();
				int h = mGray.height();
				OpenTLD(mGray.getNativeObjAddr(), mRgba.getNativeObjAddr(),
						(long) w - w / 4, (long) h / 2 - h / 4, (long) w / 2,
						(long) h / 2);
				uno = false;
			} else {

				ProcessTLD(mGray.getNativeObjAddr(), mRgba2.getNativeObjAddr());
				double px = (double) mRgba.width() / (double) mRgba2.width();
				double py = (double) mRgba.height() / (double) mRgba2.height();
				int[] l = getRect();
				if (l != null) {
					Rect r = new Rect();
					r.x = (int) (l[0] * px);
					r.y = (int) (l[1] * py);
					r.width = (int) (l[2] * px);
					r.height = (int) (l[3] * py);

					Core.rectangle(mRgba, r.tl(), r.br(), new Scalar(0, 0, 255,
							0), 5);
				}
				uno = false;

				// Mat mTemp=mRgba;

				// mRgba=UnReduceColor(mRgba2,mTemp.width(),mTemp.height());
				// mTemp.release();

			}

			// mRgba2.release();
			// mGray.release();
			break;

		case VIEW_MODE_CMT:
		// input frame has RGBA format
		{
			mRgba = inputFrame.rgba();
			mGray = inputFrame.gray();
			mGray = Reduce(mGray);

			mRgba2 = ReduceColor(mRgba);

			// FindFeatures(mGray.getNativeObjAddr(), mRgba.getNativeObjAddr());
			if (uno) {
				int w = mGray.width();
				int h = mGray.height();
				OpenCMT(mGray.getNativeObjAddr(), mRgba.getNativeObjAddr(),
						(long) w - w / 4, (long) h / 2 - h / 4, (long) w / 2,
						(long) h / 2);
				uno = false;
			} else {

				ProcessCMT(mGray.getNativeObjAddr(), mRgba2.getNativeObjAddr());
				double px = (double) mRgba.width() / (double) mRgba2.width();
				double py = (double) mRgba.height() / (double) mRgba2.height();

				int[] l = CMTgetRect();
				if (l != null) {
					Point topLeft = new Point(l[0] * px, l[1] * py);
					Point topRight = new Point(l[2] * px, l[3] * py);
					Point bottomLeft = new Point(l[4] * px, l[5] * py);
					Point bottomRight = new Point(l[6] * px, l[7] * py);

					Core.line(mRgba, topLeft, topRight, new Scalar(255, 255,
							255), 3);
					Core.line(mRgba, topRight, bottomRight, new Scalar(255,
							255, 255), 3);
					Core.line(mRgba, bottomRight, bottomLeft, new Scalar(255,
							255, 255), 3);
					Core.line(mRgba, bottomLeft, topLeft, new Scalar(255, 255,
							255), 3);

				}
				uno = false;

				// Mat mTemp=mRgba;

				// mRgba=UnReduceColor(mRgba2,mTemp.width(),mTemp.height());
				// mTemp.release();

			}
		}
			// mRgba2.release();
			// mGray.release();
			break;

		}

		return mRgba;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);

		if (item == mItemPreviewRGBA) {
			mViewMode = VIEW_MODE_RGBA;
		} else if (item == mItemPreviewGray) {
			mViewMode = VIEW_MODE_GRAY;
		} else if (item == mItemPreviewCanny) {
			mViewMode = VIEW_MODE_CANNY;
		} else if (item == mItemPreviewFeatures) {
			mViewMode = START_TLD;
			_trackedBox = null;
			uno = true;
		} else if (item == mItemPreviewCMT) {
			mViewMode = START_CMT;
			_trackedBox = null;
			uno = true;
		}else if (item == mItemPreviewBody) {
			mViewMode = VIEW_MODE_BODY;
		}

		return true;
	}

	Mat Reduce(Mat m) {
		// return m;
		Mat dst = new Mat();
		Imgproc.resize(m, dst, new org.opencv.core.Size(WIDTH, HEIGHT));
		return dst;
	}

	Mat ReduceColor(Mat m) {
		Mat dst = new Mat();
		Bitmap bmp = Bitmap.createBitmap(m.width(), m.height(),
				Bitmap.Config.ARGB_8888);
		Utils.matToBitmap(m, bmp);
		Bitmap bmp2 = Bitmap.createScaledBitmap(bmp, WIDTH, HEIGHT, false);

		Utils.bitmapToMat(bmp2, dst);
		// Imgproc.resize(m, dst, new Size(WIDTH,HEIGHT), 0, 0,
		// Imgproc.INTER_CUBIC);
		return dst;
	}

	Mat UnReduceColor(Mat m, int w, int h) {
		// return m;

		Mat dst = new Mat();
		Bitmap bmp = Bitmap.createBitmap(m.width(), m.height(),
				Bitmap.Config.ARGB_8888);
		Utils.matToBitmap(m, bmp);
		Bitmap bmp2 = Bitmap.createScaledBitmap(bmp, w, h, false);

		Utils.bitmapToMat(bmp2, dst);

		// Imgproc.resize(m, dst, new
		// org.opencv.core.Size(w,h),0,0,Imgproc.INTER_LINEAR);
		m.release();
		return dst;
	}

	public native void FindFeatures(long matAddrGr, long matAddrRgba);

	public native void OpenTLD(long matAddrGr, long matAddrRgba, long x,
			long y, long w, long h);

	public native void ProcessTLD(long matAddrGr, long matAddrRgba);

	private static native int[] getRect();

	public native void OpenCMT(long matAddrGr, long matAddrRgba, long x,
			long y, long w, long h);

	public native void ProcessCMT(long matAddrGr, long matAddrRgba);

	private static native int[] CMTgetRect();

}
