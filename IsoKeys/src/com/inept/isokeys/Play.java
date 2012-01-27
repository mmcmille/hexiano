/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *                                                                         *
 *   IsoKeys, an isomorphic musical keyboard for Android                   *
 *   Copyright 2011, 2012 David A. Randolph                                *
 *                                                                         *
 *   FILE: Play.java                                                       *
 *                                                                         *
 *   This file is part of IsoKeys, an open-source project                  *
 *   hosted at http://isokeys.sourceforge.net.                             *
 *                                                                         *
 *   IsoKeys is free software: you can redistribute it and/or              *
 *   modify it under the terms of the GNU General Public License           *
 *   as published by the Free Software Foundation, either version          *
 *   3 of the License, or (at your option) any later version.              *
 *                                                                         *
 *   AndroidWorld is distributed in the hope that it will be useful,       *
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 *   GNU General Public License for more details.                          *
 *                                                                         *
 *   You should have received a copy of the GNU General Public License     *
 *   along with IsoKeys.  If not, see <http://www.gnu.org/licenses/>.      *
 *                                                                         *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package com.inept.isokeys;

import com.google.ads.*;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.content.pm.PackageManager.NameNotFoundException;

public class Play extends Activity implements OnSharedPreferenceChangeListener
{
	GoogleAnalyticsTracker mTracker;
	
	final static String MY_AD_UNIT_ID = "a14f0bbe2e7d0ab";
	final static int ABOUT_DIALOG_ID = 1;
	static SharedPreferences mPrefs;
	static FrameLayout mFrame;
	static HexKeyboard mBoard;
	static AdView mAd;
	static FrameLayout mAdFrame;

	private String getVersionName()
	{
		String versionName = "";
		
		try
		{
			versionName = getPackageManager().
					getPackageInfo(getPackageName(), 0).versionName;
		}
		catch (NameNotFoundException e)
		{
			Log.e("getVersionName", e.getMessage());
		}
		
		return versionName;
	}

	
	/**
	 * @see android.app.Activity#onCreate(Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        mPrefs.registerOnSharedPreferenceChangeListener(this);
		
		mTracker = GoogleAnalyticsTracker.getInstance();

	    // Start the tracker in manual dispatch mode...
	    // mTracker.startNewSession("UA-28224231-1", this);
	    // ...alternatively, the tracker can be started with a dispatch interval (in seconds).
	    mTracker.startNewSession("UA-28224231-1", 100, this);
    	String versionStr = this.getVersionName();
    	mTracker.setCustomVar(1, "versionName", versionStr, 2);
	    mTracker.trackPageView("/play");
	    trackPreferences(3); // Page-level tracking.
        
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
       
        loadKeyboard();
	}

	protected void setOrientation()
	{
		String layout = mPrefs.getString("layout", "Sonome");

		int orientationId = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;

		if (layout.equals("Sonome"))
		{
			boolean isLandscape = mPrefs.getBoolean("sonomeLandscape", true);
			if (! isLandscape)
			{
				orientationId = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
			}
		}
		else if (layout.equals("Jammer"))
		{
			boolean isLandscape = mPrefs.getBoolean("jammerLandscape", false);
			if (! isLandscape)
			{
				orientationId = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
			}
		}
		else if (layout.equals("Janko"))
		{
			boolean isLandscape = mPrefs.getBoolean("jankoLandscape", true);
			if (! isLandscape)
			{
				orientationId = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
			}
		}

		this.setRequestedOrientation(orientationId);
	}
	
	protected void loadKeyboard()
	{
	    setOrientation();	
		Context con = this.getApplicationContext();
		
        AdRequest adRequest = new AdRequest();
        adRequest.addTestDevice(AdRequest.TEST_EMULATOR);
        mAd = new AdView(this, AdSize.BANNER, MY_AD_UNIT_ID);
       
		mFrame = new FrameLayout(con);
        mAdFrame = new FrameLayout(con);
		mBoard = new HexKeyboard(con, mAdFrame);
		mBoard.setUpBoard(this.getRequestedOrientation());
		mBoard.invalidate();

		mFrame.addView(mBoard);
		LayoutParams layoutParams = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT, Gravity.TOP | Gravity.CENTER_HORIZONTAL); 
        mAdFrame.setLayoutParams(layoutParams);
        mAdFrame.addView(mAd);
        mFrame.addView(mAdFrame);
        
        // adRequest.addTestDevice("TEST_DEVICE_ID"); 
        mAd.loadAd(adRequest);
		
		this.setContentView(mFrame);
		// this.setContentView(mBoard);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		Log.d("Play", "Menu, ho!");
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu); 
		return true; 
	}

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case ABOUT_DIALOG_ID:
                return new AboutDialog(this);
            default:
                return null;
        }
    }
   
    private void trackPreferences(int scopeId)
    {
    	String layoutStr = mPrefs.getString(
    			"layout", getString(R.string.default_layout));
    	mTracker.setCustomVar(1, "layout", layoutStr, scopeId);
    	
    	String colorSchemeStr = mPrefs.getString(
    			"colorScheme", getString(R.string.default_color_scheme));
    	mTracker.setCustomVar(2, "colorScheme", colorSchemeStr, scopeId);
    	
    	String scaleStr = mPrefs.getString(
    			"scale", getString(R.string.default_scale));
    	mTracker.setCustomVar(3, "scale", scaleStr, scopeId);
    	
    	String labelTypeStr = mPrefs.getString(
    			"labelType", getString(R.string.default_layout));
    	mTracker.setCustomVar(4, "labelType", labelTypeStr, scopeId);
    	
    	String instrumentStr = mPrefs.getString(
    			"instrument", getString(R.string.default_instrument));
    	mTracker.setCustomVar(5, "instrument", instrumentStr, scopeId);
    }
    
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		    case R.id.preferences:
		    	mTracker.trackPageView("/preferences");
			    startActivity(new Intent(this, Prefer.class)); 
			    setOrientation();
			    mTracker.trackPageView("/play");
			    trackPreferences(3); // Page level tracking.
			    break;
		    case R.id.quit:
		    	mTracker.trackPageView("/user_exit");
		    	startActivity(new Intent(this, Inneract.class));
			    finish();
			    break;
		    case R.id.about:
		    	mTracker.trackPageView("/about");
			    showDialog(ABOUT_DIALOG_ID);
			    mTracker.trackPageView("/play");
			    break;
		}

		return true; 
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key)
	{
		setOrientation();
		mBoard.setUpBoard(this.getRequestedOrientation());
		mBoard.invalidate();
	}
	
	  @Override
	  protected void onDestroy() {
	    super.onDestroy();
	    // Stop the tracker when it is no longer needed.
	    mTracker.stopSession();
	    mAd.destroy();
	  }
}
