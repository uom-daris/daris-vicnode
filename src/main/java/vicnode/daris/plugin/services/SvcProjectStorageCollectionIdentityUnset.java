package vicnode.daris.plugin.services;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.xml.XmlDoc;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcProjectStorageCollectionIdentityUnset extends PluginService {

    public static final String SERVICE_NAME = "vicnode.daris.project.storage.collection.identity.unset";

    private Interface _defn;

    public SvcProjectStorageCollectionIdentityUnset() {
        _defn = new Interface();
        _defn.add(new Interface.Element("cid", CiteableIdType.DEFAULT, "Citeable id of the project.", 1, 1));
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
        return "Unset the associated VicNode collection code.";
    }

    @Override
    public void execute(Element args, Inputs arg1, Outputs arg2, XmlWriter arg3) throws Throwable {
        String cid = args.value("cid");
        XmlDoc.Element ae = getAssetMeta(executor(), cid);
        if (ae.elementExists("meta/" + SvcProjectStorageCollectionIdentitySet.DOC_TYPE)) {
            XmlDocMaker dm = new XmlDocMaker("args");
            dm.add("cid", cid);
            dm.push("meta", new String[] { "action", "remove" });
            String docId = ae.value("meta/" + SvcProjectStorageCollectionIdentitySet.DOC_TYPE + "/@id");
            dm.add(SvcProjectStorageCollectionIdentitySet.DOC_TYPE, new String[] { "id", docId });
            dm.pop();
            executor().execute("asset.set", dm.root());
        }
    }

    private static XmlDoc.Element getAssetMeta(ServiceExecutor executor, String cid) throws Throwable {
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("cid", cid);
        return executor.execute("asset.get", dm.root()).element("asset");
    }

    @Override
    public String name() {
        return SERVICE_NAME;
    }

}
