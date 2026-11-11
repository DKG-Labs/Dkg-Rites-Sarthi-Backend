package com.sarthi.udmcomplaint;

/** Names/values shared by the UDM complaint workflow (match database_migrations/V20261111_001). */
public final class UdmComplaintConstants {

    private UdmComplaintConstants() { }

    public static final String WORKFLOW_NAME = "UDM_COMPLAINT";

    // TRANSITION_MASTER.TRANSITIONNAME
    public static final String TR_RECEIVED = "UDM_COMPLAINT_RECEIVED";
    public static final String TR_SEND_TO_CM = "CC_SEND_TO_CM";
    public static final String TR_CLOSE = "CC_CLOSE_COMPLAINT";

    // udm_complaint.status
    public static final String ST_PENDING_CC = "PENDING_WITH_COMPLAINT_CELL";
    public static final String ST_SENT_TO_CM = "SENT_TO_CM";
    public static final String ST_CLOSED = "CLOSED";

    // Roles
    public static final String ROLE_COMPLAINT_CELL = "Complaint Cell";
    public static final String ROLE_CM_NAME = "Controlling Manager";
    /**
     * ROLE_MASTER names (lower-case) used to look up role ids at runtime — ids differ per environment.
     * See UdmRoleResolver.
     */
    public static final java.util.List<String> CM_ROLE_NAMES = java.util.List.of("control manager", "controlling manager", "cm");
    public static final java.util.List<String> SBU_HEAD_ROLE_NAMES = java.util.List.of("sbu head");
    public static final java.util.List<String> RAILWAY_BOARD_ROLE_NAMES = java.util.List.of("railway board");

    public static final int REMARKS_MAX = 500;

    // ---------------- Phase 2: Controlling Manager stages (V20261111_003)
    public static final String TR_CM_SCHEDULE_JI = "CM_SCHEDULE_JI";
    public static final String TR_CM_START_AUDIT = "CM_START_AUDIT";
    public static final String TR_CM_COMPLETE_JI = "CM_COMPLETE_JI";
    public static final String TR_CM_COMPLETE_AUDIT = "CM_COMPLETE_AUDIT";
    public static final String TR_CM_SUBMIT_RCA = "CM_SUBMIT_RCA";
    public static final String TR_CM_SUBMIT_CAPA = "CM_SUBMIT_CAPA";

    public static final String ST_JI_SCHEDULED = "JI_SCHEDULED";
    public static final String ST_AUDIT_IN_PROGRESS = "AUDIT_IN_PROGRESS";
    public static final String ST_JI_COMPLETED = "JI_COMPLETED";
    public static final String ST_AUDIT_COMPLETED = "AUDIT_COMPLETED";
    public static final String ST_RCA_SUBMITTED = "RCA_SUBMITTED";
    public static final String ST_CAPA_SUBMITTED = "CAPA_SUBMITTED";

    // udm_complaint_stage.stage
    public static final String STAGE_DECISION = "DECISION";
    public static final String STAGE_JI_COMPLETE = "JI_COMPLETE";
    public static final String STAGE_AUDIT_COMPLETE = "AUDIT_COMPLETE";
    public static final String STAGE_RCA = "RCA";
    public static final String STAGE_CAPA = "CAPA";

    public static final String PATH_JI = "JI";
    public static final String PATH_AUDIT = "AUDIT";

    // udm_complaint_document.doc_type
    public static final String DOC_JI_REPORT = "JI_REPORT";
    public static final String DOC_AUDIT_REPORT = "AUDIT_REPORT";
    public static final String DOC_RCA_EVIDENCE = "RCA_EVIDENCE";
    public static final String DOC_CAPA_EVIDENCE = "CAPA_EVIDENCE";

    // udm_complaint_dropdown_master.category
    public static final String DD_JI_OUTCOME = "JI_OUTCOME";
    public static final String DD_ATTRIBUTABLE_TO = "ATTRIBUTABLE_TO";
    public static final String DD_ROOT_CAUSE = "ROOT_CAUSE";
    public static final String DD_CORRECTIVE = "CORRECTIVE_ACTION";
    public static final String DD_PREVENTIVE = "PREVENTIVE_ACTION";
    public static final String DD_AUDIT_TYPE = "AUDIT_TYPE";

    public static final String ROLE_SBU_HEAD_NAME = "SBU Head";
    public static final String ROLE_RAILWAY_BOARD_NAME = "Railway Board";

    /** First submission round; each return to the CM for rework adds 1 (udm_complaint.current_round). */
    public static final int FIRST_ROUND = 1;

    // ---------------- Phase 3a: SBU Head review (V20261111_006)
    public static final String TR_SBU_FORWARD_TO_RB = "SBU_FORWARD_TO_RB";
    public static final String TR_SBU_RETURN_TO_CM = "SBU_RETURN_TO_CM";

    public static final String ST_SENT_TO_RB = "SENT_TO_RAILWAY_BOARD";
    public static final String ST_RETURNED_TO_CM = "RETURNED_TO_CM";
    /** Set by Railway Board "Return" (Phase 3b); SBU Head can act on it again. */
    public static final String ST_RETURNED_TO_SBU = "RETURNED_TO_SBU";

    public static final String STAGE_SBU_REVIEW = "SBU_REVIEW";
    public static final String DECISION_FORWARD = "FORWARD";
    public static final String DECISION_RETURN = "RETURN";

    public static final int JI_TEAM_MAX = 1000;
    public static final int JI_TEAM_OTHERS_MAX = 300;
    public static final int JI_TEAM_MAX_MEMBERS = 30;
    public static final String JI_TYPE_CM = "CM";
    public static final String JI_TYPE_IE = "IE";
    public static final int FINDINGS_MAX = 1000;
    /** Upload rule: PDF only, max 5 MB. */
    public static final long DOC_MAX_BYTES = 5L * 1024 * 1024;
    /** Recommendation flag: failed within this many days of IC / material receipt. */
    public static final int FAILED_WITHIN_DAYS = 90;
}
