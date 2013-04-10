
package edu.buffalo.cse.cse486586.simplemessenger;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class SimpleMessenger extends Activity {

	//Initializing Class variables
	private Socket clientSocket= null ;
	private DataOutputStream outp_client = null;
	private String portStr;
	boolean connection = false;
	String text = null; 
	String serverText=null,clientText=null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		TelephonyManager tel = (TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE);
		//To find the port number of Emulator
		portStr = tel.getLine1Number().substring(tel.getLine1Number().length()-	4) ;
		
		// To Start the server 
		new ServerAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		
		//Creating TextBox
		final EditText editText = (EditText) findViewById(R.id.editText1);
		
		// Getting text enter in the text box
		clientText = editText.getText().toString();
		
		//Creating "send" button
		final Button button = (Button) findViewById(R.id.button1);
		
		// OnClick of Send button
		button.setOnClickListener(new View.OnClickListener() {

			//@Override
			public void onClick(View v) {
				// Call Client  class
				if(clientText.getBytes().length <= 128 ){
					new ClientAsyncTask(clientText).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
					editText.setText("");
				}
				else
				{
					Toast.makeText(getApplicationContext(), "Only 128 bytes of characters are allowed", Toast.LENGTH_LONG).show();
				}
			}
		});

		editText.setOnKeyListener(new OnKeyListener() {

			public boolean onKey(View arg0, int keyCode, KeyEvent event) {
				if(keyCode != KeyEvent.KEYCODE_ENTER)
				{
					clientText = editText.getText().toString();
				}
				if((keyCode== KeyEvent.KEYCODE_ENTER))
				{    
					if(clientText.getBytes().length <= 128 ){
						new ClientAsyncTask(clientText).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
						editText.setText(""); 
					}
					else
					{
						Toast.makeText(getApplicationContext(), "Only 128 bytes of characters are allowed", Toast.LENGTH_LONG).show();
					}
				}
				return false;
			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	private class ServerAsyncTask extends AsyncTask<Void, String, Void>{

		@Override
		protected void onProgressUpdate(String... serverText) {
			// TODO Auto-generated method stub
			//Displaying the text in the textView 
			TextView textView  = (TextView) findViewById(R.id.textView1);
			textView.setText(serverText[0]);
		}
		@Override
		protected Void doInBackground(Void... params) {

			try {
				//Opening the server port 
				ServerSocket serv = new ServerSocket(10000);

				while(true)
				{
					//Accepting the socket connection
					Socket servSocket = serv.accept();

					//Reading the client message using buffered reader
					BufferedReader br = new BufferedReader(new InputStreamReader(servSocket.getInputStream()));

					//Reading line-by-line message
					serverText = br.readLine();

					//Passing the server text to GUI which invokes onProgressUpdate() 
					publishProgress(serverText);

					//Close the Buffer Stream reader
					br.close();

					//Close the server socket
					servSocket.close();

				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
	}
    	private class ClientAsyncTask extends AsyncTask<Void,Void,Void>{

		private String clientTextmessage;
		private ClientAsyncTask(String text){
			this.clientTextmessage= text;
		}


		@Override
		protected Void doInBackground(Void... params) {
			try {
				//avd0 5554 emulator is used
				if(portStr.equals("5554")){

					clientSocket = new Socket (InetAddress.getByName("10.0.2.2"), 11112);
				}
				//avd1 5556 emulator is used
				else if(portStr.equals("5556"))
				{    
					clientSocket = new Socket (InetAddress.getByName("10.0.2.2"), 11108);
				}
				else {

					Toast.makeText(getApplicationContext(), "Connection not established", Toast.LENGTH_LONG).show();
				}
				
				//Client sending data to Dataoutput Stream
				outp_client = new DataOutputStream(clientSocket.getOutputStream());
				
				clientTextmessage=clientTextmessage+'\n';

				//Writing the data to Stream
				outp_client.writeBytes(clientTextmessage);

				//Flush the output stream
				outp_client.flush();
				
				//close the output stream
				outp_client.close();
				
				// Close the client socket
				clientSocket.close();

			} catch (Exception e) {

				e.printStackTrace();
			}
			return null;
		}

	}
}
