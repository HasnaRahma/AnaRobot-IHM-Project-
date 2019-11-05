package com.lounis.ana_robot;


import java.util.ArrayList;
import java.util.Set;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class MainActivity extends Activity implements OnItemClickListener{
	private ArrayAdapter<String>  mArrayAdapterString;    //listAdapter
	private ArrayList<String> mArrayListStringName;	       //
	private ArrayList<String> mArrayListStringAddres;	   //
	private ListView mListView;
	
	private BluetoothAdapter mBluetoothAdapter;    //btAdabter
	private Set<BluetoothDevice> mBluetoothDeviceArray;


	//----------------------------------------------------------------------
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		   init1();
		   turnOnBt();
	 	   getPairedDevices();
	}
	@Override
	protected void onResume() {
		super.onResume();
		   init1();
		   turnOnBt();
	 	   getPairedDevices();
	}
	//----------------------------------------------------------------------
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {	
			String st=mArrayListStringAddres.get(arg2);
			Intent mIntent =new Intent(getApplicationContext(),FaceDT.class);
			mIntent.putExtra("address", st);
			startActivity(mIntent);	
	}
	//----------------------------------------------------------------------
	public void init1(){
	mBluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
	
	mListView =(ListView)findViewById(R.id.vListView);
	mListView.setOnItemClickListener(this);
	mArrayAdapterString=new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,0);
	mListView.setAdapter(mArrayAdapterString);
	
	mArrayListStringName=new ArrayList<String>();
	mArrayListStringAddres=new ArrayList<String>();
	mArrayListStringName.clear();
	mArrayListStringAddres.clear();
	}
	//----------------------------------------------------------------------
	public void getPairedDevices(){
		mBluetoothDeviceArray=mBluetoothAdapter.getBondedDevices();
			for(BluetoothDevice mBluetoothDevice :mBluetoothDeviceArray){
				mArrayAdapterString.add(mBluetoothDevice.getName()+"\n"+mBluetoothDevice.getAddress() );
				mArrayListStringName.add( mBluetoothDevice.getName() );
				mArrayListStringAddres.add(mBluetoothDevice.getAddress() );
			}
	}
	//----------------------------------------------------------------------
	public void turnOnBt(){
        if(mBluetoothAdapter == null)Toast.makeText(getBaseContext(), "error not exist Adapter bluetooth", Toast.LENGTH_LONG).show(); 	
        else {
		if(!mBluetoothAdapter.isEnabled())mBluetoothAdapter.enable();
		if(!mBluetoothAdapter.isEnabled());
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
	//*******************************************************************************
    public void showToast(String title, String message){
   	 	Toast.makeText(getBaseContext(),title + " - " + message, Toast.LENGTH_SHORT).show();
    }
    //********************************************************************************
    public void BluetoothOff(View v){
    	   if(mBluetoothAdapter == null) { showToast("Error ", "No bluetooth adapter available");}
           else {  
               	if(mBluetoothAdapter.isEnabled() ){
               		mBluetoothAdapter.disable();
               		try{Thread.sleep(100);}catch(Exception e){}
               	}
           }
    	   init1();
    	   checkBTState();
    	   
    }
    //********************************************************************************
    public void BluetoothOn(View v){
        if(mBluetoothAdapter == null) { showToast("Error ", "No bluetooth adapter available");}
        else {
            	if(!mBluetoothAdapter.isEnabled()){
            		mBluetoothAdapter.enable();
            		try{Thread.sleep(2000);}catch(Exception e){}
            		}
        }
		init1();
	 	getPairedDevices();
	 	checkBTState();
    }
    //********************************************************************************
}
