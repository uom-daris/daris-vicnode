proc set_role_permissions { doc_ns role_ns dict_ns service_prefix } {

    # Standard document types
    set doc_perms [ list \
        [ list document ${doc_ns}:pssd-subject ACCESS ] \
        [ list document ${doc_ns}:pssd-subject PUBLISH ] \
        [ list document ${doc_ns}:pssd-human-subject ACCESS ] \
        [ list document ${doc_ns}:pssd-human-subject PUBLISH ] \
        [ list document ${doc_ns}:pssd-identity ACCESS ] \
        [ list document ${doc_ns}:pssd-identity PUBLISH ] \
        [ list document ${doc_ns}:pssd-human-identity ACCESS ] \
        [ list document ${doc_ns}:pssd-human-identity PUBLISH ] \
        [ list document ${doc_ns}:pssd-animal-subject ACCESS ] \
        [ list document ${doc_ns}:pssd-animal-subject PUBLISH ] ]

    # Service access
    set service_perms  [ list \
        [ list service "${service_prefix}.*" ACCESS ] \
        [ list service "${service_prefix}.*" MODIFY ] \
        [ list service server.database.describe ACCESS ] ]

    # Create role namespace
    authorization.role.namespace.create :namespace ${role_ns} :ifexists ignore

    # Role for user of this package; grant this to your users.
    set domain_model_user_role        ${role_ns}:pssd.model.user
    authorization.role.create :ifexists ignore :role ${domain_model_user_role}
    actor.grant :name ${domain_model_user_role} :type role :perm < :access ACCESS :resource -type dictionary:namespace ${dict_ns} >


    grant_role_perms ${domain_model_user_role} ${doc_perms}
    grant_role_perms ${domain_model_user_role} ${service_perms}
     
    # Grant end users the right to access the  document namespace
    grant_role_perms ${domain_model_user_role} [ list [ list "document:namespace" ${doc_ns} ACCESS ] ]


    # Role: ${role_ns}:pssd.administrator 
    #
    # Holders of this role should be able to undertake nig-pssd admin activities
    # without the full power of system:administrator.  Admin services
    # require permission ADMINISTER to operate. Also grants the
    # daris:pssd.administrator (which holds and daris:essentials.administrator) roles
    authorization.role.create :ifexists ignore :role ${role_ns}:pssd.administrator
    actor.grant :name ${role_ns}:pssd.administrator :type role  \
        :role -type role  ${domain_model_user_role} \
        :role -type role daris:pssd.administrator
      
    # These services need ADMINISTER to be able to execute. 
    # Create the service ${service_prefix}.pssd.user.create with access ADMINISTER
    actor.grant :name ${role_ns}:pssd.administrator :type role \
                :perm < :access ADMINISTER :resource -type service ${service_prefix}.* >

    # DICOM server permissions 

    # Set the permissions that allow the <ns>.pssd.subject.meta.set service to be called
    # and used by the DICOM server framework
    set dicom_ingest_doc_perms [ list \
        [ list document ${doc_ns}:pssd-subject ACCESS ] \
        [ list document ${doc_ns}:pssd-subject PUBLISH ] \
        [ list document ${doc_ns}:pssd-human-subject ACCESS ] \
        [ list document ${doc_ns}:pssd-human-subject PUBLISH ] \
        [ list document ${doc_ns}:pssd-identity ACCESS ] \
        [ list document ${doc_ns}:pssd-identity PUBLISH ] \
        [ list document ${doc_ns}:pssd-human-identity ACCESS ] \
        [ list document ${doc_ns}:pssd-human-identity PUBLISH ] \
        [ list document ${doc_ns}:pssd-animal-subject ACCESS ] \
        [ list document ${doc_ns}:pssd-animal-subject PUBLISH ]]

    # Service that allows the DICOM server to set domain-specific meta-data
    set dicom_ingest_service_perms [ list [ list service ${service_prefix}.* MODIFY ] ]

    # This is the role to grant your DICOM proxy users
    set domain_dicom_ingest_role ${role_ns}:pssd.dicom-ingest
    authorization.role.create :ifexists ignore :role ${domain_dicom_ingest_role}

    # Grant DICOM users the right to access the  document namespace
    actor.grant :name  $domain_dicom_ingest_role :type role :perm < :resource -type document:namespace ${doc_ns} :access ACCESS >

    # Doc and service perms
    grant_role_perms $domain_dicom_ingest_role $dicom_ingest_doc_perms
    grant_role_perms $domain_dicom_ingest_role $dicom_ingest_service_perms

}

