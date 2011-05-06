/*
 * Copyright (C) 2011 by Alexandre Jasmin <alexandre.jasmin@gmail.com>
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.github.ajasmin.telususageandroidwidget;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class HowToAddWidgetActivity extends Activity {
	int framePos = 0;
	private ImageView animated_image;
	protected Handler animate_handler;
	Bitmap animationFrame;
	private boolean isAnimating;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		isAnimating = false;
		
		setContentView(R.layout.how_to_add_widget);
		
		TextView howToAddText = (TextView) findViewById(R.id.how_to_add_text);
        howToAddText.setText(Html.fromHtml(getString(R.string.how_to_add)));
        
        Button okButton = (Button) findViewById(R.id.ok_button);
        okButton.setOnClickListener(new View.OnClickListener() { public void onClick(View v) {
        	finish();
		}});
        
        animated_image = (ImageView) findViewById(R.id.animated_image);

        if (savedInstanceState != null) {
        	animationFrame = savedInstanceState.getParcelable("animationFrame");
        	framePos = savedInstanceState.getInt("framePos");
        }
    }
	
	protected void onSaveInstanceState(Bundle outState) {
		outState.putInt("framePos", framePos);
		outState.putParcelable("animationFrame", animationFrame);
	};
	
	protected void onResume() {
		super.onResume();
		
		isAnimating = true;
        animate_handler = new Handler();
        animate_handler.postDelayed(animate, 66);
	}

	protected void onPause() {
	    super.onPause();
	    
	    isAnimating = false;
	}
	
	Runnable animate = new Runnable() { public void run() {
		if (!isAnimating)
			return;

		if (animated_image.getWidth() > 0) {
			BitmapDrawable drawable = (BitmapDrawable) getResources().getDrawable(frames[framePos]);
			Bitmap bitmap = drawable.getBitmap();

			if (animationFrame == null) {
				animationFrame = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
			}
			
			Canvas canvas = new Canvas(animationFrame);
			canvas.drawBitmap(bitmap, 0, 0, null);
			
			animated_image.setImageBitmap(animationFrame);
			framePos = ++framePos % frames.length;
		}
        
        animate_handler = new Handler();
        animate_handler.postDelayed(animate, 66);
	}};
	private static final int[] frames = {R.drawable.anim_how00000001, R.drawable.anim_how00000002, R.drawable.anim_how00000002, R.drawable.anim_how00000002, R.drawable.anim_how00000002, R.drawable.anim_how00000002, R.drawable.anim_how00000002, R.drawable.anim_how00000002, R.drawable.anim_how00000009, R.drawable.anim_how00000009, R.drawable.anim_how00000011, R.drawable.anim_how00000011, R.drawable.anim_how00000011, R.drawable.anim_how00000011, R.drawable.anim_how00000015, R.drawable.anim_how00000016, R.drawable.anim_how00000017, R.drawable.anim_how00000018, R.drawable.anim_how00000019, R.drawable.anim_how00000020, R.drawable.anim_how00000021, R.drawable.anim_how00000022, R.drawable.anim_how00000022, R.drawable.anim_how00000024, R.drawable.anim_how00000025, R.drawable.anim_how00000025, R.drawable.anim_how00000027, R.drawable.anim_how00000028, R.drawable.anim_how00000029, R.drawable.anim_how00000030, R.drawable.anim_how00000030, R.drawable.anim_how00000030, R.drawable.anim_how00000033, R.drawable.anim_how00000034, R.drawable.anim_how00000035, R.drawable.anim_how00000036, R.drawable.anim_how00000037, R.drawable.anim_how00000038, R.drawable.anim_how00000039, R.drawable.anim_how00000040, R.drawable.anim_how00000040, R.drawable.anim_how00000040, R.drawable.anim_how00000040, R.drawable.anim_how00000040, R.drawable.anim_how00000040, R.drawable.anim_how00000040, R.drawable.anim_how00000047, R.drawable.anim_how00000047, R.drawable.anim_how00000049, R.drawable.anim_how00000049, R.drawable.anim_how00000051, R.drawable.anim_how00000051, R.drawable.anim_how00000053, R.drawable.anim_how00000054, R.drawable.anim_how00000055, R.drawable.anim_how00000056, R.drawable.anim_how00000056, R.drawable.anim_how00000058, R.drawable.anim_how00000058, R.drawable.anim_how00000058, R.drawable.anim_how00000058, R.drawable.anim_how00000062, R.drawable.anim_how00000062, R.drawable.anim_how00000062, R.drawable.anim_how00000062, R.drawable.anim_how00000066, R.drawable.anim_how00000066, R.drawable.anim_how00000068, R.drawable.anim_how00000069, R.drawable.anim_how00000070, R.drawable.anim_how00000070, R.drawable.anim_how00000070, R.drawable.anim_how00000070, R.drawable.anim_how00000074, R.drawable.anim_how00000074, R.drawable.anim_how00000074, R.drawable.anim_how00000074, R.drawable.anim_how00000078, R.drawable.anim_how00000079, R.drawable.anim_how00000080, R.drawable.anim_how00000080, R.drawable.anim_how00000080, R.drawable.anim_how00000083, R.drawable.anim_how00000084, R.drawable.anim_how00000085, R.drawable.anim_how00000085, R.drawable.anim_how00000087, R.drawable.anim_how00000088, R.drawable.anim_how00000088, R.drawable.anim_how00000090, R.drawable.anim_how00000091, R.drawable.anim_how00000091, R.drawable.anim_how00000091, R.drawable.anim_how00000094, R.drawable.anim_how00000094, R.drawable.anim_how00000094, R.drawable.anim_how00000094, R.drawable.anim_how00000098, R.drawable.anim_how00000098, R.drawable.anim_how00000100, R.drawable.anim_how00000101, R.drawable.anim_how00000102, R.drawable.anim_how00000103, R.drawable.anim_how00000104, R.drawable.anim_how00000105, R.drawable.anim_how00000105, R.drawable.anim_how00000105, R.drawable.anim_how00000108, R.drawable.anim_how00000109, R.drawable.anim_how00000109, R.drawable.anim_how00000109, R.drawable.anim_how00000112, R.drawable.anim_how00000112, R.drawable.anim_how00000112, R.drawable.anim_how00000115, R.drawable.anim_how00000116, R.drawable.anim_how00000117, R.drawable.anim_how00000117, R.drawable.anim_how00000117, R.drawable.anim_how00000120, R.drawable.anim_how00000121, R.drawable.anim_how00000121, R.drawable.anim_how00000123, R.drawable.anim_how00000123, R.drawable.anim_how00000123, R.drawable.anim_how00000123, R.drawable.anim_how00000127, R.drawable.anim_how00000127, R.drawable.anim_how00000127, R.drawable.anim_how00000127, R.drawable.anim_how00000131, R.drawable.anim_how00000131, R.drawable.anim_how00000131, R.drawable.anim_how00000134, R.drawable.anim_how00000134, R.drawable.anim_how00000134, R.drawable.anim_how00000137, R.drawable.anim_how00000138, R.drawable.anim_how00000139, R.drawable.anim_how00000139, R.drawable.anim_how00000141, R.drawable.anim_how00000141, R.drawable.anim_how00000143, R.drawable.anim_how00000144, R.drawable.anim_how00000145, R.drawable.anim_how00000146, R.drawable.anim_how00000147, R.drawable.anim_how00000148, R.drawable.anim_how00000149, R.drawable.anim_how00000150, R.drawable.anim_how00000151, R.drawable.anim_how00000152, R.drawable.anim_how00000152, R.drawable.anim_how00000152, R.drawable.anim_how00000152, R.drawable.anim_how00000152, R.drawable.anim_how00000152, R.drawable.anim_how00000152, R.drawable.anim_how00000152, R.drawable.anim_how00000152, R.drawable.anim_how00000152, R.drawable.anim_how00000162, R.drawable.anim_how00000162, R.drawable.anim_how00000162, R.drawable.anim_how00000162, R.drawable.anim_how00000162, R.drawable.anim_how00000162, R.drawable.anim_how00000162, R.drawable.anim_how00000162, R.drawable.anim_how00000170, R.drawable.anim_how00000170, R.drawable.anim_how00000170, R.drawable.anim_how00000170, R.drawable.anim_how00000170, R.drawable.anim_how00000170, R.drawable.anim_how00000170, R.drawable.anim_how00000177, R.drawable.anim_how00000177, R.drawable.anim_how00000177, R.drawable.anim_how00000177, R.drawable.anim_how00000177, R.drawable.anim_how00000177, R.drawable.anim_how00000177, R.drawable.anim_how00000177, R.drawable.anim_how00000177, R.drawable.anim_how00000177, R.drawable.anim_how00000177, R.drawable.anim_how00000177, R.drawable.anim_how00000177, R.drawable.anim_how00000177, R.drawable.anim_how00000177, R.drawable.anim_how00000177};
}
