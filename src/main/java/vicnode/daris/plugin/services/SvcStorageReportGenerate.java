package vicnode.daris.plugin.services;

import java.io.ByteArrayInputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.StringType;
import arc.xml.XmlDoc;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcStorageReportGenerate extends PluginService {

    public static final String SERVICE_NAME = "vicnode.daris.storage.report.generate";

    // public static final String DOC_TYPE = "vicnode.daris:vicnode-collection";

    public static final String DEFAULT_NAMESPACE = "daris/VicNode DaRIS Storage Usage Reports";

    public static final String ASSET_NAME_PREFIX = "VicNode DaRIS Storage Usage Report - ";

    private Interface _defn;

    public SvcStorageReportGenerate() {
        _defn = new Interface();
        _defn.add(new Interface.Element("namespace", StringType.DEFAULT,
                "If specified, the generated report will be saved as an asset in the namespace. Defaults to '"
                        + DEFAULT_NAMESPACE + "'. Ignored if service output is specified.",
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
        Output output = (outputs != null && outputs.size() > 0) ? outputs.output(0) : null;
        String namespace = output != null ? null : args.stringValue("namespace", DEFAULT_NAMESPACE);
        String report = generateCSVReport(executor());

        if (namespace != null) {
            String assetId = createCSVReportAsset(executor(), report, namespace);
            String assetPath = executor().execute("asset.get", "<args><id>" + assetId + "</id></args>", null, null)
                    .value("asset/path");
            w.add("id", new String[] { "path", assetPath }, assetId);
        } else if (output != null) {
            byte[] reportBytes = report.getBytes();
            ByteArrayInputStream is = new ByteArrayInputStream(reportBytes);
            output.setData(is, reportBytes.length, "text/csv");
        }
    }

    private static String createCSVReportAsset(ServiceExecutor executor, String report, String namespace)
            throws Throwable {
        byte[] reportBytes = report.getBytes();
        ByteArrayInputStream is = new ByteArrayInputStream(reportBytes);

        /*
         * save as an asset
         */
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("namespace", new String[] { "create", "true" }, namespace);
        String assetName = ASSET_NAME_PREFIX + (new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())) + ".csv";
        dm.add("name", assetName);
        PluginService.Input input = new PluginService.Input(is, reportBytes.length, "text/csv", null);
        String assetId = executor.execute("asset.create", dm.root(), new PluginService.Inputs(input), null).value("id");
        return assetId;
    }

    private static String generateCSVReport(ServiceExecutor executor) throws Throwable {

        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("detailed", false);
        List<XmlDoc.Element> ces = executor.execute(SvcStorageCollectionDescribe.SERVICE_NAME, dm.root())
                .elements("collection");

        StringBuilder sb = new StringBuilder();
        sb.append("Collection Code,Collection Name,Allocated Storage(GB),Used Storage(GB),Description,\n");
        if (ces != null) {
            for (XmlDoc.Element ce : ces) {
                String code = ce.value("@code");
                String name = ce.value("name");
                double quota = ce.doubleValue("quota");
                double used = ce.doubleValue("used");
                String description = ce.value("description");
                sb.append('"').append(code).append("\",");
                if (name != null) {
                    sb.append('"').append(name).append("\",");
                } else {
                    sb.append(",");
                }
                sb.append(quota).append(",");
                sb.append(used).append(",");
                if (description != null) {
                    sb.append('"').append(description).append("\",");
                } else {
                    sb.append(",");
                }
                sb.append("\n");
            }
        }
        return sb.toString();
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

}
