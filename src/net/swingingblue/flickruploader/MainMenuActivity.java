package net.swingingblue.flickruploader;

import net.swingingblue.flickruploader.adapter.ImageListArrayAdapter;
import net.swingingblue.flickruploader.data.ImageListData;
import net.swingingblue.flickruploader.flickrapi.FlickrLibrary;
import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AbsListView.OnScrollListener;

/**
 * メインメニュー画面
 * @author tsugimot
 *
 */
public class MainMenuActivity extends Activity {

	// 画面部品
	private Button btnPick;
	private ListView listview;
	private TextView textview;
	
	private ImageListArrayAdapter listadapter;

	private static final String LOG_TAG = MainMenuActivity.class.getSimpleName();
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		setContentView(R.layout.picturepicker);
		
		btnPick = (Button)findViewById(R.id.BtnPick);
		btnPick.setOnClickListener(lister);
		
		listview = (ListView)findViewById(R.id.ListView);
		listadapter = new ImageListArrayAdapter(this, R.layout.list_picture);

		listview.setAdapter(listadapter);
		listview.setOnScrollListener(new OnScrollListener() {
			
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				Log.d(LOG_TAG, "onScrollStateChanged state " + scrollState);
				
				view.invalidate();
			}
			
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
//				Log.d(LOG_TAG, String.format("onScroll firstVisibleItem: %d, visibleItemCount %d, totalItemCount: %d", firstVisibleItem, visibleItemCount, totalItemCount));
			}
		});
		
		listview.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View view,
					int position, long id) {
				boolean check = listadapter.getItem(position).isCheck();
				listadapter.getItem(position).setCheck(!check);
				listadapter.notifyDataSetChanged();
			}
		});
		
		textview = (TextView)findViewById(R.id.TextViewLog);
		
		super.onCreate(savedInstanceState);
	}
    
	@Override
	protected void onResume() {
		
		// SDが刺さっているかチェック
		if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
			Toast.makeText(this, "No SD Card.", Toast.LENGTH_SHORT).show();
			super.onResume();
//			finish();
			return;
		}
		
		ContentResolver cr = getContentResolver();
		Cursor cl = MediaStore.Images.Media.query(cr, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null);

		cl.moveToFirst();
		int count = cl.getCount();

		for (int i = 1; i < count; i++ ) {
			// ファイルパス名を表示
			Log.d(LOG_TAG, "now " + i + " " + cl.getString(1));

			ImageListData listdata = new ImageListData();
			
			// 画像へのUriは、EXTERNAL_CONTENT_URIにgeString(0)で取れるidを付けることで指定ができる
			Uri uri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cl.getString(0));
			// リスト用データ組み立て
			listdata.setUri(uri);
			listdata.setPath(cl.getString(1));

			listadapter.add(listdata);
			cl.moveToNext();
		}
		
		super.onResume();
	}
    
	
	private OnClickListener lister = new OnClickListener() {
		
		public void onClick(View v) {
			// development test use.
			FlickrLibrary flickrLib = new FlickrLibrary();
			flickrLib.getFlob();
		}
	};    
}