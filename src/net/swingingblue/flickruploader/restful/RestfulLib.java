package net.swingingblue.flickruploader.restful;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import net.swingingblue.flickruploader.data.RestRequestData;
import net.swingingblue.flickruploader.restful.CountingMultipartEntity.ProgressListener;

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
		Map<String, Object> queryParam = request.getQueryParam();
		
		Iterator<Map.Entry<String, Object>> it = queryParam.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, Object> map = (Map.Entry<String, Object>)it.next();
			builder.appendQueryParameter(map.getKey(), map.getValue().toString());
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

	/**
	 * HTTPの要求(GET/POST)を投げてレスポンスを取得する
	 * @param request
	 * @return
	 */
	public String httpGetrequset(RestRequestData request) {
		return httpGetRequest(makeUrl(request).toString());
	}
	
	/**
	 * HTTPの要求(GET/POST)を投げてレスポンスを取得する
	 * @param method
	 * @param uri
	 * @return
	 */
	public static String httpGetRequest(String uri) {
		
		String entity = null;
		HttpUriRequest httpRequest = null;
		
	    // HTTP GET request
		httpRequest = new HttpGet(uri);
	    
	    DefaultHttpClient defaultHttpClient = new DefaultHttpClient();
	    HttpResponse httpResponse;
		try {
			httpResponse = defaultHttpClient.execute(httpRequest);
			
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


	/**
	 * HTTPの要求(GET/POST)を投げてレスポンスを取得する
	 * SinglePartの要求を生成する
	 * @param method
	 * @param uri
	 * @return
	 */
	public static String httpPostRequest(RestRequestData request) {
		
		String entity = null;
		HttpPost httpPost = null;
		
		httpPost = new HttpPost(request.getUrl());

		// POSTデータ組み立て
		List<NameValuePair> pairList = new ArrayList<NameValuePair>();
		
		Map<String, Object> queryParam = request.getQueryParam();
		
		Iterator<Map.Entry<String, Object>> it = queryParam.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, Object> map = (Map.Entry<String, Object>)it.next();
			BasicNameValuePair pair = new BasicNameValuePair(map.getKey(), map.getValue().toString());
			pairList.add(pair);
		}
		
		try {
			httpPost.setEntity(new UrlEncodedFormEntity(pairList));
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
	    
	    DefaultHttpClient defaultHttpClient = new DefaultHttpClient();
	    HttpResponse httpResponse;
		try {
			httpResponse = defaultHttpClient.execute(httpPost);
			
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
	
	/**
	 * HTTPの要求(GET/POST)を投げてレスポンスを取得する
	 * MultiPartの要求を生成する
	 * @param method
	 * @param uri
	 * @return
	 */
	public static String httpPostRequestMultipart(RestRequestData request, ProgressListener listner ) {
		
		String entity = null;
		HttpPost httpPost = null;
		
		Uri.Builder builder = new Uri.Builder();
		builder.scheme(schemeHttp);
		builder.authority(request.getUrl());
		builder.path(request.getPath());

		httpPost = new HttpPost(builder.toString());

		// POSTデータ組み立て
		CountingMultipartEntity multiEntity = null;
		multiEntity = new CountingMultipartEntity(listner);
		
		Map<String, Object> queryParam = request.getQueryParam();
		
		Iterator<Map.Entry<String, Object>> it = queryParam.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, Object> map = (Map.Entry<String, Object>)it.next();
			
			try {
				if (map.getValue() instanceof String) {
					if (map.getKey().equals("photo")) {
						multiEntity.addPart(map.getKey(), new FileBody(new File(map.getValue().toString())));
					} else {
						multiEntity.addPart(map.getKey(), new StringBody(map.getValue().toString()));
					}
				} else if (map.getValue() instanceof URI) {
					URI uri = (URI)map.getValue();
					multiEntity.addPart(map.getKey(), new FileBody(new File(uri)));
				}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}

		long length = multiEntity.getContentLength();
		
		httpPost.setEntity(multiEntity);
	    
	    DefaultHttpClient defaultHttpClient = new DefaultHttpClient();
	    
	    HttpResponse httpResponse;
		try {
			httpResponse = defaultHttpClient.execute(httpPost);
			
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

