package net.swingingblue.flickruploader.data;

import java.util.Map;

public class RestRequestData {

	// スキーマ
	private String scheme;
	
	// URL
	private String url;
		
	// パス
	private String path;
	
	// クエリーパラメータ
	Map<String, String> queryParam;
	

	public String getScheme() {
		return scheme;
	}

	public void setScheme(String scheme) {
		this.scheme = scheme;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public Map<String, String> getQueryParam() {
		return queryParam;
	}

	public void setQueryParam(Map<String, String> queryParam) {
		this.queryParam = queryParam;
	}

	
}
