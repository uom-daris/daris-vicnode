#============================================================================#
# Simple method for generic animal acquisitions.                             #
#                                                                            #
# Arguments:                                                                 #
#     action: action to take if method pre-exists, action = 0 (do nothing),  #
#             1 (replace), 2 (create new)                                    #
#     fillin: when creating Method,                                          #
#             fillin=0 (don't fill in cid allocator space),                  #
#                    1 (fill in cid allocator space)                         #
#============================================================================#

proc create_method_animal_generic { doc_ns { action 0 } { fillin 0 } } {
    
    set name "DaRIS Generic Animal Method"
    set description "DaRIS Method for animal subjects with generic image data acquisitions."

    # look for existing method with the same name
    set id [xvalue id [om.pssd.method.find :name ${name}]]

    # do nothing if it pre-exists
    if { ${id} != "" && ${action} == 0 } {
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
   
    set method_ns [get_asset_namespace_for_methods] 
    set args "${args} \
        :namespace \"${method_ns}\"  \
        :name \"${name}\" \
        :description \"${description}\" \
        :subject < \
            :project < \
                :public < \
                    :metadata < :definition -requirement optional ${doc_ns}:pssd-identity > \
                    :metadata < :definition -requirement optional ${doc_ns}:pssd-subject  :value < :type constant(animal) > > \
                    :metadata < :definition -requirement optional ${doc_ns}:pssd-animal-subject > \
                    :metadata < :definition -requirement optional mf-note > > > > \
        :step < \
            :name \"Generic acquisition for animal subject\" \
            :description \"Generic acquisition for animal subject\" \
            :study < :type Unspecified :metadata < :definition -requirement optional ${doc_ns}:vicnode-study > > >"

    if { ${id} != "" && ${action} == 1 } {
        # replace (update) the existing method
        om.pssd.method.for.subject.update $args
    } else {
        # create new method
        set id [xvalue id [om.pssd.method.for.subject.update $args]]
    }
    return ${id}     
}
