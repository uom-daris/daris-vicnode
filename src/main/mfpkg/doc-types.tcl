#============================================================================#
# creates doc type: ${doc_ns}:vicnode-collections                            #
#============================================================================#
proc create_doc_type_vicnode_collections { doc_ns } {
    asset.doc.type.update :create yes :type ${doc_ns}:vicnode-collections \
        :label "VicNode Collections" \
        :description "VicNode collections associated with the DaRIS projects." \
        :definition < \
            :element -name collection -type document -index true -min-occurs 0 -max-occurs infinity < \
                :description "VicNode collection" \
                :attribute -name code -type string -index true -min-occurs 1 < \
                    :description "The unique VicNode collection code." > \
                :element -name name -type string -index true -min-occurs 0 -max-occurs 1 < \
                    :description  "Name of the VicNode collection" > \
                :element -name quota -type double -index true -min-occurs 1 -max-occurs 1 < \
                    :description "The storage quota(unit: GB)." > \
                :element -name contact -type document -index true -min-occurs 0 -max-occurs 1 < \
                    :description "The contact information for the collection." \
                    :element -name name -type string -index true -min-occurs 1 -max-occurs 1 < \
                        :description "The name of the contact person." > \
                    :element -name email -type email-address -index true -min-occurs 0 -max-occurs 1 < \
                        :description "The email of the contact person" > > \
                :element -name attribute -type string -index true -min-occurs 0 -max-occurs 255 < \
                    :description "Arbitrary attribute of the collection." \
                    :attribute -name name -type string -index true -min-occurs 1 < \
                        :description "Attribute name" > > > >
}


#============================================================================#
# creates doc type: ${doc_ns}:vicnode-collection-identity                    #
#============================================================================#
proc create_doc_type_vicnode_collection_identity { doc_ns } {
    asset.doc.type.update :create yes :type ${doc_ns}:vicnode-collection-identity \
        :label "VicNode Collection Identity" \
        :description "The identity informantion about the vicnode collection." \
        :definition < \
            :element -name code -type string -index true -min-occurs 1 -max-occurs 1 < \
                :description "The unique identity code of the VicNode collection." > >
}

#============================================================================#
# creates doc type: ${doc_ns}:pssd-subject                                   #
#============================================================================#
proc create_doc_type_pssd_subject { doc_ns } {
    asset.doc.type.update :create yes :type ${doc_ns}:pssd-subject \
        :label "Subject" \
        :description "Basic domain-specific document type for a subject" \
        :definition < \
            :element -name "type" -type "enumeration" -index "true" -max-occurs "1" < \
                :description "Type of subject. Artificial might be used for, e.g. a phantom in an MR scanner. Internal might be used for an internally generated instrument system test (e.g. quality assurance" \
                :restriction -base "enumeration" < \
                    :value "animal" \
                    :value "vegetable" \
                    :value "mineral" \
                    :value "artificial" \
                    :value "internal" \
                    :value "unknown" > > \
            :element -name "control" -type "boolean" -index "true" -min-occurs "0" -max-occurs "1" < \
                :description "Subject is a member of a control group" > >
}

#============================================================================#
# creates doc type: ${doc_ns}:pssd-animal-subject                            #
#============================================================================#
proc create_doc_type_pssd_animal_subject { doc_ns dict_ns } {
    asset.doc.type.update :create yes :type ${doc_ns}:pssd-animal-subject \
        :label "Animal subject" \
        :description "Basic document type for a domain specific animal (humans included) subject" \
        :definition < \
            :element -name "species" -type "enumeration" -index "true" -min-occurs "0" -max-occurs "1" < \
                :description "Species type of the animal" \
                :restriction -base "enumeration" < \
                    :dictionary "${dict_ns}:pssd.animal.species" > > \
            :element -name "body-part" -type "enumeration" -index "true" -min-occurs "0" < \
                :description "Body part of the animal" \
                :restriction -base "enumeration" < \
                    :dictionary "${dict_ns}:pssd.animal.bodypart" > \
                :attribute -name "sidedness" -type "boolean" -min-occurs "0" < \
                    :description "If the body part comes from the left or right (your convention for orientation) side you can specify here.  Don't supply to leave unspecified." > > \
            :element -name "gender" -type "enumeration" -index "true" -min-occurs "0" -max-occurs "1" < \
                :description "Gender of the subject" \
                :restriction -base "enumeration" < \
                    :value "male" \
                    :value "female" \
                    :value "other" \
                    :value "unknown" > > \
            :element -name "birthDate" -type "date" -index "true" -min-occurs "0" -max-occurs "1" < \
                :description "Birth date of the subject" \
                :restriction -base "date" < :time false > > \
            :element -name "deceased" -type "boolean" -index "true" -min-occurs "0" -max-occurs "1" < \
                :description "Subject is deceased (cadaver)" > \
            :element -name "deathDate" -type "date" -index "true" -min-occurs "0" -max-occurs "1" < \
                :description "Death date of the subject" \
                :restriction -base "date" < :time false > > \
            :element -name "age-at-death" -type "integer" -index "true" -min-occurs "0" -max-occurs "1" < \
                :description "Age of subject (days or weeks) at time of death (intended for non-human subjects)." \
                :restriction -base "integer" < \
                    :minimum "0" > \
                :attribute -name "units" -type "enumeration" -min-occurs "0" < \
                    :restriction -base "enumeration" < \
                        :value "days" \
                        :value "weeks" > > > \
            :element -name "weight-at-death" -type "float" -index "true" -min-occurs "0" -max-occurs "1" < \
                :description "Weight of subject (g or Kg) at time of death (intended for non-human subjects." \
                :restriction -base "float" < \
                    :minimum "0" > \
                :attribute -name "units" -type "enumeration" -min-occurs "0" < \
                    :restriction -base "enumeration" < \
                        :value "g" \
                        :value "Kg" > > > >
}

#============================================================================#
# creates doc type: ${doc_ns}:pssd-human-subject                             #
#============================================================================#
proc create_doc_type_pssd_human_subject { doc_ns } {
    asset.doc.type.update :create yes :type ${doc_ns}:pssd-human-subject \
        :label "Human Subject" \
        :description "Document type for a Human subject" \
        :definition < \
            :element -name "handedness" -type "enumeration" -index "true" -min-occurs "0" -max-occurs "1" < \
                :description "Handedness of the subject" \
                :restriction -base "enumeration" < \
                    :value "left" \
                    :value "right" \
                    :value "ambidextrous" \
                    :value "unknown" > > \
            :element -name "height" -type "float" -index "true" -min-occurs "0" -max-occurs "1" < \
                :description "Height of subject (m)" \
                :restriction -base "float" < \
                    :minimum "0" > \
                :attribute -name "units" -type "enumeration" -min-occurs "0" < \
                    :restriction -base "enumeration" < \
                        :value "m" > > > >
}

#============================================================================#
# creates doc type: ${doc_ns}:pssd-identity                                  #
#============================================================================#
proc create_doc_type_pssd_identity { doc_ns } {
    asset.doc.type.update :create yes :type ${doc_ns}:pssd-identity \
        :label "External Subject Identifier" \
        :description "Document type for subject identity" \
        :definition < \
            :element -name "id" -type "string" -index "true" -min-occurs "0" < \
                :description "Unique identifier for the subject allocated by some other authority for cross-referencing" > >
}
    
#============================================================================#
# creates doc type: ${doc_ns}:pssd-human_identity                            #
#                                                                            #
# Generally, human subjects are  re-used and so these meta-data should be    #
# placed on the identity object                                              #
#============================================================================#
proc create_doc_type_pssd_human_identity { doc_ns } {
    asset.doc.type.update :create true :type ${doc_ns}:pssd-human-identity \
        :label "Human Identification" \
        :description "Document type for human subject identity" \
        :definition < \
            :element -name prefix -type string -min-occurs 0 -max-occurs 1 -length 20 -label "Prefix" \
            :element -name first  -type string -min-occurs 1 -max-occurs 1 -length 40 -label "First" \
            :element -name middle -type string -min-occurs 0 -max-occurs 1 -length 100 -label "Middle" < \
                :description "If there are several 'middle' names then put them in this field" > \
            :element -name last   -type string -min-occurs 1 -max-occurs 1 -length 40 -label "Last" \
            :element -name suffix -type string -min-occurs 0 -max-occurs 1 -length 20 -label "Suffix" >
}

#============================================================================#
# creates doc type: ${doc_ns}:vicnode-study                                  #
#                                                                            #
# Information about the generic VicNode study.                               #  
#============================================================================#
proc create_doc_type_vicnode_study { doc_ns } {
    asset.doc.type.update :create true \
        :type ${doc_ns}:vicnode-study \
        :label "VicNode study" \
        :description "A VicNode generic study." \
        :definition < \
            :element -name "ingest" -type "document" -index "true" -min-occurs "0" -max-occurs "1" < \
                :description "Ingest details." \
                :element -name "date" -type "date" -index "true" -max-occurs "1" < \
                    :description "Date and time when the study was ingested." > \
                :element -name "domain" -type "string" -index "true" -max-occurs "1" < \
                    :description "Domain of the user that ingested this study." > \
                :element -name "user" -type "string" -index "true" -max-occurs "1" < \
                    :description "User that ingested this study." > > \
            :element -name "sdate" -type date -min-occurs 0 -max-occurs 1 -index true < \
                :description "Date on which acquisition of the study was started." \
                :restriction -base date < :time false > > >
}


#============================================================================#
# creates doc type: ${doc_ns}:femur-subject                                  #
#                                                                            #
# Information about the MDS Femur (human) subject.                           #  
#============================================================================#
proc create_doc_type_femur_subject { doc_ns } {
    asset.doc.type.update \
        :create true :type ${doc_ns}:femur-subject \
        :label "Femur subject" \
        :description "MDS Femur subject." \
        :definition < \
            :element -name "specimen-number" -type integer -min-occurs 1 -max-occurs 2 -index true < :description "The specimen number." > \
            :element -name "vifm-case-number" -type string -min-occurs 0 -max-occurs 1 -index true < :description "The VIFM case number." > \
            :element -name "age" -type float -min-occurs 0 -max-occurs 1 -index true < :description "The age of the human subject. (unit: year)" > \
            :element -name "sex" -type enumeration -min-occurs 0 -max-occurs 1 -index true < \
                :description "The sex of the subject." \
                :restriction -base enumeration < :value "male" :value "female" :value "unknown" > > \
            :element -name "height" -type float -min-occurs 0 -max-occurs 1 -index true < :description "The subject's height. (unit: cm)" > \
            :element -name "weight" -type float -min-occurs 0 -max-occurs 1 -index true < :description "The subject's weight. (unit: kg)" > >
}


#============================================================================#
# creates doc type: ${doc_ns}:femur-study                                    #
#                                                                            #
# Information about the MDS Femur study.                                     #  
#============================================================================#
proc create_doc_type_femur_study { doc_ns dict_ns } {
    asset.doc.type.update :create true \
        :type ${doc_ns}:femur-study \
        :label "Femur study" \
        :description "A Femur study." \
        :definition < \
            :element -name "ingest" -type "document" -index "true" -min-occurs "0" -max-occurs "1" < \
                :description "Ingest details." \
                :element -name "date" -type "date" -index "true" -max-occurs "1" < \
                    :description "Date and time when the study was ingested." > \
                :element -name "domain" -type "string" -index "true" -max-occurs "1" < \
                    :description "Domain of the user that ingested this study." > \
                :element -name "user" -type "string" -index "true" -max-occurs "1" < \
                    :description "User that ingested this study." > > \
            :element -name "sdate" -type date -min-occurs 0 -max-occurs 1 -index true < \
                :description "Date on which acquisition of the study was started." \
                :restriction -base date < :time false > > \
            :element -name "subject" -type document -min-occurs 0 -max-occurs 1 -index true < \
                :description "Subject statistics, at the time of study." \
                :element -name "age" -type float -min-occurs 0 -max-occurs 1 -index true < :description "The age of the human subject. (unit: year)" > \
                :element -name "sex" -type enumeration -min-occurs 0 -max-occurs 1 -index true < \
                    :description "The sex of the subject." \
                    :restriction -base enumeration < :value "male" :value "female" :value "unknown" > > \
                :element -name "height" -type float -min-occurs 0 -max-occurs 1 -index true < :description "The subject's height. (unit: cm)" > \
                :element -name "weight" -type float -min-occurs 0 -max-occurs 1 -index true < :description "The subject's weight. (unit: kg)" > > \
            :element -name "specimen-number" -type integer -min-occurs 1 -max-occurs 2 -index true < :description "The specimen number." > \
            :element -name "vifm-case-number" -type string -min-occurs 0 -max-occurs 1 -index true < :description "The VIFM case number." > \
            :element -name "specimen-type" -type enumeration -min-occurs 1 -max-occurs 1 -index true < \
                :description "The specimen type of the subject." \
                :restriction -base enumeration < :dictionary ${dict_ns}:femur.specimen.type > > \
            :element -name "blood" -type boolean -min-occurs 1 -max-occurs 1 -index true < :description "blood" > \
            :element -name "hard-ground-sections" -type boolean -min-occurs 1 -max-occurs 1 -index true < :description "hard ground sections" > \
            :element -name "mounted-sections" -type boolean -min-occurs 1 -max-occurs 1 -index true < :description "mounted sections" > \
            :element -name "two-inch-glass-plates" -type boolean -min-occurs 0 -max-occurs 1 -index true < :description "2 inch glass plates" > \
            :element -name "plane-radio-graph-of-pelvis" -type boolean -min-occurs 0 -max-occurs 1 -index true < :description "plane radio graph of pelvis" > \
            :element -name "mid-shaft-porosity-and-cross-sectional-geometry-data" -type boolean -min-occurs 0 -max-occurs 1 -index true < :description "mid-shaft porosity and cross-sectional geometry data" > \
            :element -name "autopsy-report-or-medical-questionnaire" -type boolean -min-occurs 0 -max-occurs 1 -index true < :description "Autopsy Report or Medical Questionnaire" > >
}


#============================================================================#
# creates doc type: ${doc_ns}:femur-dataset                                  #
#                                                                            #
# Information about the MDS Femur dataset.                                   #  
#============================================================================#
proc create_doc_type_femur_dataset { doc_ns dict_ns } {
    asset.doc.type.update :create true \
        :type ${doc_ns}:femur-dataset \
        :label "Femur dataset" \
        :description "A Femur dataset." \
        :definition < \
            :element -name "specimen-type" -type enumeration -min-occurs 1 -max-occurs 1 -index true < \
                :description "The specimen type of the subject." \
                :restriction -base enumeration < :dictionary ${dict_ns}:femur.specimen.type > > \
            :element -name "image-type" -type enumeration -min-occurs 1 -max-occurs 1 -index true < \
                :description "The image type" \
                :restriction -base enumeration < :dictionary ${dict_ns}:femur.image.type > > >
}

#============================================================================#
# creates all domain specific doc types                                      #
#============================================================================#
proc create_doc_types { doc_ns dict_ns } {
    create_doc_type_vicnode_collections ${doc_ns}
    create_doc_type_vicnode_collection_identity ${doc_ns}
    create_doc_type_pssd_subject ${doc_ns}
    create_doc_type_pssd_animal_subject ${doc_ns} ${dict_ns}
    create_doc_type_pssd_human_subject ${doc_ns}
    create_doc_type_pssd_identity ${doc_ns}
    create_doc_type_pssd_human_identity ${doc_ns}
    create_doc_type_vicnode_study ${doc_ns}
    create_doc_type_femur_subject ${doc_ns}
    create_doc_type_femur_study ${doc_ns} ${dict_ns}
    create_doc_type_femur_dataset ${doc_ns} ${dict_ns}
}

#============================================================================#
# destroys all domain specific doc types                                     #
#============================================================================#
proc destroy_doc_types { doc_ns } {
    try { asset.doc.type.destroy :type ${doc_ns}:vicnode-collections         :force true } catch { Throwable } { }
    try { asset.doc.type.destroy :type ${doc_ns}:vicnode-collection-identity :force true } catch { Throwable } { }
    try { asset.doc.type.destroy :type ${doc_ns}:pssd-subject                :force true } catch { Throwable } { }
    try { asset.doc.type.destroy :type ${doc_ns}:pssd-animal-subject         :force true } catch { Throwable } { }
    try { asset.doc.type.destroy :type ${doc_ns}:pssd-human-subject          :force true } catch { Throwable } { }
    try { asset.doc.type.destroy :type ${doc_ns}:pssd-identity               :force true } catch { Throwable } { }
    try { asset.doc.type.destroy :type ${doc_ns}:pssd-human-identity         :force true } catch { Throwable } { }
    try { asset.doc.type.destroy :type ${doc_ns}:vicnode-study               :force true } catch { Throwable } { }
    try { asset.doc.type.destroy :type ${doc_ns}:femur-subject               :force true } catch { Throwable } { }
    try { asset.doc.type.destroy :type ${doc_ns}:femur-study                 :force true } catch { Throwable } { }
    try { asset.doc.type.destroy :type ${doc_ns}:femur-dataset               :force true } catch { Throwable } { }
}
