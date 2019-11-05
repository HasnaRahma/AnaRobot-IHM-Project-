package com.lounis.ana_robot;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;
//**************************************************************************
//**************************************************************************
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
//import org.opencv.objdetect.Objdetect;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.TextView;
//*************************************************************************
//*************************************************************************
@SuppressLint("HandlerLeak")
public class FaceDT extends Activity implements CvCameraViewListener2{
    private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();;
    private BluetoothDevice  mBluetoothDevice = null;
    private BluetoothSocket  mBluetoothSocket = null;
    private OutputStream     mOutputStream = null;
    private InputStream      mInputStream =null;
    private String           address;
    //*******************************************************************
    //******************************************************************
    private static final String    TAG                 = "OCVSample::Activity";
    public static final int        JAVA_DETECTOR       = 0;
    //--------------------------
    private Mat                    mRgba;
    private Mat                    mGray;
    private int                    mAbsoluteFaceSize   = 0;
    private float                  mRelativeFaceSize   = 0.2f;
    private CascadeClassifier      mJavaDetector;
//    private CascadeClassifier   mCascadeER;
//    private CascadeClassifier   mCascadeEL;
    //--------------------------
    private CameraBridgeViewBase mOpenCvCameraView;
    private TextView tv1;
    private long i=0;
    private long cpt=0;
    private Handler mHandler;
    int ax;
    int ay;
    //--------------------------
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                		  //-------------------------------------
                      try {
                          // load cascade file from application resources
                          InputStream is = getResources().openRawResource(R.raw.lbpcascade_frontalface);
                          File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                          File mCascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
                          FileOutputStream os = new FileOutputStream(mCascadeFile);

                          byte[] buffer = new byte[4096];
                          int bytesRead;
                          while ((bytesRead = is.read(buffer)) != -1) {
                              os.write(buffer, 0, bytesRead);
                          }
                          is.close();
                          os.close();
                          
                     /*     // --------------------------------- load left eye classificator -----------------------------------
                          InputStream iser = getResources().openRawResource(R.raw.haarcascade_righteye_2splits);
                          File cascadeDirER = getDir("cascadeER", Context.MODE_PRIVATE);
                          File cascadeFileER = new File(cascadeDirER, "haarcascade_eye_right.xml");
                          FileOutputStream oser = new FileOutputStream(cascadeFileER);

                          byte[] bufferER = new byte[4096];
                          int bytesReadER;
                          while ((bytesReadER = iser.read(bufferER)) != -1) {
                              oser.write(bufferER, 0, bytesReadER);
                          }
                          iser.close();
                          oser.close();
                     */     //----------------------------------------------------------------------------------------------------
                          
                          
                      /*    // --------------------------------- load right eye classificator ------------------------------------
                          InputStream isel = getResources().openRawResource(R.raw.haarcascade_lefteye_2splits);
                          File cascadeDirEL = getDir("cascadeEL", Context.MODE_PRIVATE);
                          File cascadeFileEL = new File(cascadeDirEL, "haarcascade_eye_left.xml");
                          FileOutputStream osel = new FileOutputStream(cascadeFileEL);

                          byte[] bufferEL = new byte[4096];
                          int bytesReadEL;
                          while ((bytesReadEL = isel.read(bufferEL)) != -1) {
                              osel.write(bufferEL, 0, bytesReadEL);
                          }
                          isel.close();
                          osel.close();
                          
                      */    // ------------------------------------------------------------------------------------------------------
                        
                          //-------------------------------------
                          mJavaDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());
                      //   mCascadeER = new CascadeClassifier(cascadeFileER.getAbsolutePath());
                      //    mCascadeEL = new CascadeClassifier(cascadeFileEL.getAbsolutePath());
                          if (mJavaDetector.empty()) {  mJavaDetector = null;
                      //        							mCascadeER=null;
                      //        							mCascadeEL=null; 
                          } 
                          else {Log.i(TAG, "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());}

                          cascadeDir.delete();

                      } catch (IOException e) {
                    	  		e.printStackTrace();
                          		Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
                      }
                   
                	//-------------------------------------
                	 
                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };
    
    //*************************************************************************
//****************************************************************************
    //-------------------------------------------------------------------
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_face_dt);
        
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.tutorial1_activity_java_surface_view);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        
        tv1=(TextView)findViewById(R.id.TV1);
        
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {        	
            		 tv1.setText(msg.obj.toString());
            		 sendData(msg.obj.toString());
            }
        };
        //****************************************************************
        
        Bundle mBundel= getIntent().getExtras();
        if(mBundel!=null){
        	address=mBundel.getString("address");
        	BluetoothOn();
        	connecteBT(address);
        	ListenData();
        	}    
    }
    //********************************************************************************
    @Override
    public void onPause() {
    	super.onPause();
    	closeBT();
        if (mOpenCvCameraView != null)mOpenCvCameraView.disableView();
    }
    //********************************************************************************
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 		closeBT();
        if (mOpenCvCameraView != null)mOpenCvCameraView.disableView();
 	}
 	//********************************************************************************
	@Override
	public void onResume() {
		super.onResume();
        if (!OpenCVLoader.initDebug()) {
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback); 
        } else {
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);   
        }
	}
    //********************************************************************************
    volatile boolean stopWorker;
    byte[] readBuffer;
    
    public void ListenData(){
  	  stopWorker = false; 
  	  final Handler mHandler = new Handler(); 
  	  final byte delimiter = 32;   //00-32 This is the ASCII code for system control
  	  readBuffer = new byte[1024];
  	    
  	Thread mThread=new Thread(){
  		@Override
  		public void run() {
  			super.run();
  			while(!Thread.currentThread().isInterrupted() && !stopWorker){
  			try {     int bytesAvailable = mInputStream.available();                        
                      if(bytesAvailable > 0) {
                          byte[] packetBytes = new byte[bytesAvailable];
                          mInputStream.read(packetBytes);
                          for(int i=0;i<bytesAvailable;i++) if(packetBytes[i] < delimiter) {packetBytes=Arrays.copyOf(packetBytes, i);break;}
                          final String data = new String(packetBytes, "US-ASCII");
                          mHandler.post(new Runnable() {
                                public void run() {
                                tv1.setText(data);
                               }
                             });
                          }
                 }catch (Exception e){stopWorker = true;}
  			}	
  		} 
  	  }; 
  	  mThread.start();
    }
    //*******************************************************************************
    public void showToast(String title, String message){
   	 	Toast.makeText(getBaseContext(),title + " - " + message, Toast.LENGTH_SHORT).show();
    }
    //********************************************************************************
    public void sendData(String message) {
      byte[] msgBuffer = message.getBytes();
      try {mOutputStream.write(msgBuffer);}
      catch (Exception e) { showToast("","error :sendData "+e.getMessage()); }
    }
    //********************************************************************************
    boolean firstConnecteBT=true;
    public void connecteBT(String address) {
	    final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //Standard SerialPortService ID
    	
				    try {mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(address);}
				    catch (Exception e) {showToast("Error BluetoothAdapter address ", e.getMessage());}
	   	    
			    	try {mBluetoothSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(uuid);}
			    	catch (Exception e) {showToast("Error", "socket create failed: " + e.getMessage() + ".");}
	   	    	
		if(firstConnecteBT){ 
			 	    try {   mBluetoothSocket.connect();
			 	    		showToast("Socket is connected ",";");
			 	    		
					 		try {mOutputStream = mBluetoothSocket.getOutputStream();showToast("OutputStream is OK",";");}
					 		catch (Exception e){showToast("Error", "OutputStream creation failed: " + e.getMessage());}
					 			    
					 		try {mInputStream = mBluetoothSocket.getInputStream();showToast("InputStream is OK",";");}
					 		catch (Exception e) {showToast("Error", "InputStream creation failed: " + e.getMessage());}
					 		
					 		firstConnecteBT=false;
			 	    }
			 	    catch (Exception e1){
			 	    	try{ mBluetoothSocket.close();}
			 	    	catch(Exception e2){showToast("Error close BluetoothSocket",e2.getMessage());} 
			 	        showToast("error in Socket ",";");
			 	    }
			}
    }
    //********************************************************************************
    public  void checkBTState() {
        if(mBluetoothAdapter == null) { showToast("Error ", "No bluetooth adapter available"); }
        else {
		    	if(!mBluetoothAdapter.isEnabled()){showToast("", "bluetooth is OFF");}
		    	else if(mBluetoothAdapter.isEnabled()){showToast("", "bluetooth is ON");}
        }      
    }
    //********************************************************************************
    public void closeBT() {
    	 stopWorker = true;      // stop readData tread
    	 firstConnecteBT=true;   // stop connected
    	 try{
	    	 if(mBluetoothAdapter!=null)mBluetoothAdapter.cancelDiscovery();
	    	 if(mOutputStream!=null)mOutputStream.close(); 
	    	 if(mInputStream!=null)mInputStream.close(); 
	    	 if(mBluetoothSocket!=null)mBluetoothSocket.close(); 
    	 }catch(Exception e){showToast("Error closeBT()",e.getMessage());} 
    	 
    }
    //********************************************************************************
    public void BluetoothOff(){
    	   if(mBluetoothAdapter == null) { showToast("Error ", "No bluetooth adapter available");}
           else {  
               	if(mBluetoothAdapter.isEnabled() ){
               		mBluetoothAdapter.disable();
               		try{Thread.sleep(100);}catch(Exception e){}
               	}
               	
               	checkBTState();
           }
    }
    //********************************************************************************
    public void BluetoothOn(){
        if(mBluetoothAdapter == null) { showToast("Error ", "No bluetooth adapter available");}
        else {
            	if(!mBluetoothAdapter.isEnabled()){
            		mBluetoothAdapter.enable();
            		try{Thread.sleep(2000);}catch(Exception e){}
            		}
	    		checkBTState();
        }
    }
    //********************************************************************************
  //************************************************************************************
  //************************************************************************************
  //************************************************************************************
  //************************************************************************
    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
    			
    	i++;    if(i>=10){ Message msg = new Message();
		                   msg.obj = new String("")+ax+"  "+ay +"\n";
		                   mHandler.sendMessage(msg);	
    			           i=0;
    			}
        
        //--------------------------------------------------------------
        mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();

        if (mAbsoluteFaceSize == 0) {
            int height = mGray.rows();
            if (Math.round(height * mRelativeFaceSize) > 0) {
                mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
            }
        }
        ax=0;ay=0;
        //----------------------------------
        MatOfRect faces = new MatOfRect();
        if (mJavaDetector != null)mJavaDetector.detectMultiScale(mGray, faces, 1.1, 2, 2,new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());
        Rect[] facesArray = faces.toArray();
            for (int i = 0; i < facesArray.length; i++)
            {
            	ax=(int)facesArray[i].tl().x;
            	ay=(int)facesArray[i].tl().y;
            	Imgproc.rectangle(mRgba, facesArray[i].tl(), facesArray[i].br(), new Scalar(0, 255, 0, 255), 3);
            }
                
     /*   //----------------------------------
        MatOfRect r_eye = new MatOfRect();
        if (mCascadeER != null)mCascadeER.detectMultiScale(mGray, r_eye, 1.15, 2, Objdetect.CASCADE_FIND_BIGGEST_OBJECT|Objdetect.CASCADE_SCALE_IMAGE,new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());  
        Rect[] r_eyeArray = r_eye.toArray();
        for (int i = 0; i < r_eyeArray.length; i++)
            Imgproc.rectangle(mRgba, r_eyeArray[i].tl(), r_eyeArray[i].br(), new Scalar(255, 0, 0, 255), 3);
     */   //----------------------------------
     /*     //----------------------------------  
        MatOfRect l_eye = new MatOfRect();
	    if (mCascadeEL != null)mCascadeEL.detectMultiScale(mGray, l_eye, 1.15, 2, Objdetect.CASCADE_FIND_BIGGEST_OBJECT|Objdetect.CASCADE_SCALE_IMAGE,new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());  
        Rect[] l_eyeArray = l_eye.toArray();
        for (int i = 0; i < l_eyeArray.length; i++)
        	Imgproc.rectangle(mRgba, l_eyeArray[i].tl(), l_eyeArray[i].br(), new Scalar(0, 0, 255, 255), 3);
     */  //----------------------------------
        
        
        return mRgba;
    }
    //************************************************************************

    //--------------------------
    //--------------------------
    public void onCameraViewStarted(int width, int height) {
    	
    }

    public void onCameraViewStopped() {
    }
    
    
    //************************************************************************
    
  //************************************************************************************
  //************************************************************************************
    
}
