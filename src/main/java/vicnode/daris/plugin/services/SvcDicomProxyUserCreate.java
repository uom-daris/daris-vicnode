package vicnode.daris.plugin.services;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.StringType;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcDicomProxyUserCreate extends PluginService {

    public static final String SERVICE_NAME = "vicnode.daris.dicom.proxy.user.create";
    public static final String SERVICE_DESCRIPTION = "Creates a DICOM proxy user in DICOM authentication domain. The user name should be the calling/sending AE's title.";

    public static final String DEFAULT_AUTH_DOMAIN = "dicom";
    public static final String DICOM_INGEST_ROLE = "daris:pssd.dicom-ingest";
    public static final String DOMAIN_DICOM_INGEST_ROLE = "vicnode.daris:pssd.dicom-ingest";

    private Interface _defn;

    public SvcDicomProxyUserCreate() {
        _defn = new Interface();
        _defn.add(new Interface.Element(
                "domain",
                StringType.DEFAULT,
                "The authentication domain for DICOM proxy users. Defaults to 'dicom'.",
                0, 1));
        _defn.add(new Interface.Element(
                "user",
                StringType.DEFAULT,
                "The DICOM proxy user's name. It should be the same as the sending/calling AE's title.",
                1, 1));
        _defn.add(new Interface.Element("note", StringType.DEFAULT,
                "A optional note for the user.", 0, 1));
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
        return SERVICE_DESCRIPTION;
    }

    @Override
    public void execute(Element args, Inputs inputs, Outputs outputs,
            XmlWriter w) throws Throwable {
        String domain = args.stringValue("domain", DEFAULT_AUTH_DOMAIN);
        String user = args.value("user");
        String note = args.value("note");
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("domain", domain);
        dm.add("user", user);
        dm.add("name", user);
        dm.add("add-role", DICOM_INGEST_ROLE);
        dm.add("add-role", DOMAIN_DICOM_INGEST_ROLE);
        if (note != null) {
            dm.push("meta");
            dm.push("mf-note");
            dm.add("note", note);
            dm.pop();
            dm.pop();
        }
        executor().execute("user.create", dm.root());
    }

    @Override
    public String name() {
        return SERVICE_NAME;
    }

}
