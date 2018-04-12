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
package apiconnector;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.junit.Ignore;
import org.junit.Test;
import org.openml.apiconnector.algorithms.Conversion;
import org.openml.apiconnector.io.OpenmlConnector;
import org.openml.apiconnector.xml.EvaluationRequest;
import org.openml.apiconnector.xml.Run;
import org.openml.apiconnector.xml.RunList;
import org.openml.apiconnector.xml.RunTag;
import org.openml.apiconnector.xml.RunUntag;
import org.openml.apiconnector.xml.Task;
import org.openml.apiconnector.xml.UploadRun;
import org.openml.apiconnector.xstream.XstreamXmlMapping;

import com.thoughtworks.xstream.XStream;

public class TestRunFunctionality {
	private static final int probe = 67;
	private static final String predictions_path = "data/predictions_task53.arff";
	private static final int FLOW_ID = 10;

	private static final String url_test = "https://test.openml.org/";
	private static final String url_live = "https://www.openml.org/";
	private static final OpenmlConnector client_write_test = new OpenmlConnector(url_test, "8baa83ecddfe44b561fd3d92442e3319");
	private static final OpenmlConnector client_read_live = new OpenmlConnector(url_live, "c1994bdb7ecb3c6f3c8f3b35f4b47f1f");
	
	private static final XStream xstream = XstreamXmlMapping.getInstance();
	private static final String tag = "junittest";
	
	@Test
	public void testApiRunDownload() throws Exception {
		
		Run run = client_read_live.runGet(probe);
		
		File tempXml = Conversion.stringToTempFile(xstream.toXML(run), "run", "xml");
		File tempXsd = client_read_live.getXSD("openml.run.upload");
		
		System.out.println(xstream.toXML(run));
		
		assertTrue(Conversion.validateXML(tempXml, tempXsd));
		
		// very easy checks, should all pass
		assertTrue(run.getRun_id() == probe);
		
	}
	
	@Test
	@Ignore
	public void testApiRunList() throws Exception {
		List<Integer> uploaderFilter = new ArrayList<Integer>();
		uploaderFilter.add(16);
		
		Map<String, List<Integer>> filters = new HashMap<String, List<Integer>>();
		filters.put("uploader", uploaderFilter);
		
		RunList rl = client_read_live.runList(filters, 100, 0);
		assertTrue(rl.getRuns().length == 100);
		
		for (org.openml.apiconnector.xml.RunList.Run r : rl.getRuns()) {
			assertTrue(uploaderFilter.contains(r.getUploader()));
		}
		
	}
	
	@Test
	public void testApiRunUpload() throws Exception {
		String[] tags = {"first_tag", "another_tag"};
		
		Run r = new Run(probe, null, FLOW_ID, null, null, tags);
		String runXML = xstream.toXML(r);
		
		File runFile = Conversion.stringToTempFile(runXML, "runtest",  "xml");
		File predictions = new File(predictions_path); 
		
		Map<String,File> output_files = new HashMap<String, File>();
		
		output_files.put("predictions", predictions);
		
		UploadRun ur = client_write_test.runUpload(runFile, output_files);
		
		Run newrun = client_write_test.runGet(ur.getRun_id());
		
		Set<String> uploadedTags = new HashSet<String>(Arrays.asList(newrun.getTag()));
		Set<String> providedTags = new HashSet<String>(Arrays.asList(tags));
		
		assertTrue(uploadedTags.equals(providedTags));
		
		RunTag rt = client_write_test.runTag(ur.getRun_id(), tag);
		assertTrue(Arrays.asList(rt.getTags()).contains(tag));
		RunUntag ru = client_write_test.runUntag(ur.getRun_id(), tag);
		assertTrue(Arrays.asList(ru.getTags()).contains(tag) == false);
		
		client_write_test.runDelete(ur.getRun_id());
	}
	
	@Test
	public void testApiEvaluationRequest() throws Exception {
		// this test assumes that there are runs on the test server. 
		// might not be the case just after reset 
		
		client_write_test.setVerboseLevel(1);
		
		// gives evaluation id "42", which does not exist. 
		// therefore we get an actual run that is not evaluated by this engine back. 
		Map<String, String> filters = new TreeMap<String, String>();
		Integer[] ttids = {3,4};
		String ttidString = Arrays.toString(ttids).replaceAll(" ", "").replaceAll("\\[", "").replaceAll("\\]", "");
		
		filters.put("ttid", ttidString);
		EvaluationRequest er = client_write_test.evaluationRequest(42, "random", filters);
		assertTrue(er.getRuns().length == 1);
		Run r = client_write_test.runGet(er.getRuns()[0].getRun_id());
		Task t = client_write_test.taskGet(r.getTask_id());
		assertTrue(Arrays.asList(ttids).contains(t.getTask_type_id()));
	}
}
