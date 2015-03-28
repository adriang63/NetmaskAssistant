/*
 * Copyright (c) by Adrian Griffis.  All rights reserved.
 */
package org.nerds.adrian.nmask;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

public class NMask extends Activity {
	static final byte ff = (byte) 0xff;
	public static final String IP_ADDR_KEY = "ipAddr";
	public static final String NET_MASK_KEY = "netMask";
	
	public static final String AD_UNIT_ID = "a14da2693739188";

	EditText ipAddr = null;
	EditText netMask = null;
	EditText netAddr = null;
	EditText broadAddr = null;
	
	Button shiftLeft = null;
	Button byClass = null;
	Button shiftRight = null;
	
	InetAddress ipAddrInet = null;
	InetAddress netMaskInet = null;
	
	MaskCalculator calculatorRunnable = null;
	Thread calculatorThread = null;
	long calculateAfter = 0;
	long calculatorDelay = 1000;
	boolean recalculateFlag = true;
	boolean recalculateLoopFlag = true;
	
	NMaskButtonListener bListener = null;
	TextWatcher watcher = null;
	
	AdView adView = null;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (null !=  savedInstanceState) {
            String ipAddrString = null;
            String netMaskString = null;
            if (savedInstanceState.containsKey(IP_ADDR_KEY)) {
            	ipAddrString = savedInstanceState.getString(IP_ADDR_KEY);
            	if (null != ipAddrString) {
    				try {
    					ipAddrInet = InetAddress.getByName(ipAddrString);
    				}
    				catch (Exception e) {
    					// TODO:
    				}
            	}
            }
            if (savedInstanceState.containsKey(NET_MASK_KEY)) {
            	netMaskString = savedInstanceState.getString(NET_MASK_KEY);
            	if (null != netMaskString) {
    				try {
    					netMaskInet = InetAddress.getByName(netMaskString);
    				}
    				catch (Exception e) {
    					// TODO:
    				}
            	}
            }
        }
        
        if (null == ipAddrInet) {
        	String ipAddrString = NMaskPreferenceScreen.getPrefIPAddr(this);
        	if ((null != ipAddrString) && (!ipAddrString.equals(""))) {
        		try {
        			ipAddrInet = InetAddress.getByName(ipAddrString);
        		}
        		catch (Exception e) {
        			// TODO:
        		}
        	}
        }
        
        if (null == netMaskInet) {
        	String netMaskString = NMaskPreferenceScreen.getPrefNetMask(this);
        	if ((null != netMaskString) && (!netMaskString.equals(""))) {
				try {
					netMaskInet = InetAddress.getByName(netMaskString);
				}
				catch (Exception e) {
					// TODO:
				}
        	}
        }
        
		createWidgets();

        calculatorRunnable = new MaskCalculator();
        calculatorThread = new Thread(calculatorRunnable);
        calculatorThread.setDaemon(true);
        calculatorThread.start();
    	
    }
    
    @Override
    public void onStart() {
    	super.onStart();
    	
    	if (null == adView) {
    		long currentTime = System.currentTimeMillis();
    		long adStopUntil = NMaskPreferenceScreen.getPrefAdStopUntil(this);
    		if (adStopUntil <= currentTime) {
    			turnOnAds();
    		}
    	}
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	
    	if (null != adView) {
    		AdRequest adRequest = new AdRequest();
    		// adRequest.addKeyword("network");
    		adView.loadAd(adRequest);
    	}
    }
    
    @Override
    public void onPause() {
    	super.onPause();
    	
    	if (null != adView) {
    		adView.stopLoading();
    	}
    }
    
    public void turnOnAds() {
    	if (null != adView) { return; }
    	adView = new AdView(this, AdSize.BANNER, AD_UNIT_ID);
    	LinearLayout layout = (LinearLayout)findViewById(R.id.sub_main_layout);
    	layout.addView(adView);
    }
    
    public void turnOffAds() {
    	if (null == adView) { return; }
    	adView.stopLoading();
    	LinearLayout layout = (LinearLayout)findViewById(R.id.sub_main_layout);
    	layout.removeView(adView);
    	adView = null;
    }
    
    public void stopAds() {
    	turnOffAds();
    	long currentTime = System.currentTimeMillis();
    	NMaskPreferenceScreen.setPrefAdStopUntil(this,currentTime+72000000);
    }
    
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
    	String ipAddrString = null;
    	if (null != ipAddr) {
    		ipAddrString = ipAddr.getText().toString();
    	}
    	savedInstanceState.putString(IP_ADDR_KEY, ipAddrString);
    	String netMaskString = null;
    	if (null != netMask) {
    		netMaskString = netMask.getText().toString();
    	}
    	savedInstanceState.putString(NET_MASK_KEY, netMaskString);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	super.onCreateOptionsMenu(menu);
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.main_menu,menu);
    	
    	return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
		int itemId;
		itemId = item.getItemId();
		switch (itemId) {
		case R.id.mn_settings:
			startActivity(new Intent(this, NMaskPreferenceScreen.class));
			return true;
		case R.id.mn_stop_adds:
			stopAds();
			return true;
		}
    	
		return false;
    }
    
    protected void createWidgets() {
        setContentView(R.layout.main);
        
        watcher = new NMaskTextWatcher();
        
        View v = findViewById(R.id.e_ip_addr);
        ipAddr = (EditText) v;
        if (ipAddrInet == null) {
            try {
            	ipAddrInet = InetAddress.getByName("192.168.0.1");
    		} catch (UnknownHostException e) {
    			// TODO Auto-generated catch block
    		}
        }
        if (ipAddrInet != null) {
        	ipAddr.setText(ipAddrInet.getHostAddress());
        }
        ipAddr.addTextChangedListener(watcher);
        
        v = findViewById(R.id.e_net_mask);
        netMask = (EditText) v;
        if (null == netMaskInet) {
            if (null != ipAddrInet) {
            	setMaskFromIP();
            }
        }
        else {
        	netMask.setText(netMaskInet.getHostAddress());
        }
        netMask.addTextChangedListener(watcher);
        
        v = findViewById(R.id.e_net_addr);
        netAddr = (EditText) v;
        
        v = findViewById(R.id.e_broad_addr);
        broadAddr = (EditText) v;
        
        bListener = new NMaskButtonListener();
        
        v = findViewById(R.id.b_shift_left);
        shiftLeft = (Button) v;
        shiftLeft.setOnClickListener(bListener);
        
        v = findViewById(R.id.b_by_class);
        byClass = (Button) v;
        byClass.setOnClickListener(bListener);
        
        v = findViewById(R.id.b_shift_right);
        shiftRight = (Button) v;
        shiftRight.setOnClickListener(bListener);
        
    }
    
    protected void shiftMaskLeft() {
		if (netMaskInet == null) {
			try {
				String netMaskString = netMask.getText().toString();
				netMaskInet = InetAddress.getByName(netMaskString);
			}
			catch (Exception e) {
				// TODO: exception handling
			}
		}
    	
		byte[] netMaskBytes = netMaskInet.getAddress();
		int t0;
		boolean carry = false;
		for (t0=netMaskBytes.length-1; t0>=0; t0--) {
			byte netByte = netMaskBytes[t0];
			boolean oldCarry = carry;
			carry = (netByte & 0x80) != 0;
			netByte = (byte) (netByte << 1);
			if (oldCarry) { netByte |= 1; }
			netMaskBytes[t0] = netByte;
		}
		try {
			netMaskInet = InetAddress.getByAddress(netMaskBytes);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			netMaskInet = null;
		}
		if (null != netMaskInet) {
			netMask.setText(netMaskInet.getHostAddress());
		}

		notifyTextChange();
    	
		return;
    }
    
    protected void shiftMaskRight() {
		if (netMaskInet == null) {
			try {
				String netMaskString = netMask.getText().toString();
				netMaskInet = InetAddress.getByName(netMaskString);
			}
			catch (Exception e) {
				// TODO: exception handling
			}
		}
    	
		byte[] netMaskBytes = netMaskInet.getAddress();
		int t0;
		boolean carry = true;
		for (t0=0; t0<netMaskBytes.length; t0++) {
			byte netByte = netMaskBytes[t0];
			boolean oldCarry = carry;
			carry = (netByte & 0x01) != 0;
			netByte = (byte) (netByte >> 1);
			if (oldCarry) { netByte |= 0x80; }
			netMaskBytes[t0] = netByte;
		}
		try {
			netMaskInet = InetAddress.getByAddress(netMaskBytes);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			netMaskInet = null;
		}
		if (null != netMaskInet) {
			netMask.setText(netMaskInet.getHostAddress());
		}

		notifyTextChange();
    	
		return;
    }
    
    protected void setMaskFromIP() {
		if (ipAddrInet == null) {
			try {
				String ipAddrString = ipAddr.getText().toString();
				ipAddrInet = InetAddress.getByName(ipAddrString);
			}
			catch (Exception e) {
				// TODO: exception handling
			}
		}

		byte[] ipAddrBytes = ipAddrInet.getAddress();
    	byte[] netMaskBytes = null;
    	if (null != ipAddrBytes) {
    		if (ipAddrBytes.length == 4) {
    			byte topByte = ipAddrBytes[0];
    			if ((topByte&0x80) == 0) { netMaskBytes = new byte[] { ff, 0, 0, 0 }; }
    			else if ((topByte&0x40) == 0) { netMaskBytes = new byte[] { ff, ff, 0, 0 }; }
    			else if ((topByte&0x20) == 0) { netMaskBytes = new byte[] { ff, ff, ff, 0 }; }
    		}
    	}
    	if (null != netMaskBytes) {
    		try {
				netMaskInet = InetAddress.getByAddress(netMaskBytes);
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
			}
			if (null != netMaskInet) {
				netMask.setText(netMaskInet.getHostAddress());
			}
    	}
    	
    	notifyTextChange();
    	
    	return;
    }
    
    protected void notifyTextChange() {
    	if (null == calculatorRunnable) { return; }
    	synchronized (calculatorRunnable) {
    		ipAddrInet = null;
    		netMaskInet = null;
    		recalculateFlag = true;
    		long tmpDelay = NMaskPreferenceScreen.getPrefDelay(this);
    		if (tmpDelay >= 0) { calculatorDelay = tmpDelay; }
    		long currentTime = System.currentTimeMillis();
    		calculateAfter = currentTime + calculatorDelay;
    		calculatorRunnable.notify();
    	}
    }
    
    protected class NMaskButtonListener implements OnClickListener {

		public void onClick(View v) {
			if (null == calculatorRunnable) { return; }
			synchronized (calculatorRunnable) {
				if (v == shiftLeft) {
					shiftMaskLeft();
				}
				else if (v == shiftRight) {
					shiftMaskRight();
				}
				else if (v == byClass) {
					setMaskFromIP();
				}
			}
		}
    	
    }
    
    protected class NMaskTextWatcher implements TextWatcher {

		public void afterTextChanged(Editable s) {
			// TODO Auto-generated method stub
			notifyTextChange();
		}

		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			// TODO Auto-generated method stub
			
		}

		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			// TODO Auto-generated method stub
			
		}
    	
    }
    
    protected class MaskCalculator implements Runnable {

		public synchronized void run() {
			while (recalculateLoopFlag) {
				long currentTime = System.currentTimeMillis();
				long waitInterval = 0;
				if (calculateAfter > 0) {
					waitInterval = calculateAfter - currentTime;
					if (waitInterval < 0) waitInterval = 0;
				}
				if ((waitInterval > 0) || (!recalculateFlag)) {
					try {
						if (waitInterval > 0) {
							wait(waitInterval);
						}
						else {
							wait();
						}
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					continue;
				}
				if (!recalculateFlag) { continue; }
	
				recalculateFlag = false;
				recalculate();
			}
		}
		
		public synchronized void recalculate() {
			if (ipAddrInet == null) {
				try {
					String ipAddrString = ipAddr.getText().toString();
					ipAddrInet = InetAddress.getByName(ipAddrString);
				}
				catch (Exception e) {
					// TODO: exception handling
				}
			}
			if (netMaskInet == null) {
				try {
					String netMaskString = netMask.getText().toString();
					netMaskInet = InetAddress.getByName(netMaskString);
				}
				catch (Exception e) {
					// TODO: exception handling
				}
			}
			if ((ipAddrInet == null) || (netMaskInet == null)) {
				runOnUiThread(new UpdateResults("",""));
				return;
			}
			
			byte[] ipAddrBytes = ipAddrInet.getAddress();
			byte[] netMaskBytes = netMaskInet.getAddress();
			if (ipAddrBytes.length != netMaskBytes.length) {
				runOnUiThread(new UpdateResults("",""));
				return;
			}
			
			int t0;
			byte[] netAddrBytes = new byte[ipAddrBytes.length];
			byte[] broadAddrBytes = new byte[ipAddrBytes.length];
			for (t0=0; t0<ipAddrBytes.length; t0++) {
				byte ipByte = ipAddrBytes[t0];
				byte maskByte = netMaskBytes[t0];
				netAddrBytes[t0] = (byte) (ipByte & maskByte);
				broadAddrBytes[t0] = (byte) (ipByte | (0xff ^ maskByte));
			}
			
			InetAddress netAddrInet = null;
			InetAddress broadAddrInet = null;
			try {
				netAddrInet = InetAddress.getByAddress(netAddrBytes);
				broadAddrInet = InetAddress.getByAddress(broadAddrBytes);
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
			}

			String tmpNet = null;
			String tmpBroad = null;
			if (netAddrInet != null) {
				tmpNet = netAddrInet.getHostAddress();
			}
			if (broadAddrInet != null) {
				tmpBroad = broadAddrInet.getHostAddress();
			}
			runOnUiThread(new UpdateResults(tmpNet,tmpBroad));
		}
    	
    }
    
    protected class UpdateResults implements Runnable {

    	String netAddrString;
    	String broadAddrString;
    	
    	protected UpdateResults(String net, String broad) {
    		netAddrString = net;
    		broadAddrString = broad;
    	}
    	
		public void run() {
			if (null != netAddrString) {
				netAddr.setText(netAddrString);
			}
			if (null != broadAddrString) {
				broadAddr.setText(broadAddrString);
			}
		}
    	
    }
}