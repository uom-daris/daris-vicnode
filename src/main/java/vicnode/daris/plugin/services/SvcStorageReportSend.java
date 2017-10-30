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
        _defn.add(new Interface.Element("format", new EnumType(new String[] { "csv", "xml" }),
                "Report file format. Defaults to csv.", 0, 1));
        _defn.add(new Interface.Element("to", EmailAddressType.DEFAULT, "The receipient email address.", 1, 256));
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
    public void execute(Element args, Inputs inputs, Outputs outputs, XmlWriter w) throws Throwable {
        String format = args.stringValue("format", "csv");
        Collection<String> receipients = args.values("to");

        String report = "csv".equalsIgnoreCase(format) ? generateCSVReport(executor()) : generateXMLReport(executor());
        String mimeType = "csv".equalsIgnoreCase(format) ? "text/csv" : "test/xml";

        sendEmail(executor(), receipients, report, mimeType);

    }

    private static String generateCSVReport(ServiceExecutor executor) throws Throwable {
        StringBuilder sb = new StringBuilder();
        // header
        sb.append(
                "Extraction Date, Collection Code, Collection Name, Managing Software, Mediaflux/DaRIS ID, Allocated Storage (GB), Used Storage (GB),\n");
        String date = new SimpleDateFormat("d-MMM-yyyy").format(new Date());
        List<XmlDoc.Element> ces = getStorageCollections(executor);
        if (ces != null) {
            for (XmlDoc.Element ce : ces) {
                String code = ce.value("@code");
                double quota = ce.doubleValue("quota");
                List<XmlDoc.Element> pes = ce.elements("project");
                int nbProjects = pes == null ? 0 : pes.size();
                if (nbProjects > 0) {
                    double pquota = quota == 0 ? 0 : (quota / nbProjects);
                    for (XmlDoc.Element pe : pes) {
                        String cid = pe.value("@cid");
                        double pused = pe.doubleValue("used");
                        String pname = pe.value("name");
                        sb.append('"').append(date).append('"').append(",");
                        sb.append('"').append("VicNode:").append(code).append(":DaRIS-Project-").append(cid).append('"')
                                .append(",");
                        sb.append('"').append(pname).append('"').append(",");
                        sb.append('"').append("DaRIS/Mediaflux").append('"').append(",");
                        sb.append('"').append(cid).append('"').append(",");
                        sb.append('"').append(String.format("%.3f GB", pquota)).append('"').append(",");
                        sb.append('"').append(String.format("%.3f GB", pused)).append('"').append(",");
                        sb.append("\n");
                    }
                }
            }
        }
        return sb.toString();
    }

    private static String generateXMLReport(ServiceExecutor executor) throws Throwable {
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("detailed", true);
        XmlDoc.Element re = executor.execute(SvcStorageCollectionDescribe.SERVICE_NAME, dm.root());
        return re.toString();
    }

    private static List<XmlDoc.Element> getStorageCollections(ServiceExecutor executor) throws Throwable {
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("detailed", true);
        return executor.execute(SvcStorageCollectionDescribe.SERVICE_NAME, dm.root()).elements("collection");
    }

    private static void sendEmail(ServiceExecutor executor, Collection<String> receipients, String report,
            String mimeType) throws Throwable {
        String ext = "csv";
        if ("text/xml".equalsIgnoreCase(mimeType)) {
            ext = "xml";
        }
        XmlDocMaker dm = new XmlDocMaker("args");

        dm.add("subject", SUBJECT_PREFIX + " [" + new SimpleDateFormat(DateTime.DATE_FORMAT).format(new Date()) + "]");

        dm.add("body", "Please see the attached DaRIS project storage usage report.");

        dm.push("attachment");
        dm.add("name", ATTACHMENT_NAME_PREFIX + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + "." + ext);
        dm.add("type", mimeType);
        dm.pop();

        for (String receipient : receipients) {
            dm.add("to", receipient);
        }

        dm.add("async", false);
        PluginService.Input input = new PluginService.StringInput(report, mimeType);
        executor.execute("mail.send", dm.root(), new PluginService.Inputs(input), null);
    }

    @Override
    public String name() {
        return SERVICE_NAME;
    }

}
