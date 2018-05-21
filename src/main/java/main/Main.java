package main;

import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.document.json.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Main {
    public static void main(String[] args) {
        var hello = "world";
    }
}

class LamportClockGenerator {
    int counter = 1;
    final String nodeId;

    public LamportClockGenerator(String nodeId) {
        this.nodeId = nodeId;
    }

    public LamportTimestamp inc() {
        LamportTimestamp out = new LamportTimestamp(nodeId, counter);
        return out;
    }
}

/**
 * Identifies a position in a json document.
 */
class Cursor {
    List<CursorNode> nodes = new ArrayList<>();
    String leaf;
    public static final CursorNode DOC_NODE = new CursorNode(NodeType.MAP_T, "doc");
    final LamportClockGenerator timestamps;


    public Cursor(LamportClockGenerator timestamps) {
        this.timestamps = timestamps;
    }

    public Cursor doc() {
        Cursor out = new Cursor(timestamps);
        out.leaf = DOC_NODE.id;
        return out;
    }

    public Cursor get(String field) {
        Cursor out = new Cursor(timestamps);
        out.nodes.addAll(nodes);
        out.nodes.add(new CursorNode(NodeType.MAP_T, leaf));
        out.leaf = field;
        return out;
    }


    public Cursor idx(int index) {
        Cursor out = new Cursor(timestamps);
        out.nodes.addAll(nodes);
        out.nodes.add(new CursorNode(NodeType.LIST_T, leaf));
        out.leaf = "head"; // TODO ??
        return out;
    }


    public Cursor insertAfter(String field) {
        Cursor out = new Cursor(timestamps);
        out.nodes.addAll(nodes);

        Operation op = new Operation();
        op.id = timestamps.inc();
        op.mut = MutationType.INSERT;


        out.leaf = field;
        return out;
    }

    void makeOp() {
        Operation op = new Operation();
        op.id = timestamps.inc();
        op.mut = MutationType.INSERT;

    }

    JsonDocument state;
    List<Operation> generatedOps = new ArrayList<>();
    List<Operation> processedOps = new ArrayList<>();

    static Object getFromCursor(Cursor c, JsonDocument d) {
        var it = c.nodes.iterator();
        Object cur = d.content();

        while (it.hasNext()) {
            var next = it.next();

            switch (next.type) {
                case MAP_T:
                    if (cur instanceof JsonObject) {
                        cur = ((JsonObject) cur).getObject(next.id);
            }
            else if (cur instanceof JsonArray) {
                        cur = ((JsonArray) cur)(next.id);
                    }

                    break;
                case LIST_T:
                    if (cur instanceof JsonObject) {
                        cur = ((JsonObject) cur).getArray(next.id);
                    }
                    else if (cur instanceof JsonArray) {
                        cur = ((JsonArray) cur).getArray(next.id);
                    }
                    break;
            }
        }

        return cur;
    }

    void applyLocal(Operation op) {
        applySingleOp(op);
        generatedOps.add(op);
        processedOps.add(op);
    }

    void applySingleOp(Operation op) {
        switch(op.mut) {
            case INSERT:
                Object next = getFromCursor(op.cur, state);
                if (next instanceof JsonObject) {

                }
                break;
            case ASSIGN:
                break;
            case DELETE:
                break;

        }
    }

    void send() {
        // send all in generatedOps over network
           }

           void recv(List<Operation> ops) {
        for(Operation op: ops) {
            applySingleOp(op);
        }
        processedOps.addAll(ops);
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
    final String nodeId;
    final int counter;

    public LamportTimestamp(String nodeId, int counter) {
        this.nodeId = nodeId;
        this.counter = counter;
    }
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