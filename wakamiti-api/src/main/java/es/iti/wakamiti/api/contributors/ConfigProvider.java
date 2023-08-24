package es.iti.wakamiti.api.contributors;

import imconfig.Config;
import jexten.ExtensionPoint;

@ExtensionPoint
public interface ConfigProvider extends Contributor {

	Config config();


}
