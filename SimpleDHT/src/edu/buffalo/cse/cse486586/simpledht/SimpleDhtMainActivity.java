package edu.buffalo.cse.cse486586.simpledht;

import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class SimpleDhtMainActivity extends Activity {

	ContentResolver cr;
	ContentValues cv;
	Uri muri;
	public static final String KEY_FIELD = "key";
	public static final String VALUE_FIELD = "value";
	TextView tv ;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_dht_main);
        
        tv = (TextView) findViewById(R.id.textView1);
        tv.setMovementMethod(new ScrollingMovementMethod());
        muri = buildUri("content", "edu.buffalo.cse.cse486586.simpledht.provider");
		cr=getContentResolver();
        
		
        findViewById(R.id.button1).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Cursor c=cr.query(muri, null, "Ldump", null, null);
				c.moveToFirst();
				String key="";
				String value="";
				tv.append("LDUMP"+"\n");
				for(int i = 0;i<c.getCount();i++)
                {
                    key = c.getString(c.getColumnIndex(KEY_FIELD));
                    value = c.getString(c.getColumnIndex(VALUE_FIELD));
                    tv.append("KEY: "+key+" VAL: "+value);
                    tv.append("\n");
                    c.moveToNext();
                }
						
			}
		});
		
        findViewById(R.id.button2).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Cursor c=cr.query(muri, null, "Gdump", null, null);
				
					c.moveToFirst();
					tv.append("GDUMP"+"\n");
				 for(int i = 0;i<c.getCount();i++)
	                {
	                    String key = c.getString(c.getColumnIndex(KEY_FIELD));
	                    String value = c.getString(c.getColumnIndex(VALUE_FIELD));
	                    tv.append("KEY: "+key+" VAL: "+value);
	                    tv.append("\n");
	                    c.moveToNext();
	                }
			}
		});
        
        findViewById(R.id.button3).setOnClickListener(
                new OnTestClickListener(tv, getContentResolver()));
        
        Log.i("oncreatemain", "endofcreate");
        
    }
    private Uri buildUri(String scheme, String authority) {
		Uri.Builder uriBuilder = new Uri.Builder();
		uriBuilder.authority(authority);
		uriBuilder.scheme(scheme);
		return uriBuilder.build();
	}
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_simple_dht_main, menu);
        return true;
    }

}
