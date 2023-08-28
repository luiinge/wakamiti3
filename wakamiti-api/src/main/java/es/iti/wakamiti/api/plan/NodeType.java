package es.iti.wakamiti.api.plan;

public enum NodeType {

    /** Regular node that aggregates other nodes. */
    AGGREGATOR("\uD83D\uDD36",1),

    /** Root node for an individual test case. */
    TEST_CASE("\uD83D\uDD35",2),

    /** Aggregator node within a test case. */
    STEP_AGGREGATOR("\uD83D\uDCA0",3),

    /** Executable final node within a test case. Cannot have children. */
    STEP("\uD83D\uDD39",4),

    /** Non-executable final node within a test case. Cannot have children. */
    VIRTUAL_STEP("▫️",5);


    public final String symbol;
    public final int numericValue;

    NodeType(String symbol, int numericValue) {
        this.symbol = symbol;
        this.numericValue = numericValue;
    }



    public static NodeType of(int numericValue) {
        for (var nodeType : NodeType.values()) {
            if (nodeType.numericValue == numericValue) {
                return  nodeType;
            }
        }
        throw new IllegalArgumentException();
    }
}
