package org.openml.apiconnector.xml;

import java.io.Serializable;

import org.openml.apiconnector.settings.Constants;

public class Task_new implements Serializable {
	
	private static final long serialVersionUID = -313419200748747105L;
	private final String oml = Constants.OPENML_XMLNS;
	private Integer task_id;
	private Integer task_type_id;
	private Input[] inputs;
	private String[] tags;
	
	public Task_new(Integer task_id, Integer task_type_id, Input[] inputs, String[] tags) {
		this.task_id = task_id;
		this.task_type_id = task_type_id;
		this.inputs = inputs;
		this.tags = tags;
	}
	
	public String getOml() {
		return oml;
	}
	
	public Integer getTask_id() {
		return task_id;
	}

	public Integer getTask_type_id() {
		return task_type_id;
	}
	
	public Input[] getInput() {
		return inputs;
	}
	
	public String[] getTags() {
		return tags;
	}

	public static class Input implements Serializable {
		
		private static final long serialVersionUID = -8040649288439821180L;
		private String name;
		private String value;
		
		public String getName() {
			return name;
		}

		public String getValue() {
			return value;
		}

		public Input(String name, String value) {
			this.name = name;
			this.value = value;
		}
	}
}
