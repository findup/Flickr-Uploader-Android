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
	
	private String fileName;
	
	private long takendate;
	
	// [_id, _data, _size, _display_name, mime_type, title, date_added, date_modified, description, picasa_id, isprivate, latitude, longitude, datetaken, orientation, mini_thumb_magic, bucket_id, bucket_display_name, micro_thumb_id, sd_serial]
	
	public long getTakendate() {
		return takendate;
	}

	public void setTakendate(long takendate) {
		this.takendate = takendate;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

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
