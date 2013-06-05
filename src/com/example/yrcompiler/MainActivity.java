package com.example.yrcompiler;

import grammar.test_grammarLexer;
import grammar.test_grammarParser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session.AccessType;
import com.dropbox.client2.session.TokenPair;

public class MainActivity extends Activity {
	private boolean mLoggedIn;
	final static private String APP_KEY = "2ottkmwadg2d962";
	final static private String APP_SECRET = "xdy0llqhpu8rs8p";
	final static private AccessType ACCESS_TYPE = AccessType.APP_FOLDER;
	final static private String ACCOUNT_PREFS_NAME = "prefs";
    final static private String ACCESS_KEY_NAME = "ACCESS_KEY";
    final static private String ACCESS_SECRET_NAME = "ACCESS_SECRET";
    private Button mSubmit;
    private Button mRetrieve;
    private Button mSave;
    private EditText mCode;
	private EditText mError;
	BufferedReader  reader = null;
	private DropboxAPI<AndroidAuthSession> mApi;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AndroidAuthSession session = buildSession();
		mApi = new DropboxAPI<AndroidAuthSession>(session);
		setContentView(R.layout.activity_main);
		mError= (EditText)findViewById(R.id.editText2);
		mSave = (Button)findViewById(R.id.button2);
		mCode = (EditText)findViewById(R.id.editText1);
		mSubmit = (Button)findViewById(R.id.button3);
		mRetrieve = (Button)findViewById(R.id.button4);
		mRetrieve.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				SaveFileRetrieve download = new SaveFileRetrieve(MainActivity.this, mApi, "/", mCode);
                download.execute();
			}
		});
		mSave.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String Code =mCode.getText().toString();
				 try{
                 	
                     FileWriter fstream = new FileWriter(MainActivity.this.getExternalFilesDir("projdata").getAbsolutePath() + "/code.c",false);
                     BufferedWriter fbw = new BufferedWriter(fstream);
                     fbw.newLine();
                     if(Code!=null)
                     {
                     fbw.write(Code);
                     }
                     fbw.close();
                 }catch (Exception e) {
                     Log.i("ErrorFile: ",e.getMessage());
                 }
				RetrieveFileSave upload = new RetrieveFileSave(MainActivity.this, mApi, "/", mCode);
                upload.execute();
			}
		});
	    mSubmit.setOnClickListener(new OnClickListener() {   
	        	public void onClick(View v) {
	                
	        		// This logs you out if you're logged in, or vice versa
	                if (mLoggedIn) {
	                    logOut();
	                } else {
	                    // Start the remote authentication
	                    mApi.getSession().startAuthentication(MainActivity.this);
	                }
	            }
	        });
		Button compileButton = (Button) findViewById(R.id.button1);
		final PipedOutputStream pipeOut = new PipedOutputStream();

		final PipedInputStream pipeIn;
		try {
			pipeIn = new PipedInputStream(pipeOut);

			 reader = new BufferedReader(new InputStreamReader(pipeIn));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		System.setErr(new PrintStream(pipeOut));
		// Set Click Listener

		compileButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {

				//lexer splits input into tokens
			//	EditText in = (EditText)findViewById(R.id.editText1);
			//	EditText out = (EditText)findViewById(R.id.editText2);

				String source = mCode.getText().toString();
				test_grammarLexer lexer = new test_grammarLexer(new ANTLRStringStream(source));
				test_grammarParser parser = new test_grammarParser(new CommonTokenStream(lexer));

					try {
						parser.function_definition();
					} catch (RecognitionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					try {
						mError.setText("");
						pipeOut.flush();
						if(reader.ready()) {
						String data;
						data = reader.readLine();

						mError.setText(data);
						} else {
							mError.setText("success");
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				
			}});
	}
	 private void logOut() {
	        // Remove credentials from the session
	        mApi.getSession().unlink();

	        // Clear our stored keys
	        clearKeys();
	        // Change UI state to display logged out version
	        setLoggedIn(false);
	    }

	    /**
	     * Convenience function to change UI state based on being logged in
	     */
	    private void setLoggedIn(boolean loggedIn) {
	    	mLoggedIn = loggedIn;
	    	if (loggedIn) {
	    		mSubmit.setText("UnLink");
	            mSave.setVisibility(View.VISIBLE);
	            mRetrieve.setVisibility(View.VISIBLE);
	    	} else {
	    		mSubmit.setText("Link");
	    		mSave.setVisibility(View.GONE);
	            mRetrieve.setVisibility(View.GONE);
	    	}
	    }
	    private AndroidAuthSession buildSession() {
	        AppKeyPair appKeyPair = new AppKeyPair(APP_KEY, APP_SECRET);
	        AndroidAuthSession session;

	        String[] stored = getKeys();
	        if (stored != null) {
	            AccessTokenPair accessToken = new AccessTokenPair(stored[0], stored[1]);
	            session = new AndroidAuthSession(appKeyPair, ACCESS_TYPE, accessToken);
	        } else {
	            session = new AndroidAuthSession(appKeyPair, ACCESS_TYPE);
	        }

	        return session;
	    }
	    private String[] getKeys() {
	        SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
	        String key = prefs.getString(ACCESS_KEY_NAME, null);
	        String secret = prefs.getString(ACCESS_SECRET_NAME, null);
	        if (key != null && secret != null) {
	        	String[] ret = new String[2];
	        	ret[0] = key;
	        	ret[1] = secret;
	        	return ret;
	        } else {
	        	return null;
	        }
	    }

	 protected void onResume() {
	        super.onResume();
	        AndroidAuthSession session = mApi.getSession();

	    
	        if (session.authenticationSuccessful()) {
	            try {
	                // Mandatory call to complete the auth
	                session.finishAuthentication();

	                // Store it locally in our app for later use
	                TokenPair tokens = session.getAccessTokenPair();
	                storeKeys(tokens.key, tokens.secret);
	                setLoggedIn(true);
	            } catch (IllegalStateException e) {
	            }
	        }
	    }

 private void storeKeys(String key, String secret) {
     // Save the access key for later
     SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
     Editor edit = prefs.edit();
     edit.putString(ACCESS_KEY_NAME, key);
     edit.putString(ACCESS_SECRET_NAME, secret);
     edit.commit();
 }

 private void clearKeys() {
     SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
     Editor edit = prefs.edit();
     edit.clear();
     edit.commit();
 }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}