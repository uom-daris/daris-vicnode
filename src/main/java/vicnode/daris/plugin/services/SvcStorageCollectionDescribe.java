package vicnode.daris.plugin.services;

import java.util.ArrayList;
import java.util.List;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.BooleanType;
import arc.mf.plugin.dtype.StringType;
import arc.xml.XmlDoc;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcStorageCollectionDescribe extends PluginService {

    public static final String SERVICE_NAME = "vicnode.daris.storage.collection.describe";
    private Interface _defn;

    public SvcStorageCollectionDescribe() {
        _defn = new Interface();
        _defn.add(new Interface.Element("code", StringType.DEFAULT, "Unique VicNode identity code.", 0, 1));
        _defn.add(new Interface.Element("prefix", StringType.DEFAULT,
                "Prefix(filter) of the unique VicNode identity code.", 0, 1));
        _defn.add(new Interface.Element("suffix", StringType.DEFAULT,
                "Suffix(filter) of the unique VicNode identity code.", 0, 1));
        _defn.add(new Interface.Element("detailed", BooleanType.DEFAULT,
                "Show detailed usage of associated projects. Defaults to false.", 0, 1));
    }

    @Override
    public Access access() {
        return ACCESS_ACCESS;
    }

    @Override
    public Interface definition() {
        return _defn;
    }

    @Override
    public String description() {
        return "Describe DaRIS VicNode collections.";
    }

    @Override
    public void execute(Element args, Inputs arg1, Outputs arg2, XmlWriter w) throws Throwable {
        String code = args.value("code");
        String prefix = args.value("prefix");
        String suffix = args.value("suffix");
        boolean detailed = args.booleanValue("detailed", false);

        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("where", SvcStorageCollectionAdd.DOC_TYPE + " has value");
        dm.add("size", 1);
        dm.add("action", "get-meta");
        XmlDoc.Element de = executor().execute("asset.query", dm.root())
                .element("asset/meta/" + SvcStorageCollectionAdd.DOC_TYPE);

        int nbCollections = 0;
        int nbProjects = 0;
        double totalQuotaGB = 0.0;
        double totalUsedGB = 0.0;
        if (de != null) {
            List<XmlDoc.Element> ces = de.elements("collection");
            if (ces != null) {
                for (XmlDoc.Element ce : ces) {
                    String cc = ce.value("@code");
                    if ((code != null && code.equals(cc)) || (prefix != null && cc.startsWith(prefix))
                            || (suffix != null && cc.endsWith(suffix))
                            || (code == null || prefix == null || suffix == null)) {
                        double quota = ce.doubleValue("quota");
                        double usedGB = 0.0;
                        nbCollections++;
                        totalQuotaGB += quota;
                        w.push("collection", new String[] { "code", cc });
                        w.add(ce, false);
                        List<ProjectUsage> pus = getStorageUsages(executor(), cc);
                        if (pus != null) {
                            for (ProjectUsage pu : pus) {
                                if (detailed) {
                                    w.push("project", new String[] { "cid", pu.cid });
                                    w.add("name", pu.name);
                                    w.add("used", new String[] { "unit", "gb" }, pu.usageGB);
                                    w.pop();
                                }
                                nbProjects++;
                                usedGB += pu.usageGB;
                            }
                        }
                        w.add("used", new String[] { "unit", "gb" }, usedGB);
                        totalUsedGB += usedGB;
                        w.pop();
                    }
                }
            }
        }
        w.add("total-quota", new String[] { "unit", "gb" }, totalQuotaGB);
        w.add("total-used", new String[] { "unit", "gb" }, totalUsedGB);
        w.add("number-of-collections", nbCollections);
        w.add("number-of-projects", nbProjects);

    }

    private static List<ProjectUsage> getStorageUsages(ServiceExecutor executor, String code) throws Throwable {
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("where", "model='om.pssd.project' and xpath(" + SvcProjectStorageCollectionIdentitySet.DOC_TYPE
                + "/code)='" + code + "'");
        dm.add("action", "get-value");
        dm.add("xpath", new String[] { "ename", "cid" }, "cid");
        dm.add("xpath", new String[] { "ename", "name" }, "daris:pssd-object/name");
        dm.add("size", "infinity");
        List<XmlDoc.Element> aes = executor.execute("asset.query", dm.root()).elements("asset");
        if (aes != null && !aes.isEmpty()) {
            List<ProjectUsage> pus = new ArrayList<ProjectUsage>();
            for (XmlDoc.Element ae : aes) {
                ProjectUsage pu = new ProjectUsage();
                pu.cid = ae.value("cid");
                pu.name = ae.value("name");
                pu.usageGB = getStorageUsage(pu.cid, executor);
                pus.add(pu);
            }
            return pus;
        }
        return null;
    }

    private static double getStorageUsage(String cid, ServiceExecutor executor) throws Throwable {
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("cid", cid);
        return executor.execute("daris.project.disk-usage.get", dm.root()).doubleValue("project/disk-usage/@gb");
    }

    private static class ProjectUsage {
        String cid;
        String name;
        double usageGB;
    }

    @Override
    public String name() {
        return SERVICE_NAME;
    }

}
