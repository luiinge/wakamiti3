package es.iti.wakamiti.api.plan;

import java.util.*;

import lombok.*;

@Getter @Setter @NoArgsConstructor @EqualsAndHashCode @ToString
public class PlanNode {

    private UUID nodeID;
    private NodeType nodeType;
    private String name;
    private String language;
    private String identifier;
    private String source;
    private String keyword;
    private DataTable dataTable;
    private Document document;
    private String description;
    private Set<String> tags = new HashSet<>();
    private SortedMap<String,String> properties = new TreeMap<>();
    private String displayNamePattern;


	public PlanNode(NodeType type) {
        this.nodeType = type;
	}


    public PlanNode addProperties(Map<String, String> properties) {
        this.properties.putAll(properties);
        return this;
    }


    public PlanNode addProperty(String key, String value) {
        this.properties.put(key,value);
        return this;
    }



    public PlanNode addTag(String tag) {
        this.tags.add(tag);
        return this;
    }


    public PlanNode addTags(Collection<String> tags) {
        this.tags.addAll(tags);
        return this;
    }


    public boolean hasProperty(String property, String value) {
        return value.equals(this.properties.get(property));
    }


    public boolean hasTag(String tag) {
        return tags.contains(tag);
    }


    public String displayName() {
        return displayNamePattern == null ? name :
            displayNamePattern
                .replace("{name}",Objects.requireNonNullElse(name,""))
                .replace("{keyword}",Objects.requireNonNullElse(keyword,""))
                .replace("{identifier}",Objects.requireNonNullElse(identifier,""))
                .replace("{nodeType}", nodeType == null ? "" : nodeType.toString())
            ;
    }


}
