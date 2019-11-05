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
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class BT_Control extends Activity {
    EditText myTextbox;
    
    private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();;
    private BluetoothDevice  mBluetoothDevice = null;
    private BluetoothSocket  mBluetoothSocket = null;
    private OutputStream     mOutputStream = null;
    private InputStream      mInputStream =null;
    private String           address;
    
    //-------------------------------------------------------------------
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bt__control);
        
        myTextbox = (EditText)findViewById(R.id.entry);
        
        Bundle mBundel= getIntent().getExtras();
        if(mBundel!=null){
        	address=mBundel.getString("address");
        	myTextbox.setText(address);
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
    }
    //********************************************************************************
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 		closeBT();
 	}
 	//********************************************************************************
	@Override
	public void onResume() {
		super.onResume();
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
                                myTextbox.setText(data);
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
    public void ButtonReadDataOnClick(View v){
    	ListenData();	
    }
    public void ButtonSendDataOnClick(View v){
        sendData(myTextbox.getText().toString());
    }
    public void Button0_OnClick(View v){
    	sendData("0");
     }
    public void Button1_Click(View v){
    	sendData("1");
     }
    public void ButtonConnectOnClick(View v){
       connecteBT(address);
    }
    public void ButtonDisconnectOnClick(View v){
       closeBT();
    }

}
