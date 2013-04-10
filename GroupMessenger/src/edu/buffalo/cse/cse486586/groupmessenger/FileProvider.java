package edu.buffalo.cse.cse486586.groupmessenger;
import java.io.*;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.util.Log;


public class FileProvider extends ContentProvider{
	
	@Override
	public int delete(Uri arg0, String arg1, String[] arg2) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getType(Uri arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	   public Uri insert(Uri arg0, ContentValues arg1) {
			// TODO Auto-generated method stub
			Context c = getContext();	
			FileOutputStream fos;
			
			String fileValue=arg1.get("key").toString();	
			
				try{				
				fos=c.openFileOutput(fileValue, Context.MODE_PRIVATE);
				fos.write(arg1.get("value").toString().getBytes());
				fos.close();
				}
			 catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			

			return null;
		}

	@Override
	public boolean onCreate() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	   public Cursor query(Uri arg0, String[] arg1, String arg2, String[] arg3,  //PROJECT ,SELECTION, SELECTIONAARGS
				String arg4) {
			// TODO Auto-generated method stub
		   Context context=getContext();
			FileInputStream fis;
			MatrixCursor mCursor=new MatrixCursor(new String[]{"key","value"});
			try {
				fis=context.openFileInput(arg2);
				
				BufferedReader br = new BufferedReader(new InputStreamReader(fis));
				String output=br.readLine();
				br.close();
				mCursor.addRow(new String[]{arg2,output});
				//fis.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return mCursor;
		}

	@Override
	public int update(Uri arg0, ContentValues arg1, String arg2, String[] arg3) {
		// TODO Auto-generated method stub
		return 0;
	}

}
