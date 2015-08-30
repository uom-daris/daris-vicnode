# include the utility functions in utils.tcl.
source utils.tcl

# organization short name
set ORG            [string tolower "vicnode"]
# java source package
set PKG            "vicnode.daris.plugin"
# maven artifact id
set ARTIFACT_ID    "daris-vicnode"

# remove plugin module
set plugin_namespace  mflux/plugins
set plugin_jar        "${ARTIFACT_ID}-plugin.jar"
set module_class      "${PKG}.DarisPluginModule"
remove_plugin_module ${plugin_namespace} ${plugin_jar} ${module_class}

