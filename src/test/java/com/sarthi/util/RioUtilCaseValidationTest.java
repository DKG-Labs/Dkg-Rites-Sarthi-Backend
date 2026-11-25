package com.sarthi.util;

import com.sarthi.entity.UserMaster;
import com.sarthi.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.*;

/** Unit tests for IE RIO vs Case Number validation helpers in {@link RioUtil}. */
class RioUtilCaseValidationTest {

    private UserMaster makeUser(Integer id, String roleName, String rio) {
        UserMaster u = new UserMaster();
        u.setUserId(id);
        u.setRoleName(roleName);
        u.setRio(rio);
        u.setEmployeeCode("EMP" + id);
        return u;
    }

    @Test
    void matchingRioAllowsInspectionAcrossAllRegionsAndAliases() {
        assertDoesNotThrow(() -> RioUtil.validateIeCaseRioAccess(
                makeUser(1, "ERC Process IE", "WRIO"), null, null, (JdbcTemplate) null, null, "W26090142", "EP-26111919"));
        assertDoesNotThrow(() -> RioUtil.validateIeCaseRioAccess(
                makeUser(2, "ERC IE", "NIRIO"), null, null, (JdbcTemplate) null, null, "N25100022", "EF-1001"));
        assertDoesNotThrow(() -> RioUtil.validateIeCaseRioAccess(
                makeUser(3, "Sleeper Main IE", "EASTERN KOLKATA"), null, null, (JdbcTemplate) null, null, "E25100017", "SF-2001"));
        assertDoesNotThrow(() -> RioUtil.validateIeCaseRioAccess(
                makeUser(4, "Rail Main IE", "SRIO"), null, null, (JdbcTemplate) null, null, "S26010011", "RPF-3001"));
        assertDoesNotThrow(() -> RioUtil.validateIeCaseRioAccess(
                makeUser(5, "Process IE", "BHILAI"), null, null, (JdbcTemplate) null, null, "C26010099", "RPP-4001"));
    }

    @Test
    void mismatchedRioBlocksCrossRioInspectionEvenForActionUserFallback() {
        BusinessException ex = assertThrows(BusinessException.class, () -> RioUtil.validateIeCaseRioAccess(
                makeUser(10, "ERC Process IE", "ERIO"), null, null, (JdbcTemplate) null, null, "W26090142", "EP-26111919"));
        assertTrue(ex.getErrorDetails().getMessage().contains("ERIO"));
        assertTrue(ex.getErrorDetails().getMessage().contains("WRIO"));

        assertThrows(BusinessException.class, () -> RioUtil.validateIeCaseRioAccess(
                null, makeUser(11, "Sleeper Main IE", "NRIO"), null, (JdbcTemplate) null, null, "S26010011", "SF-2001"));
        assertThrows(BusinessException.class, () -> RioUtil.validateIeCaseRioAccess(
                null, makeUser(12, "Rail Main IE", "CRIO"), null, (JdbcTemplate) null, null, "E25100017", "RPF-3001"));
    }

    @Test
    void commaSeparatedCaseNumbersAllowWhenAnyTokenMatchesAndBlockOtherwise() {
        assertDoesNotThrow(() -> RioUtil.validateIeCaseRioAccess(
                makeUser(20, "ERC IE", "NRIO"), null, null, (JdbcTemplate) null, null, "E25100017, N25100022", "EP-1"));
        assertThrows(BusinessException.class, () -> RioUtil.validateIeCaseRioAccess(
                makeUser(21, "ERC IE", "WRIO"), null, null, (JdbcTemplate) null, null, "E25100017, N25100022", "EP-1"));
    }

    @Test
    void blankHyphenNumericOrMissingCaseNumbersAreNotBlocked() {
        UserMaster ie = makeUser(30, "ERC IE", "ERIO");
        assertDoesNotThrow(() -> RioUtil.validateIeCaseRioAccess(ie, null, null, (JdbcTemplate) null, null, null, "EP-1"));
        assertDoesNotThrow(() -> RioUtil.validateIeCaseRioAccess(ie, null, null, (JdbcTemplate) null, null, "-", "EP-1"));
        assertDoesNotThrow(() -> RioUtil.validateIeCaseRioAccess(ie, null, null, (JdbcTemplate) null, null, "N/A", "EP-1"));
        assertDoesNotThrow(() -> RioUtil.validateIeCaseRioAccess(ie, null, null, (JdbcTemplate) null, null, "26090142", "EP-1"));
    }

    @Test
    void nonIeAndAdminRolesAreNeverBlocked() {
        for (String role : new String[] { "RIO Help Desk", "Vendor", "Finance", "Control Manager", "SBU Head", "Admin", "Super Admin", "ERC IE, Admin" }) {
            UserMaster user = makeUser(40, role, "ERIO");
            assertFalse(RioUtil.isActingUserRestrictedIe(user, null), "Role should not be IE-restricted: " + role);
            assertDoesNotThrow(() -> RioUtil.validateIeCaseRioAccess(
                    user, null, null, (JdbcTemplate) null, null, "W26090142", "EP-26111919"));
        }
        assertTrue(RioUtil.isActingUserRestrictedIe(makeUser(41, "IE", "ERIO"), null));
        assertTrue(RioUtil.isActingUserRestrictedIe(makeUser(42, "ERC Process IE", "ERIO"), null));
        assertTrue(RioUtil.isActingUserRestrictedIe(makeUser(43, "Rail Main IE", "ERIO"), null));
    }
}
