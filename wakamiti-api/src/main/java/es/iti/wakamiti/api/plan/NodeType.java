package es.iti.wakamiti.api.plan;

public enum NodeType {

    /** Regular node that aggregates other nodes. */
    AGGREGATOR("\uD83D\uDD36"),

    /** Root node for an individual test case. */
    TEST_CASE("\uD83D\uDD35"),

    /** Aggregator node within a test case. */
    STEP_AGGREGATOR("\uD83D\uDCA0"),

    /** Executable final node within a test case. Cannot have children. */
    STEP("\uD83D\uDD39"),

    /** Non-executable final node within a test case. Cannot have children. */
    VIRTUAL_STEP("▫️");


    public final String symbol;


    NodeType(String symbol) {
        this.symbol = symbol;
    }
}
