package com.szofttech.snake;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class Helpers {
	public static String readTextFileFromRawResource(final Context context, final int resourceId) throws IOException{
        final InputStream inputStream = context.getResources().openRawResource(resourceId);
        final InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        final BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
 
        String nextLine;
        final StringBuilder body = new StringBuilder();
 
        while ((nextLine = bufferedReader.readLine()) != null){
            body.append(nextLine);
            body.append('\n');
        }
       
        return body.toString();
    }


	static public void showErrorMessage(final Context context, int messageId, int titleId){
		AlertDialog alertDialog = new AlertDialog.Builder(context).create();
		alertDialog.setTitle(titleId);
		alertDialog.setMessage(context.getString(messageId));
		
		// Setting Icon to Dialog
		alertDialog.setIcon(android.R.drawable.ic_dialog_alert);
	
		alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL, context.getString(R.string.error_dialog_close_button),
																			new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int which) {
		        }
		});
		
		alertDialog.show();
	}
}
