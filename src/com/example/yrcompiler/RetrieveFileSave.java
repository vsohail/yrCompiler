package com.example.yrcompiler;

import java.io.File;
import java.nio.*;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore.Files;
import android.sax.StartElementListener;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.DropboxFileInfo;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.exception.DropboxIOException;
import com.dropbox.client2.exception.DropboxParseException;
import com.dropbox.client2.exception.DropboxPartialFileException;
import com.dropbox.client2.exception.DropboxServerException;
import com.dropbox.client2.exception.DropboxUnlinkedException;

/**
 * Here we show getting metadata for a directory and downloading a file in a
 * background thread, trying to show typical exception handling and flow of
 * control for an app that downloads a file from Dropbox.
 */

public class RetrieveFileSave extends AsyncTask<Void, Long, Boolean>{


    private Context mContext;
    private final ProgressDialog mDialog;
    private DropboxAPI<?> mApi;
    private String mPath;
    private ImageView mView;
    private Drawable mDrawable;
    private EditText mText;
    private FileOutputStream mFos;
    private boolean mCanceled;
    private Long mFileLen;
    private String mErrorMsg;
    private String cachePath;
    // Note that, since we use a single file name here for simplicity, you
    // won't be able to use this code for two simultaneous downloads.
    //private final static String COMMENT_FILE_NAME = "comments.txt";
    public RetrieveFileSave(Context context, DropboxAPI<?> api,
            String dropboxPath, EditText Edit) {
        // We set the context this way so we don't accidentally leak activities
        mContext = context.getApplicationContext();

        mApi = api;
        mPath = dropboxPath;
        mText = Edit;
        mDialog = new ProgressDialog(context);
        mDialog.setMessage("Downloading Content");
        mDialog.setButton("Cancel", new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                mCanceled = true;
                mErrorMsg = "Canceled";

                // This will cancel the getThumbnail operation by closing
                // its stream
                if (mFos != null) {
                    try {
                        mFos.close();
                    } catch (IOException e) {
                    }
                }
            }
        });

        mDialog.show();
    }
    @SuppressLint("NewApi")
	@Override
    protected Boolean doInBackground(Void... params) {
    	
        try {
        	
            if (mCanceled) {
                return false;
            }

            // Get the metadata for a directory
            Entry dirent = mApi.metadata(mPath, 1000, null, true, null);

            if (!dirent.isDir || dirent.contents == null) {
                // It's not a directory, or there's nothing in it
                mErrorMsg = "File or empty directory";
                return false;
            }
            
            // Make a list of everything in it that we can get a thumbnail for
            ArrayList<Entry> thumbs = new ArrayList<Entry>();
            for (Entry ent: dirent.contents) {
            	if((!ent.isDir))
            	{
                	Log.i("list:",ent.path+" "+ent.fileName());
                    thumbs.add(ent);
            	}
                	// Add it to the list of files we can choose from
  
                }
            	
            if (mCanceled) {
                return false;
            }
           /* String cachePath = mContext.getCacheDir().getAbsolutePath() + "/" + IMAGE_FILE_NAME;
            // Get file.
            FileOutputStream outputStream = null;
            try {
                File file = new File(cachePath);
                outputStream = new FileOutputStream(file);
                DropboxFileInfo info = mApi.getFile("/01.jpg", null, outputStream, null);
                // /path/to/new/file.txt now has stuff in it.
            } catch (DropboxException e) {
                Log.e("DbExampleLog", "Something went wrong while downloading.");
            } catch (FileNotFoundException e) {
                Log.e("DbExampleLog", "File not found.");
            } finally {
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e) {}
                }
            }
            */
           // Now pick a random one
         //   int index = (int)(Math.random() * thumbs.size());
           // Entry ent = thumbs.get(index);
            String path = "/code.c";
            String name = "code.c";
           // int x=name.lastIndexOf('.');
           // extention=name.substring(x+1, name.length());
       //     int y = (int)(Math.random()*10000);
           // mFileLen = ent.bytes;
           // if(ent.thumbExists)
           // {
            cachePath = mContext.getExternalFilesDir("projdata").getAbsolutePath() + "/" +name;
            // Get file.
            FileInputStream inputStream = null;
            try {
                File file = new File(mContext.getExternalFilesDir("projdata").getAbsolutePath() + "/" +name);
                inputStream = new FileInputStream(file);
                Entry newEntry = mApi.putFileOverwrite("/code.c", inputStream,
                        file.length(), null);
                Log.i("DbExampleLog", "The uploaded file's rev is: " + newEntry.rev);
            } catch (DropboxUnlinkedException e) {
                // User has unlinked, ask them to link again here.
                Log.e("DbExampleLog", "User has unlinked.");
            } catch (DropboxException e) {
                Log.e("DbExampleLog", "Something went wrong while uploading.");
            } catch (FileNotFoundException e) {
                Log.e("DbExampleLog", "File not found.");
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {}
                }
            }

            

            // This downloads a smaller, thumbnail version of the file.  The
            // API to download the actual file is roughly the same.
            mDrawable = Drawable.createFromPath(cachePath);
            // We must have a legitimate picture
            return true;
         /*   }
            else if(extention.equalsIgnoreCase("pdf"))
            {
            	Log.i("pdf:", "biatch");
            	cachePath = mContext.getExternalFilesDir("projdata").getAbsolutePath() + "/" +FILE_NAME+y+PDF_FILE;
                // Get file.
                FileOutputStream outputStream = null;
                try {
                    File file = new File(cachePath);
                    file.setReadable(true);
                    file.setWritable(true);
                    file.setExecutable(true);
                    outputStream = new FileOutputStream(file);
                    DropboxFileInfo info = mApi.getFile(path, null, outputStream, null);
                    // /path/to/new/file.txt now has stuff in it.
                } catch (DropboxException e) {
                    Log.e("DbExampleLog", "Something went wrong while downloading.");
                } catch (FileNotFoundException e) {
                    Log.e("DbExampleLog", "File not found.");
                } finally {
                    if (outputStream != null) {
                        try {
                            outputStream.close();
                        } catch (IOException e) {}
                    }
                }
            	return false;
            }
            else if(extention.equalsIgnoreCase("mp3"))
            {
            	Log.i("audio:", "biatch");
            	cachePath = mContext.getExternalFilesDir("projdata").getAbsolutePath() + "/" +FILE_NAME+y+MP3_FILE;
                // Get file.
                FileOutputStream outputStream = null;
                try {
                    File file = new File(cachePath);
                    file.setReadable(true);
                    file.setWritable(true);
                    outputStream = new FileOutputStream(file);
                    DropboxFileInfo info = mApi.getFile(path, null, outputStream, null);
                    // /path/to/new/file.txt now has stuff in it.
                } catch (DropboxException e) {
                    Log.e("DbExampleLog", "Something went wrong while downloading.");
                } catch (FileNotFoundException e) {
                    Log.e("DbExampleLog", "File not found.");
                } finally {
                    if (outputStream != null) {
                        try {
                            outputStream.close();
                        } catch (IOException e) {}
                    }
                }
            	return false;
            }
            else if(extention.equalsIgnoreCase("mp4"))
            {
            	Log.i("video:", "biatch");
            	cachePath = mContext.getExternalFilesDir("projdata").getAbsolutePath() + "/" +FILE_NAME+y+MP4_FILE;
                // Get file.
                FileOutputStream outputStream = null;
                try {
                    File file = new File(cachePath);
                    file.setReadable(true);
                    file.setWritable(true);
                    outputStream = new FileOutputStream(file);
                    DropboxFileInfo info = mApi.getFile(path, null, outputStream, null);
                    // /path/to/new/file.txt now has stuff in it.
                } catch (DropboxException e) {
                    Log.e("DbExampleLog", "Something went wrong while downloading.");
                } catch (FileNotFoundException e) {
                    Log.e("DbExampleLog", "File not found.");
                } finally {
                    if (outputStream != null) {
                        try {
                            outputStream.close();
                        } catch (IOException e) {}
                    }
                }
            	return false;
            }*/
            
        } catch (DropboxUnlinkedException e) {
            // The AuthSession wasn't properly authenticated or user unlinked.
        } catch (DropboxPartialFileException e) {
            // We canceled the operation
            mErrorMsg = "Download canceled";
        } catch (DropboxServerException e) {
            // Server-side exception.  These are examples of what could happen,
            // but we don't do anything special with them here.
            if (e.error == DropboxServerException._304_NOT_MODIFIED) {
                // won't happen since we don't pass in revision with metadata
            } else if (e.error == DropboxServerException._401_UNAUTHORIZED) {
                // Unauthorized, so we should unlink them.  You may want to
                // automatically log the user out in this case.
            } else if (e.error == DropboxServerException._403_FORBIDDEN) {
                // Not allowed to access this
            } else if (e.error == DropboxServerException._404_NOT_FOUND) {
                // path not found (or if it was the thumbnail, can't be
                // thumbnailed)
            } else if (e.error == DropboxServerException._406_NOT_ACCEPTABLE) {
                // too many entries to return
            } else if (e.error == DropboxServerException._415_UNSUPPORTED_MEDIA) {
                // can't be thumbnailed
            } else if (e.error == DropboxServerException._507_INSUFFICIENT_STORAGE) {
                // user is over quota
            } else {
                // Something else
            }
            // This gets the Dropbox error, translated into the user's language
            mErrorMsg = e.body.userError;
            if (mErrorMsg == null) {
                mErrorMsg = e.body.error;
            }
        } catch (DropboxIOException e) {
            // Happens all the time, probably want to retry automatically.
            mErrorMsg = "Network error.  Try again.";
        } catch (DropboxParseException e) {
            // Probably due to Dropbox server restarting, should retry
            mErrorMsg = "Dropbox error.  Try again.";
        } catch (DropboxException e) {
            // Unknown error
            mErrorMsg = "Unknown error.  Try again.";
        }
        return false;
    }

    @Override
    protected void onProgressUpdate(Long... progress) {
        int percent = (int)(100.0*(double)progress[0]/mFileLen + 0.5);
        mDialog.setProgress(percent);
    }

    @Override
    protected void onPostExecute(Boolean result) {
        mDialog.dismiss();
        if (result) {
        /*	File file = new File(cachePath);
        	try{
			FileReader fileReader = new FileReader(file);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
//			StringBuffer stringBuffer = new StringBuffer();
			String line;
			mText.setText("");
			while ((line = bufferedReader.readLine()) != null) {
                mText.append(line+"\n");

			}
            fileReader.close();
        	}
        	catch(Exception e)
        	{}*/
        	Toast error = Toast.makeText(mContext, "File Uploaded Successfully", Toast.LENGTH_LONG);
	        error.show();
           
        } else{
        	Toast error = Toast.makeText(mContext, "File Uploaded UnSuccessfully", Toast.LENGTH_LONG);
	        error.show();
        }
    }


}
