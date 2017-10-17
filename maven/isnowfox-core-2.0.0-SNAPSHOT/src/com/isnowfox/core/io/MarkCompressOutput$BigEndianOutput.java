// Decompiled by Jad v1.5.8e2. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://kpdus.tripod.com/jad.html
// Decompiler options: packimports(3) braces fieldsfirst ansi nonlb space 
// Source File Name:   MarkCompressOutput.java

package com.isnowfox.core.io;

import io.netty.buffer.ByteBuf;
import java.io.IOException;
import java.io.OutputStream;

// Referenced classes of package com.isnowfox.core.io:
//			MarkCompressOutput

private static final class MarkCompressOutput$BigEndianOutput extends MarkCompressOutput {

	protected void putShort(int v) throws IOException {
		out.write(v >>> 8);
		out.write(v >>> 0);
	}

	protected void putThree(int v) throws IOException {
		out.write(v >>> 16);
		out.write(v >>> 8);
		out.write(v >>> 0);
	}

	protected void putInt(int v) throws IOException {
		out.write(v >>> 24);
		out.write(v >>> 16);
		out.write(v >>> 8);
		out.write(v >>> 0);
	}

	protected void putFive(long v) throws IOException {
		out.write((int)(v >>> 32));
		out.write((int)(v >>> 24));
		out.write((int)(v >>> 16));
		out.write((int)(v >>> 8));
		out.write((int)(v >>> 0));
	}

	protected void putSix(long v) throws IOException {
		out.write((int)(v >>> 40));
		out.write((int)(v >>> 32));
		out.write((int)(v >>> 24));
		out.write((int)(v >>> 16));
		out.write((int)(v >>> 8));
		out.write((int)(v >>> 0));
	}

	protected void putSeven(long v) throws IOException {
		out.write((int)(v >>> 48));
		out.write((int)(v >>> 40));
		out.write((int)(v >>> 32));
		out.write((int)(v >>> 24));
		out.write((int)(v >>> 16));
		out.write((int)(v >>> 8));
		out.write((int)(v >>> 0));
	}

	protected void putLong(long v) throws IOException {
		out.write((int)(v >>> 56));
		out.write((int)(v >>> 48));
		out.write((int)(v >>> 40));
		out.write((int)(v >>> 32));
		out.write((int)(v >>> 24));
		out.write((int)(v >>> 16));
		out.write((int)(v >>> 8));
		out.write((int)(v >>> 0));
	}

	private MarkCompressOutput$BigEndianOutput(OutputStream out, String charset) {
		super(out, charset);
	}

	public MarkCompressOutput$BigEndianOutput(ByteBuf byteBuf, String charset) {
		super(byteBuf, charset);
	}

}
