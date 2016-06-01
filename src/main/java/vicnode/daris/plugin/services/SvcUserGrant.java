package vicnode.daris.plugin.services;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.StringType;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

/**
 * This service grants daris:pssd.model.user role and
 * vicnode.daris:pssd.model.user role to the specified user. It calls
 * actor.grant service.
 * 
 * @author wliu5
 *
 */
public class SvcUserGrant extends PluginService {

    public static final String SERVICE_NAME = "vicnode.daris.user.grant";
    public static final String SERVICE_DESCRIPTION = "Grants existing user with daris model user roles.";

    private Interface _defn;

    public SvcUserGrant() {
        _defn = new Interface();
        _defn.add(new Interface.Element("domain", StringType.DEFAULT,
                "The authentication domain of the user", 1, 1));
        _defn.add(new Interface.Element("user", StringType.DEFAULT, "The user.",
                1, 1));
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
        String domain = args.value("domain");
        String user = args.value("user");
        grantUser(executor(), domain, user);
    }

    static void grantUser(ServiceExecutor executor, String domain, String user)
            throws Throwable {
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("name", domain + ":" + user);
        dm.add("type", "user");
        dm.add("role", new String[] { "type", "role" },
                SvcUserCreate.MODEL_USER_ROLE);
        dm.add("role", new String[] { "type", "role" },
                SvcUserCreate.DOMAIN_MODEL_USER_ROLE);
        dm.add("role", new String[] { "type", "role" },
                SvcUserCreate.SUBJECT_CREATOR_ROLE);
        executor.execute("actor.grant", dm.root());
    }

    @Override
    public String name() {
        return SERVICE_NAME;
    }

}
