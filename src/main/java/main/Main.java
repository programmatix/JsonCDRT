package main;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Main {
    public static void main(String[] args) {
        var hello = "world";
    }
}

/**
 * Identifies a position in a json document.
 */
class Cursor {
    List<CursorNode> nodes = new ArrayList<>();
    String leaf;
    public static final CursorNode DOC_NODE = new CursorNode(NodeType.MAP_T, "doc");

    public static Cursor doc() {
        Cursor out = new Cursor();
        out.leaf = DOC_NODE.id;
        return out;
    }

    public Cursor get(String field) {
        Cursor out = new Cursor();
        out.nodes.addAll(nodes);
        out.nodes.add(new CursorNode(NodeType.MAP_T, leaf));
        out.leaf = field;
        return out;
    }


    public Cursor idx(int index) {
        Cursor out = new Cursor();
        out.nodes.addAll(nodes);
        out.nodes.add(new CursorNode(NodeType.LIST_T, leaf));
        out.leaf = "head"; // TODO ??
        return out;
    }


    public Cursor insertAfter(String field) {
        Cursor out = new Cursor();
        out.nodes.addAll(nodes);

        out.leaf = field;
        return out;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cursor cursor = (Cursor) o;
        return Objects.equals(nodes, cursor.nodes) &&
                Objects.equals(leaf, cursor.leaf);
    }

    @Override
    public int hashCode() {

        return Objects.hash(nodes, leaf);
    }
}

enum NodeType {
    MAP_T,
    LIST_T
}

class CursorNode {
    NodeType type;
    String id;

    public CursorNode(NodeType type, String id) {
        this.type = type;
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CursorNode that = (CursorNode) o;
        return type == that.type &&
                Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {

        return Objects.hash(type, id);
    }
}

class LamportTimestamp {
    String nodeId;
    int counter;
}

enum MutationType {
    INSERT,
    DELETE,
    ASSIGN
}

class Operation {
    LamportTimestamp id;
    List<LamportTimestamp> deps;
    Cursor cur;
    MutationType mut;
    Object value;
}