package com.sarthi.udmcomplaint.service;

import com.sarthi.udmcomplaint.repository.UdmComplaintRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.sarthi.udmcomplaint.UdmComplaintConstants.*;

/**
 * Resolves ROLE_MASTER ids by role NAME. Ids differ between local / UAT / PROD, so they are never hard-coded.
 * Found ids are cached for the life of the app; a missing role is looked up again next time.
 */
@Component
@RequiredArgsConstructor
public class UdmRoleResolver {

    /** Used in queries when a role does not exist — matches no ROLEID (queries also match by name). */
    private static final int NO_ROLE = -1;

    private final UdmComplaintRepository repository;
    private final Map<String, Integer> cache = new ConcurrentHashMap<>();

    /** Controlling Manager role id, or null if the role is not configured. */
    public Integer cmRoleId() { return resolve("CM", CM_ROLE_NAMES); }

    /** SBU Head role id, or null if the role is not configured. */
    public Integer sbuHeadRoleId() { return resolve("SBU", SBU_HEAD_ROLE_NAMES); }

    /** Railway Board role id, or null if the role is not configured. */
    public Integer railwayBoardRoleId() { return resolve("RB", RAILWAY_BOARD_ROLE_NAMES); }

    /** CM role id for native queries (never null). */
    public int cmRoleIdForQuery() {
        Integer id = cmRoleId();
        return id != null ? id : NO_ROLE;
    }

    private Integer resolve(String key, List<String> names) {
        Integer cached = cache.get(key);
        if (cached != null) return cached;
        List<Integer> ids = repository.findRoleIdsByNames(names);
        Integer id = ids.isEmpty() ? null : ids.get(0);
        if (id != null) cache.put(key, id);
        return id;
    }
}
