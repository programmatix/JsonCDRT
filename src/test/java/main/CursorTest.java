package main;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CursorTest {
    private Cursor doc() {
        return Cursor.doc();
    }

    @Test
    public void getShopping() {
        Cursor c = doc().get("shopping");

        assertEquals(1, c.nodes.size());
          assertEquals(Cursor.DOC_NODE, c.nodes.get(0));
          assertEquals("shopping", c.leaf);
    }
}
