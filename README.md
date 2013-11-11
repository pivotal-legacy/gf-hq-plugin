vFabric GemFire and SQLFire Hyperic Plugin
==========================================

The vFabric GemFire and SQLFire Hyperic plugin monitors the following products:

 * vFabric GemFire 6.x
 * vFabric GemFire 7.x
 * vFabric SQLFire 1.0
 * vFabric SQLFire 1.1

This README includes separate installation and configuration instructions for each of the four products that the plugin can monitor.  Although many of the steps are similar, each product requires some specific configuration, so you should follow the Setup Instructions for the specific *Fire product you want to monitor with Hyperic.

This plugin does not auto-discover either the GemFire or SQLFire distributed system (DS). Instead, you must manually add the DS to the Hyperic inventory as a new Platform; the instructions are provided below.  After you add the DS to the Hyperic inventory, Hyperic will subsequently auto-discover Members and Regions.    The new Platform will consume a Hyperic license.


Known Issues
============

See http://kb.vmware.com/kb/2039923 for the list of known issues in this Hyperic plugin along with troubleshooting information. 


Setup Instructions for vFabric GemFire 6.x
==========================================

Prerequisites:

 * vFabric Hyperic must be version 4.6 or higher.
 * For each GemFire member (such as servers and locators) that you want to monitor, set the "statistic-sampling-enabled=true" property.  You typically set this property in the "gemfire.properties" file.
 * Ensure that each GemFire member has a unique name.
 * Configure and start a GemFire JMX Agent to connect to your GemFire distributed system. This Hyperic plugin requires only a single JMX Agent to be running.
 * Install the Hyperic Agent on a computer that can connect to the JMX port of the GemFire JMX Agent.  Make a note of the IP address and port of this Hyperic Agent because you will need it later.  If the Hyperic Agent will connect to the GemFire JMX Agent remotely, be sure any firewalls allow access to the Hyperic agent.
 * In the "conf/agent.properties" file of the Hyperic Agent, add the property "Gemfire.autodiscovery.includeInstances=all".  This ensures that all GemFire 6.x services are auto-discovered. This step is optional and can be skipped if detailed metrics are not desired. The Hyperic Agent will need to be restarted after applying this property.


Procedure:

1. Download the vFabric GemFire/SQLFire Plugin JAR file (called "vfgf-plugin.jar") to a directory that is accessible by the vFabric Hyperic Server.

2. Using the Hyperic Plugin Manager, upload the "vfgf-plugin.jar" file to the Hyperic Server.  See the Hyperic Plugin Manager documentation (http://pubs.vmware.com/vfabricHyperic50/topic/com.vmware.vfabric.hyperic.5.0/ui-Administration.Plugin.Manager.html).

3. Log in to the Hyperic user interface as a user with privileges to add a new platform. 

4. Click the Resources tab.

5. Select New Platform from the Tools Menu drop-down list in the upper-left corner.

6. Enter the following information about the new Platform:

   * In the Name field, enter a unique name for the new Platform, such as "vFabric GemFire 6.X".  This name MUST be unique to avoid conflicts in Hyperic.
   * Optionally enter a Description and Location in the corresponding fields.
   * In the Platform Type drop-down list, select "vFabric GemFire Distributed System 6.x". 
   * In the Agent Connection drop-down list, select the Hyperic Agent that has access to the JMX port of the GemFire JMX Agent, as described in the Prerequisites section. 
   * In the IP Address field, enter any IP Address except 127.0.0.1. Note: This plugin does not use this field, which is why you can enter any value except 127.0.0.1, which Hyperic Server does not allow.
   * In the Fully Qualified Domain Name field, enter any unique name. Note: This plugin does not use this field, so you can enter any value.

7. Click OK.  The new Platform is added and its configuration page appears.

8. In the Inventory tab for this new Platform, click the Edit button in the Configuration Properties section.

9. Enter the following information:

   * In the jmx.url field, enter the JMX URL for connecting to the GemFire JMX Agent.  For example, if you are using the default JMX port of 1099, the URL might be "service:jmx:rmi:///jndi/rmi://localhost:1099/jmxconnector".  
   * If you have configured user credentials to connect to the GemFire JMX Agent, enter the username and password in the jmx.username and jmx.password fields.  By default, the GemFire JMX Agent does not require credentials, although your environment may differ.

10. Click OK.  A new auto-discovery is automatically triggered and any GemFire members known by the GemFire JMX Agent will be added to the Hyperic inventory.

11. In the Hyperic UI, browse to the new vFabric GemFire 6.X platform to view its services and servers and the metrics Hyperic is gathering.


Setup Instructions for vFabric GemFire 7.x
==========================================

Prerequisites:

 * vFabric Hyperic must be version 4.6 or higher.
 * Start at least one GemFire locator. The locators will start a JMX Manager automatically. 
 * For each GemFire member (such as servers and locators) that you want to monitor, set the "statistic-sampling-enabled=true" property.  You typically set this property in the "gemfire.properties" file.
 * Ensure that each GemFire member has a unique name.
 * Install the Hyperic Agent on a computer that can connect to the JMX port of the GemFire JMX Manager. Note that the GemFire JMX Manager is distributed and can move between hosts.  Make a note of the IP address and port of this Hyperic Agent because you will need it later.  If the Hyperic Agent will connect to the JMX Manager remotely, be sure any firewalls allow access to the Hyperic Agent.

Procedure:

1. Download the vFabric GemFire/SQLFire Plugin JAR file (called "vfgf-plugin.jar") to a directory that is accessible by the vFabric Hyperic Server.

2. Using the Hyperic Plugin Manager, upload the "vfgf-plugin.jar" file to the Hyperic Server.  See the Hyperic Plugin Manager documentation (http://pubs.vmware.com/vfabricHyperic50/topic/com.vmware.vfabric.hyperic.5.0/ui-Administration.Plugin.Manager.html).

3. Log in to the Hyperic user interface as a user with privileges to add a new platform. 

4. Click the Resources tab.

5. Select New Platform from the Tools Menu drop-down list in the upper-left corner.

6. Enter the following information about the new Platform:

   * In the Name field, enter a unique name for the new Platform, such as "vFabric GemFire 7.X".  This name MUST be unique to avoid conflicts in Hyperic.
   * Optionally enter a Description and Location in the corresponding fields.
   * In the Platform Type drop-down list, select "vFabric GemFire Distributed System 7.x". 
   * In the Agent Connection drop-down list, select the Hyperic Agent that has access to the JMX port of the GemFire JMX Manager, as described in the Prerequisites section. 
   * In the IP Address field, enter any IP Address except 127.0.0.1. Note: This plugin does not use this field, which is why you can enter any value except 127.0.0.1, which Hyperic Server does not allow.
   * In the Fully Qualified Domain Name field, enter any unique name. Note: This plugin does not use this field, so you can enter any value.

7. Click OK.  The new Platform is added and its configuration page appears.

8. In the Inventory tab for this new Platform, click the Edit button in the Configuration Properties section.

9. In the Locators field, enter up to three GemFire locators in the form "host:port", separated by commas.  For example, "hostOne:55221,hostTwo:55221,hostThree:55222".

10. Click OK.  A new auto-discovery is automatically triggered and any GemFire members known by the GemFire JMX Manager will be added to the Hyperic inventory.

11. In the Hyperic UI, browse to the new vFabric GemFire 7.X platform to view its services and servers and the metrics Hyperic is gathering.



Setup Instructions for vFabric SQLFire 1.0
==========================================

Prerequisites:

 * vFabric Hyperic must be version 4.6 or higher.
 * For each SQLFire member (such as servers and locators) that you want to monitor, set the "statistic-sampling-enabled=true" property.  You typically set this property in the "sqlfire.properties" file or at the "sqlf" command line when you start a server or locator.
 * Ensure that each SQLFire member has a unique name.
 * Configure and start a SQLFire JMX Agent to connect to your SQLFire distributed system. This Hyperic plugin requires only a single JMX Agent to be running.
 * Install the Hyperic Agent on a computer that can connect to the JMX port of the SQLFire JMX Agent.  Make a note of the IP address and port of this Hyperic Agent because you will need it later.  If the Hyperic Agent will connect to the SQLFire JMX Agent remotely, be sure any firewalls allow access to the Hyperic agent.


Procedure:

1. Download the vFabric GemFire/SQLFire Plugin JAR file (called "vfgf-plugin.jar") to a directory that is accessible by the vFabric Hyperic Server.

2. Using the Hyperic Plugin Manager, upload the "vfgf-plugin.jar" file to the Hyperic Server.  See the Hyperic Plugin Manager documentation (http://pubs.vmware.com/vfabricHyperic50/topic/com.vmware.vfabric.hyperic.5.0/ui-Administration.Plugin.Manager.html).

3. Log in to the Hyperic user interface as a user with privileges to add a new platform. 

4. Click the Resources tab.

5. Select New Platform from the Tools Menu drop-down list in the upper-left corner.

6. Enter the following information about the new Platform:

   * In the Name field, enter a unique name for the new Platform, such as "vFabric SQLFire 1.0".  This name MUST be unique to avoid conflicts in Hyperic.
   * Optionally enter a Description and Location in the corresponding fields.
   * In the Platform Type drop-down list, select "vFabric SQLFire Distributed System 1.0". 
   * In the Agent Connection drop-down list, select the Hyperic Agent that has access to the JMX port of the SQLFire JMX Agent, as described in the Prerequisites section. 
   * In the IP Address field, enter any IP Address except 127.0.0.1. Note: This plugin does not use this field, which is why you can enter any value except 127.0.0.1, which Hyperic Server does not allow.
   * In the Fully Qualified Domain Name field, enter any unique name. Note: This plugin does not use this field, so you can enter any value.

7. Click OK.  The new Platform is added and its configuration page appears.

8. In the Inventory tab for this new Platform, click the Edit button in the Configuration Properties section.

9. Enter the following information:

   * In the jmx.url field, enter the JMX URL for connecting to the SQLFire JMX Agent.  For example, if you are using the default JMX port of 1099, the URL might be "service:jmx:rmi:///jndi/rmi://localhost:1099/jmxconnector".  
   * If you have configured user credentials to connect to the SQLFire JMX Agent, enter the username and password in the jmx.username and jmx.password fields.  By default, the SQLFire JMX Agent does not require credentials, although your environment may differ.

10. Click OK.  A new auto-discovery is automatically triggered and any SQLFire members known by the SQLFire JMX Agent will be added to the Hyperic inventory.

11. In the Hyperic UI, browse to the new SQLFire 1.0 platform to view its services and servers and the metrics Hyperic is gathering.

Setup Instructions for vFabric SQLFire 1.1
==========================================

Prerequisites:

 * vFabric Hyperic must be version 4.6 or higher.
 * Start at least one SQLFire locator.   Start one of the locators using the "jmx-manager=true" property so that it also acts as a JMX Manager.
 * Ensure that each SQLFire member has a unique name.
 * Install the Hyperic Agent on a computer that can connect to the JMX port of the GemFire JMX Manager. Note that the GemFire JMX Manager is distributed and can move between hosts.  Make a note of the IP address and port of this Hyperic Agent because you will need it later.  If the Hyperic Agent will connect to the JMX Manager remotely, be sure any firewalls allow access to the Hyperic Agent.

Procedure:

1. Download the vFabric GemFire/SQLFire Plugin JAR file (called "vfgf-plugin.jar") to a directory that is accessible by the vFabric Hyperic Server.

2. Using the Hyperic Plugin Manager, upload the "vfgf-plugin.jar" file to the Hyperic Server.  See the Hyperic Plugin Manager documentation (http://pubs.vmware.com/vfabricHyperic50/topic/com.vmware.vfabric.hyperic.5.0/ui-Administration.Plugin.Manager.html).

3. Log in to the Hyperic user interface as a user with privileges to add a new platform. 

4. Click the Resources tab.

5. Select New Platform from the Tools Menu drop-down list in the upper-left corner.

6. Enter the following information about the new Platform:

   * In the Name field, enter a unique name for the new Platform, such as "vFabric SQLFire 1.1".  This name MUST be unique to avoid conflicts in Hyperic.
   * Optionally enter a Description and Location in the corresponding fields.
   * In the Platform Type drop-down list, select "vFabric SQLFire Distributed System 1.1". 
   * In the Agent Connection drop-down list, select the Hyperic Agent that has access to the JMX port of the GemFire JMX Manager, as described in the Prerequisites section. 
   * In the IP Address field, enter any IP Address except 127.0.0.1. Note: This plugin does not use this field, which is why you can enter any value except 127.0.0.1, which Hyperic Server does not allow.
   * In the Fully Qualified Domain Name field, enter any unique name. Note: This plugin does not use this field, so you can enter any value.

7. Click OK.  The new Platform is added and its configuration page appears.

8. In the Inventory tab for this new Platform, click the Edit button in the Configuration Properties section.

9. In the Locators field, enter the SQLFire locator that is also acting as a JMX Manager in the form "host:port".  For example, "hostOne:10334".

10. Click OK.  A new auto-discovery is automatically triggered and any SQLFire members known by the SQLFire JMX Manager will be added to the Hyperic inventory.

11. In the Hyperic UI, browse to the new vFabric SQLFire 1.1 platform to view its services and servers and the metrics Hyperic is gathering.

