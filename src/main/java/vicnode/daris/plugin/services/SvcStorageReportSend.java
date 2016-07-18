package vicnode.daris.plugin.services;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.EmailAddressType;
import arc.mf.plugin.dtype.EnumType;
import arc.utils.DateTime;
import arc.xml.XmlDoc;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcStorageReportSend extends PluginService {

    public static final String SERVICE_NAME = "vicnode.daris.storage.report.send";

    public static final String DOC_TYPE = "vicnode.daris:vicnode-collection";

    public static final String SUBJECT_PREFIX = "VicNode DaRIS Storage Report";

    public static final String ATTACHMENT_NAME_PREFIX = "vicnode_daris_storage_report-";

    private Interface _defn;

    public SvcStorageReportSend() {
        _defn = new Interface();
        _defn.add(new Interface.Element("format",
                new EnumType(new String[] { "csv", "xml" }),
                "Report file format. Defaults to csv.", 0, 1));
        _defn.add(new Interface.Element("to", EmailAddressType.DEFAULT,
                "The receipient email address.", 1, 256));
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
        return "Send storage usage report to specified email addresses.";
    }

    @Override
    public void execute(Element args, Inputs inputs, Outputs outputs,
            XmlWriter w) throws Throwable {
        String format = args.stringValue("format", "csv");
        Collection<String> receipients = args.values("to");
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("size", "infinity"); // There should not be too many projects.
        dm.add("action", "get-meta");
        dm.add("where",
                "model='om.pssd.project' and " + DOC_TYPE + " has value");
        List<XmlDoc.Element> aes = executor().execute("asset.query", dm.root())
                .elements("asset");
        if (aes == null || aes.isEmpty()) {
            return; // throw exception?
        }

        String report;
        String mimeType;
        if ("csv".equalsIgnoreCase(format)) {
            report = createCsvReport(aes);
            mimeType = "text/csv";
        } else {
            report = createXmlReport(aes);
            mimeType = "text/xml";
        }
        sendEmail(receipients, report, mimeType);

    }

    private void sendEmail(Collection<String> receipients, String report,
            String mimeType) throws Throwable {
        XmlDocMaker dm = new XmlDocMaker("args");

        dm.add("subject", SUBJECT_PREFIX + " ["
                + new SimpleDateFormat(DateTime.DATE_FORMAT).format(new Date())
                + "]");

        dm.add("body",
                "Please see the attached DaRIS project storage usage report.");

        dm.push("attachment");
        dm.add("name", ATTACHMENT_NAME_PREFIX
                + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
        dm.add("type", mimeType);
        dm.pop();

        for (String receipient : receipients) {
            dm.add("to", receipient);
        }

        dm.add("async", false);
        PluginService.Input input = new PluginService.StringInput(report,
                mimeType);
        executor().execute("mail.send", dm.root(),
                new PluginService.Inputs(input), null);

    }

    private String createXmlReport(List<Element> aes) throws Throwable {
        XmlDocMaker dm = new XmlDocMaker("vicnode-collections");
        for (XmlDoc.Element ae : aes) {
            String cid = ae.value("cid");
            long du = getProjectDiskUsage(executor(), cid);

            dm.push("collection");
            dm.add("code", ae.value("meta/" + DOC_TYPE + "/code"));
            dm.add("name", ae.value("meta/daris:pssd-object/name"));
            dm.add("cid", cid);
            if (ae.elementExists("meta/" + DOC_TYPE + "/quota")) {
                double quota = ae.doubleValue("meta/" + DOC_TYPE + "/quota");
                String quotaUnit = ae
                        .value("meta/" + DOC_TYPE + "/quota/@unit");
                if ("pb".equalsIgnoreCase(quotaUnit)) {
                    quota = quota * 1000 * 1000;
                } else if ("tb".equalsIgnoreCase(quotaUnit)) {
                    quota = quota * 1000;
                }
                dm.add("allocated", new String[] { "unit", "gb" },
                        String.format("%.3f", quota));
            }
            dm.add("used", new String[] { "unit", "gb" },
                    String.format("%.3f", (double) du / 1000000000.0));
            dm.pop();
        }
        return dm.root().toString();
    }

    private String createCsvReport(List<Element> aes) throws Throwable {
        StringBuilder sb = new StringBuilder();
        // header
        sb.append(
                "Extraction Date, Collection Code, Collection Name, Managing Software, Mediaflux/DaRIS ID, Allocated Storage (GB), Used Storage (GB),\n");
        for (XmlDoc.Element ae : aes) {
            String cid = ae.value("cid");
            long du = getProjectDiskUsage(executor(), cid);

            /*
             * Extraction Date
             */
            sb.append('"').append(
                    new SimpleDateFormat("d-MMM-yyyy").format(new Date()))
                    .append('"').append(",");

            /*
             * Collection Code
             */
            sb.append('"')
                    .append(ae.stringValue("meta/" + DOC_TYPE + "/code", ""))
                    .append('"').append(",");

            /*
             * Collection Name
             */
            sb.append('"')
                    .append(ae.stringValue("meta/daris:pssd-object/name", "")
                            .replace('"', '\''))
                    .append('"').append(",");

            /*
             * Managing Software
             */
            sb.append('"').append("DaRIS/Mediaflux").append('"').append(",");

            /*
             * Mediaflux/DaRIS ID
             */
            sb.append('"').append(cid).append('"').append(",");

            /*
             * Allocated Storage
             */
            sb.append('"');
            if (ae.elementExists("meta/" + DOC_TYPE + "/quota")) {
                double quota = ae.doubleValue("meta/" + DOC_TYPE + "/quota");
                String quotaUnit = ae
                        .value("meta/" + DOC_TYPE + "/quota/@unit");
                if ("pb".equalsIgnoreCase(quotaUnit)) {
                    quota = quota * 1000 * 1000;
                } else if ("tb".equalsIgnoreCase(quotaUnit)) {
                    quota = quota * 1000;
                }
                sb.append(String.format("%.3f GB", quota));
            }
            sb.append('"').append(",");

            /*
             * Used Storage
             */
            sb.append('"')
                    .append(String.format("%.3f GB",
                            (double) du / 1000000000.0))
                    .append('"').append(",");

            sb.append("\n");

        }
        return sb.toString();
    }

    private static long getProjectDiskUsage(ServiceExecutor executor,
            String projectCid) throws Throwable {
        return executor.execute("daris.project.disk-usage.get",
                "<args><cid>" + projectCid + "</cid></args>", null, null)
                .longValue("project/disk-usage");
    }

    @Override
    public String name() {
        return SERVICE_NAME;
    }

}
