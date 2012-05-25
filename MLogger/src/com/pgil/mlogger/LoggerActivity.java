/*
    Copyright Pablo Gil Montano, 2012
  
    
    This file is part of MLogger.

    MLogger is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    MLogger is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with MLogger.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.pgil.mlogger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pgil.mlogger.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ToggleButton;

public class LoggerActivity extends Activity{
	
	private static final Logger log = LoggerFactory.getLogger(LoggerActivity.class);
	
	private ToggleButton tbEnableLogger;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        tbEnableLogger = (ToggleButton)findViewById(R.id.tbEnableLogger);
        
        tbEnableLogger.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				if(arg1){
					startService();
				}
				else{
					stopService();
				}
			}
		});
        
    }
    
    
    @Override
	protected void onResume() {
		super.onResume();
		tbEnableLogger.setChecked(LoggerService.running);
	}


	private void startService(){
    	if(LoggerService.running){
    		log.warn("Service already started");
    	}
    	else{
    		Intent intent = new Intent(this, LoggerService.class);
    		startService(intent);
    	}
    }
    
    private void stopService(){
    	if(LoggerService.running == false){
    		log.warn("Service is not running");
    	}
    	else{
    		Intent intent = new Intent(this, LoggerService.class);
    		stopService(intent);
    	}
    }
}