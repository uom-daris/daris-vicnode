package vicnode.daris.plugin.services;

import java.util.List;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.DoubleType;
import arc.mf.plugin.dtype.EmailAddressType;
import arc.mf.plugin.dtype.StringType;
import arc.mf.plugin.dtype.XmlDocType;
import arc.xml.XmlDoc;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcStorageCollectionAdd extends PluginService {

    public static final String SERVICE_NAME = "vicnode.daris.storage.collection.add";

    public static final String ASSET_NAME = "daris-vicnode-storage-collections";

    public static final String DOC_TYPE = "vicnode.daris:vicnode-collections";

    private Interface _defn;

    public SvcStorageCollectionAdd() {
        _defn = new Interface();
        Interface.Element collection = new Interface.Element("collection", XmlDocType.DEFAULT,
                "The VicNode collection (storage allcation).", 1, 1);
        collection.add(new Interface.Attribute("code", StringType.DEFAULT, "The unique VicNode allocation code.", 1));
        collection.add(new Interface.Element("name", StringType.DEFAULT, "Name of the VicNode allocation.", 0, 1));
        collection.add(new Interface.Element("quota", DoubleType.POSITIVE, "Storage quota of the collection. Unit: GB.",
                1, 1));
        Interface.Element contact = new Interface.Element("contact", XmlDocType.DEFAULT, "Contact.", 0, 1);
        contact.add(new Interface.Element("name", StringType.DEFAULT, "Name of the contact person.", 1, 1));
        contact.add(new Interface.Element("email", EmailAddressType.DEFAULT, "Email of the contact person.", 0, 1));
        collection.add(contact);
        Interface.Element attribute = new Interface.Element("attribute", StringType.DEFAULT, "Arbitrary attribute.", 0,
                255);
        attribute.add(new Interface.Attribute("name", StringType.DEFAULT, "Atrribute name.", 1));
        collection.add(attribute);
        _defn.add(collection);
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
        return "Add new VicNode storage collection information.";
    }

    @Override
    public void execute(Element args, Inputs arg1, Outputs arg2, XmlWriter arg3) throws Throwable {
        String namespace = executor().execute("daris.namespace.default.get").value("namespace");
        if (namespace == null) {
            throw new Exception("Failed to get the default daris namespace using daris.namespace.default.get service.");
        }
        if (!namespaceExists(executor(), namespace)) {
            throw new Exception("Default namespace: '" + namespace + "' does not exist.");
        }
        XmlDoc.Element collection = args.element("collection");
        String code = collection.value("@code");
        String assetPath = PathUtils.join(namespace, ASSET_NAME);
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("id", "path=" + assetPath);
        dm.push("meta", new String[] { "action", "replace" });
        dm.push(DOC_TYPE);
        if (assetExists(executor(), "path=" + assetPath)) {
            XmlDoc.Element de = getAssetMeta(executor(), "path=" + assetPath).element("meta/" + DOC_TYPE);
            if (de != null) {
                List<XmlDoc.Element> ces = de.elements("collection");
                if (ces != null) {
                    for (XmlDoc.Element ce : ces) {
                        if (!code.equals(ce.value("@code"))) {
                            dm.add(ce, true);
                        }
                    }
                }
            }
        }
        dm.add(collection, true);
        dm.pop();
        dm.pop();
        dm.add("create", true);
        executor().execute("asset.set", dm.root());
    }

    static boolean namespaceExists(ServiceExecutor executor, String namespace) throws Throwable {
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("namespace", namespace);
        return executor.execute("asset.namespace.exists", dm.root()).booleanValue("exists");
    }

    static boolean assetExists(ServiceExecutor executor, String id) throws Throwable {
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("id", id);
        return executor.execute("asset.exists", dm.root()).booleanValue("exists");
    }

    static XmlDoc.Element getAssetMeta(ServiceExecutor executor, String id) throws Throwable {
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("id", id);
        return executor.execute("asset.get", dm.root()).element("asset");
    }

    @Override
    public String name() {
        return SERVICE_NAME;
    }

}
