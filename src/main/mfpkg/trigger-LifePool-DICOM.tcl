##
## Trigger for LifePool DICOM uploads to extract some extra meta-data
##
set asset_detail [asset.get :id $id]
set asset_type   [xvalue asset/type  $asset_detail]
set asset_model  [xvalue asset/model $asset_detail]
if { $asset_type == "dicom/series" && $asset_model == "om.pssd.dataset" } {
   vicnode.daris.lifepool.metadata.extract :id ${id}
}