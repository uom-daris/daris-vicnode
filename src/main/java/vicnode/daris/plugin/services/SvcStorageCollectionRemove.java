package vicnode.daris.plugin.services;

import java.util.List;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.StringType;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcStorageCollectionRemove extends PluginService {

    public static final String SERVICE_NAME = "vicnode.daris.storage.collection.remove";

    private Interface _defn;

    public SvcStorageCollectionRemove() {
        _defn = new Interface();
        _defn.add(new Interface.Element("code", StringType.DEFAULT, "Unique VicNode identity code.", 1, 1));
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
        return "Remove the VicNode collection from the asset metadata.";
    }

    @Override
    public void execute(Element args, Inputs arg1, Outputs arg2, XmlWriter arg3) throws Throwable {
        String code = args.value("code");

        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("where", "xpath(" + SvcStorageCollectionAdd.DOC_TYPE + "/collection[@code='" + code + "']) has value");
        dm.add("size", 1);
        dm.add("action", "get-meta");
        XmlDoc.Element ae = executor().execute("asset.query", dm.root()).element("asset");
        if (ae == null) {
            // asset not exist or collection element not exist
            return;
        }

        XmlDoc.Element de = ae.element("meta/" + SvcStorageCollectionAdd.DOC_TYPE);

        dm = new XmlDocMaker("args");
        dm.add("id", ae.value("@id"));
        dm.push("meta", new String[] { "action", "replace" });
        dm.push(SvcStorageCollectionAdd.DOC_TYPE);
        List<XmlDoc.Element> ces = de.elements("collection");
        if (ces != null) {
            for (XmlDoc.Element ce : ces) {
                if (!code.equals(ce.value("@code"))) {
                    dm.add(ce, true);
                }
            }
        }
        dm.pop();
        dm.pop();
        executor().execute("asset.set", dm.root());
    }

    @Override
    public String name() {
        return SERVICE_NAME;
    }

}
