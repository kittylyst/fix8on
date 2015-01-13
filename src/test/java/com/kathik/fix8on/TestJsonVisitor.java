package com.kathik.fix8on;

import com.kathik.fix8on.Main.FindJsonVisitor;
import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Test;

public class TestJsonVisitor {

    @Test
    public void testFind() throws IOException {
        FindJsonVisitor visitor = new FindJsonVisitor();
        Files.walkFileTree(Paths.get("src/main/java/com/kathik/fix8on/config"), visitor);
        assertEquals(2, visitor.getFiles().size());
    }

}
