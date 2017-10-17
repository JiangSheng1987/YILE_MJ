// Decompiled by Jad v1.5.8e2. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://kpdus.tripod.com/jad.html
// Decompiler options: packimports(3) braces fieldsfirst ansi nonlb space 
// Source File Name:   MainFrame.java

package com.isnowfox.io.serialize.tool.ui;

import com.isnowfox.io.serialize.tool.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.ExecutionException;
import javax.swing.*;
import org.slf4j.Logger;

// Referenced classes of package com.isnowfox.io.serialize.tool.ui:
//			MainFrame

class MainFrame$5
	implements ActionListener {

	final JButton val$startButtonServer;
	final JButton val$startButtonClient;
	final Config val$c;
	final JTextArea val$logArea;
	final MainFrame this$0;

	public void actionPerformed(ActionEvent e) {
		val$startButtonServer.setEnabled(false);
		val$startButtonClient.setEnabled(false);
		updateCfg();
		(new SwingWorker() {

			final MainFrame._cls5 this$1;

			protected Void doInBackground() throws Exception {
				MessageBuilder b = new MessageBuilder(c, new LogProxy() {

					final _cls1 this$2;

					public transient void info(String str, Object args[]) {
						String msg = String.format(str, args);
						MainFrame.access$300().info(msg);
						logArea.append((new StringBuilder()).append(msg).append("\n").toString());
					}

					 {
						this.this$2 = _cls1.this;
						super();
					}
				});
				b.buildClient();
				return null;
			}

			protected void done() {
				try {
					get();
				}
				catch (InterruptedException e) {
					MainFrame.access$300().info(e.getMessage(), e);
					StringWriter sw = new StringWriter();
					e.printStackTrace(new PrintWriter(sw));
					logArea.append((new StringBuilder()).append(sw.toString()).append("\n").toString());
				}
				catch (ExecutionException ee) {
					Throwable e = ee.getCause();
					MainFrame.access$300().info(e.getMessage(), e);
					StringWriter sw = new StringWriter();
					e.printStackTrace(new PrintWriter(sw));
					logArea.append((new StringBuilder()).append(sw.toString()).append("\n").toString());
				}
				startButtonServer.setEnabled(true);
				startButtonClient.setEnabled(true);
			}

			protected volatile Object doInBackground() throws Exception {
				return doInBackground();
			}

			 {
				this.this$1 = MainFrame._cls5.this;
				super();
			}
		}).execute();
	}

	MainFrame$5() {
		this.this$0 = this$0;
		val$startButtonServer = jbutton;
		val$startButtonClient = jbutton1;
		val$c = config;
		val$logArea = JTextArea.this;
		super();
	}
}
