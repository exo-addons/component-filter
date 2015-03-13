Component-Filter
==================

This addon allows you to easily disable some parts of eXo Platform product (components and component plugins) 

How to install
==============

1/ Copy this JAR inside platform.ear/lib (JBoss distribution) or TOMCAT/lib (Tomcat distribution)


2/ Modify configuration.properties to add those keys:

---- 
\# **Example**:

\#          exo.container.delete.components=org.gatein.sso.agent.cas.CASAgent

\#          This will avoid the instanciation of the Service 'org.gatein.sso.agent.cas.CASAgent' in eXo Platform

\# **Usage**:

\# FQN of Services Keys to delete (separated by a commar ',')

* exo.container.delete.components=

----
\# **Example**:

\#          exo.container.filter.components=org.exoplatform.services.wcm.core.WebSchemaConfigService,

\#          exo.container.delete.plugins=CSSFileHandler,org.exoplatform.services.wcm.javascript.JSFileHandler

\# 

\#          This will delete two plugins (used as listeners of a Service or for data injection), one with type 'org.exoplatform.services.wcm.javascript.JSFileHandler' and another with name CSSFileHandler

\# **Usage**:

\# FQN of Services Keys to consider for plugins deletion (separated by a commar ',')

* exo.container.filter.components=

\# Type OR Name of Component Plugins to delete from filtered components to delete (separated by a commar ',')

* exo.container.delete.plugins=

----
\# **Example**:
\#          exo.container.modify.params.components=org.exoplatform.portal.config.UserPortalConfigService:default.import.mode:conserve
\# 
\#          This will modify or add a parameter with name 'default.import.mode' in Service UserPortalConfigService with value 'conserve'

\# **Usage**:

\# FQN of Services Keys to consider for init params modification (separated by a commar ',')

* exo.container.modify.params.components=