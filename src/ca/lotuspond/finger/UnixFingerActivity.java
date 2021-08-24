/*******************************************************************************
 * Copyright (c) 2011 John A. Selmys.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     John A. Selmys - initial API and implementation
 ******************************************************************************/
package ca.lotuspond.finger;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class UnixFingerActivity extends Activity implements Runnable {

	private String finger_output = "Nothing";
	private TextView tv;
	private ProgressDialog pd;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		tv = (TextView) this.findViewById(R.id.textView1);
		//btn = (Button) this.findViewById(R.id.button1);
		//btn.setBackgroundColor(Color.GREEN);
	}

	public void onFingerClick(View view) {
		pd = ProgressDialog.show(this, "Wait", "Connecting", true, false);
		Thread thread = new Thread(this);
		thread.start();
		return;
	}

	public void onAboutClick(View view) {
		tv.setText(R.string.about);
		return;
	}
	
	public void run() {
		EditText mEdit;
		mEdit = (EditText) findViewById(R.id.editText1);
		String address = mEdit.getText().toString();
		if (isValidEmail(address) || isValidEmail("a"+address)) {
			int index = address.indexOf('@');
			String user = address.substring(0, index);
			String host = address.substring(index + 1);
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			Socket s;
			try {
				s = new Socket();
				SocketAddress sa = new InetSocketAddress(host, 79);
				s.connect(sa, 5000);
				PrintStream out = new PrintStream(s.getOutputStream());
				out.println(user);
				DataInputStream in = new DataInputStream(s.getInputStream());
				String line = in.readLine();
				finger_output = line + '\n';
				while (line != null) {
					System.out.println(line);
					line = in.readLine();
					if (line != null)
						finger_output = finger_output + line + '\n';
				}
				s.close();
			} catch (UnknownHostException e) {
				finger_output = "Unknown Host!";
				// Toast.makeText(this, "Sorry, "+host+" is an invalid host!",
				// Toast.LENGTH_LONG).show();
				e.printStackTrace();
			} catch (SocketException e) {
				finger_output = "No Response From Host!";
				// Toast.makeText(this, "Sorry, no response from host!",
				// Toast.LENGTH_LONG).show();
			} catch (IOException e) {
				finger_output = "Connect Error!";
				e.printStackTrace();
			}
		} else {
			finger_output = "Bad Address";
		}
		handler.sendEmptyMessage(0);
	}

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			pd.dismiss();
			tv.setText(finger_output);
		}
	};

	public final static boolean isValidEmail(CharSequence target) {
		try {
			return android.util.Patterns.EMAIL_ADDRESS.matcher(target)
					.matches();
		} catch (NullPointerException exception) {
			return false;
		}
	}

}
