package net.swingingblue.flickruploader.flickrapi;

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
	
	private static final String schemeHttp = "http";
	private static final String baseUrl = "flickr.com/services";
	private static final String authPath = "/auth";
	private static final String restPath = "/rest";

	// for Development only.
	private static final String apiKey ="";
	
	// Auth
	// Upload
	// list

	public void getFlob() {

		// (example)
		// http://flickr.com/services/rest/?method=flickr.auth.getFrob&api_key=987654321&api_sig=5f3870be274f6c49b3e31a0c6728957f
		
		Uri.Builder builder = new Uri.Builder();
		builder.scheme(schemeHttp);
		builder.authority(baseUrl);
		builder.path(restPath);
		builder.query("flickr.auth.getFrob");
		builder.appendQueryParameter("api_key", apiKey);
		builder.appendQueryParameter("api_sig", "");
		
		Log.d(LOG_TAG, "getFlob Url = " + builder.build().toString());
	}
	
	private String makeToken(String param) {
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
		
		return null;
	}
	
}
