package com.sarthi.notification.sms;

/**
 * SMS texts sent on call assignment / reassignment.
 * Each text must match its DLT registration exactly; the DLT template id comes from
 * application property  xpertsms.template.call.&lt;propertyKey&gt;  (empty → SMS skipped, not sent).
 *
 * Placeholders: {callNo} {product} {plant} {ieName} {ieMobile} {oldIeName}
 */
public enum CallSmsTemplate {

    IE_ASSIGNED("ie-assigned",
            "Call {callNo} ({product}) at {plant} has been assigned to you for inspection. -RITES/QA"),
    VENDOR_ASSIGNED("vendor-assigned",
            "Your inspection call {callNo} has been assigned to IE {ieName}, Mob {ieMobile}. -RITES/QA"),
    CM_ASSIGNED("cm-assigned",
            "Call {callNo} ({product}, {plant}) has been assigned to IE {ieName}. -RITES/QA"),

    IE_REASSIGNED_NEW("ie-reassigned-new",
            "Call {callNo} ({product}) at {plant} has been reassigned to you for inspection. -RITES/QA"),
    IE_REASSIGNED_OLD("ie-reassigned-old",
            "Call {callNo} has been reassigned to another IE. No action is needed from you. -RITES/QA"),
    VENDOR_REASSIGNED("vendor-reassigned",
            "Your inspection call {callNo} has been reassigned to IE {ieName}, Mob {ieMobile}. -RITES/QA"),
    CM_REASSIGNED("cm-reassigned",
            "Call {callNo} ({product}, {plant}) has been reassigned from IE {oldIeName} to IE {ieName}. -RITES/QA");

    private final String propertyKey;
    private final String text;

    CallSmsTemplate(String propertyKey, String text) {
        this.propertyKey = propertyKey;
        this.text = text;
    }

    public String propertyKey() { return propertyKey; }

    public String render(java.util.Map<String, String> values) {
        String out = text;
        for (java.util.Map.Entry<String, String> e : values.entrySet()) {
            out = out.replace("{" + e.getKey() + "}", e.getValue() == null ? "-" : e.getValue());
        }
        return out;
    }
}
