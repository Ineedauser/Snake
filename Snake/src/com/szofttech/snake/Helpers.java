package com.szofttech.snake;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.content.Context;

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
}
