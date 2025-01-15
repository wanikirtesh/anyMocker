package com.wanikirtesh.anymocker.core;

import com.wanikirtesh.anymocker.core.components.OpenApiImporter;
import com.wanikirtesh.anymocker.core.components.Request;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

class AnyMockerApplicationTests {
	@Test
	void contextLoads() throws IOException, NoSuchMethodException, InstantiationException, IllegalAccessException {
		//List<Request> requests = OpenApiImporter.importFromOpenApiSpec(new File("test.yaml"));
		//System.out.printf("requests: %s%n", requests);
	}
}
