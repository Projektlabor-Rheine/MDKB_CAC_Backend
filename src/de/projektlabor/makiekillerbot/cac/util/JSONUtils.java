package de.projektlabor.makiekillerbot.cac.util;

import java.util.stream.Collector;

import org.json.JSONArray;

public class JSONUtils {
	
	// A collectors to convert a stream into a json-array
	public static Collector<Object, JSONArray, JSONArray> JSON_ARRAY_COLLECTOR = Collector.of(JSONArray::new,JSONArray::put,JSONArray::put);

}
