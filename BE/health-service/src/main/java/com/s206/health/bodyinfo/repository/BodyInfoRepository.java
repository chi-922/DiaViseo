package com.s206.health.bodyinfo.repository;

import com.s206.health.bodyinfo.dto.response.BodyInfoProjection;
import com.s206.health.bodyinfo.entity.BodyInfo;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BodyInfoRepository extends JpaRepository<BodyInfo, Integer> {

	List<BodyInfo> findByUserId(Integer userId);

	Optional<BodyInfo> findByBodyIdAndIsDeletedFalse(Integer bodyId);

	Optional<BodyInfo> findTop1ByUserIdAndMeasurementDateAndIsDeletedFalseOrderByCreatedAtDesc(
			Integer userId, LocalDate date);


	@Query("SELECT b.measurementDate AS measurementDate, " +
			"b.weight AS weight, " +
			"b.muscleMass AS muscleMass, " +
			"b.bodyFat AS bodyFat, " +
			"b.height AS height " +
			"FROM BodyInfo b " +
			"WHERE b.userId = :userId " +
			"AND b.isDeleted = false " +
			"AND b.measurementDate BETWEEN :startDate AND :endDate " +
			"AND b.createdAt = (" +
			"    SELECT MAX(b2.createdAt) " +
			"    FROM BodyInfo b2 " +
			"    WHERE b2.userId = b.userId " +
			"    AND b2.measurementDate = b.measurementDate " +
			"    AND b2.isDeleted = false" +
			")")
	List<BodyInfoProjection> findByUserIdAndMeasurementDateBetween(
			@Param("userId") Integer userId,
			@Param("startDate") LocalDate startDate,
			@Param("endDate") LocalDate endDate
	);

	@Query("SELECT DISTINCT b.userId FROM BodyInfo b WHERE b.userId IN (:userIds) AND b.measurementDate = :date AND b.isDeleted = false")
	List<Integer> findUserIdsWithWeightUpdate(@Param("userIds") List<Integer> userIds,
			@Param("date") LocalDate date);

	List<BodyInfo> findByUserIdAndIsDeletedFalse(Integer userId);

	/**
	 * 특정 날짜 이전(해당 날짜 포함)의 가장 최근 체성분 정보를 조회합니다.
	 */
	@Query("SELECT b FROM BodyInfo b WHERE b.userId = :userId " +
			"AND b.isDeleted = false " +
			"AND b.measurementDate <= :date " +
			"ORDER BY b.measurementDate DESC, b.createdAt DESC")
	List<BodyInfo> findBodyInfoBeforeOrEqualDate(@Param("userId") Integer userId,
												 @Param("date") LocalDate date);
}
