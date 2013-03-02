package com.szofttech.snake;

import com.larvalabs.svgandroid.SVG;
import com.larvalabs.svgandroid.SVGParser;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.widget.ImageView;

public class MainWindow extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_window);
        
        ImageView image=(ImageView)findViewById(R.id.startImage);
        SVG svg = SVGParser.getSVGFromResource(getResources(), R.raw.snake);
        image.setImageDrawable(svg.createPictureDrawable());
    }
 
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main_window, menu);
        return true;
    }
    
}
