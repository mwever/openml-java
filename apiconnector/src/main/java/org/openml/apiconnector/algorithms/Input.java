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
package org.openml.apiconnector.algorithms;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class Input {

	public static InputStreamReader getURL( URL url ) throws IOException {
		HttpURLConnection urlConnection = (HttpURLConnection) (url.openConnection());
		urlConnection.setInstanceFollowRedirects(true);
		urlConnection.setConnectTimeout(1000);
		urlConnection.setReadTimeout(30000);
		urlConnection.connect();
		int responseCode = urlConnection.getResponseCode();
		Conversion.log("OK", "URL", "HTTP request status code [" + responseCode + "] URL [" + url + "]");
		return new InputStreamReader(urlConnection.getInputStream());
	}
	
	public static InputStreamReader getFile( String filename ) throws IOException {
		return new InputStreamReader( new FileInputStream( new File( filename ) ) );
	}
	
	public static String filename( String sUrl ) {
		if(sUrl.substring(sUrl.lastIndexOf('/') + 1).contains(".") == false ) {
			return sUrl.substring( sUrl.lastIndexOf('/') + 1 );
		}
		return sUrl.substring( sUrl.lastIndexOf('/') + 1, sUrl.lastIndexOf('.') );
	}
}
