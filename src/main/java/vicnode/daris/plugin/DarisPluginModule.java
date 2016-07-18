package vicnode.daris.plugin;

import java.util.Collection;
import java.util.Vector;

import arc.mf.plugin.ConfigurationResolver;
import arc.mf.plugin.PluginService;
import vicnode.daris.plugin.services.SvcDicomProxyUserCreate;
import vicnode.daris.plugin.services.SvcStorageReportSend;
import vicnode.daris.plugin.services.SvcSubjectMetaSet;
import vicnode.daris.plugin.services.SvcUserAAFCreate;
import vicnode.daris.plugin.services.SvcUserCreate;
import vicnode.daris.plugin.services.SvcUserGrant;

/**
 * The plugin module class.
 * 
 * All your services should be registered in initialize() method.
 * 
 * @author wliu5
 *
 */
public class DarisPluginModule implements arc.mf.plugin.PluginModule {

    private Collection<PluginService> _services = null;

    @Override
    public String description() {

        return Constants.ORG_NAME_SHORT.toUpperCase() + " Plugin Module.";
    }

    @Override
    public void initialize(ConfigurationResolver config) throws Throwable {

        _services = new Vector<PluginService>();
        _services.add(new SvcDicomProxyUserCreate());
        _services.add(new SvcStorageReportSend());
        _services.add(new SvcSubjectMetaSet());
        _services.add(new SvcUserAAFCreate());
        _services.add(new SvcUserCreate());
        _services.add(new SvcUserGrant());

    }

    @Override
    public void shutdown(ConfigurationResolver config) throws Throwable {

    }

    @Override
    public String vendor() {

        return Constants.ORG_NAME_FULL;
    }

    @Override
    public String version() {

        return "1.0";
    }

    @Override
    public Collection<PluginService> services() {

        return _services;
    }

}