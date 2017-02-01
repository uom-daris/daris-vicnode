package vicnode.daris.plugin.services;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcLifePoolMetaExtract extends PluginService {

    public static final String SERVICE_NAME = "vicnode.daris.lifepool.metadata.extract";

    public static final String SERVICE_DESCRIPTION = "Specialised service for the LifePool project to extract specific DICOM meta-data tags.";

    private Interface _defn;

    public SvcLifePoolMetaExtract () {
        _defn = new Interface();
        _defn.add(new Interface.Element("id", CiteableIdType.DEFAULT,
                "The asset ID of the dicom/series asset. ", 1, 1));
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
    public void execute(Element args, Inputs inputs, Outputs outputs,
            XmlWriter w) throws Throwable {
        String id = args.value("id");
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("id", id);
        dm.add("if-exists", "replace");
        dm.add("tag", "00080008");   // Image Type
        dm.add("tag", "00080050");   // Accession Number
        dm.add("tag", "00080060");   // Modality
        dm.add("tag", "00080068");   // Presentation Intent Type
        dm.add("tag", "00080070");   // Instrument Manufacturer Name
        dm.add("tag", "00080080");   // Institution
        dm.add("tag", "0008103E");   // Series description
        dm.add("tag", "00081090");   // Instrument Model
        dm.add("tag", "00181400");   // Acquisition Device Processing Description
        dm.add("tag", "00185101");   // View position
        dm.add("tag", "00200062");   // Image Laterality
        XmlDoc.Element r = executor().execute("dicom.metadata.populate", dm.root());
        w.addAll(r.elements());
}
   

    @Override
    public String name() {
        return SERVICE_NAME;
    }

}
