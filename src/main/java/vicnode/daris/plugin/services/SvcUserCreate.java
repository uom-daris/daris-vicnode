package vicnode.daris.plugin.services;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.atomic.AtomicOperation;
import arc.mf.plugin.atomic.AtomicTransaction;
import arc.mf.plugin.dtype.EmailAddressType;
import arc.mf.plugin.dtype.StringType;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcUserCreate extends PluginService {
    public static final String SERVICE_NAME = "vicnode.daris.user.create";
    public static final String SERVICE_DESCRIPTION = "Creates a user in the given local domain, and grant the user daris model user roles.";

    public static final String MODEL_USER_ROLE = "daris:pssd.model.user";
    public static final String SUBJECT_CREATOR_ROLE = "daris:pssd.subject.create";
    public static final String DOMAIN_MODEL_USER_ROLE = "vicnode.daris:pssd.model.user";

    private Interface _defn;

    public SvcUserCreate() {
        _defn = new Interface();
        _defn.add(new Interface.Element("domain", StringType.DEFAULT,
                "The authentication domain of the user", 1, 1));
        _defn.add(new Interface.Element("user", StringType.DEFAULT,
                "The user.", 1, 1));
        _defn.add(new Interface.Element("password", StringType.DEFAULT,
                "The user's password.", 1, 1));
        _defn.add(new Interface.Element("name", StringType.DEFAULT,
                "The user's full name.", 1, 1));
        _defn.add(new Interface.Element("email", EmailAddressType.DEFAULT,
                "The user's email.", 1, 1));

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
    public void execute(final Element args, Inputs inputs, Outputs outputs,
            XmlWriter w) throws Throwable {
        new AtomicTransaction(new AtomicOperation() {

            @Override
            public boolean execute(ServiceExecutor executor) throws Throwable {
                String domain = args.value("domain");
                String user = args.value("user");
                /*
                 * create the user
                 */
                executor().execute("user.create", args);
                /*
                 * grant roles
                 */
                XmlDocMaker dm = new XmlDocMaker("args");
                dm.add("type", "user");
                dm.add("name", domain + ":" + user);
                dm.add("role", new String[] { "type", "role" }, MODEL_USER_ROLE);
                dm.add("role", new String[] { "type", "role" },
                        DOMAIN_MODEL_USER_ROLE);
                dm.add("role", new String[] { "type", "role" },
                        SUBJECT_CREATOR_ROLE);
                executor.execute("actor.grant", dm.root());
                return false;
            }
        }).execute(executor());
    }

    @Override
    public String name() {
        return SERVICE_NAME;
    }
}
