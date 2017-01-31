# Install triggers
#
# LifePool : a specific DaRIS project. The DaRIS project CID must be supplied to install or uninstall the trigger
#
proc install_LifePool_trigger { CID } {

set LP_ns [xvalue asset/namespace [asset.get :cid ${CID}]]
set LP_trigger_script trigger-LifePool-DICOM.tcl
set LP_trigger_script_ns system/triggers
set LP_trigger_script_label [string toupper  DaRIS-LifePool]


if { [xvalue exists [asset.namespace.exists :namespace $LP_ns]] == "true" } {
    
    # destroy the script asset if it pre-exists
    if { [xvalue exists [asset.exists :id path=${LP_trigger_script_ns}/${LP_trigger_script}]] == "true" } {
        asset.hard.destroy :id path=${LP_trigger_script_ns}/${LP_trigger_script}
    }

    # create the new trigger script asset
    asset.create :url archive:///$LP_trigger_script \
        :namespace -create yes $LP_trigger_script_ns \
        :label -create yes $LP_trigger_script_label :label PUBLISHED \
	    :name $LP_trigger_script

    # remove all old triggers on the namespace
    asset.trigger.destroy :namespace $LP_ns

    # create the triggers
    asset.trigger.post.create :namespace -descend true $LP_ns :event create :script -type ref ${LP_trigger_script_ns}/${LP_trigger_script}
}


proc uninstall_LifePool_trigger { CID } {

set LP_ns [xvalue asset/namespace [asset.get :cid ${CID}]]
set LP_trigger_script trigger-LifePool-DICOM.tcl
set LP_trigger_script_ns system/triggers

# Destroy trigger
if { [xvalue exists [asset.namespace.exists :namespace ${LP_ns}]] == "true" } {
    asset.trigger.destroy :namespace $LP_ns
}

# Destroy trigger script asset
if { [xvalue exists [asset.exists :id path=${LP_trigger_script_ns}/${LP_trigger_script}]] == "true" } {
    asset.hard.destroy :id path=${LP_trigger_script_ns}/${LP_trigger_script}
}

}