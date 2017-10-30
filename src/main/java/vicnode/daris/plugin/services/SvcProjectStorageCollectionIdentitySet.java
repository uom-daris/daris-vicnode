package vicnode.daris.plugin.services;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.StringType;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcProjectStorageCollectionIdentitySet extends PluginService {

    public static final String SERVICE_NAME = "vicnode.daris.project.storage.collection.identity.set";

    public static final String DOC_TYPE = "vicnode.daris:vicnode-collection-identity";

    private Interface _defn;

    public SvcProjectStorageCollectionIdentitySet() {
        _defn = new Interface();
        _defn.add(new Interface.Element("cid", CiteableIdType.DEFAULT, "Citeable id of the project.", 1, 1));
        _defn.add(new Interface.Element("code", StringType.DEFAULT, "The unique VicNode allocation code.", 1, 1));
    }

    @Override
    public Access access() {
        return ACCESS_ADMINISTER;
    }

    @Override
    public Interface definition() {
        return _defn;
    }

    @Override
    public String description() {
        return "Set the associated VicNode collection code.";
    }

    @Override
    public void execute(Element args, Inputs arg1, Outputs arg2, XmlWriter arg3) throws Throwable {
        String cid = args.value("cid");
        String code = args.value("code");

        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("cid", cid);
        dm.push("meta");
        dm.push(DOC_TYPE);
        dm.add("code", code);
        dm.pop();
        dm.pop();
        executor().execute("asset.set", dm.root());
    }

    @Override
    public String name() {
        return SERVICE_NAME;
    }

}
