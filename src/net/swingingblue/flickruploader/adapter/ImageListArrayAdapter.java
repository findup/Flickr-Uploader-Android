package net.swingingblue.flickruploader.adapter;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.swingingblue.flickruploader.R;
import net.swingingblue.flickruploader.data.ImageListData;
import net.swingingblue.flickruploader.util.BitmapUtil;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * list_picture向けArrayAdaptorクラス
 * @author findup
 *
 */
public class ImageListArrayAdapter extends ArrayAdapter<ImageListData> {

	static class ViewHolder {  
	    TextView text;  
	    ImageView icon;
	    CheckBox checkbox;
	};
	
	private static final String LOG_TAG = ImageListArrayAdapter.class.getSimpleName();
	
	LayoutInflater inflater;
	ExecutorService execute = Executors.newSingleThreadExecutor();

//	AsyncPictureDecoder async = new AsyncPictureDecoder();
	
	final Handler handler = new Handler();	
	
	/** ListViewがスクロール中かどうかの状態を保持 */
	private boolean isScrolling = false;
	
	public void setScrolling(boolean isScrolling) {
		this.isScrolling = isScrolling;
	}

	/**
	 * すでにList<ImageListData>が存在する場合に指定するコンストラクタ
	 * @param context
	 * @param textViewResourceId
	 * @param objects
	 */
	public ImageListArrayAdapter(Context context, int textViewResourceId,
			List<ImageListData> objects) {
		super(context, textViewResourceId, objects);
		
		inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	/**
	 * 空のList<ImageListData>を作成し、動的にaddする場合に使うコンストラクタ
	 * @param context
	 * @param textViewResourceId
	 */
	public ImageListArrayAdapter(Context context, int textViewResourceId) {
		super(context, textViewResourceId);
		inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	/**
	 * ListViewから各行のレンダリングの際に呼び出される。
	 * convertViewもしくは内部でListのレイアウトからViewを生成し、その各部品にデータをセットしていく。
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

//		Log.d(LOG_TAG, "position " + position);
		ViewHolder holder;
		
		// convertviewに値が入ってくるのかは不明
		View view = convertView;
		final ImageListData listdata = getItem(position);
		
		if (view == null) {
			// list_pictureレイアウトをインスタンス化
			view = inflater.inflate(R.layout.list_picture, null);
			
			holder = new ViewHolder();
			holder.icon = (ImageView)view.findViewById(R.id.ImageViewBitmap);
			holder.text = (TextView)view.findViewById(R.id.TextViewPath);
			holder.checkbox = (CheckBox)view.findViewById(R.id.CheckBox);

			view.setTag(holder);
		} else {
			holder = (ViewHolder)view.getTag();
		}
		
		holder.icon.setImageDrawable(view.getContext().getResources().getDrawable(R.drawable.spinner_white_48));
		
		// Layoutをオブジェクトにすると、findViewById()が使えるので目的のUI部品を指定する
		// 対応する番号のインスタンスを取り出し
		if (listdata.getBitmap() == null) {
			
//			async.execute(listdata);
			// ListViewがスクロールしているときは画像は表示しない。スクロールが止まった時のみデコードする
			if (isScrolling == false) {
				execute.submit(new ThreadPictureDecoder(listdata));
			}
		
		} else {
			holder.icon.setImageBitmap(listdata.getBitmap());
		}
		
		holder.text.setText(listdata.getPath().toString());
		holder.checkbox.setChecked(listdata.isCheck());
		
		return view;
	}

	
//	private class AsyncPictureDecoder extends AsyncTask<ImageListData, ImageListData, ImageListData> {
//
//		@Override
//		protected ImageListData doInBackground(ImageListData... param) {
//			
//			try {
//				final ImageListData listdata = param[0];
//				Log.d(LOG_TAG, "Thread running " + listdata.getUri());
//				
//				final Bitmap bitmap = BitmapUtil.getBitmap(getContext(), listdata.getUri());
//				listdata.setBitmap(bitmap);
//				
//				Log.d(LOG_TAG, "Thread end " + listdata.getUri());
//			} catch (IOException e1) {
//				Log.e(LOG_TAG, "Error", e1);
//				e1.printStackTrace();
//			} catch (Exception e) {
//				Log.e(LOG_TAG, "Error", e);
//				e.printStackTrace();
//			}
//			
//			return null;
//		}
//
//		@Override
//		protected void onPostExecute(ImageListData result) {
//			Log.d(LOG_TAG, "Handler running " + result.getUri());
//			notifyDataSetChanged();
//			
//			super.onPostExecute(result);
//		}
//	}

	/**
	 * 画像デコードスレッド
	 */
	private class ThreadPictureDecoder implements Runnable {

		private ImageListData listdata;
		
		public ThreadPictureDecoder(ImageListData listdata) {
			super();
			this.listdata = listdata;
		}

		public void run() {
			try {
				if (listdata.getBitmap() == null) {
					Bitmap bitmap = BitmapUtil.getBitmap(getContext(), listdata.getUri());
					listdata.setBitmap(bitmap);
				
					handler.post(new Runnable() {
						
						public void run() {
							notifyDataSetChanged();
						}
					});
				} else {
//					Log.d(LOG_TAG, "No decorded.");
				}				
			} catch (Exception e) {
				Log.e(LOG_TAG, "Error", e);
				e.printStackTrace();
			}
		}
	}
	
}
