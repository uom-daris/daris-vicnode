package vicnode.daris.plugin;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

import nig.iio.metadata.DomainMetaData;
import nig.mf.Executor;
import nig.util.DateUtil;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;

/**
 * Supply the 'vicnode' domain-specific project, subject and study meta-data to
 * the DICOM server or Bruker client. The framework is Data Model driven so that
 * only the meta-data that could be attached is attached
 * 
 * The only reason we have functions for each object type is for clarity and
 * because some objects are handled slightly differently to others
 * (add/merge/replace). But the in reality, the framework could hand in the
 * object type and the test on object type be done internally to this class. It
 * does not matter much which way you do it.
 * 
 * The superclass, DomainMetaData sits in commons. In this way, specific
 * packages like daris-vicnode can make use of the infrastructure but implement
 * their own fully self-contained domain-specific meta-data handler.
 * 
 * @author nebk
 *
 */
public class VicnodeDomainMetaData extends DomainMetaData {

    private static final String AMRIF_FACILITY = "aMRIF";
    private static final String RCH_FACILITY = "RCH";
    private static final String DATE_FORMAT = "dd-MMM-yyyy";

    // Constructor
    public VicnodeDomainMetaData() {
        //
    }

    /**
     * 
     * @param executor
     * @param metaType
     *            SHould hold children elements "dicom" and/or "bruker" (but
     *            their children are irrelevant). This gives the context for
     *            which document types we are interested in.
     * @param id
     * @param objectType
     *            "project", "subject", "study"
     * @param currentMeta
     *            The contents of xpath("asset/meta") after retrieval by
     *            asset.get
     * @throws Throwable
     */
    protected void removeElements(Executor executor, XmlDoc.Element metaType,
            String id, String objectType, XmlDoc.Element currentMeta)
            throws Throwable {
        XmlDoc.Element dicom = metaType.element("dicom");
        if (dicom != null) {
            removeElementsDicom(executor, id, objectType, currentMeta);
        }

        XmlDoc.Element bruker = metaType.element("bruker");
        if (bruker != null) {
            removeElementsBruker(executor, id, objectType, currentMeta);
        }
    }

    /**
     * Update the meta-data on the project object. This function must do the
     * actual update with the appropriate service (e.g. om.pssd.project.update).
     * This function over-rides the default implementation.
     * 
     * @param id
     *            The citeable ID of the object to update
     * @param meta
     *            The DICOM Study Metadata or Bruker identifier metadata. This
     *            class must understand the structure of this object it's up to
     *            you what you put in it. This class is invoked by the servuce
     *            nig.pssd.subject.meta.set and so its interface determines the
     *            structure
     * @param privacyType
     *            The element to find the meta-data in the object description
     *            For SUbjects and RSubjects should be one of "public",
     *            "private", "identity" For other object types, should be "meta"
     * @param docType
     *            the document type to write meta-data for. The values must be
     *            mapped from the Study MetaData
     * @param currentMeta
     *            The meta-data that are attached to the asset (:foredit false)
     * @throws Throwable
     */
    protected void addTranslatedProjectDocument(Executor executor, String id,
            XmlDoc.Element meta, String privacyType, String docType,
            XmlDoc.Element currentMeta) throws Throwable {
        if (meta == null)
            return;

        XmlDocMaker dm = null;
        if (docType.equals("vicnode.daris:pssd.project")) {
            if (checkDocTypeExists(executor, "vicnode.daris:pssd.project")) {
                dm = new XmlDocMaker("args");
                dm.add("id", id);
                dm.push(privacyType, new String[] { "action", "merge" });
                boolean doIt = addPSSDProjectFacilityIDOuter(meta, currentMeta,
                        dm);
                if (!doIt)
                    dm = null;
            }
        }

        // Update the Project
        if (dm != null) {
            updateProject(executor, dm);
        }
    }

    /**
     * Update the meta-data on the subject object. This function must do the
     * actual update with the appropriate service (e.g. om.pssd.subject.update).
     * This function over-rides the default implementation.
     * 
     * @param id
     *            The citeable ID of the object to update
     * @param meta
     *            The DICOM Study Metadata or Bruker identifier metadata
     * @param privacyType
     *            The element to find the meta-data in the object description
     *            For SUbjects and RSubjects should be one of "public",
     *            "private", "identity" (RSubjects) For other object types,
     *            should be "meta"
     * @param docType
     *            the document type to write meta-data for. The values must be
     *            mapped from the Study MetaData
     * @param currentMeta
     *            The meta-data that are attached to the asset (:foredit false)
     * @throws Throwable
     */
    protected void addTranslatedSubjectDocument(Executor executor, String id,
            XmlDoc.Element meta, String privacyType, String docType,
            XmlDoc.Element currentMeta) throws Throwable {
        if (meta == null)
            return;

        XmlDocMaker dm = null;
        if (docType.equals("vicnode.daris:pssd.identity")) {
            if (checkDocTypeExists(executor, "vicnode.daris:pssd.identity")) {
                dm = new XmlDocMaker("args");
                dm.add("id", id);
                dm.push(privacyType);
                boolean doIt = addPSSDIdentityOuter(meta, currentMeta, dm);
                if (!doIt)
                    dm = null;
            }
        } else if (docType.equals("vicnode.daris:pssd.animal.subject")) {
            if (checkDocTypeExists(executor,
                    "vicnode.daris:pssd.animal.subject")) {
                dm = new XmlDocMaker("args");
                dm.add("id", id);
                dm.push(privacyType);
                boolean doIt = addPSSDAnimalSubjectOuter(meta, currentMeta, dm);
                if (!doIt)
                    dm = null;
            }
        }

        // Update the SUbject
        if (dm != null) {
            updateSubject(executor, dm);
        }

    }

    /**
     * Update the meta-data on the study object. This function must do the
     * actual update with the appropriate service (e.g. om.pssd.project.update).
     * This function over-rides the default implementation.
     * 
     * @param id
     *            The citeable ID of the object to update
     * @param meta
     *            The DICOM Study Metadata or Bruker identifier metadata
     * @param privacyType
     *            The element to find the meta-data in the object description
     *            For SUbjects and RSubjects should be one of "public",
     *            "private", "identity" For other object types, should be "meta"
     * @param docType
     *            the document type to write meta-data for. The values must be
     *            mapped from the Study MetaData
     * @param ns
     *            An addition namespace to be set on the meta-data being
     *            updated. Its purpose is for Method namespaces like cid_step
     *            that must be set on the Method specified Study meta-data
     * @param currentMeta
     *            The meta-data that are attached to the asset (:foredit false)
     * @throws Throwable
     */
    protected void addTranslatedStudyDocument(Executor executor, String id,
            XmlDoc.Element meta, String privacyType, String docType, String ns,
            XmlDoc.Element currentMeta) throws Throwable {
        if (meta == null)
            return;

        // No DICOM mapping at this time

        // Bruker.
        // This does not need to use the Method namespace because it uses its
        // own specialised 'bruker' namespace
        XmlDocMaker dm = null;
        if (docType.equals("hfi-bruker-study")) {
            if (checkDocTypeExists(executor, "hfi-bruker-study")) {
                dm = new XmlDocMaker("args");
                dm.add("id", id);
                dm.push(privacyType, new String[] { "action", "merge" });
                boolean doIt = addPSSDStudyOuter(meta, currentMeta, dm);
                if (!doIt)
                    dm = null;
            }
        }

        // Update the Study
        if (dm != null) {
            updateStudy(executor, dm);
        }
    }

    private boolean addPSSDProjectFacilityIDOuter(XmlDoc.Element meta,
            XmlDoc.Element currentMeta, XmlDocMaker dm) throws Throwable {

        return addPSSDProjectFacilityIDDICOM(meta.element("dicom"),
                currentMeta, dm)
                || addPSSDProjectFacilityIDBruker(meta.element("bruker"),
                        currentMeta, dm);
    }

    private boolean addPSSDIdentityOuter(XmlDoc.Element meta,
            XmlDoc.Element currentMeta, XmlDocMaker dm) throws Throwable {
        return addPSSDIdentityDICOM(meta.element("dicom"), currentMeta, dm)
                || addPSSDIdentityBruker(meta.element("bruker"), currentMeta,
                        dm);
    }

    private boolean addPSSDAnimalSubjectOuter(XmlDoc.Element meta,
            XmlDoc.Element currentMeta, XmlDocMaker dm) throws Throwable {

        XmlDoc.Element dicom = meta.element("dicom");
        Boolean set = false;
        if (dicom != null) {
            XmlDoc.Element subject = dicom.element("subject");
            if (subject != null) {

                // Extract values from container
                Date dob = subject.element("dob") != null ? subject
                        .dateValue("dob") : null;
                String dobString = null;
                if (dob != null) {
                    SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT);
                    dobString = df.format(dob).toString();
                }
                //
                String gender = null;
                gender = subject.value("sex");
                //
                set = addPSSDAnimalSubject(dobString, gender, currentMeta, dm);
            }
        }
        //
        XmlDoc.Element bruker = meta.element("bruker");
        if (bruker != null) {
            String dob = null; // Not available
            String gender = bruker.value("gender");
            if (gender != null) {
                if (gender.equalsIgnoreCase("M")) {
                    gender = "male";
                } else if (gender.equalsIgnoreCase("F")) {
                    gender = "female";
                } else {
                    gender = "unknown";
                }
            }
            Boolean set2 = addPSSDAnimalSubject(dob, gender, currentMeta, dm);
            if (set2)
                set = true;
        }
        return set;
    }

    private boolean addPSSDStudyOuter(XmlDoc.Element meta,
            XmlDoc.Element currentMeta, XmlDocMaker dm) throws Throwable {

        // No mapping for DICOM data at this time.
        return addPSSDStudyBruker(meta.element("bruker"), currentMeta, dm);
    }

    /**
     * Function to add the Project Facility ID to the Project meta-data if it
     * does not already exist
     * 
     * @param executor
     * @param currentMeta
     * @param cid
     * @throws Throwable
     */
    private boolean addPSSDProjectFacilityIDDICOM(XmlDoc.Element sm,
            XmlDoc.Element currentMeta, XmlDocMaker dm) throws Throwable {
        if (sm == null)
            return false;

        // There is no really good candidate for the project ID in the
        // Study meta-data. Perhaps the element
        // STUDY_DESCRIPTION = new DataElementTag(0x0008,0x1030);
        // would be ok.

        // Extract DICOM meta data
        String projectDescription = sm.value("description");

        // We really can't know who generated this description.
        // We can't assume it's anything to do with the facility that
        // actually provides the data.
        String facilityType = "Other";

        // Add/merge the facility ID
        return addMergeFacilityID(currentMeta, projectDescription,
                facilityType, dm);

    }

    /**
     * Function to add the Subject ID (DICOM element (0010,0020)) to the SUbject
     * meta-data if it does not already exist not already exist
     * 
     * @param executor
     * @param currentMeta
     * @param cid
     * @throws Throwable
     */
    private boolean addPSSDIdentityDICOM(XmlDoc.Element sm,
            XmlDoc.Element currentMeta, XmlDocMaker dm) throws Throwable {
        if (sm == null)
            return false;

        // Extract DICOM meta data
        String patientID = sm.value("id");
        if (patientID == null)
            return false;

        // Set type of identity; i.e. who supplied this identity
        String typeID = "Other";
        String scanFac = scannerFacility(sm);
        if (scanFac.equals(RCH_FACILITY)) {
            typeID = "RCH";
        } else if (scanFac.equals(AMRIF_FACILITY)) {
            typeID = "aMRIF";
        }

        // Add/merge the identity if needed.
        return addMergeIdentity(currentMeta, patientID, typeID, dm);
    }

    /**
     * Function to add the Project Facility ID to the Project meta-data if it
     * does not already exist
     * 
     * @param executor
     * @param currentMeta
     * @param cid
     * @throws Throwable
     */
    private boolean addPSSDProjectFacilityIDBruker(XmlDoc.Element sm,
            XmlDoc.Element currentMeta, XmlDocMaker dm) throws Throwable {
        if (sm == null)
            return false;

        // Extract Bruker meta data. Now the "project description", as used at
        // aMRIF, is really just
        // a String (one word) describing the Project Name. So it's legitimate
        // to treat it
        // as a Facility ID
        String projectDescription = sm.value("project_descriptor");
        String facilityType = "aMRIF"; // Because this is a NIG class, we are
                                       // allowed to 'know' this

        // Add/merge the facility ID
        return addMergeFacilityID(currentMeta, projectDescription,
                facilityType, dm);

    }

    /**
     * Function to add the Subject ID (DICOM element (0010,0020)) to the SUbject
     * meta-data if it does not already exist not already exist
     * 
     * @param executor
     * @param currentMeta
     * @param cid
     * @throws Throwable
     */
    private boolean addPSSDIdentityBruker(XmlDoc.Element sm,
            XmlDoc.Element currentMeta, XmlDocMaker dm) throws Throwable {
        if (sm == null)
            return false;

        // Extract Bruker meta data
        String animalID = sm.value("animal_id");

        // Set type of identity; i.e. who supplied this identity
        // OK because this is a NIG class and should only be utilised at NIG
        // (for now)
        // TODO: find some way of getting the actual station into here.
        String typeID = "aMRIF";

        // Add/merge the identity if needed.
        return addMergeIdentity(currentMeta, animalID, typeID, dm);

    }

    /**
     * Function to add the meta-data parsed from the FNI Small ANimal Facility
     * subject ID coded strings
     * 
     * @param executor
     * @param currentMeta
     * @param cid
     * @throws Throwable
     */
    private boolean addPSSDStudyBruker(XmlDoc.Element sm,
            XmlDoc.Element currentMeta, XmlDocMaker dm) throws Throwable {
        if (sm == null)
            return false;

        // Add/merge the identity if needed.
        Date date = sm.element("date").hasValue() ? sm.dateValue("date") : null;
        String dateStr = null;
        if (date != null) {
            dateStr = DateUtil.formatDate(date, "dd-MMM-yyyy");
        }

        return addMergeaMRIFStudy(currentMeta, sm.value("coil"), dateStr, dm);
    }

    private boolean addMergeaMRIFStudy(XmlDoc.Element currentMeta, String coil,
            String date, XmlDocMaker dm) throws Throwable {

        // Set updated meta-data
        // We add a new document with the details. SHould never be more than
        // one...
        Boolean someMeta = false;

        // Should get this namespace from a central class...
        dm.push("hfi-bruker-study", new String[] { "ns", "bruker" });
        if (coil != null) {
            dm.add("coil", coil);
            someMeta = true;
        }
        if (date != null) {
            dm.add("date", date);
            someMeta = true;
        }
        dm.pop();
        dm.pop(); // "meta" pop
        return someMeta;
    }

    private boolean addMergeFacilityID(XmlDoc.Element currentMeta,
            String projectID, String idType, XmlDocMaker dm) throws Throwable {

        // See if this specific identity already exists on the object
        Collection<XmlDoc.Element> identities = null;
        if (currentMeta != null) {
            identities = currentMeta.elements("vicnode.daris:pssd.project");
            if (identities != null) {
                for (XmlDoc.Element el : identities) {
                    String id = el.value("facility-id");
                    String type = el.value("facility-id/@type");

                    // If we have this specific identity already, return
                    if (id != null && type != null) {
                        if (id.equals(projectID) && type.equals(idType))
                            return false;
                    }
                }
            }
        }

        // So we did not find this identity and need to add it.
        // If we have just one pre-existing identity, merge with it
        // Otherwise add a new one
        dm.push("vicnode.daris:pssd.project");
        if (identities != null && identities.size() == 1) {
            XmlDoc.Element identity = currentMeta
                    .element("vicnode.daris:pssd.project");
            Collection<XmlDoc.Element> els = identity.elements();
            for (XmlDoc.Element el : els) {
                dm.add(el);
            }
        }
        //
        dm.add("facility-id", new String[] { "type", idType }, projectID);
        dm.pop();

        // We want to merge this identity with others on the same document
        dm.pop(); // "meta" pop
        return true;
    }

    private boolean addMergeIdentity(XmlDoc.Element currentMeta,
            String subjectID, String typeID, XmlDocMaker dm) throws Throwable {

        if (subjectID == null || typeID == null)
            return false;

        // See if this specific identity already exists on the object
        Collection<XmlDoc.Element> identities = null;
        if (currentMeta != null) {
            identities = currentMeta.elements("vicnode.daris:pssd.identity");
            if (identities != null) {
                for (XmlDoc.Element identity : identities) {
                    Collection<XmlDoc.Element> els = identity.elements("id");
                    if (els != null) {
                        for (XmlDoc.Element el : els) {
                            String id = el.value();
                            String type = el.value("@type");

                            // If we have this specific identity already, return
                            if (id != null && type != null) {
                                if (id.equals(subjectID) && type.equals(typeID))
                                    return false;
                            }
                        }
                    }
                }
            }
        }

        // So we did not find this identity and need to add it.
        // If we have just one pre-existing identity document, merge with it
        // Otherwise add a new one
        dm.push("vicnode.daris:pssd.identity");
        if (identities != null && identities.size() == 1) {
            XmlDoc.Element identity = currentMeta
                    .element("vicnode.daris:pssd.identity");
            Collection<XmlDoc.Element> els = identity.elements();
            for (XmlDoc.Element el : els) {
                dm.add(el);
            }
        }
        //
        dm.add("id", new String[] { "type", typeID }, subjectID);
        dm.pop();

        // We want to merge this identity with others on the same document
        dm.pop(); // "public" or "private" pop
        dm.add("action", "merge");
        return true;
    }

    private boolean addPSSDAnimalSubject(String dob, String gender,
            XmlDoc.Element currentMeta, XmlDocMaker dm) throws Throwable {

        // Get current meta-data for appropriate DocType
        if (currentMeta != null) {
            XmlDoc.Element subjectMeta = currentMeta
                    .element("vicnode.daris:pssd.animal.subject");

            // We assume that if the element is already set on the object that
            // it is correct
            if (subjectMeta != null) {
                String currGender = subjectMeta.value("gender");
                if (currGender != null)
                    gender = currGender;
                //
                String currDate = subjectMeta.value("birthDate");
                if (currDate != null)
                    dob = currDate;
            }
        }

        // Set updated meta-data
        if (gender != null || dob != null) {
            dm.push("vicnode.daris:pssd.animal.subject");
            if (gender != null)
                dm.add("gender", gender);
            if (dob != null)
                dm.add("birthDate", dob);
            dm.pop();
        } else {
            return false;
        }

        // Merge these details
        dm.pop(); // "public" or "private" pop
        dm.add("action", "merge");
        return true;
    }

    /**
     * What Facility are these data from?
     * 
     * @param sm
     * @return
     */
    public String scannerFacility(XmlDoc.Element sm) throws Throwable {
        String institution = sm.value("institution").toUpperCase();
        String station = sm.value("station").toUpperCase();
        //
        if (institution.contains("HOWARD FLOREY INSTITUTE")
                && station.equals("EPT")) {
            return AMRIF_FACILITY;
        } else if (institution.contains("CHILDREN")
                && institution.contains("RCH") && station.equals("MRC35113")) {
            return RCH_FACILITY;
        } else {
            return institution;
        }
    }

    /**
     * Remove the mapped elements for when we are considering DICOM meta-data.
     * 
     * @param executor
     * @param id
     * @param objectType
     * @param currentMeta
     * @throws Throwable
     */
    private void removeElementsDicom(Executor executor, String id,
            String objectType, XmlDoc.Element currentMeta) throws Throwable {
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("cid", id);
        //
        boolean some = false;
        if (objectType.equals(DomainMetaData.PROJECT_TYPE)) {
            if (prepareRemovedMetaData(dm, currentMeta,
                    "vicnode.daris:pssd.project",
                    new String[] { "facility-id" }))
                some = true;
        } else if (objectType.equals(DomainMetaData.SUBJECT_TYPE)) {
            if (prepareRemovedMetaData(dm, currentMeta,
                    "vicnode.daris:pssd.identity", new String[] { "id" }))
                some = true;
            if (prepareRemovedMetaData(dm, currentMeta,
                    "vicnode.daris:pssd.animal.subject", new String[] {
                            "gender", "birthDate" }))
                some = true;
        } else if (objectType.equals(DomainMetaData.STUDY_TYPE)) {
            // No DICOM mappings for now
        }
        //
        if (some) {
            executor.execute("asset.set", dm);
        }
    }

    /**
     * Remove the mapped elements for when we are considering Bruker meta-data.
     * 
     * @param executor
     * @param id
     * @param objectType
     * @param currentMeta
     * @throws Throwable
     */
    private void removeElementsBruker(Executor executor, String id,
            String objectType, XmlDoc.Element currentMeta) throws Throwable {
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("cid", id);
        //
        boolean some = false;
        if (objectType.equals(DomainMetaData.PROJECT_TYPE)) {
            if (prepareRemovedMetaData(dm, currentMeta,
                    "vicnode.daris:pssd.project",
                    new String[] { "facility-id" }))
                some = true;
        } else if (objectType.equals(DomainMetaData.SUBJECT_TYPE)) {
            if (prepareRemovedMetaData(dm, currentMeta,
                    "vicnode.daris:pssd.identity", new String[] { "id" }))
                some = true;
            if (prepareRemovedMetaData(dm, currentMeta,
                    "vicnode.daris:pssd.animal.subject", new String[] {
                            "gender", "birthDate" }))
                some = true;
        } else if (objectType.equals(DomainMetaData.STUDY_TYPE)) {
            if (prepareRemovedMetaData(dm, currentMeta, "hfi-bruker-study",
                    new String[] { "coil", "date" }))
                some = true;
        }
        //
        if (some) {
            executor.execute("asset.set", dm);
        }
    }
}
