package edu.buffalo.cse.cse486586.groupmessenger;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;

public class GroupMessengerActivity extends Activity {
	int sequenceOfAvd0=0,sequenceOfAvd1=0,sequenceOfAvd2=0;
	ServerSocket serverSocket;
	Socket clientSocket;
	String ipAddress="10.0.2.2";
	Queue<String> holdBackQueue = new LinkedList<String>();//To hold message
	BufferedReader bufferedReader;
	String inputData;
	String portStr;
	Socket sockOfClientSequence;
	DataOutputStream dataOutputStream = null;
	int local_Time_Stamp[]={0,0,0};// Time Stamp of local emulator
	int global_Time_Stamp[]={0,0,0};//Time Stamp of sequencer i.e avd0
	int testCaseNo;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_group_messenger);
		TextView tv = (TextView) findViewById(R.id.textView1);
		tv.setMovementMethod(new ScrollingMovementMethod());
		findViewById(R.id.button1).setOnClickListener(
				new OnPTestClickListener(tv, getContentResolver()));

		final EditText editText=(EditText)findViewById(R.id.editText1);
		TelephonyManager tel =(TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
		portStr= tel.getLine1Number().substring(tel.getLine1Number().length() - 4);

		//Test1 button Click
		findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				ClientClass clientClass=new ClientClass(1);
				clientClass.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);					
				editText.setText("");
			}
		});

		//Test2 Button click
		findViewById(R.id.button3).setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				ClientClass clientClass=new ClientClass(2,0);
				clientClass.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);					
				editText.setText("");
			}
		});
		ServerClass serverClass= new ServerClass();
		serverClass.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_group_messenger, menu);
		return true;
	}
	
	public class ServerClass extends AsyncTask<Void, String, Void>{

		@Override
		protected void onProgressUpdate(String... values) {
			// TODO Auto-generated method stub

			TextView textView = (TextView) findViewById(R.id.textView1);
			textView.append(values[0]+"\n");

		}
		@Override
		protected Void doInBackground(Void... values)
		{
			serverSocket = null;
			clientSocket = null;
			bufferedReader = null;

			try
			{
				serverSocket = new ServerSocket(10000);
				while(true)
				{
					clientSocket = serverSocket.accept();
					bufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
					if(bufferedReader!=null)	
						inputData = bufferedReader.readLine();
					
					//avd0 acts like a sequencer
					if(portStr.equals("5554"))
					{	//Following message checks the sequence of message and orders it accordingly
						boolean sequencerResult = Sequencer(inputData);
						if(sequencerResult)
						{  
						//Multi-cast input to avd5556
						sockOfClientSequence = new Socket(InetAddress.getByName(ipAddress),11112);
						dataOutputStream = new DataOutputStream(sockOfClientSequence.getOutputStream());
						dataOutputStream.writeBytes(inputData);
						sockOfClientSequence.close();

						//Multi-cast input to avd5558
						sockOfClientSequence = new Socket(InetAddress.getByName(ipAddress),11116);
						dataOutputStream = new DataOutputStream(sockOfClientSequence.getOutputStream());
						dataOutputStream.writeBytes(inputData);
						sockOfClientSequence.close();
						}
					}
					//To publish data in avd1 and avd2
					if(!inputData.contains(";"))
					{
						publishProgress(inputData);
					}
					else
					{
						publishProgress(inputData.split(";")[0]);
					}
					//For Test case 2 
					if(inputData.split(";").length==3)
					{
						ClientClass client=new ClientClass(2,2);
						client.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
					}
				}
			}
			catch(IOException e)
			{

			} 

			try {
				//Close the client socket
				if (clientSocket != null) 
					clientSocket.close();

			} catch (final IOException ex) {
			}

			try {
				//Close the Server socket
				if (serverSocket != null) 
					serverSocket.close();

			} catch (final IOException ex) {
			}
			return null;
		}

		protected void onPostExecute(String result) {
		}

		protected void onPreExecute() {
		}
		
		
		public boolean Sequencer(String inputMessage)
		{
			int time_Stamp_Array[]=new int[3];
			String tempMessage=inputMessage.split(";")[1];

			int diff_In_timeStamp=0;
			for(int i=0;i<3;i++)
			{
				time_Stamp_Array[i]=Integer.parseInt(tempMessage.split(",")[i]);
			}

			for(int i=0;i<3;i++)
			{
				if(Math.abs(global_Time_Stamp[i]-time_Stamp_Array[i])>1 || diff_In_timeStamp>1)
				{
				holdBackQueue.add(inputMessage);
				break;
				}
				else if(Math.abs(global_Time_Stamp[i]-time_Stamp_Array[i])==1)
					diff_In_timeStamp++;
			}
			// Local acts as global time stamp when difference is 0 or 1 
			if(diff_In_timeStamp==1 || diff_In_timeStamp==0 || diff_In_timeStamp ==2)
			{
				for(int i=0;i<3;i++)
				{
					global_Time_Stamp[i]=time_Stamp_Array[i];
				}
				return true;
			}
            // It checks the global time stamp with the messages stored in queue
			return false;
		}
	}

	public class ClientClass extends AsyncTask<Void, Void, Void>{

		public int messageToBeDisplayed;
		public ClientClass(int testCase)
		{
			testCaseNo = testCase;
			this.messageToBeDisplayed = 5;
		}
		//This constructor is used for Test2 Case
		public ClientClass(int testCase,int noOfMessageToBeDisplayed)
		{
			testCaseNo = testCase;   
			this.messageToBeDisplayed=noOfMessageToBeDisplayed;   // 2, 0
		}
		@Override
		protected Void doInBackground(Void... para)
		{
			try
			{
				String messge="";
				// For test 2 cases
				if(testCaseNo==2 && messageToBeDisplayed==0)
				{
					if(portStr.equals("5554"))
					{
						local_Time_Stamp[0]+=1;
						messge="avd0:"+sequenceOfAvd0;
						sequenceOfAvd0++;

					}
					else if(portStr.equals("5556")){
						local_Time_Stamp[1]+=1;
						messge="avd1:"+sequenceOfAvd1;
						sequenceOfAvd1++;
					}
					else if(portStr.equals("5558")){
						local_Time_Stamp[2]+=1;
						messge="avd2:"+sequenceOfAvd2;
						sequenceOfAvd2++;
					}
					//Passing every message to sequencer for total ordering

					sockOfClientSequence = new Socket(InetAddress.getByName(ipAddress),11108);
					dataOutputStream = new DataOutputStream(sockOfClientSequence.getOutputStream());
					//Appending the message with local time-stamp
					messge=messge+";";	//avd0:0;0,0,1;6
					messge=messge+local_Time_Stamp[0]+","+local_Time_Stamp[1]+","+local_Time_Stamp[2]+";6";
					dataOutputStream.writeBytes(messge);
					sockOfClientSequence.close();
				}

				messge="";
				for(int i=0;i<messageToBeDisplayed;i++)
				{
					if(portStr.equals("5554"))
					{  
						local_Time_Stamp[0]+=1;
						messge="avd0:"+sequenceOfAvd0;
						sequenceOfAvd0++;
					}
					else if(portStr.equals("5556")){
						local_Time_Stamp[1]+=1;
						messge="avd1:"+sequenceOfAvd1;
						sequenceOfAvd1++;
					}
					else if(portStr.equals("5558")){
						local_Time_Stamp[2]+=1;
						messge="avd2:"+sequenceOfAvd2;
						sequenceOfAvd2++;
					}
					//Passing every message to sequencer for total ordering

					sockOfClientSequence = new Socket(InetAddress.getByName(ipAddress),11108);
					dataOutputStream = new DataOutputStream(sockOfClientSequence.getOutputStream());
					//Appending the message with local time-stamp
					messge=messge+";";	//avd0:0;
					messge=messge+local_Time_Stamp[0]+","; //avd0:0;1,
					messge=messge+local_Time_Stamp[1]+",";//avd0:0;1,0,
					messge=messge+local_Time_Stamp[2];//avd0:0;1,0,1
					dataOutputStream.writeBytes(messge);
					sockOfClientSequence.close();
					if(testCaseNo==1)
						Thread.sleep(3000);
				}
			}
			catch(Exception e){} 
			return null;
		}

		protected void onPostExecute(String result) {
		}

		protected void onPreExecute() {
		}

		protected void onProgressUpdate(Void... values) {

		}
	}


}



