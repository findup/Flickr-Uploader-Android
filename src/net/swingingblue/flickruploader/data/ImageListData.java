package net.swingingblue.flickruploader.data;

import android.graphics.Bitmap;
import android.net.Uri;

/**
 * リスト部品用のデータクラス
 * @author findup
 *
 */
public class ImageListData {

	// Bitmapのデータ
	private Bitmap bitmap;
	
	// Uri
	private Uri Uri;

	// ファイルパス
	private String path;
	
	// Checkboxのチェック状態
	private boolean check;
	
	public Bitmap getBitmap() {
		return bitmap;
	}

	public void setBitmap(Bitmap bitmap) {
		this.bitmap = bitmap;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public void setUri(Uri uri) {
		Uri = uri;
	}

	public Uri getUri() {
		return Uri;
	}

	public void setCheck(boolean check) {
		this.check = check;
	}

	public boolean isCheck() {
		return check;
	}
	
}
