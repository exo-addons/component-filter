package org.exoplatform.addons.component.filter;

import org.exoplatform.container.BaseContainerLifecyclePlugin;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.container.xml.Component;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * 
 * 10/11/2014
 * 
 * @author <a href="mailto:boubaker@exoplatform.com">Boubaker Khanfir</a>
 */
public class FilterInitParamsFromContainer extends BaseContainerLifecyclePlugin {

  private static final Log LOG = ExoLogger.getLogger("FilterInitParamsFromContainer");

  public void initContainer(ExoContainer container) {
    String entries = System.getProperty("exo.container.modify.params.components", "");
    if (entries.isEmpty()) {
      return;
    }

    String[] componentsArray = entries.split(",");
    ConfigurationManager cm = (ConfigurationManager) container.getComponentInstanceOfType(ConfigurationManager.class);
    for (String componentParamDef : componentsArray) {
      if (componentParamDef == null || componentParamDef.isEmpty()) {
        continue;
      }
      String[] componentParamDefs = componentParamDef.split(":");
      if (componentParamDefs.length != 3) {
        LOG.warn("Parameter 'exo.container.modify.params.components' with value '" + componentParamDef + "' is not set correctly, add parameters using this format 'COMPONETKEY:PARAMNAME:VALUE'");
        continue;
      }
      String componentKey = componentParamDefs[0];
      String paramName = componentParamDefs[1];
      String paramValue = componentParamDefs[2];
      try {
        // Test if this class exists
        Class.forName(componentKey);
      } catch (ClassNotFoundException e) {
        LOG.warn("Uknown Class '{}', verify settings putted in configuration.properties file.", componentKey);
        continue;
      }
      Component component = cm.getComponent(componentKey);
      if (component == null) {
        LOG.warn("Component '{}' doesn't exist, please verify used settings in configuration.properties file.", componentKey);
      } else {
        filterInitParams(component, paramName, paramValue);
      }
    }
  }

  private void filterInitParams(Component component, String paramName, String paramValue) {
    InitParams initParams = component.getInitParams();
    if (initParams == null) {
      LOG.warn("Init Params of component '{}' are empty.", component.getKey());
      return;
    }

    ValueParam valueParam = null;
    if (initParams.containsKey(paramName)) {
      if (!(initParams.get(paramName) instanceof ValueParam)) {
        LOG.warn("Init Param '{}' of component '{}' is not a 'value param' type, but '{}'.", paramName, component.getKey(), initParams.get(paramName).getClass().getSimpleName());
        return;
      }
      LOG.info("Setting Init Param '{}' of component '{}' with value '{}'.", paramName, component.getKey(), paramValue);
      valueParam = (ValueParam) initParams.get(paramName);
      valueParam.setValue(paramValue);
    } else {
      valueParam = new ValueParam();
      valueParam.setName(paramName);
      valueParam.setValue(paramValue);
      LOG.info("Adding Init Param '{}' of component '{}' with value '{}'.", paramName, component.getKey(), paramValue);
      initParams.addParameter(valueParam);
    }
  }
}