package com.sarthi.udmcomplaint.repository;

import com.sarthi.udmcomplaint.entity.UdmComplaint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface UdmComplaintRepository extends JpaRepository<UdmComplaint, Long> {

    Optional<UdmComplaint> findByUdmRejectionId(Long udmRejectionId);

    /** Bulk lookup for the rejection list (no N+1). */
    List<UdmComplaint> findByUdmRejectionIdIn(Collection<Long> udmRejectionIds);

    /** Complaints assigned to a CM, newest first (paginated). */
    Page<UdmComplaint> findByAssignedCmUserIdOrderByUpdatedAtDescCreatedAtDesc(Integer cmUserId, Pageable pageable);

    /** RIOs that have at least one Controlling Manager user (for the RIO dropdown). */
    @Query(value = """
        SELECT DISTINCT TRIM(um.Region) AS rio
        FROM user_master um
        JOIN user_role_master urm ON urm.userid = um.userid
        JOIN role_master rm ON rm.roleid = urm.roleid
        WHERE (rm.roleid = :cmRoleId OR LOWER(TRIM(rm.rolename)) IN ('cm', 'control manager', 'controlling manager'))
          AND um.Region IS NOT NULL AND TRIM(um.Region) <> ''
          AND (um.status IS NULL OR UPPER(um.status) NOT IN ('INACTIVE', 'DISABLED', 'DELETED'))
        ORDER BY rio
        """, nativeQuery = true)
    List<String> findRiosHavingCm(@Param("cmRoleId") Integer cmRoleId);

    /** Controlling Managers of a RIO: [0] userId, [1] name, [2] employee code. */
    @Query(value = """
        SELECT DISTINCT um.userid,
               COALESCE(NULLIF(TRIM(um.FULL_NAME), ''), NULLIF(TRIM(um.SHORT_NAME), ''), um.username) AS name,
               COALESCE(um.EMPLOYEE_CODE, '') AS empCode
        FROM user_master um
        JOIN user_role_master urm ON urm.userid = um.userid
        JOIN role_master rm ON rm.roleid = urm.roleid
        WHERE (rm.roleid = :cmRoleId OR LOWER(TRIM(rm.rolename)) IN ('cm', 'control manager', 'controlling manager'))
          AND UPPER(TRIM(um.Region)) = UPPER(TRIM(:rio))
          AND (um.status IS NULL OR UPPER(um.status) NOT IN ('INACTIVE', 'DISABLED', 'DELETED'))
        ORDER BY name
        """, nativeQuery = true)
    List<Object[]> findCmsByRio(@Param("rio") String rio, @Param("cmRoleId") Integer cmRoleId);

    /** True if the user holds the CM role (used to validate the chosen CM). */
    @Query(value = """
        SELECT COUNT(*) FROM user_role_master urm
        JOIN role_master rm ON rm.roleid = urm.roleid
        JOIN user_master um ON um.userid = urm.userid
        WHERE urm.userid = :userId
          AND (rm.roleid = :cmRoleId OR LOWER(TRIM(rm.rolename)) IN ('cm', 'control manager', 'controlling manager'))
          AND UPPER(TRIM(um.Region)) = UPPER(TRIM(:rio))
        """, nativeQuery = true)
    long countCmInRio(@Param("userId") Integer userId, @Param("rio") String rio, @Param("cmRoleId") Integer cmRoleId);

    /** All active Controlling Managers (every RIO): [0] userId, [1] name, [2] employee code, [3] region. */
    @Query(value = """
        SELECT DISTINCT um.userid,
               COALESCE(NULLIF(TRIM(um.FULL_NAME), ''), NULLIF(TRIM(um.SHORT_NAME), ''), um.username) AS name,
               COALESCE(um.EMPLOYEE_CODE, '') AS empCode,
               COALESCE(TRIM(um.Region), '') AS region
        FROM user_master um
        JOIN user_role_master urm ON urm.userid = um.userid
        JOIN role_master rm ON rm.roleid = urm.roleid
        WHERE (rm.roleid = :cmRoleId OR LOWER(TRIM(rm.rolename)) IN ('cm', 'control manager', 'controlling manager'))
          AND (um.status IS NULL OR UPPER(um.status) NOT IN ('INACTIVE', 'DISABLED', 'DELETED'))
        ORDER BY name
        """, nativeQuery = true)
    List<Object[]> findAllCms(@Param("cmRoleId") Integer cmRoleId);

    /**
     * Inspecting Engineers of ERC / Sleeper / Rail Pad for the JI team — Process IE roles are excluded.
     * [0] userId, [1] name, [2] employee code, [3] region (RIO).
     */
    @Query(value = """
        SELECT DISTINCT um.userid,
               COALESCE(NULLIF(TRIM(um.FULL_NAME), ''), NULLIF(TRIM(um.SHORT_NAME), ''), um.username) AS name,
               COALESCE(um.EMPLOYEE_CODE, '') AS empCode,
               COALESCE(TRIM(um.Region), '') AS region
        FROM user_master um
        JOIN user_role_master urm ON urm.userid = um.userid
        JOIN role_master rm ON rm.roleid = urm.roleid
        WHERE LOWER(TRIM(rm.rolename)) IN ('ie', 'ie secondary', 'main ie', 'sleeper ie', 'sleeper main ie',
                                          'rail main ie', 'railpad ie', 'railpad main ie', 'erc ie')
          AND (um.status IS NULL OR UPPER(um.status) NOT IN ('INACTIVE', 'DISABLED', 'DELETED'))
        ORDER BY name
        """, nativeQuery = true)
    List<Object[]> findInspectingEngineers();

    /** Role names of a user (for backend permission checks). */
    @Query(value = """
        SELECT rm.rolename FROM user_role_master urm
        JOIN role_master rm ON rm.roleid = urm.roleid
        WHERE urm.userid = :userId
        """, nativeQuery = true)
    List<String> findRoleNamesByUserId(@Param("userId") Integer userId);
}
