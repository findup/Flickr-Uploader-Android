package net.swingingblue.flickruploader.flickrapi;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import net.swingingblue.flickruploader.data.RestRequestData;
import net.swingingblue.flickruploader.restful.RestfulLib;

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
	
	private static final String baseUrl = "flickr.com";
	private static final String servicePath = "services";
	private static final String authPath = "auth";
	private static final String restPath = "rest";
	
	private static final String paramApiKey = "api_key";
	private static final String paramApiSig = "api_sig";
	private static final String paramMethod = "method";
	
	// for Development only.
	private static final String apiKey ="xxxxxxxx";
	private static final String secretKey = "yyyyyyy";
	
	// Auth
	// Upload
	// list

	/**
	 * 認証用のFlobを取得する
	 */
	public void getFlob() {

		// (example)
		// http://flickr.com/services/rest/?method=flickr.auth.getFrob&api_key=987654321&api_sig=5f3870be274f6c49b3e31a0c6728957f
		TreeMap<String, String> map = new TreeMap<String, String>();
		
		map.put(paramMethod, "flickr.auth.getFrob");
		map.put(paramApiKey, apiKey);
		map.put(paramApiSig, makeToken(map));

		RestRequestData request = new RestRequestData();
		request.setUrl(baseUrl);
		request.setPath(servicePath + "/" + restPath);
		request.setQueryParam(map);
		
		RestfulLib.makeUrl(request);
	}
	
	/**
	 * リクエスト用のトークンを生成する
	 * @param param
	 * @return
	 */
	private String makeToken(Map<String, String> map) {
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
	
		TreeMap<String, String> treeMap = (TreeMap<String, String>) map;
		Set<Map.Entry<String, String>> set = treeMap.entrySet();
		Iterator<Entry<String, String>> it = set.iterator();
		
		String token = new String();
		StringBuilder builder = new StringBuilder();
		
		// まずは先頭にかならずSecureKey
		builder.append(secretKey);
		
		while (it.hasNext()) {
			// ソートして取り出し
			Entry<String, String> entry = it.next();

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
	
}
