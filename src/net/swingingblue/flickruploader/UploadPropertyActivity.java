package net.swingingblue.flickruploader;

import android.app.Activity;
import android.os.Bundle;
import android.widget.GridView;

/**
 * アップロード前の画像ごとの属性設定画面
 * @author findup
 *
 */
public class UploadPropertyActivity extends Activity {

	GridView gridview;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.upload_property_grid);
		
		gridview = (GridView)findViewById(R.id.PropertyGridView);
		
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	
	
	
}
