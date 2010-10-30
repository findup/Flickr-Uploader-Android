package net.swingingblue.flickruploader.restful;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import net.swingingblue.flickruploader.data.RestRequestData;

import android.net.Uri;
import android.util.Log;

/**
 * Restful library
 * @author tsugimot
 *
 */
public class RestfulLib {
	
	private static final String LOG_TAG = RestfulLib.class.getSimpleName();
	
	private static final String schemeHttp = "http";

	
	/**
	 * 
	 * @param json
	 */
	public void parceJSON(String json) {
		JSONArray jsons;
		try {
			jsons = new JSONArray(json);
			for (int i = 0; i < jsons.length(); i++) {
			    JSONObject jsonObj = jsons.getJSONObject(i);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * URLおよびクエリパラメータを組み立てて返す
	 * @param request
	 * @return
	 */
	public static URL makeUrl(RestRequestData request) {
		
		Uri.Builder builder = new Uri.Builder();
		builder.scheme(schemeHttp);
		builder.authority(request.getUrl());
		builder.path(request.getPath());
		
		// クエリ組み立て
		Map<String, String> queryParam = request.getQueryParam();
		
		Iterator<Map.Entry<String, String>> it = queryParam.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, String> map = (Map.Entry<String, String>)it.next();
			builder.appendQueryParameter(map.getKey(), map.getValue());
		}

		Log.d(LOG_TAG, "URL = " + builder.toString());

		URL url = null;
		try {
			url = new URL(builder.toString());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		
		return url;
	}

	
	public static String httpGet(String uri) {
		
		String entity = null;
		
	    // HTTP GET request
	    HttpUriRequest httpGet = new HttpGet(uri);
	    
	    DefaultHttpClient defaultHttpClient = new DefaultHttpClient();
	    HttpResponse httpResponse;
		try {
			httpResponse = defaultHttpClient.execute(httpGet);
			
		    if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
		        // HTTP response
		        entity = EntityUtils.toString(httpResponse.getEntity());
		    }
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return entity;
	}
	
}
