package vicnode.daris.plugin.services;

import nig.mf.Executor;
import nig.mf.plugin.util.PluginExecutor;
import vicnode.daris.plugin.VicnodeDomainMetaData;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.BooleanType;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.DateType;
import arc.mf.plugin.dtype.DoubleType;
import arc.mf.plugin.dtype.StringType;
import arc.mf.plugin.dtype.XmlDocType;
import arc.xml.XmlDoc.Element;
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
        dm.add("tag","00080008");
        dm.add("tag", "00080050");
        dm.add("tag", "00080060");
        dm.add("tag", "00080068");
        dm.add("tag", "00080070");
        dm.add("tag", "00080080");
        dm.add("tag", "0008103E");
        dm.add("tag", "00081090");
        dm.add("tag", "00181400");
        dm.add("tag", "00185101");
        dm.add("tag", "00200062");
        executor().execute("dicom.metadata.populate", dm.root());
}
   

    @Override
    public String name() {
        return SERVICE_NAME;
    }

}
