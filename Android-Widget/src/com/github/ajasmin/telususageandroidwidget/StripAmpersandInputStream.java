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

import java.io.IOException;
import java.io.InputStream;

public class StripAmpersandInputStream extends InputStream {
	private final InputStream is;
	
	public StripAmpersandInputStream(InputStream is) {
		this.is = is;
	}
	
	@Override
	public int available() throws IOException {
		return is.available();
	}
	
	@Override
	public void close() throws IOException {
		is.close();
	}
	
	@Override
	public void mark(int readlimit) {
		is.mark(readlimit);
	}
	
	@Override
	public boolean markSupported() {
		return is.markSupported();
	}
	
	@Override
	public int read() throws IOException {
		int ch = is.read();
		return ch == '&' ? ' ' : ch;
	}
	
	@Override
	public int read(byte[] b) throws IOException {
		int count = is.read(b);
		for (int i=0; i<count; i++)
			if (b[i] == '&')
				b[i] = ' ';
		return count;
	}
	
	@Override
	public int read(byte[] b, int offset, int length) throws IOException {
		int count = is.read(b, offset, length);
		for (int i=offset; i<offset+count; i++)
			if (b[i] == '&')
				b[i] = ' ';
		return count;
	}
	
	@Override
	public synchronized void reset() throws IOException {
		is.reset();
	}
	
	@Override
	public long skip(long byteCount) throws IOException {
		return is.skip(byteCount);
	}
}
