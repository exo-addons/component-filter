package org.exoplatform.addons.component.filter;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.exoplatform.container.BaseContainerLifecyclePlugin;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.container.xml.Component;
import org.exoplatform.container.xml.ComponentPlugin;
import org.exoplatform.container.xml.Configuration;
import org.exoplatform.container.xml.ExternalComponentPlugins;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.picocontainer.ComponentAdapter;

/**
 * 
 * 10/11/2014
 * 
 * @author <a href="mailto:boubaker@exoplatform.com">Boubaker Khanfir</a>
 */
public class FilterComponentPluginsFromContainer extends BaseContainerLifecyclePlugin {

  private static final Log LOG = ExoLogger.getLogger("FilterComponentPluginsFromContainer");

  // Components to delete
  private Set<String> toDeletedComponents = new HashSet<String>();

  // Components to consider for plugins deletion
  private Set<String> filteredComponents = new HashSet<String>();

  // Component Plugin Types to delete from filtered components to delete
  private Set<String> toDeletePlugins = new HashSet<String>();

  // Component Plugin Names to delete from filtered components to delete
  private Set<String> toDeletePluginNames = new HashSet<String>();

  public void initContainer(ExoContainer container) {
    addEntries("exo.container.delete.components", toDeletedComponents);

    addEntries("exo.container.filter.components", filteredComponents);
    filteredComponents.removeAll(toDeletedComponents);

    addEntries("exo.container.delete.plugins.types", toDeletePlugins);

    addEntries("exo.container.delete.plugins.names", toDeletePluginNames);

    // Delete Components
    for (String componentKey : toDeletedComponents) {
      LOG.info("Delete component : " + componentKey);
      try {
        Class<?> compponentClass = Class.forName(componentKey);
        ComponentAdapter componentAdapter = container.getComponentAdapterOfType(compponentClass);
        if (componentAdapter == null) {
          LOG.warn("'{}' component wasn't found in container, coponent deletion ignored.", componentKey);
        } else {
          container.unregisterComponent(compponentClass);
        }
      } catch (ClassNotFoundException e) {
        LOG.warn("Can't delete component '{}' because class is not found.", componentKey);
      }
    }

    ConfigurationManager cm = (ConfigurationManager) container.getComponentInstanceOfType(ConfigurationManager.class);
    Configuration configuration = cm.getConfiguration();

    // Delete ComponentPlugins from Component definition
    @SuppressWarnings("unchecked")
    Collection<Component> loadedComponents = configuration.getComponents();
    for (Component component : loadedComponents) {
      List<ComponentPlugin> componentPlugins = component.getComponentPlugins();
      String componentKey = component.getKey();

      filterComponentPlugins(componentPlugins, componentKey);
    }

    // Delete ComponentPlugins from ExternalComponentPlugins definition
    @SuppressWarnings("unchecked")
    Iterator<ExternalComponentPlugins> iterator = configuration.getExternalComponentPluginsIterator();
    while (iterator.hasNext()) {
      ExternalComponentPlugins externalComponentPlugins = (ExternalComponentPlugins) iterator.next();

      String componentKey = externalComponentPlugins.getTargetComponent();
      List<ComponentPlugin> componentPlugins = externalComponentPlugins.getComponentPlugins();

      filterComponentPlugins(componentPlugins, componentKey);
    }
  }

  private void addEntries(String propertyName, Set<String> impactedEntries) {
    String entries = System.getProperty(propertyName, "");
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
      }
      impactedEntries.add(componentKey.trim());
    }
  }

  private void filterComponentPlugins(List<ComponentPlugin> componentPlugins, String componentKey) {
    if (!filteredComponents.contains(componentKey)) {
      return;
    }
    LOG.info("Delete some component plugins of component '{}'", componentKey);
    if (componentPlugins == null || componentPlugins.isEmpty()) {
      return;
    }
    Iterator<ComponentPlugin> iterator = componentPlugins.iterator();
    while (iterator.hasNext()) {
      ComponentPlugin componentPlugin = (ComponentPlugin) iterator.next();
      if (toDeletePlugins.contains(componentPlugin.getType()) || toDeletePluginNames.contains(componentPlugin.getName())) {
        LOG.info("Disable component plugin : " + componentPlugin.getType());
        iterator.remove();
      }
    }
  }
}