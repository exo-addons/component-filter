component-filter
==================

This addon allows you to easily disable some parts of eXo Platform product (components and component plugins) 

How to install
==============

1/ Copy this JAR inside platform.ear/lib (JBoss distribution) or TOMCAT/lib (Tomcat distribution)
2/ Modify configuration.properties to add those keys:

\# Components to delete (separated by a commar ',')
exo.container.delete.components=
\# Components to consider for plugins deletion (separated by a commar ',')
exo.container.filter.components=
\# Component Plugin Types to delete from filtered components to delete (separated by a commar ',')
exo.container.delete.plugins.types=
\# Component Plugin Names to delete from filtered components to delete (separated by a commar ',')
exo.container.delete.plugins.names=
{code}
