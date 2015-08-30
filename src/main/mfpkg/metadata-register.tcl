#============================================================================#
# Register generic meta-data with specific PSSD objects                      #
# This is domain-specific, but not method-specific meta-data                 #
#============================================================================#
proc register_metadata { doc_ns } {

    # Notifications
    set mtypeArgs ":mtype -requirement optional daris:pssd-notification"
    
    # Basic project info
    set mtypeArgs "${mtypeArgs} :mtype -requirement optional daris:pssd-project-governance"
    set mtypeArgs "${mtypeArgs} :mtype -requirement optional daris:pssd-project-research-category"
    
    # Generic Project owner 
    set mtypeArgs "${mtypeArgs} :mtype -requirement optional daris:pssd-project-owner"
    
    # Publications
    #set mtypeArgs "${mtypeArgs} :mtype -requirement optional daris:pssd-publications"
   
    # Your own doc types
    #set mtypeArgs "${mtypeArgs} :mtype -requirement optional ${doc_ns}:your-pssd-project-doc"
    
    # Replace any pre-existing (use :append true to append)
    set args ":append false :type project ${mtypeArgs}"
    om.pssd.type.metadata.set $args 
}
