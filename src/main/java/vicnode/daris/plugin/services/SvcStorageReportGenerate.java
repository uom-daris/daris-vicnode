package vicnode.daris.plugin.services;

import java.io.ByteArrayInputStream;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.StringType;
import arc.xml.XmlDoc;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcStorageReportGenerate extends PluginService {

    public static final String SERVICE_NAME = "vicnode.daris.storage.report.generate";

    public static final String DOC_TYPE = "vicnode.daris:vicnode-collection";

    public static final String DEFAULT_NAMESPACE = "daris/VicNode DaRIS Storage Usage Reports";

    public static final String ASSET_NAME_PREFIX = "VicNode DaRIS Storage Usage Report - ";

    private Interface _defn;

    public SvcStorageReportGenerate() {
        _defn = new Interface();
        _defn.add(new Interface.Element("namespace", StringType.DEFAULT,
                "If specified, the generated report will be saved as an asset in the namespace. Defaults to '"
                        + DEFAULT_NAMESPACE + "' only if not service output is specified.",
                0, 1));
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
        return "Generate";
    }

    @Override
    public void execute(Element args, Inputs inputs, Outputs outputs, XmlWriter w) throws Throwable {
        boolean hasOutput = outputs != null && outputs.size() > 0;
        String namespace = hasOutput ? args.value("namespace") : args.stringValue("namespace", DEFAULT_NAMESPACE);
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("where", "model='om.pssd.project' and " + DOC_TYPE + " has value");
        dm.add("size", "infinity");
        dm.add("action", "get-meta");

        List<XmlDoc.Element> aes = executor().execute("asset.query", dm.root()).elements("asset");
        if (aes != null) {
            Map<String, VNCollection> vncs = new TreeMap<String, VNCollection>();
            for (XmlDoc.Element ae : aes) {
                String projectCid = ae.value("cid");
                XmlDoc.Element vnce = ae.element("meta/" + DOC_TYPE);
                String code = vnce.value("code");
                String name = vnce.value("name");
                String unit = vnce.value("quota/@unit");
                double allocated = vnce.doubleValue("quota");
                double projectAllocatedGigabytes = "gb".equals(unit) ? allocated
                        : ("tb".equals(unit) ? allocated * 1000.0 : allocated * 1000000.0);
                double projectUsedGigabytes = getProjectUsedGigabytes(executor(), projectCid);
                // TODO:
                //
                // Currently, each VicNode allocation may be used by multiple
                // daris projects. We then split the allocated storage for each
                // daris project inside daris. That is:

                // VN_Collection_Total_Allocated = DaRIS_Project1_Allocated +
                // DaRIS_Project2_Allocated + ...

                // When the manual entry of the sent report from
                // vicnode.daris.storate.report.send service is stopped. We may
                // adjust this:

                VNCollection vnc = vncs.get(code);
                if (vnc == null) {
                    vnc = new VNCollection(code, name, projectAllocatedGigabytes, projectUsedGigabytes, null);
                    vncs.put(code, vnc);
                } else {
                    vnc.allocatedGigabytes += projectAllocatedGigabytes;
                    vnc.usedGigabytes += projectUsedGigabytes;
                    if (vnc.name == null) {
                        vnc.name = name;
                    }
                }
            }
            if (!vncs.isEmpty()) {
                String csvText = generateCSVText(vncs);
                byte[] csvBytes = csvText.getBytes();
                ByteArrayInputStream is = new ByteArrayInputStream(csvBytes);

                /*
                 * save as an asset
                 */
                if (namespace != null) {
                    dm = new XmlDocMaker("args");
                    dm.add("namespace", new String[] { "create", "true" }, namespace);
                    String assetName = ASSET_NAME_PREFIX + (new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()))
                            + ".csv";
                    dm.add("name", assetName);
                    PluginService.Input input = new PluginService.Input(is, csvBytes.length, "text/csv", null);
                    String assetId = executor()
                            .execute("asset.create", dm.root(), new PluginService.Inputs(input), null).value("id");
                    w.add("id", new String[] { "path", namespace + "/" + assetName }, assetId);
                }

                /*
                 * output stream
                 */
                if (outputs != null && outputs.size() > 0) {
                    outputs.output(0).setData(is, csvBytes.length, "text/csv");
                }
            } else {
                if (outputs != null && outputs.size() > 0) {
                    // expect output stream but no results
                    throw new Exception("No DaRIS project (with " + DOC_TYPE + " metadata) found.");
                }
            }
        }
    }

    @Override
    public int minNumberOfOutputs() {
        return 0;
    }

    @Override
    public int maxNumberOfOutputs() {
        return 1;
    }

    @Override
    public String name() {
        return SERVICE_NAME;
    }

    private static double getProjectUsedGigabytes(ServiceExecutor executor, String projectCid) throws Throwable {
        return executor
                .execute("daris.project.disk-usage.get", "<args><cid>" + projectCid + "</cid></args>", null, null)
                .doubleValue("project/disk-usage/@gb");
    }

    private static String generateCSVText(Map<String, VNCollection> collections) {
        Collection<VNCollection> values = collections.values();
        StringBuilder sb = new StringBuilder();
        sb.append("Collection Code,Collection Name,Allocated Storage(GB),Used Storage(GB),Description,\n");
        for (VNCollection vnc : values) {
            sb.append('"').append(vnc.code).append("\",");
            if (vnc.name != null) {
                sb.append('"').append(vnc.name).append("\",");
            } else {
                sb.append(",");
            }
            sb.append(vnc.allocatedGigabytes).append(",");
            sb.append(vnc.usedGigabytes).append(",");
            if (vnc.description != null) {
                sb.append('"').append(vnc.description).append("\",");
            } else {
                sb.append(",");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    private class VNCollection {

        public String code;
        public String name;
        public double allocatedGigabytes;
        public double usedGigabytes;
        public String description;

        public VNCollection(String code, String name, double allocatedGigabytes, double usedGigabytes,
                String description) {
            this.code = code;
            this.name = name;
            this.allocatedGigabytes = allocatedGigabytes;
            this.usedGigabytes = usedGigabytes;
            this.description = description;
        }
    }

}
