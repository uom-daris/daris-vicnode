package vicnode.daris.plugin.services;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.BooleanType;
import arc.mf.plugin.dtype.StringType;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcUserAAFCreate extends PluginService {

    public static final String SERVICE_NAME = "vicnode.daris.user.aaf.create";

    public static final String DOMAIN = "aaf";

    public static final String NOTIFICATION_FROM = "daris-support@lists.unimelb.edu.au";

    public static final String NOTIFICATION_CC = "daris-support@lists.unimelb.edu.au";

    public static final String NOTIFICATION_SUBJECT = "VicNode Mediaflux/DaRIS account created";

    public static final String NOTIFICATION_TYPE = "text/html";

    private Interface _defn;

    public SvcUserAAFCreate() {
        _defn = new Interface();
        _defn.add(new Interface.Element("mail", StringType.DEFAULT,
                "The email address can be used for AAF authentication.", 1, 1));
        _defn.add(new Interface.Element("name", StringType.DEFAULT,
                "The full  name of the user/person.", 0, 1));
        _defn.add(new Interface.Element("notify", BooleanType.DEFAULT,
                "Send a notification email to the user. Defaults to false.", 0,
                1));
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
        return "Creates an AAF account and grants DaRIS roles. It wraps authentication.user.create service.";
    }

    @Override
    public void execute(Element args, Inputs inputs, Outputs outputs,
            XmlWriter w) throws Throwable {
        String mail = args.value("mail");
        String name = args.value("name");
        boolean notify = args.booleanValue("notify", false);

        /*
         * create user
         */
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("domain", DOMAIN);
        dm.add("user", mail);
        dm.add("email", mail);
        if (name != null) {
            dm.add("name", name);
        }
        executor().execute("authentication.user.create", dm.root());

        /*
         * grant daris:pssd-model-user role
         */
        SvcUserGrant.grantUser(executor(), DOMAIN, mail);

        /*
         * notify the user
         */
        if (notify) {
            notify(executor(), mail, name);
        }
    }

    @Override
    public String name() {
        return SERVICE_NAME;
    }

    private static void notify(ServiceExecutor executor, String mail,
            String name) throws Throwable {
        StringBuilder sb = new StringBuilder();
        sb.append("Your DaRIS/Mediaflux AAF proxy account has been created. ");
        sb.append(
                "<br/>You can now access DaRIS web portal at: <a href=\"https://mediaflux.vicnode.org.au/daris/?dti=no\">https://mediaflux.vicnode.org.au:443/daris/?dti=no</a> ");
        sb.append(" and login as follows:<br/>");
        sb.append("<ul><li><b>Domain:</b> " + DOMAIN + "</li>");
        sb.append("<li><b>Provider:</b> [Select your institution]</li>");
        sb.append("<li><b>User:</b> [Your institutional login]</li>");
        sb.append(
                "<li><b>Password:</b> [Your institutional password]</li></ul>");
        sb.append(
                "<br/><br/>If you are having trouble logging in, please contact <a href=\"mailto:"
                        + NOTIFICATION_FROM + "\">DaRIS support</a>.");

        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("async", true);
        dm.add("from", NOTIFICATION_FROM);
        dm.add("to", mail);
        dm.add("cc", NOTIFICATION_CC);
        dm.add("subject", NOTIFICATION_SUBJECT);
        dm.add("body", new String[] { "type", NOTIFICATION_TYPE },
                sb.toString());
        executor.execute("mail.send", dm.root());
    }

}
