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

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.zip.GZIPInputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;

/**
 * This view displays an animation stored in a special animation resource.<br>
 *
 * <p>It avoids the scrolling issue of using a VideoView inside a ScrollView.</p>
 * <p>The animation format is similar to animated gif but more compact</p>
 * <p>TODO: Publish this as a stand alone project on github and write documentation.</p>
 */
public class AnimationView extends View {
    private static final String ns = "https://github.com/ajasmin/telus_usage_android_widget";

    private int frameIndex = 0;
    private final int animationResource;
    DataInputStream animationStream;
    int width;
    int height;
    ByteBuffer pixelBuffer;
    private Bitmap transparentBitmap;
    private Bitmap animationFrame;
    private boolean redrawPending;

    public AnimationView(Context context) {
        this(context, null);
    }

    public AnimationView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AnimationView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        animationResource = attrs.getAttributeResourceValue(ns, "animation", 0);
        if (animationResource == 0)
            throw new Error("Missing required XML attribute");
    }

    protected void onDraw(Canvas canvas) {
        if (animationFrame == null)
            drawNextFrame();

        Rect rect = new Rect(0, 0, getWidth(), getHeight());

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setDither(true);

        canvas.drawBitmap(animationFrame, null, rect, paint);

        if (!redrawPending) {
            redrawPending = true;
            postDelayed(new Runnable() { public void run() {
                redrawPending = false;
                drawNextFrame();
                invalidate();
            }}, 66);
        }
    }

    private void drawNextFrame() {
        if (animationStream == null) {
            openStream();
        }

        if (animationFrame == null) {
            int frameSize = width*height*4;
            pixelBuffer = ByteBuffer.allocate(frameSize);
            transparentBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            animationFrame = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        }

        try {
            switch (animationStream.read()) {
                case 0: // No change in this frame. Skip it
                    frameIndex++;
                    return;
                case -1: // EOF reached. Start over
                    openStream();
                    frameIndex = 0;
                    drawNextFrame();
                    return;
                case 1: // Read frame
                    byte[] arr = pixelBuffer.array();
                    animationStream.readFully(arr);
                    transparentBitmap.copyPixelsFromBuffer(pixelBuffer);
                    new Canvas(animationFrame).drawBitmap(transparentBitmap, 0, 0, null);

                    frameIndex++;
            }
        } catch (IOException e) {throw new Error(e);};
    }

    private void openStream() {
        try {
            InputStream rawStream = getResources().openRawResource(animationResource);
            animationStream = new DataInputStream(new GZIPInputStream(rawStream));
            width = animationStream.readInt();
            height = animationStream.readInt();
        } catch(IOException e) {throw new Error(e);};
    }

    //// INSTANCE STATE SAVE & RESTORE MACHINERY BELOW
    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);

        ss.frameIndex = this.frameIndex;
        return ss;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());

        // Get back to where we were
        openStream();
        this.frameIndex = 0;
        for (int i=0; i <= ss.frameIndex; i++) {
            drawNextFrame();
        }
    }

    static class SavedState extends BaseSavedState {
        int frameIndex;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            this.frameIndex = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(frameIndex);
        }

        // required field that makes Parcelables from a Parcel
        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }
}
