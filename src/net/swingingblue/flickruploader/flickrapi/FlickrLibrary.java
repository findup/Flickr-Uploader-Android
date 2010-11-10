package net.swingingblue.flickruploader.flickrapi;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import org.json.JSONException;
import org.json.JSONObject;

import net.swingingblue.flickruploader.data.RestRequestData;
import net.swingingblue.flickruploader.restful.CountingMultipartEntity.ProgressListener;
import net.swingingblue.flickruploader.restful.RestfulLib;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.util.Log;

/**
 * Flickr Access API
 * 
 * @author tsugimot
 *
 */
public class FlickrLibrary {

	private static final String LOG_TAG = FlickrLibrary.class.getSimpleName();
	
	private static final String baseUrl = "api.flickr.com";
	private static final String servicePath = "services";
	private static final String authPath = "auth";
	private static final String restPath = "rest";
	private static final String uploadPath = "upload";
	private static final String jsonFormat = "json";
	
	// params
	private static final String paramApiKey = "api_key";
	private static final String paramApiSig = "api_sig";
	private static final String paramMethod = "method";
	private static final String paramResponseFormat = "format";
	private static final String paramPerm = "perms";
	private static final String paramFlob = "frob";
	private static final String paramAuthToken = "auth_token";
	private static final String paramPhoto = "photo";
	private static final String paramIsPublic = "is_public";
	private static final String paramTitle = "title";
	
	// methods
	private static final String AUTH_GET_FLOB = "flickr.auth.getFrob";
	private static final String AUTH_GET_TOKEN = "flickr.auth.getToken";
	private static final String AUTH_CHECK_TOKEN = "flickr.auth.checkToken";
	
	// for Development only.
	private static final String apiKey ="fabe03eede6638eabe357a8be3ddc84a";
	private static final String secretKey = "a6745c71e56f4f6c";
	
	// authentic data
	private String frob = "";
	private String token = "";
	
	private static String PREFERENCE_NAME = "flickrData";
	
	private Context context;
	
	//
	
	public FlickrLibrary(Context applicationContext) {
		this.context = applicationContext;
	}

	/**
	 * 認証用のFlobを取得する
	 */
	public void getFlob() {

		// (example)
		// http://flickr.com/services/rest/?method=flickr.auth.getFrob&api_key=987654321&api_sig=5f3870be274f6c49b3e31a0c6728957f
		TreeMap<String, Object> map = new TreeMap<String, Object>();
		
		map.put(paramMethod, AUTH_GET_FLOB);
		map.put(paramApiKey, apiKey);
		map.put(paramResponseFormat, jsonFormat);
		map.put(paramApiSig, makeToken(map));

		RestRequestData request = new RestRequestData();
		request.setUrl(baseUrl);
		request.setPath(servicePath + "/" + restPath + "/");
		request.setQueryParam(map);
		
		URL url = RestfulLib.makeUrl(request);
		String response = RestfulLib.httpGetRequest(url.toString());
		parseFrob(response);
	}
	
	/**
	 * flickeの認証用ページへリダイレクトする
	 */
	public Uri redirectAuthPage() {
		// http://flickr.com/services/auth/?api_key=987654321&perms=write&frob=1a2b3c4d5e&api_sig=6f3870be274f6c49b3e31a0c6728957f
		TreeMap<String , Object> map = new TreeMap<String, Object>();
		
		map.put(paramApiKey, apiKey);
		map.put(paramPerm, "write");
		map.put(paramFlob, frob);
		map.put(paramApiSig, makeToken(map));
		
		RestRequestData request = new RestRequestData();
		request.setUrl(baseUrl);
		request.setPath(servicePath + "/" + authPath + "/");
		request.setQueryParam(map);

		URL url = RestfulLib.makeUrl(request);
		return Uri.parse(url.toString());
	}
	
	/**
	 * 認証用トークン取得要求
	 * Webでの認証後に呼び出される
	 */
	public void getToken() {
		// http://flickr.com/services/rest/?method=flickr.auth.getToken&api_key=987654321&frob=1a2b3c4d5e&api_sig=7f3870be274f6c49b3e31a0c6728957f.
		TreeMap<String , Object> map = new TreeMap<String, Object>();
		
		map.put(paramApiKey, apiKey);
		map.put(paramMethod, AUTH_GET_TOKEN);
		map.put(paramResponseFormat, jsonFormat);
		map.put(paramFlob, frob);
		map.put(paramApiSig, makeToken(map));
		
		RestRequestData request = new RestRequestData();
		request.setUrl(baseUrl);
		request.setPath(servicePath + "/" + restPath + "/");
		request.setQueryParam(map);

		URL url = RestfulLib.makeUrl(request);
		String response = RestfulLib.httpGetRequest(url.toString());
		parseToken(response);
	}
	
	
	/**
	 * 認証Tokenが有効かどうかをチェックする
	 */
	public boolean checkToken() {
		// alreay has Token?
		SharedPreferences preference = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
		this.token = preference.getString("token", null);
		
		if (this.token == null) {
			return false;
		}
		
		// check Token validated.
		// http://flickr.com/services/rest/?method=flickr.auth.checkToken&auth_token=sdkjlsa983&api_key=987654321&frob=1a2b3c4d5e&api_sig=7f3870be274f6c49b3e31a0c6728957f.
		TreeMap<String , Object> map = new TreeMap<String, Object>();
		
		map.put(paramApiKey, apiKey);
		map.put(paramMethod, AUTH_CHECK_TOKEN);
		map.put(paramResponseFormat, jsonFormat);
//		map.put(paramFlob, frob);
		map.put(paramAuthToken, this.token);
		map.put(paramApiSig, makeToken(map));
		
		RestRequestData request = new RestRequestData();
		request.setUrl(baseUrl);
		request.setPath(servicePath + "/" + restPath + "/");
		request.setQueryParam(map);

		URL url = RestfulLib.makeUrl(request);
		String response = RestfulLib.httpGetRequest(url.toString());
		return parseCheckToken(response);
	}

	
	/**
	 * ファイルをアップロードする
	 * @param urlList
	 */
	public String upload(List<String> uriList, final UploadProgressListner listner) {
		
		String response = null;

		TreeMap<String , Object> map = new TreeMap<String, Object>();
		
		map.put(paramApiKey, apiKey);
		map.put(paramResponseFormat, jsonFormat);
		map.put(paramAuthToken, this.token);
		map.put(paramTitle, "upload test");
		map.put(paramIsPublic, "0");
		map.put(paramApiSig, makeToken(map));

		InputStream is = null;
		try {
//			ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uriList.get(0), "r");
//			long size = pfd.getStatSize();
//			try {
//				pfd.close();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
			
//			is = context.getContentResolver().openInputStream(uriList.get(0));
			
			String path = null;
			path = uriList.get(0);
			
			map.put(paramPhoto, path);
			
			RestRequestData request = new RestRequestData();
			request.setUrl(baseUrl);
			request.setPath(servicePath + "/" + uploadPath + "/");
			request.setQueryParam(map);
	
			response = RestfulLib.httpPostRequestMultipart(request, new ProgressListener() {
				
				@Override
				public void transferred(long num, long contentLength) {
					Log.d(LOG_TAG, num + " byte written.");
					listner.onProgress(num, contentLength);
				}
			});
		} catch (Exception e) {
			
		}

		return "";
//		return parseUpload(response);
	}
	
	
	
	/**
	 * リクエスト用のトークンを生成する
	 * @param param
	 * @return
	 */
	private String makeToken(Map<String, Object> map) {
		/*
		8. Signing

		All API calls using an authentication token must be signed. In addition, calls to the flickr.auth.* methods and redirections to the auth page on flickr must also be signed.

		The process of signing is as follows.

		Sort your argument list into alphabetical order based on the parameter name.
		e.g. foo=1, bar=2, baz=3 sorts to bar=2, baz=3, foo=1
		concatenate the shared secret and argument name-value pairs
		e.g. SECRETbar2baz3foo1
		calculate the md5() hash of this string
		append this value to the argument list with the name api_sig, in hexidecimal string form
		e.g. api_sig=1f3870be274f6c49b3e31a0c6728957f		
		*/
	
		TreeMap<String, Object> treeMap = (TreeMap<String, Object>) map;
		Set<Map.Entry<String, Object>> set = treeMap.entrySet();
		Iterator<Entry<String, Object>> it = set.iterator();
		
		String token = new String();
		StringBuilder builder = new StringBuilder();
		
		// まずは先頭にかならずSecureKey
		builder.append(secretKey);
		
		while (it.hasNext()) {
			// ソートして取り出し
			Entry<String, Object> entry = it.next();

			// KeyとValueをくっつける
			builder.append(entry.getKey() + entry.getValue());
		}
		
		// MD5変換
		try {
			MessageDigest digest = MessageDigest.getInstance("MD5");
			try {
				byte[] md5value = digest.digest(builder.toString().getBytes("UTF-8"));
				
				StringBuilder b = new StringBuilder();
				for (int i = 0; i < md5value.length; i++) {
					b.append(String.format("%02x", md5value[i]));
				}
				
				token = b.toString();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
		Log.d(LOG_TAG, "token = " + token);
		
		return token;
	}
	
	/**
	 * flickr APIのJSON responseに含まれるJavascript用メソッド名定義を削除
	 * @param entity
	 * @return
	 */
	private String removeJsonPrefix(String entity) {
		// remove "jsonFlickrApi()"
		entity = entity.replace("jsonFlickrApi(", "");
		entity = entity.replace(")", "");
		
		return entity;
	}

	/**
	 * frobを取り出し
	 * @param entity
	 */
	private void parseFrob(String entity) {

		entity = removeJsonPrefix(entity);
        JSONObject jsonEntity;

        try {
			jsonEntity = new JSONObject(entity);
		
	        if (jsonEntity != null) {
	        	Log.d(LOG_TAG, jsonEntity.toString());
	        	
            	if (!jsonEntity.optString("stat").equals("ok")) {
            		Log.d(LOG_TAG, "failed. : " + jsonEntity.getString("message"));
            	}
            	
            	JSONObject jsonFrob = jsonEntity.optJSONObject("frob");
            	if (jsonFrob != null) {
                	this.frob = jsonFrob.optString("_content");
            	}
	        }
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 認証トークンの解析
	 * @param entity
	 */
	private void parseToken(String entity) {
		entity = removeJsonPrefix(entity);
        JSONObject jsonEntity;

        try {
			jsonEntity = new JSONObject(entity);
		
	        if (jsonEntity != null) {
	        	Log.d(LOG_TAG, jsonEntity.toString());
	        	
            	if (!jsonEntity.optString("stat").equals("ok")) {
            		Log.d(LOG_TAG, "failed. : " + jsonEntity.getString("message"));
            	}
            	
            	JSONObject jsonToken = jsonEntity.optJSONObject("auth").optJSONObject("token");
            	if (jsonToken != null) {
                	this.token = jsonToken.optString("_content");
                	
                	SharedPreferences preference = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
                	Editor editor = preference.edit();
                	editor.putString("token", this.token);
                	editor.commit();
            	}
	        }
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * トークンチェック要求のresposeを解析
	 * @param entity
	 * @return
	 */
	private boolean parseCheckToken(String entity) {
		entity = removeJsonPrefix(entity);
        JSONObject jsonEntity;
        boolean retval = false;

        try {
			jsonEntity = new JSONObject(entity);
		
	        if (jsonEntity != null) {
	        	Log.d(LOG_TAG, jsonEntity.toString());
	        	
            	if (!jsonEntity.optString("stat").equals("ok")) {
            		Log.d(LOG_TAG, "failed. : " + jsonEntity.getString("message"));
            	}
            	
            	JSONObject jsonToken = jsonEntity.optJSONObject("auth").optJSONObject("token");
            	if (jsonToken != null) {
                	this.token = jsonToken.optString("_content");
                	retval = true;
            	}
	        }
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return retval;
	}

	/**
	 * Upload時のレスポンスを解析する
	 * @param entity
	 */
	private String parseUpload(String entity) {
		entity = removeJsonPrefix(entity);
        JSONObject jsonEntity;
        String photoId = null;
        
        try {
			jsonEntity = new JSONObject(entity);
		
	        if (jsonEntity != null) {
	        	Log.d(LOG_TAG, jsonEntity.toString());
	        	
//            	if (!jsonEntity.optString("stat").equals("ok")) {
//            		Log.d(LOG_TAG, "failed. : " + jsonEntity.getString("message"));
//            	}
            	
            	photoId = jsonEntity.optString("photoid");
	        }
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return photoId;
	}
	
	public interface UploadProgressListner {
		void onProgress(long countByte, long size);
	}
}
