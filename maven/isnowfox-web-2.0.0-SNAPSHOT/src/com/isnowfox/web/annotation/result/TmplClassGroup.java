// Decompiled by Jad v1.5.8e2. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://kpdus.tripod.com/jad.html
// Decompiler options: packimports(3) braces fieldsfirst ansi nonlb space 
// Source File Name:   TmplClassGroup.java

package com.isnowfox.web.annotation.result;

import java.lang.annotation.Annotation;

// Referenced classes of package com.isnowfox.web.annotation.result:
//			TmplClassResult

public interface TmplClassGroup
	extends Annotation {

	public abstract TmplClassResult[] value();
}
