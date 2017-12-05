package org.jasig.cas.util.custom;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JacksonUtil {

	private static final ObjectMapper mapper = new ObjectMapper();

	private JacksonUtil() {
	}

	public static String object2Json(Object object) throws Exception {
		return mapper.writeValueAsString(object);
	}

	public static <T>T json2Object(String json, Class<T> clazz)
			throws JsonParseException, JsonMappingException, IOException {
		return (T)mapper.readValue(json, clazz);
	}
	
	public static List<?> json2List(String json, TypeReference<?> typeRef) throws JsonParseException, JsonMappingException, IOException {
		List<?> list = mapper.readValue(json, typeRef);
		return list;
	}

}
