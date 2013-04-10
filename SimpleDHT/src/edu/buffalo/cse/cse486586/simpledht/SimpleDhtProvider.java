package edu.buffalo.cse.cse486586.simpledht;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;


import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class SimpleDhtProvider extends ContentProvider {

	public String successor="";
	public String predecessor="";
	public String portStr="";
	public int count=1;
	public MatrixCursor mcursor=null;
	public int break_ConLoop=0;
	public Uri mUri;
	ServerSocket serverSocket;
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	public Uri buildUri(String scheme, String authority) {
		Uri.Builder uriBuilder = new Uri.Builder();
		uriBuilder.authority(authority);
		uriBuilder.scheme(scheme);
		return uriBuilder.build();
	}
	
	@Override
	public Uri insert(Uri uri, ContentValues values) {
		String key=values.get("key").toString();
		String value = values.get("value").toString();
		{
			FileOutputStream fos;
			try
			{
				String hKey = genHash(key);
				String hPred = genHash(predecessor);
				String hCurr = genHash(portStr);
				int flag = 0;
				if(hPred.compareTo(hCurr)>0)
				{
					if(hKey.compareTo(hPred)>0||hKey.compareTo(hCurr)<0)
						flag=1;
				}
				else 
					if(hPred.compareTo(hCurr)<0)
					{
						if(hKey.compareTo(hCurr)<0 && hKey.compareTo(hPred)>0)
							flag=1;
					}

				if(flag==1)
				{
					fos = getContext().openFileOutput(key, Context.MODE_WORLD_READABLE);
					fos.write(value.getBytes());
					fos.close();
				}
				else if(flag==0)
				{
					int portToBeForwarded=getPortStrNo(successor);
					Socket clientSocket = new Socket(InetAddress.getByName("10.0.2.2"),portToBeForwarded);
					DataOutputStream dos=new DataOutputStream(clientSocket.getOutputStream());
					dos.writeBytes("Insert_Value:"+successor+":"+key+":"+value);
					dos.flush();
					dos.close();
					clientSocket.close();
				}
			}
			catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}

	@Override
	public boolean onCreate() {
		//Get the last 4 digits of the AVD line number
		TelephonyManager tel = (TelephonyManager) this.getContext().getSystemService(Context.TELEPHONY_SERVICE);
		mUri = buildUri("content", "edu.buffalo.cse.cse486586.simpledht.provider");
		portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
		mUri = buildUri("content", "edu.buffalo.cse.cse486586.simpledht.provider");
		
		Context c=this.getContext();
		String a[]=c.fileList();
		for(int i=0;i<a.length;i++)
		c.deleteFile(a[i]);
		
		predecessor=portStr;
		successor=portStr;
		
		try {
			serverSocket = new ServerSocket(10000);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ServerAsyncTask sat = new ServerAsyncTask();

		sat.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		
		if(!portStr.equals("5554"))
		{
			ClientAsyncTask cas = new ClientAsyncTask();
			cas.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,"hi");
		}

		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
			String sortOrder) {
		FileInputStream fis;
		try {
			if(selection.equals("Ldump"))
			{
				String fileList[]=this.getContext().fileList();
				Log.e("length",""+fileList.length);
				
				MatrixCursor mc=new MatrixCursor(new String[]{"key","value"});
				int flag=0;
				for(int i=0;i<fileList.length;i++)
				{
					fis = this.getContext().openFileInput(fileList[i]);	
					BufferedReader br = new BufferedReader(new InputStreamReader(fis));
					String value = br.readLine();
					String hKey = genHash(fileList[i]);
					String hPred = genHash(predecessor);
					String hCurr = genHash(portStr);
					flag = 0;
					if(hPred.compareTo(hCurr)>0)
					{
							if(hKey.compareTo(hPred)>0||hKey.compareTo(hCurr)<0)
								flag=1;
					}
					else
						if(hPred.compareTo(hCurr)<0)
						{
							if(hKey.compareTo(hCurr)<0 && hKey.compareTo(hPred)>0)
								flag=1;
						}

					if(flag==1)
						mc.addRow(new String[]{fileList[i],value});
					
					fis.close();
				}
				
				return (Cursor)mc;
			}
			else if(selection.equals("Gdump"))
			{
				//String fileList[]=this.getContext().fileList();
				//MatrixCursor mc=new MatrixCursor(new String[]{"key","value"});
//				for(int i=0;i<fileList.length;i++)
//				{
//					fis = this.getContext().openFileInput(fileList[i]);	
//					BufferedReader br = new BufferedReader(new InputStreamReader(fis));
//					String value = br.readLine();
//					mc.addRow(new String[]{fileList[i],value});
//					fis.close();
//				}
				
				ClientAsyncTask cas1 = new ClientAsyncTask();
				AsyncTask<String, Void, Cursor> c=cas1.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,"valueofgdump");
				
				return c.get();
			}
			else{
				String hKey = genHash(selection);
				String hPred = genHash(predecessor);
				String hCurr = genHash(portStr);
				int flag = 0;
				//Log.e("in else", selection);
				if(hPred.compareTo(hCurr)>0)
				{
					if(hKey.compareTo(hPred)>0||hKey.compareTo(hCurr)<0)
					{
						flag=1;
						//Log.e("hey", "part1");
					}
				}
					else
						if(hPred.compareTo(hCurr)<0)
						{
							if(hKey.compareTo(hCurr)<0 && hKey.compareTo(hPred)>0)
							{
								flag=1;
								//Log.e("hey", "part2");
							}
						}

				if(flag==1)
				{
					//Log.e("flag1", "part1");
					fis = this.getContext().openFileInput(selection);	
					BufferedReader br = new BufferedReader(new InputStreamReader(fis));
					//Log.e("flag1", "part2");
					String value = br.readLine();
					MatrixCursor mc=new MatrixCursor(new String[]{"key","value"});
					mc.addRow(new String[]{selection,value});
					//Log.e("flag1", "part3");
					fis.close();
					//Log.e("query", selection+"");
					return (Cursor)mc;
				}
				else if(flag==0)
				{
					//Log.e("flag2", "part1");
					int portToBeForwarded = getPortStrNo(successor);
					Socket clientSoc = new Socket(InetAddress.getByName("10.0.2.2"),portToBeForwarded);
					PrintWriter pp=new PrintWriter(clientSoc.getOutputStream());
					BufferedReader br=new BufferedReader(new InputStreamReader(clientSoc.getInputStream()));
					
					//Log.e("flag2", ""+portToBeForwarded);
					
					//Log.e("flag2", ""+successor);
					pp.println("Query_Cond:"+successor+":"+selection);
					pp.flush();

					String mess="";
					while(mess=="")
					{
						mess=br.readLine();
					}
					
					//Log.e("mess", mess);
					//Log.e("query", selection+"");
					MatrixCursor mc=new MatrixCursor(new String[]{"key","value"});
					mc.addRow(new String[]{mess.split(":")[0],mess.split(":")[1]});
					pp.close();
					clientSoc.close();
					return mc;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}	

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	private String genHash(String input) throws NoSuchAlgorithmException {
		MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
		byte[] sha1Hash = sha1.digest(input.getBytes());
		Formatter formatter = new Formatter();
		for (byte b : sha1Hash) {
			formatter.format("%02x", b);
		}
		return formatter.toString();
	}

	
	public class ServerAsyncTask extends AsyncTask<Void, Void, Void>{

		@Override
		protected Void doInBackground(Void... params) {
			// TODO Auto-generated method stub
			try {
				
				while(true)
				{
					Socket clientSocket = serverSocket.accept();

					BufferedReader br=new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
					
					//Log.e("server", "entered");
					String message=br.readLine();
					//Log.e("message", message);
					int flag=0;

					if(message.equals("valueofgdump"))
					{
					
						Cursor c=query(mUri, null, "Ldump", null, null);
						DataOutputStream dos2=new DataOutputStream(clientSocket.getOutputStream());
						if(c.getCount()!=0)
						{
							c.moveToFirst();
						
							String ss="";
							for(int i=0;i<c.getCount();i++)
							{
								ss+=c.getString(c.getColumnIndex("key"))+":"+c.getString(c.getColumnIndex("key"))+":";
								c.moveToNext();
							}
							
							dos2.writeBytes(ss);
							
						}
						else
							dos2.writeBytes("ddd");
						
						dos2.flush();
						dos2.close();
					}
					else if(message.contains("Insert_Value"))
					{
						
						String key  = message.split(":")[2];
						String value = message.split(":")[3];
						ContentValues cvs = new ContentValues();
						cvs.put("key", key);
						cvs.put("value",value);
						insert(mUri,cvs);
					}
					else if(message.contains("Query_Cond"))
					{
						//Log.e("message from other avd", message);
						String temp[]= message.split(":");
						String key = temp[2];
						//Log.e("message received", key);
						Cursor mcd = query(mUri, null, key, null, null);
						if(mcd!=null)
						{
							
							mcd.moveToFirst();
							PrintWriter pw=new PrintWriter(clientSocket.getOutputStream(), true);
							//Log.e("succ mess query", mcd.getString(mcd.getColumnIndex("key"))+":"+mcd.getString(mcd.getColumnIndex("value")));
							pw.println(mcd.getString(mcd.getColumnIndex("key"))+":"+mcd.getString(mcd.getColumnIndex("value")));
							pw.flush();
							pw.close();
						}
					}
					else
					if(portStr.equals("5554"))
					{
						//Log.e("inside", "5554");
						if(message.contains("New_Node"))
						{
							count++;
							Log.e("count", count+"");
							if(count==2)
							{
								String genhmssg = genHash(message.split(":")[1]);
								String genhash5554 = 	genHash("5554");
								if(genhmssg.compareTo(genhash5554) < 0){
									predecessor = message.split(":")[1];
									successor = message.split(":")[1];
									flag=1;
								}else if(genhmssg.compareTo(genhash5554) > 0){
									successor = message.split(":")[1];
									predecessor = message.split(":")[1];
									flag=2;
								}
								Socket clientSocket1=null;
								if(message.split(":")[1].equals("5556"))
									clientSocket1 = new Socket(InetAddress.getByName("10.0.2.2"),11112);
								else if(message.split(":")[1].equals("5558"))
									clientSocket1 = new Socket(InetAddress.getByName("10.0.2.2"),11116);

								DataOutputStream dos=new DataOutputStream(clientSocket1.getOutputStream());

								if(flag==1)
									dos.writeBytes("Succpred:5554:5554");
								else if(flag==2)
									dos.writeBytes("Succpred:5554:5554");
								dos.flush();
								dos.close();
								clientSocket1.close();

							}
							else if(count==3)
							{
								String genhmssg = genHash(message.split(":")[1]);
								String genhash5554 = 	genHash("5554");
								if(genhmssg.compareTo(genhash5554) < 0){
									{
									predecessor = message.split(":")[1];
									flag=1;
									}
								}else if(genhmssg.compareTo(genhash5554) > 0){
									{
										successor = message.split(":")[1];
										flag=2;
									}
								}

								if(flag==1)
								{
									Socket clientSocket1 = new Socket(InetAddress.getByName("10.0.2.2"),11112);
									DataOutputStream dos1=new DataOutputStream(clientSocket1.getOutputStream());
									dos1.writeBytes("Succpred:5554:5558");
									dos1.flush();
									dos1.close();
									clientSocket1.close();
									
									Socket clientSocket2 = new Socket(InetAddress.getByName("10.0.2.2"),11116);
									DataOutputStream dos2=new DataOutputStream(clientSocket2.getOutputStream());
									dos2.writeBytes("Succpred:5556:5554");
									dos2.flush();
									dos2.close();
									clientSocket2.close();
								}
								else if(flag==2)
								{
									Socket clientSocket1 = new Socket(InetAddress.getByName("10.0.2.2"),11112);
									DataOutputStream dos1=new DataOutputStream(clientSocket1.getOutputStream());
									dos1.writeBytes("Succpred:5554:5558");
									dos1.flush();
									dos1.close();
									clientSocket1.close();
									
									Socket clientSocket2 = new Socket(InetAddress.getByName("10.0.2.2"),11116);
									DataOutputStream dos2=new DataOutputStream(clientSocket2.getOutputStream());
									dos2.writeBytes("Succpred:5556:5554");
									dos2.flush();
									dos2.close();
									clientSocket2.close();
								}
							}
						}
					}
					else if(portStr.equals("5556") || portStr.equals("5558"))
					{
						if(message.contains("Succpred"))
						{
							String ans[]=message.split(":");
							successor=ans[1];
							predecessor=ans[2];
						}
						
					}
					
					
//					Log.e("pred", predecessor);
//					Log.e("succ", successor);
					clientSocket.close();
				}
			}catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return null;
		}

	}

	public int getPortStrNo(String portStr)
	{
		if(portStr.equals("5554"))
			return 11108;
		else if(portStr.equals("5556"))
			return 11112;
		else
			return 11116;
	}


	public class ClientAsyncTask extends AsyncTask<String, Void, Cursor>{

		@Override
		protected Cursor doInBackground(String... params) {
			// TODO Auto-generated method stub
			try{
				if(params[0].equals("valueofgdump"))
				{
					System.out.println("client gdump");
					MatrixCursor m= (MatrixCursor) query(mUri, null, "Ldump", null, null);
					if(m!=null)
					System.out.println("local"+m.getCount());
					else
						System.out.println("nothinggggg");
					
					Socket clientSocket11 = new Socket(InetAddress.getByName("10.0.2.2"),Integer.parseInt(predecessor)*2);
					//DataOutputStream dos11=new DataOutputStream(clientSocket11.getOutputStream());
					PrintWriter pr=new PrintWriter(clientSocket11.getOutputStream());
					pr.println("valueofgdump");
					pr.flush();
					Log.e("gggggg", "dddd");
					BufferedReader br11=new BufferedReader(new InputStreamReader(clientSocket11.getInputStream()));
					
					String message="";
					while(message=="")
					{
						message=br11.readLine();
					}
					
					
					if(!message.equals("ddd"))
					{
						String sp[]=message.split(":");
						for(int i=0;i<sp.length;i+=2)
							m.addRow(new String[]{sp[i],sp[i+1]});
						System.out.println("local2 "+m.getCount());
						Log.e("value of 2", m.getCount()+"");
					}	
						pr.close();
						clientSocket11.close();
					
					
					Socket clientSocket21 = new Socket(InetAddress.getByName("10.0.2.2"),Integer.parseInt(successor)*2);
					//DataOutputStream dos21=new DataOutputStream(clientSocket21.getOutputStream());
					PrintWriter pf=new PrintWriter(clientSocket21.getOutputStream());
					pf.println("valueofgdump");
					pf.flush();
					BufferedReader br21=new BufferedReader(new InputStreamReader(clientSocket21.getInputStream()));
					message="";
					while(message=="")
					{
						message=br21.readLine();
					}	
					
					if(!message.equals("ddd"))
					{
						String sp[]=message.split(":");
						for(int i=0;i<sp.length;i+=2)
							m.addRow(new String[]{sp[i],sp[i+1]});
							
						Log.e("value of 2", m.getCount()+"");
						System.out.println("local3 "+m.getCount());
					}	
						pf.close();
						clientSocket21.close();
					
					return m;
				}
				else{
					
						if(portStr.equals("5556"))
						{
							Socket clientSocket = new Socket(InetAddress.getByName("10.0.2.2"),11108);
							DataOutputStream dos=new DataOutputStream(clientSocket.getOutputStream());
							dos.writeBytes("New_Node:5556");
							dos.flush();
							dos.close();
							clientSocket.close();
						}
						else if(portStr.equals("5558"))
						{
							Socket clientSocket = new Socket(InetAddress.getByName("10.0.2.2"),11108);
							DataOutputStream dos=new DataOutputStream(clientSocket.getOutputStream());
							dos.writeBytes("New_Node:5558");
							dos.flush();
							dos.close();
							clientSocket.close();
						}
					}
					
				}
				catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}    	

	
	}
}