#============================================================================#
# Method for MDS Femur data collection.                                      #
#                                                                            #
# Arguments:                                                                 #
#     action: action to take if method pre-exists, action = 0 (do nothing),  #
#             1 (replace), 2 (create new)                                    #
#     fillin: when creating Method,                                          #
#             fillin=0 (don't fill in cid allocator space),                  #
#                    1 (fill in cid allocator space)                         #
#============================================================================#
proc create_method_mds_femur { doc_ns { action 0 } { fillin 0 } } {
    set name "Melbourne Femur Collection"
    set description "DaRIS Method for Melbourne Dental School Femur Data Collection."

    # look for existing method with the same name
    set id [xvalue id [om.pssd.method.find :name ${name}]]

    # do nothing if it pre-exists
    if { ${id} != "" && ${action}==0 } {
        return
    }
 
    # append the args based the argument values
    set args ""
    if { $id != "" } {
        if { ${action} == 1 } {
            set args "${args} :replace 1 :id ${id}"
        } else {
            return ""
        }
    }
    if { ${fillin} == 1 } {
       set args "${args} :fillin true"
    } else {
       set args "${args} :fillin false"
    }

    # The subject meta-data just lets you populate mf-note
    # Generally this meta-data would be defined in your own package
    set method_ns [get_asset_namespace_for_methods]
    set args "${args} \
        :namespace \"${method_ns}\"  \
        :name \"${name}\" \
        :description \"${description}\" \
        :subject < \
            :project < \
                :public < \
                    :metadata < :definition -requirement optional ${doc_ns}:femur-subject > > > > \
        :step < \
            :name \"Computed Radiography (CR) acquisition\" \
            :description \"Computed Radiography (CR) acquisition\" \
            :study < \
                :type \"Computed Radiography\" :dicom < :modality CR > \
                :metadata < :definition -requirement optional ${doc_ns}:femur-study > > > \
        :step < \
            :name \"Computed Tomography (CT) acquisition\" \
            :description \"Computed Tomography (CT) acquisition\" \
            :study < \
                :type \"Computed Tomography\" :dicom < :modality CT > \
                :metadata < :definition -requirement optional ${doc_ns}:femur-study > > >"
 
    if { ${id} != "" && ${action} == 1 } {
        # replace (update) the existing method
        om.pssd.method.for.subject.update $args
    } else {
        # create new method
        set id [xvalue id [om.pssd.method.for.subject.update $args]]
    }
    return ${id}
}
