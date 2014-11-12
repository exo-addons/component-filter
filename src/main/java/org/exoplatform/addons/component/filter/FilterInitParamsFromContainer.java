package org.exoplatform.addons.component.filter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

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

  // Components to consider for init params modification
  private Map<String, Set<String>> filteredComponents = new HashMap<String, Set<String>>();

  // Components to consider for init params modification
  // private Set<String> filteredPlugins = new HashSet<String>();

  public void initContainer(ExoContainer container) {
    String entries = System.getProperty("exo.container.modify.params.components", "");
    if (entries.isEmpty()) {
      return;
    }

    String[] componentsArray = entries.split(",");
    for (String componentKey : componentsArray) {
      if (componentKey == null || componentKey.isEmpty()) {
        continue;
      }
      try {
        // Test if this class exists
        Class.forName(componentKey);
      } catch (ClassNotFoundException e) {
        LOG.warn("Uknown Class '{}', verify settings putted in configuration.properties file.", componentKey);
        continue;
      }
      Set<String> initParamsNames = new HashSet<String>();
      filteredComponents.put(componentKey.trim(), initParamsNames);
      Properties properties = System.getProperties();
      Set<String> names = properties.stringPropertyNames();
      for (String name : names) {
        if (name.startsWith(componentKey)) {
          initParamsNames.add(name.replace(componentKey + ".", ""));
        }
      }
    }

    ConfigurationManager cm = (ConfigurationManager) container.getComponentInstanceOfType(ConfigurationManager.class);

    for (String componentKey : filteredComponents.keySet()) {
      Component component = cm.getComponent(componentKey);
      if (component == null) {
        LOG.warn("Component '{}' doesn't exist, please verify used settings in configuration.properties file.", componentKey);
      } else {
        filterInitParams(component);
      }
    }
  }

  private void filterInitParams(Component component) {
    if (!filteredComponents.containsKey(component.getKey())) {
      return;
    }
    Set<String> paramsToFilter = filteredComponents.get(component.getKey());
    if (paramsToFilter.isEmpty()) {
      LOG.warn("Component '{}' is filtered to modify its initParams but no specified initParams.", component.getKey());
      return;
    }

    InitParams initParams = component.getInitParams();
    if (initParams == null) {
      LOG.warn("Init Params of component '{}' are empty.", component.getKey());
      return;
    }

    for (String initParamName : paramsToFilter) {
      if (!initParams.containsKey(initParamName)) {
        LOG.warn("Init Param '{}' of component '{}' doesn't exist.", initParamName, component.getKey());
        continue;
      }
      if (!(initParams.get(initParamName) instanceof ValueParam)) {
        LOG.warn("Init Param '{}' of component '{}' is not a 'value param' type, but {}.", initParamName, component.getKey(), initParams.get(initParamName).getClass().getSimpleName());
        continue;
      }
      String value = System.getProperty(component.getKey() + "." + initParamName);
      LOG.info("Setting Init Param '{}' of component '{}' with value {}.", initParamName, component.getKey(), value);
      ValueParam param = (ValueParam) initParams.get(initParamName);
      param.setValue(value);
    }
  }
}