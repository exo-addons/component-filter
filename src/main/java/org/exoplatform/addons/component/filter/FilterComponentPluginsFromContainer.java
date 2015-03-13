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

  public void initContainer(ExoContainer container) {
    addEntries("exo.container.delete.components", toDeletedComponents, true);

    addEntries("exo.container.filter.components", filteredComponents, true);
    filteredComponents.removeAll(toDeletedComponents);

    addEntries("exo.container.delete.plugins", toDeletePlugins, false);

    // to preserve compatibility with old versions
    addEntries("exo.container.delete.plugins.types", toDeletePlugins, true);
    addEntries("exo.container.delete.plugins.names", toDeletePlugins, false);

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

  private void addEntries(String propertyName, Set<String> impactedEntries, boolean isClass) {
    String entries = System.getProperty(propertyName, "");
    if (entries.isEmpty()) {
      return;
    }

    String[] componentsArray = entries.split(",");
    for (String component : componentsArray) {
      if (component == null || component.isEmpty()) {
        continue;
      }
      if (isClass) {
        try {
          // Test if this class exists
          Class.forName(component);
        } catch (ClassNotFoundException e) {
          LOG.warn("Uknown Class '{}', please verify used settings in configuration.properties file.", component);
          continue;
        }
      }
      impactedEntries.add(component);
    }
  }

  private void filterComponentPlugins(List<ComponentPlugin> componentPlugins, String componentKey) {
    if (!filteredComponents.contains(componentKey)) {
      return;
    }
    if (componentPlugins == null || componentPlugins.isEmpty()) {
      return;
    }
    Iterator<ComponentPlugin> iterator = componentPlugins.iterator();
    LOG.info("Disable plugins of component : " + componentKey);
    while (iterator.hasNext()) {
      ComponentPlugin componentPlugin = (ComponentPlugin) iterator.next();
      if (toDeletePlugins.contains(componentPlugin.getType()) || toDeletePlugins.contains(componentPlugin.getName())) {
        LOG.info("Disable component plugin : " + componentPlugin.getType() + " with name: " + componentPlugin.getName());
        iterator.remove();
      }
    }
  }
}