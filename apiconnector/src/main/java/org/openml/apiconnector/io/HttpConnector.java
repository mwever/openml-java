/*******************************************************************************
 * Copyright (C) 2017, Jan N. van Rijn <j.n.van.rijn@liacs.leidenuniv.nl>
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. Neither the name of the copyright holder nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package org.openml.apiconnector.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.openml.apiconnector.settings.Constants;
import org.openml.apiconnector.xml.ApiError;
import org.openml.apiconnector.xstream.XstreamXmlMapping;

import com.thoughtworks.xstream.XStream;

public class HttpConnector implements Serializable {
	
	private static final long serialVersionUID = -8589069573065947493L;
	
	public static Object doApiRequest(URL url, MultipartEntity entity, String ash, int apiVerboseLevel) throws Exception {
		if (ash == null) {
			throw new Exception("Api key not set. ");
		}
		entity.addPart("api_key", new StringBody( ash ) );
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpPost httppost = new HttpPost(url.toString());
        httppost.setEntity(entity);
        CloseableHttpResponse response = httpclient.execute(httppost);
        return wrapHttpResponse(response, url, "POST", apiVerboseLevel);
	}
	
	public static Object doApiRequest(URL url, String ash, int apiVerboseLevel) throws Exception {
		if (ash != null) {
			url = new URL(url + "?api_key=" + ash);
		}
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpGet httpget = new HttpGet(url.toString());
        CloseableHttpResponse response = httpclient.execute(httpget);
        return wrapHttpResponse(response, url, "GET", apiVerboseLevel);
	}
	
	public static Object doApiDelete(URL url, String ash, int apiVerboseLevel) throws Exception {
		if (ash == null) {
			throw new Exception("Api key not set. ");
		}
		
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpDelete httpdelete = new HttpDelete(url + "?api_key=" + ash);
        CloseableHttpResponse response = httpclient.execute(httpdelete);
		return wrapHttpResponse(response, url, "DELETE", apiVerboseLevel);
	}
	
	public static String getStringFromUrl(URL url, boolean accept_all) throws Exception {
		CloseableHttpClient client = HttpClients.createDefault();
		HttpGet httpget = new HttpGet(url.toString());
		HttpResponse httpResp = client.execute(httpget);
		int code = httpResp.getStatusLine().getStatusCode();
		if (!accept_all && code != HttpStatus.SC_OK) {
			throw new IOException("Problem getting URL, status " + code + ": " + url);
		}
		return httpEntitiToString(httpResp.getEntity());
	}
	
	private static Object wrapHttpResponse(CloseableHttpResponse response, URL url, String requestType, int apiVerboseLevel) throws Exception {
		String result = readHttpResponse(response, url, requestType, apiVerboseLevel);
		if (result.length() == 0) {
			throw new ApiException(1, "Webserver returned empty result (possibly due to temporarily high load). Please try again. ");
		}
		XStream xstreamClient = XstreamXmlMapping.getInstance();
		Object apiResult = xstreamClient.fromXML(result);
		if(apiResult instanceof ApiError) {
			ApiError apiError = (ApiError) apiResult;
			String message = apiError.getMessage();
			if( apiError.getAdditional_information() != null ) {
				message += ": " + apiError.getAdditional_information();
			}
			throw new ApiException(Integer.parseInt(apiError.getCode()), message);
		}
		return apiResult;
	}
	

	protected static String readHttpResponse(CloseableHttpResponse response, URL url, String requestType, int apiVerboseLevel) throws Exception {
		String result = "";
        HttpEntity resEntity = response.getEntity();
        int code = response.getStatusLine().getStatusCode();
		long contentLength = 0;
		try {
            if (resEntity != null) {
            	result = httpEntitiToString(resEntity);
                contentLength = resEntity.getContentLength();
            } else {
            	throw new IOException("An exception has occured while reading data input stream. ");
            }
		} finally {
            try { response.close(); } catch (Exception ignore) {}
        }
		if(apiVerboseLevel >= Constants.VERBOSE_LEVEL_XML) {
			System.out.println("===== REQUEST URI ("+requestType+"): " + url + " (Status Code: " + code + ", Content Length: "+contentLength+") =====\n" + result + "\n=====\n");
		}
		return result;
	}
	
	/**
	 * Returns a file from the openml server
	 * 
	 * @param url
	 *            - The URL to obtain
	 * @param filepath
	 *            - Where to safe the file.
	 * @return File - a pointer to the file that was saved.
	 * @throws IOException
	 *             - Can be: server down, etc.
	 * @throws URISyntaxException 
	 */
	public static File getFileFromUrl(URL url, String filepath, boolean accept_all) throws IOException, URISyntaxException, ApiException {
		File file = new File(filepath);
        HttpClient httpClient = HttpClientBuilder.create().build();
        // Compared to FileUtils.copyURLToFile this can handle http -> https redirects
        HttpGet httpget = new HttpGet(url.toURI());
        HttpResponse response = httpClient.execute(httpget);
        
        int code = response.getStatusLine().getStatusCode();
		if (!accept_all && code != HttpStatus.SC_OK) {
			throw new IOException("Problem getting URL, status " + code + ": " + url);
		}
        HttpEntity entity = response.getEntity();
        if (entity.getContentLength() == 0) {
        	throw new ApiException(1, "Webserver returned empty result (possibly due to temporarily high load). Please try again. ");
        }
        FileOutputStream fos = new java.io.FileOutputStream(file);
        entity.writeTo(fos);
        fos.close();
        
		return file;
	}
	
	protected static String httpEntitiToString(HttpEntity resEntity) throws IOException {
		StringWriter writer = new StringWriter();
		IOUtils.copy(new InputStreamReader( resEntity.getContent() ), writer );
		return writer.toString();
	}
}
