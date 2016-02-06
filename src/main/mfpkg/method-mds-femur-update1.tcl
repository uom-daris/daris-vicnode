proc update_method { cid } {
    asset.set :cid ${cid} :meta < \
            :daris:pssd-method < \
                :version "1.2" \
                :step -id 1 < \
                    :name "Computed Radiography (CR) acquisition" \
                    :description "Computed Radiography (CR) acquisition" \
                    :study < \
                        :type "Computed Radiography" \
                        :dicom < \
                            :modality "CR" > \
                        :metadata < \
                            :definition -requirement "optional" "vicnode.daris:femur-study" > > > \
                :step -id 2 < \
                    :name "Computed Tomography (CT) acquisition" \
                    :description "Computed Tomography (CT) acquisition" \
                    :study < \
                        :type "Computed Tomography" \
                        :dicom < \
                            :modality "CT" > \
                        :metadata < \
                            :definition -requirement "optional" "vicnode.daris:femur-study" > > > \
                :step -id 3 < \
                    :name "Magnetic Resonance (MR) acquisition" \
                    :description "Magnetic Resonance (MR) acquisition" \
                    :study < \
                        :type "Magnetic Resonance Imaging" \
                        :dicom < \
                            :modality "MR" > \
                        :metadata < \
                            :definition -requirement "optional" "vicnode.daris:femur-study" > > > > >
}

set method_name "Melbourne Femur Collection"
set project_cid "1128.1.1"
set method_cid [xvalue cid [asset.query :action get-cid :where model='om.pssd.method' and xpath(daris:pssd-object/name)='${method_name}']]
puts "Updating method: ${method_cid}..."
update_method ${method_cid}
foreach cid [xvalues cid [asset.query :size infinity :action get-cid :where cid starts with '${project_cid}' and model='om.pssd.ex-method' and daris:pssd-method has value]] {
    puts "Updating ex-method: ${cid}..."
    update_method $cid
}