#                                                                            #
# Usage:                                                                     #
#     package.install :arg -name <arg-name> <value>                          #
#                                                                            #
# Arguments:                                                                 #
#     studyTypes - If set to false, does not add the pssd Study type         #
#                  definitions. Defaults to true.                            #
#          model - Set to false to not make any changes to the object model  #
#                  such as what meta-data are registered with the data model.#
#                  Defaults to true.                                         #
#         fillIn - Set to true to fill in CID space for Methods when         #
#                  creating.                                                 #
#         action - If Method pre-exists, action = 0 (do nothing),            #
#                  1 (replace), 2 (create new)                               #
#                                                                            #
# include the utility functions in utils.tcl.
source utils.tcl

# organization short name
set ORG            [string tolower "vicnode"]
# java source package
set PKG            "vicnode.daris.plugin"
# maven artifact id
set ARTIFACT_ID    "daris-vicnode"

set DOC_NS         "${ORG}.daris"
set DICT_NS        "${ORG}.daris"
set ROLE_NS        "${ORG}.daris"
set SERVICE_PREFIX "${ORG}.daris"
set AUTH_DOMAIN    "daris-test"

#============================================================================
# Create dictionaries. You need to modify dictionaries.tcl to create 
# your own dictionaries.
#
# Note: it is created first because services may, when being reloaded, 
#       instantiate classes which specify dictionaries
#============================================================================
dictionary.namespace.create :namespace ${DICT_NS} :ifexists ignore
actor.grant :name daris:pssd.administrator :type role \
    :perm < :access ADMINISTER :resource -type dictionary:namespace ${DICT_NS} >
source dictionaries.tcl
create_dictionaries ${DICT_NS}

#=============================================================================
# Create document types in own doc type namespace
#=============================================================================
if { [xvalue exists [asset.doc.namespace.exists :namespace ${DOC_NS}]] == "false" } {
    asset.doc.namespace.create :namespace ${DOC_NS}
} 
source doc-types.tcl
create_doc_types ${DOC_NS} ${DICT_NS}

#============================================================================
# Add our Study Types. The command-line arguments allows you to choose to
# not add our study types, so other sites can fully define their own.
#============================================================================
set createStudyTypes 1
if { [info exists studyTypes ] } {
    if { ${studyTypes} == "false" } {
        set createStudyTypes 0
    }
}
if { ${createStudyTypes} == 1 } {
   source study-types.tcl
   create_study_types
}

#=============================================================================
# Create methods
#=============================================================================
# Method fill-in argument
set fillInMethods 1
if { [info exists fillIn ] } {
    if { $fillIn == "false" } {
        set fillInMethods 0
    }
}

# Method action argument
set methodAction 0
if { [info exists action ] } {
    set methodAction $action
}

# ============================================================================
# Create methods
# ============================================================================
source method-generic.tcl
create_method_generic ${DOC_NS} $methodAction $fillInMethods

source method-human-generic.tcl
create_method_human_generic ${DOC_NS} $methodAction $fillInMethods

source method-animal-multimode.tcl
create_method_animal_multimode ${DOC_NS} $methodAction $fillInMethods

# ============================================================================
# Add plugin module
# ============================================================================
set plugin_label      [string toupper PACKAGE_vicnode.daris.plugin]
set plugin_namespace  mflux/plugins
set plugin_zip        "${ARTIFACT_ID}-plugin.zip"
set plugin_jar        "${ARTIFACT_ID}-plugin.jar"
set module_class      "${PKG}.DarisPluginModule"
add_plugin_module ${plugin_namespace} ${plugin_zip} ${plugin_jar} ${module_class} ${plugin_label} { daris-commons.jar }

srefresh

#=============================================================================
# Set up roles & permissions
#=============================================================================
source role-permissions.tcl
set_role_permissions ${DOC_NS} ${ROLE_NS} ${DICT_NS} ${SERVICE_PREFIX}

#=============================================================================
# Register a Project role-member exemplar
#=============================================================================
#source role-members-register.tcl
#register_role_member ${ROLE_NS}

#=============================================================================
# Register metadata with the data model
#=============================================================================
set updateModel 1
if { [info exists model] } {
    if { $model == "false" } {
        set updateModel 0
    }
}
if { $updateModel == 1 } {
   source metadata-register.tcl
   register_metadata ${DOC_NS}
}
