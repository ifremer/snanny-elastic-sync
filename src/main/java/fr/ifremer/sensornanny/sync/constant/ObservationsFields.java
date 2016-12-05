package fr.ifremer.sensornanny.sync.constant;

/**
 * Created by asi on 13/10/16.
 */
public class ObservationsFields {


    private static final String DEPLOYMENTID = "deploymentid";
    private static final String NAME = "name";
    private static final String UUID = "uuid";
    private static final String DESCRIPTION = "description";
    private static final String TERMS = "terms";
    private static final String RESULTFILE = "resultfile";

    public static final String SNANNY = "snanny";

    public static final String SNANNY_OBSERVATIONS = "snanny-observations";
    public static final String SNANNY_SYSTEMS = "snanny-systems";

    public static final String SNANNY_NAME = SNANNY + "-" + NAME;

    public static final String SNANNY_UUID = SNANNY + "-" + UUID;

    public static final String SNANNY_DEPLOYMENTID = SNANNY + "-" + DEPLOYMENTID;

    public static final String SNANNY_RESULTTIMESTAMP = SNANNY + "-resulttimestamp";

    public static final String SNANNY_RESULTFILE = SNANNY + "-" + RESULTFILE;

    public static final String SNANNY_UPDATETIMESTAMP = SNANNY + "-updatetimestamp";

    public static final String SNANNY_ANCESTORS = SNANNY + "-ancestors";

    public static final String SNANNY_ANCESTOR = SNANNY + "-ancestor";

    public static final String SNANNY_ANCESTOR_NAME = SNANNY_ANCESTOR + "-" + NAME;

    public static final String SNANNY_ANCESTOR_UUID = SNANNY_ANCESTOR + "-" + UUID;

    public static final String SNANNY_ANCESTOR_DEPLOYMENTID = SNANNY_ANCESTOR + "-" + DEPLOYMENTID;

    public static final String SNANNY_ANCESTOR_DESCRIPTION = SNANNY_ANCESTOR + "-" + DESCRIPTION;

    public static final String SNANNY_ANCESTOR_TERMS = SNANNY_ANCESTOR + "-" + TERMS;

    public static final String SNANNY_ACCESS = SNANNY + "-access";

    public static final String SNANNY_ACCESS_AUTH = SNANNY_ACCESS + "-auth";

    public static final String SNANNY_ACCESS_TYPE = SNANNY_ACCESS + "-type";

    public static final String SNANNY_AUTHOR = SNANNY + "-author";

    public static final String SNANNY_DEPTH = SNANNY + "-depth";

    public static final String SNANNY_COORDINATES = SNANNY + "-coordinates";

    public static final String SNANNY_SYSTEM_UUID = SNANNY_SYSTEMS + "-" + UUID;
    public static final String SNANNY_SYSTEM_NAME = SNANNY_SYSTEMS + "-" + NAME;
    public static final String SNANNY_SYSTEM_DESCRIPTION = SNANNY_SYSTEMS + "-" + DESCRIPTION;
    public static final String SNANNY_SYSTEM_FILEID = SNANNY_SYSTEMS + "-fileid";
    public static final String SNANNY_SYSTEM_HASDATA = SNANNY_SYSTEMS + "-hasdata";

}
