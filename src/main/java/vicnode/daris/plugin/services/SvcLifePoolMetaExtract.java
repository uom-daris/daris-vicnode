package vicnode.daris.plugin.services;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.AssetType;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.IntegerType;
import arc.xml.XmlDoc;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcLifePoolMetaExtract extends PluginService {

    public static final String SERVICE_NAME = "vicnode.daris.lifepool.metadata.extract";

    public static final String SERVICE_DESCRIPTION = "Specialised service for the LifePool project to extract specific DICOM meta-data tags.";

    private Interface _defn;

    public SvcLifePoolMetaExtract() {
        _defn = new Interface();
        _defn.add(new Interface.Element("cid", CiteableIdType.DEFAULT, "The citeable ID of the dicom/series asset. ", 0,
                1));
        _defn.add(new Interface.Element("id", AssetType.DEFAULT, "The asset ID of the dicom/series asset. ", 0, 1));
        _defn.add(new Interface.Element("idx", IntegerType.POSITIVE,
                "This specifies the idx'th file in the DICOM series archive. Defaults to zero.", 0, 1));
    }

    @Override
    public Access access() {
        return ACCESS_MODIFY;
    }

    @Override
    public Interface definition() {
        return _defn;
    }

    @Override
    public String description() {
        return SERVICE_DESCRIPTION;
    }

    @Override
    public void execute(Element args, Inputs inputs, Outputs outputs, XmlWriter w) throws Throwable {
        String assetId = args.value("id");
        String cid = args.value("cid");
        if (assetId == null && cid == null) {
            throw new IllegalArgumentException("No id or cid is specified.");
        }
        if (assetId != null && cid != null) {
            throw new IllegalArgumentException("Both id and cid and specified. Expects only one.");
        }
        int idx = args.intValue("idx", 0);
        XmlDocMaker dm = new XmlDocMaker("args");
        if (cid != null) {
            dm.add("cid", cid);
        } else {
            dm.add("id", assetId);
        }
        dm.add("idx", idx);
        dm.add("doc-tag", "pssd.meta");
        dm.add("if-exists", "merge");
        dm.add("tag", "00080008"); // Image Type
        dm.add("tag", "00080018"); // SOP Instance UID  ::NOTE:: We need SOPInstanceUID to identify the source dicom file. 
        dm.add("tag", "00080050"); // Accession Number
        dm.add("tag", "00080060"); // Modality
        dm.add("tag", "00080068"); // Presentation Intent Type
        dm.add("tag", "00080070"); // Instrument Manufacturer Name
        dm.add("tag", "00080080"); // Institution
        dm.add("tag", "0008103E"); // Series Description
        dm.add("tag", "00081090"); // Instrument Model
        dm.add("tag", "00181400"); // Acquisition Device Processing Description
        dm.add("tag", "00185101"); // View Position
        dm.add("tag", "00200062"); // Image Laterality
        XmlDoc.Element r = executor().execute("dicom.metadata.populate", dm.root());
        w.addAll(r.elements());
    }

    @Override
    public String name() {
        return SERVICE_NAME;
    }

}
