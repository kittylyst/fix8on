package fix8on;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Test;

import fix8on.Main.FindJsonVisitor;

public class TestJsonVisitor {

	@Test
	public void testFind() throws IOException {
		FindJsonVisitor visitor = new FindJsonVisitor();
		Files.walkFileTree(Paths.get("src/fix8on/config"), visitor); 
		assertEquals(2, visitor.getFiles().size());
	}
	
}
