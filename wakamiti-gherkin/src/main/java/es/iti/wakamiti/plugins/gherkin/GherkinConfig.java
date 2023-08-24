package es.iti.wakamiti.plugins.gherkin;

import es.iti.wakamiti.api.contributors.ConfigProvider;
import imconfig.Config;
import java.nio.charset.StandardCharsets;
import jexten.Extension;


@Extension
public class GherkinConfig implements ConfigProvider {

    /**
     * Tag used for annotate a node as a definition
     */
    public static final String DEFINITION_TAG = "gherkin.definitionTag";

    /**
     * Tag used for annotate a node as an implementation
     */
    public static final String IMPLEMENTATION_TAG = "gherkin.implementationTag";

    /**
     * Dash-separated number list that indicates how many implementation steps
     * correspond to each definition step
     */
    public static final String STEP_MAP = "gherkin.stepMap";

    /**
     * Regex for tags used as identifier
     */
    public static final String ID_TAG_PATTERN = "gherkin.idTagPattern";


    private final static Config DEFAULT = Config.factory().accordingDefinitionsFromResource(
        "es_iti_wakamiti_plugins_gherkin-config.yaml",
        StandardCharsets.UTF_8,
        GherkinConfig.class.getClassLoader()
    );


    @Override
    public Config config() {
        return DEFAULT;
    }

}
