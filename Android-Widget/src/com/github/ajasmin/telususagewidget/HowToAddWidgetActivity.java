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

package com.github.ajasmin.telususagewidget;

import java.io.InputStream;


import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.graphics.Paint;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;

public class HowToAddWidgetActivity extends Activity {
	private Movie movie;
	private long movieStart;
	private ImageView animated_image;
	protected Handler animate_handler;
	Bitmap animationFrame = Bitmap.createBitmap(240, 320, Bitmap.Config.ARGB_8888);
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

        if (movie == null) {
	        InputStream is = getResources().openRawResource(R.drawable.how_to_add_anim_gif);
	        movie = Movie.decodeStream(is);
        }
        if (savedInstanceState != null)
        	movieStart = savedInstanceState.getLong("movieStart");
    }
	
	protected void onSaveInstanceState(Bundle outState) {
		outState.putLong("movieStart", movieStart);
	};
	
	protected void onResume() {
		super.onResume();
		
		isAnimating = true;
        animate_handler = new Handler();
        animate_handler.postDelayed(animate, 30);
	}

	protected void onPause() {
	    super.onPause();
	    
	    isAnimating = false;
	}
	
	Runnable animate = new Runnable() { public void run() {
		if (!isAnimating)
			return;
        long now = android.os.SystemClock.uptimeMillis();
        if (movieStart == 0) {   // first time
            movieStart = now;
        }
        if (movie != null) {
            int dur = movie.duration();
            if (dur == 0) {
                dur = 1000;
            }
            int relTime = (int)((now - movieStart) % dur);
            movie.setTime(relTime);
            movie.draw(new Canvas(animationFrame), 0, 0);
            animated_image.setImageBitmap(animationFrame);
        }
        animate_handler = new Handler();
        animate_handler.postDelayed(animate, 30);
	}};
}
