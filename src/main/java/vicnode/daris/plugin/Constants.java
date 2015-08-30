package vicnode.daris.plugin;

/**
 * Constants.
 * 
 * Initialize the constant values of doc namespace, role namespace, dictionary
 * namespace and service prefix.
 * 
 *
 */

public class Constants {

    //@formatter:off
	public static final String ORG_NAME_SHORT       = "vicnode";
	public static final String ORG_NAME_FULL        = "VicNode";
	public static final String DOC_NAMESPACE        = "vicnode.daris";
	public static final String DICTIONARY_NAMESPACE = "vicnode.daris";
	public static final String ROLE_NAMESPACE       = "vicnode.daris";
	public static final String SERVICE_PREFIX       = "vicnode.daris";
	//@formatter:on

    public static String prependDocNamespace(String doc) {
        return DOC_NAMESPACE + ":" + doc;
    }

    public static String prependDictionaryNamespace(String dictionary) {
        return DICTIONARY_NAMESPACE + ":" + dictionary;
    }

    public static String prependRoleNamespace(String role) {
        return ROLE_NAMESPACE + ":" + role;
    }

    private Constants() {

    }

}
