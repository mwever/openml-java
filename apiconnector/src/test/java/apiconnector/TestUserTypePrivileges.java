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

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.openml.apiconnector.algorithms.Conversion;
import org.openml.apiconnector.io.ApiException;
import org.openml.apiconnector.io.OpenmlConnector;
import org.openml.apiconnector.xml.DataFeature;
import org.openml.apiconnector.xml.DataQuality;
import org.openml.apiconnector.xml.DataQuality.Quality;
import org.openml.apiconnector.xml.DataSetDescription;
import org.openml.apiconnector.xml.Flow;
import org.openml.apiconnector.xml.Run;
import org.openml.apiconnector.xml.RunEvaluation;
import org.openml.apiconnector.xml.RunTrace;
import org.openml.apiconnector.xml.TaskInputs;
import org.openml.apiconnector.xstream.XstreamXmlMapping;

import com.thoughtworks.xstream.XStream;

public class TestUserTypePrivileges {

	private static final String data_file = "data/iris.arff";
	private static final String url = "https://test.openml.org/";
	private static final XStream xstream = XstreamXmlMapping.getInstance();
	
	// User #1 on test server, make sure to NEVER put a live admin key here
	private static final OpenmlConnector client_admin = new OpenmlConnector(url,"d488d8afd93b32331cf6ea9d7003d4c3"); 
	// Plain writing key, make sure to not put a live write key here
	private static final OpenmlConnector client_write = new OpenmlConnector(url,"8baa83ecddfe44b561fd3d92442e3319");
	// read only key from R-TEAM
	private static final OpenmlConnector client_read = new OpenmlConnector(url,"c1994bdb7ecb3c6f3c8f3b35f4b47f1f"); 
	private static final Integer EVAL_ID = 2;
	
	private static final int PRIVATE_DATASET_ID = 130;
	
	@Test(expected=ApiException.class)
	public void testApiDataQualityUpload() throws Exception {
		DataQuality dq = new DataQuality(1, EVAL_ID, new Quality[0]);
		String xml = xstream.toXML(dq);
		File description = Conversion.stringToTempFile(xml, "data-qualities", "xml");
		try {
			client_write.dataQualitiesUpload(description);
		} catch(ApiException e) {
			assertTrue(e.getCode() == 106);
			throw e;
		}
	}
	
	@Test(expected=ApiException.class)
	public void testApiAttemptDownloadPrivateDataset() throws Exception {
		client_read.dataGet(PRIVATE_DATASET_ID);
	}
	@Test(expected=ApiException.class)
	public void testApiAttemptDownloadPrivateDataFeatures() throws Exception {
		client_read.dataFeatures(PRIVATE_DATASET_ID);
	}
	@Test(expected=ApiException.class)
	public void testApiAttemptDownloadPrivateDataQualities() throws Exception {
		client_read.dataQualities(PRIVATE_DATASET_ID);
	}
	@Test(expected=IOException.class)
	public void testApiAttemptDownloadPrivateDataFile() throws Exception {
		DataSetDescription dsd = client_admin.dataGet(PRIVATE_DATASET_ID);
		client_read.datasetGet(dsd);
	}
	
	public void testApiAdminDownloadPrivateDataset() throws Exception {
		client_admin.dataGet(PRIVATE_DATASET_ID);
	}
	public void testApiAdminDownloadPrivateDataFeatures() throws Exception {
		client_admin.dataFeatures(PRIVATE_DATASET_ID);
	}
	public void testApiAdminDownloadPrivateDataQualities() throws Exception {
		client_admin.dataQualities(PRIVATE_DATASET_ID);
	}
	public void testApiAdminDownloadPrivateDataFile() throws Exception {
		DataSetDescription dsd = client_admin.dataGet(PRIVATE_DATASET_ID);
		client_admin.datasetGet(dsd);
	}
	
	@Test(expected=ApiException.class)
	public void testApiDataFeatureUpload() throws Exception {
		DataFeature df = new DataFeature(1, EVAL_ID, new DataFeature.Feature[0]);
		String xml = xstream.toXML(df);
		File description = Conversion.stringToTempFile(xml, "data-features", "xml");
		try {
			client_write.dataFeaturesUpload(description);
		} catch(ApiException e) {
			assertTrue(e.getCode() == 106);
			throw e;
		}
	}
	
	@Test(expected=ApiException.class)
	public void testApiRunEvaluationUpload() throws Exception {
		RunEvaluation re = new RunEvaluation(1, 1);
		String xml = xstream.toXML(re);
		File description = Conversion.stringToTempFile(xml, "run-evaluation", "xml");
		try {
			client_write.runEvaluate(description);
		} catch(ApiException e) {
			assertTrue(e.getCode() == 106);
			throw e;
		}
	}
	
	@Test(expected=ApiException.class)
	public void testApiRunTraceUpload() throws Exception {
		RunTrace rt = new RunTrace(1);
		String xml = xstream.toXML(rt);
		File description = Conversion.stringToTempFile(xml, "run-trace", "xml");
		try {
			client_write.runTraceUpload(description);
		} catch(ApiException e) {
			assertTrue(e.getCode() == 106);
			throw e;
		}
	}
	
	@Test(expected=ApiException.class)
	public void testApiDataUpload() throws Exception {
		DataSetDescription dsd = new DataSetDescription("test", "Unit test should be deleted", "arff", "class");
		String xml = xstream.toXML(dsd);
		File description = Conversion.stringToTempFile(xml, "test-data", "arff");
		try {
			client_read.dataUpload(description, new File(data_file));
		} catch(ApiException e) {
			assertTrue(e.getCode() == 104);
			throw e;
		}
	}

	@Test(expected=ApiException.class)
	public void testApiDataTag() throws Exception {
		try {
			client_read.dataTag(1, "default_tag");
		} catch(ApiException e) {
			assertTrue(e.getCode() == 104);
			throw e;
		}
	}

	@Test(expected=ApiException.class)
	public void testApiDataUntag() throws Exception {
		try {
			client_read.dataUntag(1, "default_tag");
		} catch(ApiException e) {
			assertTrue(e.getCode() == 104);
			throw e;
		}
	}

	@Test(expected=ApiException.class)
	public void testApiDataDelete() throws Exception {
		try {
			client_read.dataDelete(1);
		} catch(ApiException e) {
			assertTrue(e.getCode() == 104);
			throw e;
		}
	}

	@Test(expected=ApiException.class)
	public void testApiFlowUpload() throws Exception {
		Flow f = new Flow("test2", "weka.classifiers.test.javaunittest", "test", "test should be deleted",
				"english", "UnitTest");
		String xml = xstream.toXML(f);
		File description = Conversion.stringToTempFile(xml, "flow", "xml");
		try {
			client_read.flowUpload(description, null, null);
		} catch(ApiException e) {
			assertTrue(e.getCode() == 104);
			throw e;
		}
	}

	@Test(expected=ApiException.class)
	public void testApiTaskUpload() throws Exception {
		TaskInputs task = new TaskInputs(1, 1, null, null);
		String xml = xstream.toXML(task);
		File description = Conversion.stringToTempFile(xml, "flow", "xml");
		try {
			client_read.taskUpload(description);
		} catch(ApiException e) {
			assertTrue(e.getCode() == 104);
			throw e;
		}
	}

	@Test(expected=ApiException.class)
	public void testApiRunUpload() throws Exception {
		Run run = new Run(1, null, 1, null, null, null);
		String xml = xstream.toXML(run);
		File description = Conversion.stringToTempFile(xml, "flow", "xml");
		try {
			client_read.runUpload(description, null);
		} catch(ApiException e) {
			assertTrue(e.getCode() == 104);
			throw e;
		}
	}

	@Test(expected=ApiException.class)
	public void testApiFlowDelete() throws Exception {
		try {
			client_read.flowDelete(1);
		} catch(ApiException e) {
			assertTrue(e.getCode() == 104);
			throw e;
		}
	}

	@Test(expected=ApiException.class)
	public void testApiTaskDelete() throws Exception {
		try {
			client_read.taskDelete(1);
		} catch(ApiException e) {
			assertTrue(e.getCode() == 104);
			throw e;
		}
	}

	@Test(expected=ApiException.class)
	public void testApiRunDelete() throws Exception {
		try {
			client_read.runDelete(1);
		} catch(ApiException e) {
			assertTrue(e.getCode() == 104);
			throw e;
		}
	}
}
