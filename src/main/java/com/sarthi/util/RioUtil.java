package com.sarthi.util;

import com.sarthi.entity.IeProfile;
import com.sarthi.entity.RioUser;
import com.sarthi.entity.UserMaster;
import com.sarthi.repository.IeProfileRepository;
import com.sarthi.repository.RioUserRepository;
import com.sarthi.repository.UserRoleMasterRepository;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Shared utility for normalizing Regional Inspection Office (RIO) identifiers,
 * resolving user RIO & roles, and filtering Call Desk IEs by Product, IE Role, and RIO.
 */
public final class RioUtil {

    private RioUtil() {
    }

    /**
     * Normalizes an RIO name or code into a canonical short code (ER, WR, NR, SR, CR).
     */
    public static String normalizeRio(String rioStr) {
        if (rioStr == null || rioStr.trim().isEmpty()) {
            return "";
        }
        String r = rioStr.trim().toUpperCase();
        if (r.equals("ER") || r.equals("ERIO") || r.contains("EAST") || r.contains("KOLKATA")) {
            return "ER";
        }
        if (r.equals("WR") || r.equals("WRIO") || r.contains("WEST") || r.contains("MUMBAI")) {
            return "WR";
        }
        if (r.equals("NR") || r.equals("NRIO") || r.contains("NORTH") || r.contains("DELHI")) {
            return "NR";
        }
        if (r.equals("SR") || r.equals("SRIO") || r.contains("SOUTH") || r.contains("CHENNAI")) {
            return "SR";
        }
        if (r.equals("CR") || r.equals("CRIO") || r.contains("CENT") || r.contains("BHILAI") || r.contains("RAIPUR")) {
            return "CR";
        }
        return r.replace("RIO", "").replace("REGION", "").trim();
    }

    /**
     * Returns true if {@code targetRio} is blank (unfiltered) or if any comma-separated
     * segment in {@code candidateRio} normalizes to the same RIO code as {@code targetRio}.
     */
    public static boolean isRioMatching(String targetRio, String candidateRio) {
        if (targetRio == null || targetRio.trim().isEmpty()) {
            return true;
        }
        String normalizedTarget = normalizeRio(targetRio);
        if (normalizedTarget.isEmpty()) {
            return true;
        }
        if (candidateRio == null || candidateRio.trim().isEmpty()) {
            return false;
        }
        String[] parts = candidateRio.split(",");
        for (String part : parts) {
            String normalizedCandidate = normalizeRio(part);
            if (!normalizedCandidate.isEmpty() && normalizedTarget.equals(normalizedCandidate)) {
                return true;
            }
        }
        return false;
    }

    public static Map<String, String> loadRioUserMap(RioUserRepository rioUserRepository) {
        Map<String, String> map = new HashMap<>();
        if (rioUserRepository == null) {
            return map;
        }
        try {
            for (RioUser ru : rioUserRepository.findAll()) {
                if (ru.getEmployeeCode() != null && ru.getRio() != null && !ru.getRio().trim().isEmpty()) {
                    map.put(ru.getEmployeeCode().trim(), ru.getRio().trim());
                }
            }
        } catch (Exception ignored) {
        }
        return map;
    }

    public static Map<String, String> loadIeProfileRioMap(IeProfileRepository ieProfileRepository) {
        Map<String, String> map = new HashMap<>();
        if (ieProfileRepository == null) {
            return map;
        }
        try {
            for (IeProfile ip : ieProfileRepository.findAll()) {
                if (ip.getEmployeeCode() != null && ip.getRio() != null && !ip.getRio().trim().isEmpty()) {
                    map.put(ip.getEmployeeCode().trim(), ip.getRio().trim());
                }
            }
        } catch (Exception ignored) {
        }
        return map;
    }

    public static Map<String, String> loadIeProfileRioMap(JdbcTemplate jdbcTemplate) {
        Map<String, String> map = new HashMap<>();
        if (jdbcTemplate == null) {
            return map;
        }
        try {
            jdbcTemplate.query("SELECT employee_code, rio FROM ie_profile WHERE rio IS NOT NULL", rs -> {
                String code = rs.getString("employee_code");
                String rio = rs.getString("rio");
                if (code != null && rio != null && !rio.trim().isEmpty()) {
                    map.put(code.trim(), rio.trim());
                }
            });
        } catch (Exception ignored) {
        }
        return map;
    }

    public static Map<Integer, List<String>> loadUserRolesMap(
            UserRoleMasterRepository userRoleMasterRepository,
            List<UserMaster> users) {
        Map<Integer, List<String>> map = new HashMap<>();
        if (userRoleMasterRepository == null || users == null || users.isEmpty()) {
            return map;
        }
        try {
            List<Integer> userIds = users.stream()
                    .map(UserMaster::getUserId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();
            if (!userIds.isEmpty()) {
                List<Object[]> rows = userRoleMasterRepository.findUserRolesByUserIds(userIds);
                for (Object[] row : rows) {
                    if (row != null && row.length >= 2 && row[0] != null && row[1] != null) {
                        Integer uid = ((Number) row[0]).intValue();
                        String rName = row[1].toString().trim();
                        if (!rName.isEmpty()) {
                            map.computeIfAbsent(uid, k -> new ArrayList<>()).add(rName);
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return map;
    }

    public static List<String> resolveEffectiveRoles(
            UserMaster user,
            Map<Integer, List<String>> userRolesMap) {
        if (user == null) {
            return Collections.emptyList();
        }
        if (userRolesMap != null && user.getUserId() != null) {
            List<String> fromMaster = userRolesMap.get(user.getUserId());
            if (fromMaster != null && !fromMaster.isEmpty()) {
                return fromMaster;
            }
        }
        if (user.getRoleName() != null && !user.getRoleName().trim().isEmpty()) {
            return Arrays.stream(user.getRoleName().split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();
        }
        return Collections.emptyList();
    }

    /**
     * Resolves a user's RIO from {@link UserMaster}, falling back to IE profile
     * and RIO user lookup tables when {@code user.getRio()} is blank.
     */
    public static String resolveUserRio(
            UserMaster user,
            Map<String, String> ieProfileRioMap,
            Map<String, String> rioUserMap) {
        if (user == null) {
            return null;
        }
        if (user.getRio() != null && !user.getRio().trim().isEmpty()) {
            return user.getRio().trim();
        }
        String empCode = user.getEmployeeCode() != null ? user.getEmployeeCode().trim() : null;
        String username = user.getUsername() != null ? user.getUsername().trim() : null;

        String resolved = lookupByKeys(ieProfileRioMap, empCode, username);
        if (resolved != null) {
            return resolved;
        }
        return lookupByKeys(rioUserMap, empCode, username);
    }

    public static String normalizeProduct(String productStr) {
        if (productStr == null || productStr.trim().isEmpty()) {
            return "";
        }
        String p = productStr.trim().toUpperCase().replace("_", " ").replace("-", " ");
        if (p.equals("ERC") || p.startsWith("ER") || p.startsWith("EF") || p.startsWith("EP") || p.contains("ERC")) {
            return "ERC";
        }
        if (p.equals("SLEEPER") || p.startsWith("SF") || p.startsWith("SR") || p.startsWith("SP") || p.contains("SLEEPER")) {
            return "SLEEPER";
        }
        if (p.equals("RAILPAD") || p.equals("RAIL PAD") || p.startsWith("RP") || p.contains("RAIL")) {
            return "RAILPAD";
        }
        return p;
    }

    public static String normalizeIeRole(String ieRoleStr) {
        if (ieRoleStr == null || ieRoleStr.trim().isEmpty()) {
            return "";
        }
        String r = ieRoleStr.trim().toUpperCase().replace("_", " ").replace("-", " ");
        if (r.contains("PROCESS") || r.equals("EP") || r.equals("RPP") || r.equals("SP")) {
            return "PROCESS_IE";
        }
        if (r.contains("MAIN") || r.contains("FINAL") || r.contains("RAW") || r.equals("ER")
                || r.equals("EF") || r.equals("SF") || r.equals("SR") || r.equals("RPF") || r.equals("IE")) {
            return "MAIN_IE";
        }
        return r;
    }

    /**
     * Checks whether a user's roles and optional productType match the required
     * Call Desk product (ERC / SLEEPER / RAILPAD) and IE role (MAIN_IE / PROCESS_IE).
     */
    public static boolean isProductAndRoleMatching(
            List<String> roleNames,
            String roleNameCsv,
            String userProductType,
            String targetProduct,
            String targetIeRole) {
        String normProduct = normalizeProduct(targetProduct);
        String normIeRole = normalizeIeRole(targetIeRole);
        if (normProduct.isEmpty() && normIeRole.isEmpty()) {
            return true;
        }

        List<String> roles = new ArrayList<>();
        if (roleNames != null) {
            for (String r : roleNames) {
                if (r != null && !r.trim().isEmpty()) {
                    roles.add(r.trim());
                }
            }
        }
        if (roles.isEmpty() && roleNameCsv != null && !roleNameCsv.trim().isEmpty()) {
            for (String r : roleNameCsv.split(",")) {
                if (r != null && !r.trim().isEmpty()) {
                    roles.add(r.trim());
                }
            }
        }
        if (roles.isEmpty()) {
            return false;
        }

        for (String role : roles) {
            if (isSingleRoleMatching(role, userProductType, normProduct, normIeRole)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isSingleRoleMatching(
            String role,
            String userProductType,
            String targetProduct,
            String targetIeRole) {
        String r = role.trim().toUpperCase().replaceAll("[_-]+", " ").replaceAll("\\s+", " ");
        if (r.isEmpty() || r.contains("VENDOR") || r.contains("FINANCE") || r.contains("ADMIN")
                || r.contains("HELP DESK") || r.contains("CONTROL MANAGER") || r.contains("SBU")
                || r.contains("RAILWAY") || r.contains("BOARD") || r.equals("SMS")) {
            return false;
        }

        String resolvedProduct = null;
        String resolvedRole = null;
        boolean productFromRoleName = false;

        if (r.equals("ERC IE") || r.equals("ERC MAIN IE")) {
            resolvedProduct = "ERC";
            resolvedRole = "MAIN_IE";
            productFromRoleName = true;
        } else if (r.equals("IE") || r.equals("IE SECONDARY") || r.equals("SECONDARY IE")
                || r.equals("INSPECTING ENGINEER")) {
            resolvedProduct = "ERC";
            resolvedRole = "MAIN_IE";
        } else if (r.equals("ERC PROCESS IE") || r.equals("PROCESS INSPECTING ENGINEER ERC")) {
            resolvedProduct = "ERC";
            resolvedRole = "PROCESS_IE";
            productFromRoleName = true;
        } else if (r.equals("PROCESS IE") || r.equals("PROCESS INSPECTING ENGINEER")) {
            resolvedProduct = "ERC";
            resolvedRole = "PROCESS_IE";
        } else if (r.equals("SLEEPER MAIN IE") || r.equals("SLEEPER IE")
                || r.equals("PSC SLEEPER MAIN IE") || r.equals("SLEEPER INSPECTING ENGINEER")) {
            resolvedProduct = "SLEEPER";
            resolvedRole = "MAIN_IE";
            productFromRoleName = true;
        } else if (r.equals("MAIN IE")) {
            resolvedProduct = "SLEEPER";
            resolvedRole = "MAIN_IE";
        } else if (r.equals("SLEEPER PROCESS IE") || r.equals("PROCESS INSPECTING ENGINEER PSC SLEEPER")) {
            resolvedProduct = "SLEEPER";
            resolvedRole = "PROCESS_IE";
            productFromRoleName = true;
        } else if (r.equals("RAIL MAIN IE") || r.equals("RAILPAD MAIN IE")
                || r.equals("RAIL IE") || r.equals("RAILPAD IE")) {
            resolvedProduct = "RAILPAD";
            resolvedRole = "MAIN_IE";
            productFromRoleName = true;
        } else if (r.equals("RAIL PROCESS IE") || r.equals("RAILPAD PROCESS IE")) {
            resolvedProduct = "RAILPAD";
            resolvedRole = "PROCESS_IE";
            productFromRoleName = true;
        }

        // Handle generic role + explicit userProductType override (e.g. productType="Sleeper", role="Process IE")
        if (!productFromRoleName && userProductType != null && !userProductType.trim().isEmpty()) {
            if (!targetProduct.isEmpty() && doesUserProductTypeMatch(userProductType, targetProduct)) {
                resolvedProduct = targetProduct;
            } else if (!targetProduct.isEmpty() && hasExplicitNonEmptyProduct(userProductType)) {
                return false;
            }
        }

        if (resolvedRole == null) {
            return false;
        }
        if (!targetProduct.isEmpty() && !targetProduct.equals(resolvedProduct)) {
            return false;
        }
        if (!targetIeRole.isEmpty() && !targetIeRole.equals(resolvedRole)) {
            return false;
        }
        return true;
    }

    private static boolean hasExplicitNonEmptyProduct(String userProductType) {
        for (String token : userProductType.split(",")) {
            if (!normalizeProduct(token).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private static boolean doesUserProductTypeMatch(String userProductType, String targetProduct) {
        for (String token : userProductType.split(",")) {
            if (targetProduct.equals(normalizeProduct(token))) {
                return true;
            }
        }
        return false;
    }

    private static String lookupByKeys(Map<String, String> map, String primaryKey, String secondaryKey) {
        if (map == null || map.isEmpty()) {
            return null;
        }
        if (primaryKey != null && !primaryKey.isEmpty()) {
            String val = map.get(primaryKey);
            if (val != null && !val.trim().isEmpty()) {
                return val.trim();
            }
        }
        if (secondaryKey != null && !secondaryKey.isEmpty()) {
            String val = map.get(secondaryKey);
            if (val != null && !val.trim().isEmpty()) {
                return val.trim();
            }
        }
        return null;
    }
}
