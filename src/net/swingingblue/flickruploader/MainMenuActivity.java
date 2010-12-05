package net.swingingblue.flickruploader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import net.swingingblue.flickruploader.adapter.ImageListArrayAdapter;
import net.swingingblue.flickruploader.data.ImageListData;
import net.swingingblue.flickruploader.flickrapi.FlickrLibrary;
import net.swingingblue.flickruploader.flickrapi.FlickrLibrary.UploadProgressListner;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources.NotFoundException;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AbsListView.OnScrollListener;

/**
 * メインメニュー画面
 * @author tsugimot
 *
 */
public class MainMenuActivity extends Activity {

	// 画面部品
	private Button btnUpload;
	private ListView listview;
	private ProgressDialog progressDialog;
	
	private ImageListArrayAdapter listadapter;

	private FlickrLibrary flickrLib;
	
	private static final String LOG_TAG = MainMenuActivity.class.getSimpleName();
	/** StartActivityForResult()でブラウザ起動時に使う定数 */
	private static final int FLICKR_AUTH_REQUEST_CODE = 1234;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		setContentView(R.layout.picturepicker);
		
		btnUpload = (Button)findViewById(R.id.ButtonUpload);
		btnUpload.setOnClickListener(uploadBtnListener);
		// 認証が終わるまでは押せない
		btnUpload.setEnabled(false);
		
		listview = (ListView)findViewById(R.id.ListView);
		listadapter = new ImageListArrayAdapter(this, R.layout.list_picture);

		listview.setAdapter(listadapter);
		listview.setOnScrollListener(new OnScrollListener() {
			
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				Log.d(LOG_TAG, "onScrollStateChanged state " + scrollState);
				
				// スクロール状態をAdaptorに通知。Adaptorの中からは自分で取れなさそうなので仕方なく。
				((ImageListArrayAdapter)view.getAdapter()).setScrolling((scrollState != SCROLL_STATE_IDLE));

				if (scrollState == SCROLL_STATE_IDLE) {
					// スクロールが止まったらListViewを再表示（画像を表示）
					view.invalidateViews();
				}
			}
			
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				Log.d(LOG_TAG, String.format("onScroll firstVisibleItem: %d, visibleItemCount %d, totalItemCount: %d", firstVisibleItem, visibleItemCount, totalItemCount));
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
		
		// flickr ライブラリ初期化
		flickrLib = new FlickrLibrary(getApplicationContext());
		
		progressDialog = new ProgressDialog(this);

		// flickr認証処理
		autholization();
		
		super.onCreate(savedInstanceState);
	}
    
	@Override
	protected void onResume() {
		
		// SDが刺さっているかチェック
		if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
			Toast.makeText(this, R.string.no_sd_card, Toast.LENGTH_SHORT).show();
			super.onResume();
			return;
		}
		
		ContentResolver cr = getContentResolver();
		Cursor cl = MediaStore.Images.Media.query(cr, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, "date_added desc");

		cl.moveToFirst();
		
		// [_id, _data, _size, _display_name, mime_type, title, date_added, date_modified, description, picasa_id, isprivate, latitude, longitude, datetaken, orientation, mini_thumb_magic, bucket_id, bucket_display_name, micro_thumb_id, sd_serial]
		int count = cl.getCount();

		for (int i = 1; i < count; i++ ) {
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
	
	/**
	 * UploadボタンClick
	 */
	private OnClickListener uploadBtnListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			ArrayList<String> checkedList = new ArrayList<String>();
			// リストの中からチェックがついたものを列挙
			int count = listadapter.getCount();
			for (int i = 0; i < count; i++) {
				if (listadapter.getItem(i).isCheck()) {
					checkedList.add(listadapter.getItem(i).getPath());
				}
			}
			
			AsyncUpload asyncUpload = new AsyncUpload();
			asyncUpload.execute(checkedList);
		}
	};
	
	private class AsyncUpload extends AsyncTask<ArrayList<String>, Long, Void> {

		private int count = 0;
		private int size = 0;
		
		/**
		 * バックグラウンド処理からのコールバック（UIスレッド）
		 */
		@Override
		protected void onProgressUpdate(Long... values) {
			super.onProgressUpdate(values);
			
			StringBuilder sb = new StringBuilder();
			sb.append(getResources().getString(R.string.uploading));
			sb.append(" ");
			sb.append(this.count);
			sb.append("/");
			sb.append(this.size);
			progressDialog.setMessage(sb);
			
			progressDialog.setMax((int)(values[1] / 100));
			progressDialog.setProgress(values[0].intValue() / 100);
		}

		/**
		 * 後処理（UIスレッド）
		 */
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			progressDialog.dismiss();
			Toast.makeText(getApplicationContext(), "Upload complete.", Toast.LENGTH_SHORT).show();
		}

		/**
		 * 前処理（UIスレッド）
		 */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			
			progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			progressDialog.setMessage(getResources().getString(R.string.uploading));
			progressDialog.show();
		}

		/**
		 * 実際のバックグラウンド処理
		 */
		@Override
		protected Void doInBackground(ArrayList<String>... params) {
			
			ArrayList<String> list = params[0];
			
			Iterator<String> it = list.iterator();
			count = 0;
			size = list.size();
			
			while (it.hasNext()) {
				count++;
				
				String uri = it.next();
				flickrLib.upload(uri, new UploadProgressListner() {
					
					@Override
					public void onProgress(long countByte, long size) {
						publishProgress(countByte, size);
					}
				});
			}
			
			return null;
		}
		
	}
	
	/**
	 * FlickeのTokenが登録済みなら認証を試みる。
	 * Tokenが無い、もしくは期限切れの場合はFlickrの認証ページへリダイレクトする
	 */
	private void autholization() {
		try {
		
			flickrLib.getFlob();
			
			if (flickrLib.checkToken() == false) {
				// ダイアログを表示
				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
				alertDialogBuilder.setTitle(R.string.title_required_auth)
				.setCancelable(false)
				.setMessage(R.string.notify_required_auth)
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						redirectFlickr();
					}
				})
				.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				})
				.show();
				
			} else {
				// 認証済み
//				Toast.makeText(getApplicationContext(), "authentificated.", Toast.LENGTH_LONG).show();
				btnUpload.setEnabled(true);
			}
		} catch (IOException e) {
			// allmost IOException means Internet connection probrem.
			e.printStackTrace();
			btnUpload.setEnabled(false);

			showConnectionErrorDialog();
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
		if (btnUpload.isEnabled() == false) {
			menu.add(Menu.NONE, R.string.authorization, Menu.NONE, R.string.authorization);
		}
		
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		
		if (item.getItemId() == R.string.authorization) {
			// flickrサイトへ
			Toast.makeText(this,R.string.notify_required_auth, Toast.LENGTH_SHORT).show();
			redirectFlickr();
		}
		
		return super.onMenuItemSelected(featureId, item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		Log.d(LOG_TAG, String.format("onActivityResult req = %d, ret = %d", requestCode, resultCode));
		
		// ブラウザからの認証が完了しているか
		if (requestCode == FLICKR_AUTH_REQUEST_CODE) {
			// Tokenを保存
			try {
				if (flickrLib.getToken()) {
					btnUpload.setEnabled(true);
				} else {
					Toast.makeText(this, R.string.auth_err, Toast.LENGTH_LONG).show();
				}
			} catch (IOException e) {
				e.printStackTrace();
				btnUpload.setEnabled(false);
				
				showConnectionErrorDialog();
			}
		}
		
		super.onActivityResult(requestCode, resultCode, data);
	}

	/**
	 * ブラウザを起動してflickrの認証サイトに飛ぶ
	 */
	private void redirectFlickr() {
		// flickrサイトへリダイレクト
		Uri uri = flickrLib.redirectAuthPage();
		
		Intent i = new Intent(Intent.ACTION_VIEW, uri);
		startActivityForResult(i, FLICKR_AUTH_REQUEST_CODE);
	}
	
	/**
	 * 
	 */
	private void showConnectionErrorDialog() {

		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
		dialogBuilder.setMessage(R.string.connection_error)
		.setCancelable(false)
		.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// just close dialog. no more something else.
			}
		});

		AlertDialog alert = dialogBuilder.create();
		alert.show();		
	}
	
	
}